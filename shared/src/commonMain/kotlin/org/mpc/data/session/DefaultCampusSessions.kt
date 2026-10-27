package org.mpc.data.session

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mpc.domain.html.HtmlParser
import org.mpc.domain.model.PortalAuthenticationMode
import org.mpc.domain.repository.AppSettingsRepository
import org.mpc.domain.session.CampusSessions
import org.mpc.domain.session.CampusSite
import org.mpc.domain.session.CampusSites
import org.mpc.domain.session.PORTAL_BASE_URL
import org.mpc.domain.session.PortalNeed
import org.mpc.domain.session.PortalPage
import org.mpc.domain.session.PortalSignIn
import org.mpc.domain.session.PortalStatus
import org.mpc.domain.session.Renewal
import org.mpc.domain.session.SitePath
import org.mpc.domain.session.SiteResult
import org.mpc.domain.session.SiteStatus
import org.mpc.domain.session.SiteVerdict
import org.mpc.domain.session.Unavailability
import org.mpc.domain.session.isPortalUrl
import org.mpc.domain.session.parseHttpsUrl
import org.mpc.domain.session.portalPage
import kotlin.time.Clock
import kotlin.time.Instant

private const val HTTP_STATUS_OK = 200

/** Portal's long-lived browser cookie. Kept on sign-out so Portal still trusts this browser for TOTP. */
private const val PORTAL_BROWSER_COOKIE = "portal"

@ContributesTo(AppScope::class)
@BindingContainer
internal class CampusSessionsProvider {
    @Provides
    @SingleIn(AppScope::class)
    fun providesCampusSessions(
        settings: AppSettingsRepository,
        guardStore: PortalGuardStore,
        parser: HtmlParser,
    ): CampusSessions = DefaultCampusSessions(
        settings = settings,
        portalState = PortalState(guardStore),
        transport = KtorSessionTransport(createDefaultSessionHttpClient(), createPlatformCookieJar()),
        parser = parser,
    )
}

/** One site's lock and status. The status is written only while holding the lock. */
private class SiteState {
    val lock = Mutex()
    val status = MutableStateFlow<SiteStatus>(SiteStatus.Unknown)
}

/**
 * State ownership: cookies live in the [SessionTransport]'s jar, each site's status in its [SiteState], and Portal's
 * status and block in [PortalState]. Sites recover in parallel.
 */
