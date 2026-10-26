package org.mpc.data.session

import de.infix.testBalloon.framework.core.testSuite
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.mpc.domain.session.CampusSite
import org.mpc.domain.session.CampusSites
import org.mpc.domain.session.PortalNeed
import org.mpc.domain.session.PortalSignIn
import org.mpc.domain.session.PortalStatus
import org.mpc.domain.session.Renewal
import org.mpc.domain.session.SitePath
import org.mpc.domain.session.SiteResponse
import org.mpc.domain.session.SiteResult
import org.mpc.domain.session.SiteStatus
import kotlin.test.assertEquals

private val SIGNED_IN_DASHBOARD = SiteResponse(
    status = 200,
    location = null,
    html = "<a href='/dashboard/myTimeTable'></a><a href='/dashboard/latestEvent'></a>",
)
private val SIGNED_OUT_REDIRECT = SiteResponse(status = 302, location = "/index/login", html = "")
private val TIMETABLE = SitePath.of("/dashboard/myTimeTable")

/** Answers each request from a script and records what the session layer asked for, in order. */
private class ScriptedTransport(
    private val pages: ArrayDeque<TransportResult> = ArrayDeque(),
    private val handoffs: ArrayDeque<HandoffResult> = ArrayDeque(),
    /** Requests from this one on wait for [gate]. */
    private val gatedFromRequest: Int = Int.MAX_VALUE,
    private val gate: CompletableDeferred<Unit> = CompletableDeferred(Unit),
) : SessionTransport {
    val events = mutableListOf<String>()
    private var requests = 0

    override suspend fun get(url: String): TransportResult {
        events += "get $url"
        if (++requests >= gatedFromRequest) gate.await()
        return pages.removeFirst()
    }

    override suspend fun handoff(site: CampusSite): HandoffResult {
        events += "handoff ${site.id}"
        return handoffs.removeFirst()
    }

    override suspend fun clearCookies(url: String, keep: Set<String>) {
        events += "clear $url keep=$keep"
    }
}

private fun sessions(
    transport: SessionTransport,
    guardStore: PortalGuardStore = InMemoryPortalGuardStore(),
) = DefaultCampusSessions(FakeAppSettingsRepository(), PortalState(guardStore), transport)

private fun page(response: SiteResponse) = TransportResult.Response(response)

val defaultCampusSessionsTests by testSuite {
    test("a signed-out site hands off from live Portal and returns the parsed page") {
        runTest {
            val transport = ScriptedTransport(
                pages = ArrayDeque(listOf(page(SIGNED_OUT_REDIRECT), page(SIGNED_IN_DASHBOARD))),
                handoffs = ArrayDeque(listOf(HandoffResult.Done)),
            )
            val sessions = sessions(transport)

            val result = sessions.fetch(CampusSites.Eeclass, TIMETABLE) { html -> html.length }

            assertEquals(SiteResult.Page(SIGNED_IN_DASHBOARD.html.length), result)
            assertEquals(1, transport.events.count { it.startsWith("handoff") })
            assertEquals(SiteStatus.SignedIn, sessions.status(CampusSites.Eeclass).value)
            assertEquals(PortalStatus.Live, sessions.portal.value)
        }
    }

    test("manual mode stops at the user when Portal is signed out") {
        runTest {
            val transport = ScriptedTransport(
                pages = ArrayDeque(listOf(page(SIGNED_OUT_REDIRECT))),
                handoffs = ArrayDeque(listOf(HandoffResult.PortalSignedOut)),
            )
            val sessions = sessions(transport)

            val result = sessions.fetch(CampusSites.Eeclass, TIMETABLE) { it }

            assertEquals(SiteResult.NeedsPortal(PortalNeed.MANUAL_SIGN_IN), result)
            assertEquals(
                SiteStatus.SignedOut(Renewal.PORTAL_SIGN_IN_BY_USER),
                sessions.status(CampusSites.Eeclass).value,
            )
        }
    }

    test("a displaced Portal stays blocked across a restart until the user signs in") {
        runTest {
            val guardStore = InMemoryPortalGuardStore()
            val first = sessions(
                ScriptedTransport(
                    pages = ArrayDeque(listOf(page(SIGNED_OUT_REDIRECT))),
                    handoffs = ArrayDeque(listOf(HandoffResult.PortalDisplaced)),
                ),
                guardStore,
            )
            assertEquals(SiteResult.NeedsPortal(PortalNeed.DISPLACED), first.fetch(CampusSites.Eeclass, TIMETABLE) { it })

            val restarted = sessions(ScriptedTransport(pages = ArrayDeque(listOf(page(SIGNED_OUT_REDIRECT)))), guardStore)
            restarted.refresh(CampusSites.Eeclass)
            assertEquals(PortalStatus.Blocked(PortalNeed.DISPLACED), restarted.portal.value)

            assertEquals(PortalSignIn.OpenBrowser("https://portal.ncu.edu.tw/login"), restarted.signInToPortal())
            assertEquals(PortalGuard(), guardStore.guard)
        }
    }

    test("the WebView reports Portal displacement and a later signed-in Portal page lifts it") {
        runTest {
            val guardStore = InMemoryPortalGuardStore()
            val sessions = sessions(ScriptedTransport(), guardStore)

            sessions.browserPage("https://portal.ncu.edu.tw/expired", loading = false)
            assertEquals(PortalStatus.Blocked(PortalNeed.DISPLACED), sessions.portal.value)

            sessions.browserPage("https://portal.ncu.edu.tw/login/set", loading = false)
            assertEquals(PortalStatus.Blocked(PortalNeed.DISPLACED), sessions.portal.value)

            sessions.browserPage("https://portal.ncu.edu.tw/", loading = false)
            assertEquals(PortalStatus.Live, sessions.portal.value)
            assertEquals(null, guardStore.guard.block)
        }
    }

    test("signing out everywhere blocks first, keeps Portal's browser cookie, and clears every site") {
        runTest {
            val guardStore = InMemoryPortalGuardStore()
            val transport = ScriptedTransport()
            val sessions = sessions(transport, guardStore)

            sessions.signOutEverywhere()

            assertEquals(PortalBlock.USER_SIGNED_OUT, guardStore.writes.first().block)
            assertEquals(
                listOf(
                    "clear https://portal.ncu.edu.tw keep=[portal]",
                    "clear https://ncueeclass.ncu.edu.tw keep=[]",
                ),
                transport.events,
            )
            assertEquals(PortalStatus.Blocked(PortalNeed.USER_SIGNED_OUT), sessions.portal.value)
        }
    }

    test("cancelling a check restores the previous site status") {
        runTest {
            val transport = ScriptedTransport(
                pages = ArrayDeque(listOf(page(SIGNED_IN_DASHBOARD), page(SIGNED_OUT_REDIRECT))),
                gatedFromRequest = 2,
                gate = CompletableDeferred(),
            )
            val sessions = sessions(transport)
            sessions.refresh(CampusSites.Eeclass)

            val job = launch { sessions.refresh(CampusSites.Eeclass) }
            yield()
            assertEquals(SiteStatus.Checking, sessions.status(CampusSites.Eeclass).value)
            job.cancelAndJoin()

            assertEquals(SiteStatus.SignedIn, sessions.status(CampusSites.Eeclass).value)
        }
    }
}
