package org.mpc.domain.session

import io.ktor.http.Url
import org.mpc.domain.html.HtmlParser
import kotlin.jvm.JvmInline

const val PORTAL_HOST = "portal.ncu.edu.tw"
const val PORTAL_BASE_URL = "https://$PORTAL_HOST"
internal const val HTTPS_DEFAULT_PORT = 443
private const val CAMPUS_DOMAIN = "ncu.edu.tw"

/** A path on a campus site: starts with "/", plain segments only, no query string (a query could carry a token). */
@JvmInline
value class SitePath private constructor(val value: String) {
    companion object {
        fun parse(raw: String): SitePath? = raw.takeIf { path ->
            path.startsWith("/") && path.drop(1).split('/').all { segment -> segment.matches(PATH_SEGMENT) }
        }?.let(::SitePath)

        fun of(literal: String): SitePath = requireNotNull(parse(literal)) { "Invalid site path" }
    }
}

private val PATH_SEGMENT = Regex("[A-Za-z0-9_-]+")

/** What a campus site answered: status, raw `Location`, and body. No headers, so classifiers never see cookies. */
class SiteResponse(
    val status: Int,
    val location: String?,
    val html: String,
)

enum class SiteVerdict {
    SIGNED_IN,
    SIGNED_OUT,
    UNRECOGNIZED,
}

/** Reads a site's own signed-in and signed-out signals. The only code a new site writes. */
fun interface SiteSignals {
    fun classify(response: SiteResponse, parser: HtmlParser): SiteVerdict
}

/**
 * A campus site whose session comes from a Portal handoff: `GET /system/{portalSystemId}` arms it and
 * `/system/{portalSystemId}/go` redirects to the site with a fresh session.
 */
class CampusSite(
    val id: String,
    val displayName: String,
    val host: String,
    val portalSystemId: String,
    /** A page whose response proves a live session; [signals] must read [SiteVerdict.SIGNED_IN] there. */
    val probePath: SitePath,
    val signals: SiteSignals,
) {
    val baseUrl: String = "https://$host"
    val portalEntryUrl: String = "$PORTAL_BASE_URL/system/$portalSystemId"

    fun url(path: SitePath): String = baseUrl + path.value
}

/** The one list of campus sites the app keeps sessions for. */
object CampusSites {
    val Eeclass = CampusSite(
        id = "ncueeclass",
        displayName = "新 ee-class",
        host = "ncueeclass.ncu.edu.tw",
        portalSystemId = "ncueeclass",
        probePath = SitePath.of("/dashboard"),
        signals = EeclassSignals,
    )

    val all: List<CampusSite> = listOf(Eeclass)

    /** The managed site serving [url], or null for Portal, other hosts, and non-https or non-443 URLs. */
    fun forUrl(url: String): CampusSite? {
        val parsed = parseHttpsUrl(url) ?: return null
        return all.firstOrNull { it.host.equals(parsed.host, ignoreCase = true) }
    }

    /** JavaScript runs only on https campus hosts; Portal sign-in needs it for reCAPTCHA. */
    fun allowsJavascript(url: String): Boolean {
        val host = parseHttpsUrl(url)?.host?.lowercase() ?: return false
        return host == CAMPUS_DOMAIN || host.endsWith(".$CAMPUS_DOMAIN")
    }
}

internal fun parseHttpsUrl(value: String): Url? = runCatching { Url(value) }
    .getOrNull()
    ?.takeIf { it.protocol.name == "https" && it.port == HTTPS_DEFAULT_PORT }

internal fun isPortalUrl(url: Url): Boolean = url.host.equals(PORTAL_HOST, ignoreCase = true)

internal enum class PortalPage {
    /** Portal's sign-in flow, including steps such as `/login/set`. */
    SIGN_IN,

    /** Another sign-in replaced this session. */
    EXPIRED,

    /** Any other Portal page. The app opens only Portal pages that need a session, so one loading means Portal is signed in. */
    OTHER,
}

internal fun portalPage(url: Url): PortalPage = when {
    url.encodedPath == "/expired" -> PortalPage.EXPIRED
    url.encodedPath == "/login" || url.encodedPath.startsWith("/login/") -> PortalPage.SIGN_IN
    else -> PortalPage.OTHER
}