internal class DefaultCampusSessions(
    private val settings: AppSettingsRepository,
    private val portalState: PortalState,
    private val transport: SessionTransport,
    private val parser: HtmlParser,
    private val sites: List<CampusSite> = CampusSites.all,
    private val now: () -> Instant = { Clock.System.now() },
) : CampusSessions {
    private val states = sites.associate { it.id to SiteState() }

    override val portal: StateFlow<PortalStatus> get() = portalState.status

    override fun status(site: CampusSite): StateFlow<SiteStatus> = state(site).status

    override suspend fun refreshPortal() {
        portalState.load()
        when (val check = transport.checkPortal()) {
            // A live session after a block means the user signed in again in the WebView.
            PortalCheck.Live -> portalState.lift(live = true)

            PortalCheck.SignedOut -> portalState.markSignedOut()

            PortalCheck.Displaced -> portalState.block(PortalBlock.DISPLACED)

            is PortalCheck.Failed -> portalState.markUnavailable(check.reason)
        }
    }

    override suspend fun <T> fetch(site: CampusSite, path: SitePath, parse: (String) -> T): SiteResult<T> {
        val state = state(site)
        return state.lock.withLock {
            portalState.load()
            val mode = mode()
            var attempt = Attempt(portalState.automaticSignIn(mode, now()))
            var step: Step = Step.FetchPage
            var html = ""
            while (step !is Step.Finish) {
                val observation = when (step) {
                    Step.FetchPage -> observePage(transport, parser, site, path).also { html = it.html }.observation

                    Step.Handoff -> observeHandoff(transport, portalState, site)

                    // Secure-mode Portal sign-in arrives with the credential vault; until then it needs the user.
                    Step.SignInToPortal -> Observation.PortalSignInStopped(PortalNeed.NO_CREDENTIALS)

                    is Step.Finish -> error("finished steps exit the loop")
                }
                val (nextAttempt, nextStep) = next(attempt, observation)
                attempt = nextAttempt
                step = nextStep
            }
            when (val outcome = step.outcome) {
                FetchOutcome.Page -> SiteResult.Page(parse(html)).also { state.status.value = SiteStatus.SignedIn }

                is FetchOutcome.NeedsPortal -> SiteResult.NeedsPortal(outcome.need).also {
                    state.status.value = SiteStatus.SignedOut(portalState.renewal(mode, now()))
                }

                is FetchOutcome.Unavailable -> SiteResult.Unavailable(outcome.reason).also {
                    state.status.value = SiteStatus.Unavailable(outcome.reason)
                }
            }
        }
    }

    override suspend fun refresh(site: CampusSite) {
        val state = state(site)
        state.lock.withLock {
            portalState.load()
            val previous = state.status.value
            state.status.value = SiteStatus.Checking
            try {
                state.status.value = when (val result = transport.get(site.url(site.probePath))) {
                    is TransportResult.Failed -> SiteStatus.Unavailable(result.reason)

                    is TransportResult.Response -> when (site.signals.classify(result.response, parser)) {
                        SiteVerdict.SIGNED_IN -> SiteStatus.SignedIn
                        SiteVerdict.SIGNED_OUT -> SiteStatus.SignedOut(portalState.renewal(mode(), now()))
                        SiteVerdict.UNRECOGNIZED -> SiteStatus.Unavailable(Unavailability.UNEXPECTED_RESPONSE)
                    }
                }
            } catch (cancellation: CancellationException) {
                state.status.value = previous
                throw cancellation
            }
        }
    }

    override fun browserEntry(site: CampusSite, path: SitePath): String = if (state(site).status.value == SiteStatus.SignedIn) site.url(path) else site.portalEntryUrl

    override suspend fun browserPage(url: String, loading: Boolean) {
        val site = CampusSites.forUrl(url)?.takeIf { it in sites }
        val portalUrl = parseHttpsUrl(url)?.takeIf(::isPortalUrl)
        when {
            site != null && loading -> state(site).status.value = SiteStatus.Checking

            site != null -> refresh(site)

            portalUrl == null || loading -> Unit

            else -> when (portalPage(portalUrl)) {
                PortalPage.EXPIRED -> portalState.block(PortalBlock.DISPLACED)

                PortalPage.SIGN_IN -> portalState.markSignedOut()

                // A signed-in Portal page means the user signed in in the WebView: their action lifts any block.
                PortalPage.OTHER -> portalState.lift(live = true)
            }
        }
    }

    override suspend fun signInToPortal(): PortalSignIn {
        portalState.lift(live = portalState.status.value == PortalStatus.Live)
        return PortalSignIn.OpenBrowser("$PORTAL_BASE_URL/login")
    }

    override suspend fun signOut(site: CampusSite) {
        val state = state(site)
        state.lock.withLock {
            transport.clearCookies(site.baseUrl)
            state.status.value = SiteStatus.SignedOut(portalState.renewal(mode(), now()))
        }
    }

    override suspend fun signOutEverywhere() {
        // Block first: if the app stops partway, it still cannot sign back in by itself.
        portalState.block(PortalBlock.USER_SIGNED_OUT)
        transport.clearCookies(PORTAL_BASE_URL, keep = setOf(PORTAL_BROWSER_COOKIE))
        sites.forEach { site ->
            val state = state(site)
            state.lock.withLock {
                transport.clearCookies(site.baseUrl)
                state.status.value = SiteStatus.SignedOut(Renewal.PORTAL_SIGN_IN_BY_USER)
            }
        }
    }

    private suspend fun mode(): PortalAuthenticationMode = settings.settings.first().portalAuthenticationMode

    private fun state(site: CampusSite): SiteState = requireNotNull(states[site.id]) { "Unknown campus site" }
}

private class PageObservation(val observation: Observation, val html: String = "")

private suspend fun observePage(transport: SessionTransport, parser: HtmlParser, site: CampusSite, path: SitePath): PageObservation = when (val result = transport.get(site.url(path))) {
    is TransportResult.Failed -> PageObservation(Observation.Failed(result.reason))

    is TransportResult.Response -> when {
        site.signals.classify(result.response, parser) == SiteVerdict.SIGNED_OUT -> PageObservation(Observation.SiteSignedOut)
        result.response.status == HTTP_STATUS_OK -> PageObservation(Observation.PageOk, result.response.html)
        else -> PageObservation(Observation.Failed(Unavailability.UNEXPECTED_RESPONSE))
    }
}

/** Runs [site]'s handoff and records what it showed about Portal. */
private suspend fun observeHandoff(transport: SessionTransport, portal: PortalState, site: CampusSite): Observation = when (val result = transport.handoff(site)) {
    HandoffResult.Done -> Observation.HandoffDone.also { portal.markLive() }
    HandoffResult.PortalSignedOut -> Observation.PortalSignedOut.also { portal.markSignedOut() }
    HandoffResult.PortalDisplaced -> Observation.PortalDisplaced.also { portal.block(PortalBlock.DISPLACED) }
    is HandoffResult.Failed -> Observation.Failed(result.reason)
}
