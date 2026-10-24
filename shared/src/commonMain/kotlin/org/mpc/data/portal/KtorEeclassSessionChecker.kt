package org.mpc.data.portal

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.CancellationException
import org.mpc.domain.portal.EECLASS_BASE_URL
import org.mpc.domain.portal.EECLASS_DASHBOARD_URL
import org.mpc.domain.portal.EeclassSessionCheck
import org.mpc.domain.portal.EeclassSessionChecker
import org.mpc.domain.portal.EeclassUnavailableReason
import org.mpc.domain.portal.HTTPS_DEFAULT_PORT

internal interface EeclassNativeCookieStore {
    suspend fun cookieHeader(url: String): String?
    suspend fun storeSetCookies(url: String, values: List<String>)
}

internal expect fun createEeclassNativeCookieStore(): EeclassNativeCookieStore

private const val REQUEST_TIMEOUT_MILLIS = 15_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L
private const val HTTP_STATUS_MOVED_PERMANENTLY = 301
private const val HTTP_STATUS_FOUND = 302
private const val HTTP_STATUS_SEE_OTHER = 303
private const val HTTP_STATUS_TEMPORARY_REDIRECT = 307
private const val HTTP_STATUS_PERMANENT_REDIRECT = 308
private const val HTTP_STATUS_UNAUTHORIZED = 401
private const val HTTP_STATUS_OK = 200
private val LOGIN_REDIRECT_STATUS_CODES = setOf(
    HTTP_STATUS_MOVED_PERMANENTLY,
    HTTP_STATUS_FOUND,
    HTTP_STATUS_SEE_OTHER,
    HTTP_STATUS_TEMPORARY_REDIRECT,
    HTTP_STATUS_PERMANENT_REDIRECT,
)

internal data class EeclassDashboardResponse(
    val statusCode: Int,
    val location: String?,
    val body: String,
    val setCookies: List<String>,
)

internal interface EeclassDashboardClient {
    suspend fun getDashboard(cookieHeader: String?): EeclassDashboardResponse
}

@ContributesTo(AppScope::class)
@BindingContainer
internal class EeclassSessionProvider {
    @Provides
    @SingleIn(AppScope::class)
    fun providesEeclassSessionChecker(): EeclassSessionChecker = KtorEeclassSessionChecker(
        dashboardClient = KtorEeclassDashboardClient(
            HttpClient {
                expectSuccess = false
                followRedirects = false
                install(HttpTimeout) {
                    requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                    connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
                }
            },
        ),
        cookieStore = createEeclassNativeCookieStore(),
    )
}

internal class KtorEeclassDashboardClient(
    private val httpClient: HttpClient,
) : EeclassDashboardClient {
    override suspend fun getDashboard(cookieHeader: String?): EeclassDashboardResponse {
        val response = httpClient.get(EECLASS_DASHBOARD_URL) {
            if (!cookieHeader.isNullOrBlank()) header(HttpHeaders.Cookie, cookieHeader)
        }
        return EeclassDashboardResponse(
            statusCode = response.status.value,
            location = response.headers[HttpHeaders.Location],
            body = response.bodyAsText(),
            setCookies = response.headers.getAll(HttpHeaders.SetCookie).orEmpty(),
        )
    }
}

internal class KtorEeclassSessionChecker(
    private val dashboardClient: EeclassDashboardClient,
    private val cookieStore: EeclassNativeCookieStore,
) : EeclassSessionChecker {
    override suspend fun check(): EeclassSessionCheck = try {
        val cookiesBeforeRequest = cookieStore.cookieHeader(EECLASS_DASHBOARD_URL)
        val response = dashboardClient.getDashboard(cookiesBeforeRequest)
        val cookiesAfterResponse = cookieStore.cookieHeader(EECLASS_DASHBOARD_URL)
        if (cookiesBeforeRequest != cookiesAfterResponse) {
            EeclassSessionCheck.Unavailable(EeclassUnavailableReason.UNEXPECTED_RESPONSE)
        } else {
            if (response.setCookies.isNotEmpty()) {
                cookieStore.storeSetCookies(EECLASS_DASHBOARD_URL, response.setCookies)
            }
            classifyEeclassResponse(
                statusCode = response.statusCode,
                location = response.location,
                body = response.body,
            )
        }
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        EeclassSessionCheck.Unavailable(EeclassUnavailableReason.NETWORK)
    }
}

internal fun classifyEeclassResponse(
    statusCode: Int,
    location: String?,
    body: String,
): EeclassSessionCheck {
    if (statusCode in LOGIN_REDIRECT_STATUS_CODES && isEeclassLoginLocation(location)) {
        return EeclassSessionCheck.SignedOut
    }
    if (statusCode == HTTP_STATUS_UNAUTHORIZED) return EeclassSessionCheck.SignedOut
    if (statusCode != HTTP_STATUS_OK) return EeclassSessionCheck.Unavailable(EeclassUnavailableReason.UNEXPECTED_RESPONSE)

    val visibleHtml = NON_CONTENT_REGEX.replace(body, "")
    if (hasLoginForm(visibleHtml)) return EeclassSessionCheck.SignedOut
    val links = dashboardLinks(visibleHtml)
    return if ("/dashboard/myTimeTable" in links && "/dashboard/latestEvent" in links) {
        EeclassSessionCheck.Authenticated
    } else {
        EeclassSessionCheck.Unavailable(EeclassUnavailableReason.UNEXPECTED_RESPONSE)
    }
}

private fun isEeclassLoginLocation(location: String?): Boolean {
    if (location == null) return false
    val url = runCatching {
        if (location.startsWith("/")) Url("$EECLASS_BASE_URL$location") else Url(location)
    }.getOrNull() ?: return false
    return url.protocol.name == "https" && url.port == HTTPS_DEFAULT_PORT &&
        url.host.equals("ncueeclass.ncu.edu.tw", ignoreCase = true) &&
        url.encodedPath == "/index/login"
}

private fun hasLoginForm(html: String): Boolean = FORM_TAG_REGEX.findAll(html).any { tag ->
    attributeValue(tag.value, "id") == "login_form"
}

private fun dashboardLinks(html: String): Set<String> = ANCHOR_TAG_REGEX.findAll(html).mapNotNull { tag ->
    val href = attributeValue(tag.value, "href") ?: return@mapNotNull null
    val url = runCatching {
        if (href.startsWith("/")) Url("$EECLASS_BASE_URL$href") else Url(href)
    }.getOrNull() ?: return@mapNotNull null
    if (url.protocol.name != "https" || url.port != HTTPS_DEFAULT_PORT || !url.host.equals("ncueeclass.ncu.edu.tw", ignoreCase = true)) {
        return@mapNotNull null
    }
    url.encodedPath
}.toSet()

private fun attributeValue(tag: String, name: String): String? {
    val match = ATTRIBUTE_REGEX.findAll(tag).firstOrNull { it.groupValues[1].equals(name, ignoreCase = true) } ?: return null
    return match.groupValues.drop(2).firstOrNull { it.isNotEmpty() }
}

private val NON_CONTENT_REGEX = Regex("(?is)<!--.*?-->|<script\\b[^>]*>.*?</script>|<style\\b[^>]*>.*?</style>")
private val FORM_TAG_REGEX = Regex("(?is)<form\\b[^>]*>")
private val ANCHOR_TAG_REGEX = Regex("(?is)<a\\b[^>]*>")
private val ATTRIBUTE_REGEX = Regex("(?is)\\b([a-z_:][-a-z0-9_:.]*)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))")
