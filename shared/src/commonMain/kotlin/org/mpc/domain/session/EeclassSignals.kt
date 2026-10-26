package org.mpc.domain.session

import io.ktor.http.Url

private const val EECLASS_BASE_URL = "https://ncueeclass.ncu.edu.tw"
private const val HTTP_STATUS_OK = 200
private const val HTTP_STATUS_UNAUTHORIZED = 401
private val LOGIN_REDIRECT_STATUS_CODES = setOf(301, 302, 303, 307, 308)

/**
 * ee-class signals: a redirect to `/index/login`, a 401, or `form#login_form` means signed out; a dashboard page
 * with same-origin links to the timetable and latest events means signed in.
 */
internal object EeclassSignals : SiteSignals {
    override fun classify(response: SiteResponse): SiteVerdict {
        if (response.status in LOGIN_REDIRECT_STATUS_CODES && isLoginLocation(response.location)) {
            return SiteVerdict.SIGNED_OUT
        }
        if (response.status == HTTP_STATUS_UNAUTHORIZED) return SiteVerdict.SIGNED_OUT
        if (response.status != HTTP_STATUS_OK) return SiteVerdict.UNRECOGNIZED

        val visibleHtml = NON_CONTENT_REGEX.replace(response.html, "")
        if (hasLoginForm(visibleHtml)) return SiteVerdict.SIGNED_OUT
        val links = dashboardLinks(visibleHtml)
        return if ("/dashboard/myTimeTable" in links && "/dashboard/latestEvent" in links) {
            SiteVerdict.SIGNED_IN
        } else {
            SiteVerdict.UNRECOGNIZED
        }
    }
}

private fun isLoginLocation(location: String?): Boolean {
    if (location == null) return false
    val url = resolve(location) ?: return false
    return url.protocol.name == "https" && url.port == HTTPS_DEFAULT_PORT &&
        url.host.equals(CampusSites.Eeclass.host, ignoreCase = true) &&
        url.encodedPath == "/index/login"
}

private fun hasLoginForm(html: String): Boolean = FORM_TAG_REGEX.findAll(html).any { tag ->
    attributeValue(tag.value, "id") == "login_form"
}

private fun dashboardLinks(html: String): Set<String> = ANCHOR_TAG_REGEX.findAll(html).mapNotNull { tag ->
    val href = attributeValue(tag.value, "href") ?: return@mapNotNull null
    val url = resolve(href) ?: return@mapNotNull null
    if (url.protocol.name != "https" || url.port != HTTPS_DEFAULT_PORT ||
        !url.host.equals(CampusSites.Eeclass.host, ignoreCase = true)
    ) {
        return@mapNotNull null
    }
    url.encodedPath
}.toSet()

private fun resolve(reference: String): Url? = runCatching {
    if (reference.startsWith("/")) Url("$EECLASS_BASE_URL$reference") else Url(reference)
}.getOrNull()

private fun attributeValue(tag: String, name: String): String? {
    val match = ATTRIBUTE_REGEX.findAll(tag).firstOrNull { it.groupValues[1].equals(name, ignoreCase = true) } ?: return null
    return match.groupValues.drop(2).firstOrNull { it.isNotEmpty() }
}

private val NON_CONTENT_REGEX = Regex("(?is)<!--.*?-->|<script\\b[^>]*>.*?</script>|<style\\b[^>]*>.*?</style>")
private val FORM_TAG_REGEX = Regex("(?is)<form\\b[^>]*>")
private val ANCHOR_TAG_REGEX = Regex("(?is)<a\\b[^>]*>")
private val ATTRIBUTE_REGEX = Regex("(?is)\\b([a-z_:][-a-z0-9_:.]*)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))")
