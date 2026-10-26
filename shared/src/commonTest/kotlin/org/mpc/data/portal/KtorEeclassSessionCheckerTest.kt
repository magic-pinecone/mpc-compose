package org.mpc.data.portal

import de.infix.testBalloon.framework.core.testSuite
import org.mpc.data.session.CookieHeader
import org.mpc.data.session.PlatformCookieJar
import org.mpc.domain.portal.EECLASS_DASHBOARD_URL
import org.mpc.domain.portal.EeclassSessionCheck
import org.mpc.domain.portal.EeclassUnavailableReason
import kotlin.test.assertEquals

private class FakeEeclassCookieStore(
    var header: String? = null,
) : PlatformCookieJar {
    var storedValues: List<String> = emptyList()
        private set

    override suspend fun cookieHeader(url: String): CookieHeader? {
        assertEquals(EECLASS_DASHBOARD_URL, url)
        return header?.let(::CookieHeader)
    }

    override suspend fun storeSetCookies(url: String, values: List<String>) {
        assertEquals(EECLASS_DASHBOARD_URL, url)
        storedValues = values
    }

    override suspend fun clear(url: String) = error("not used by the checker")
}

private class FakeEeclassDashboardClient(
    private val response: EeclassDashboardResponse,
    private val afterRequest: () -> Unit = {},
) : EeclassDashboardClient {
    var requestCookieHeader: String? = null
        private set

    override suspend fun getDashboard(cookieHeader: String?): EeclassDashboardResponse {
        requestCookieHeader = cookieHeader
        afterRequest()
        return response
    }
}

val ktorEeclassSessionCheckerTests by testSuite {
    test("a browser cookie update invalidates a stale probe response and its cookies") {
        kotlinx.coroutines.test.runTest {
            val cookieStore = FakeEeclassCookieStore("PHPSESSID=before")
            val dashboardClient = FakeEeclassDashboardClient(
                response = EeclassDashboardResponse(
                    statusCode = 302,
                    location = "/index/login?next=%2Fdashboard",
                    body = "",
                    setCookies = listOf("PHPSESSID=stale; Path=/; Secure; HttpOnly; SameSite=Lax"),
                ),
                afterRequest = { cookieStore.header = "PHPSESSID=after" },
            )

            val result = KtorEeclassSessionChecker(dashboardClient, cookieStore).check()

            assertEquals("PHPSESSID=before", dashboardClient.requestCookieHeader)
            assertEquals(EeclassSessionCheck.Unavailable(EeclassUnavailableReason.UNEXPECTED_RESPONSE), result)
            assertEquals(emptyList(), cookieStore.storedValues)
        }
    }

    test("transport and unsupported responses stay unavailable") {
        kotlinx.coroutines.test.runTest {
            val cookieStore = FakeEeclassCookieStore()
            val networkFailure = KtorEeclassSessionChecker(
                dashboardClient = object : EeclassDashboardClient {
                    override suspend fun getDashboard(cookieHeader: String?): EeclassDashboardResponse = error("network unavailable")
                },
                cookieStore = cookieStore,
            ).check()
            val unsupportedResponse = classifyEeclassResponse(503, null, "unavailable")
            val forbiddenResponse = classifyEeclassResponse(403, null, "forbidden")

            assertEquals(EeclassSessionCheck.Unavailable(EeclassUnavailableReason.NETWORK), networkFailure)
            assertEquals(EeclassSessionCheck.Unavailable(EeclassUnavailableReason.UNEXPECTED_RESPONSE), unsupportedResponse)
            assertEquals(EeclassSessionCheck.Unavailable(EeclassUnavailableReason.UNEXPECTED_RESPONSE), forbiddenResponse)
        }
    }

    test("authenticated state requires same-origin dashboard links and no login form") {
        val body = """
            <html><body>
              <a href='/dashboard/myTimeTable'>課表</a>
              <a class='latest' href="https://ncueeclass.ncu.edu.tw/dashboard/latestEvent">活動</a>
            </body></html>
        """.trimIndent()

        assertEquals(EeclassSessionCheck.Authenticated, classifyEeclassResponse(200, null, body))
    }

    test("login redirects and login forms mean signed out") {
        assertEquals(
            EeclassSessionCheck.SignedOut,
            classifyEeclassResponse(302, "/index/login?next=%2Fdashboard", ""),
        )
        assertEquals(
            EeclassSessionCheck.SignedOut,
            classifyEeclassResponse(200, null, "<form id='login_form'><input name='password'></form>"),
        )
        assertEquals(
            EeclassSessionCheck.Unavailable(EeclassUnavailableReason.UNEXPECTED_RESPONSE),
            classifyEeclassResponse(302, "https://ncueeclass.ncu.edu.tw:8443/index/login", ""),
        )
    }

    test("markers in comments scripts and foreign links do not authenticate") {
        val body = """
            <!-- <a href='/dashboard/myTimeTable'></a> -->
            <script>const x = "<a href='/dashboard/latestEvent'>";</script>
            <a href='https://evil.example/dashboard/myTimeTable'>課表</a>
            <a href='https://evil.example/dashboard/latestEvent'>活動</a>
            <a href='https://ncueeclass.ncu.edu.tw:8443/dashboard/myTimeTable'>課表</a>
            <a href='https://ncueeclass.ncu.edu.tw:8443/dashboard/latestEvent'>活動</a>
        """.trimIndent()

        assertEquals(
            EeclassSessionCheck.Unavailable(EeclassUnavailableReason.UNEXPECTED_RESPONSE),
            classifyEeclassResponse(200, null, body),
        )
    }
}
