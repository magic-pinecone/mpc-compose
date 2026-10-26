package org.mpc.domain.session

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun signals(status: Int, location: String? = null, html: String = "") = EeclassSignals.classify(SiteResponse(status, location, html))

val campusSitesTests by testSuite {
    test("JavaScript stays scoped to managed sites and their Portal handoff") {
        assertTrue(CampusSites.allowsJavascript("https://portal.ncu.edu.tw/system/ncueeclass"))
        assertTrue(CampusSites.allowsJavascript("https://ncueeclass.ncu.edu.tw/dashboard"))
        assertFalse(CampusSites.allowsJavascript("https://portal.ncu.edu.tw/system/129"))
        assertFalse(CampusSites.allowsJavascript("https://portal.ncu.edu.tw.evil.example/system/ncueeclass"))
        assertFalse(CampusSites.allowsJavascript("http://ncueeclass.ncu.edu.tw/dashboard"))
    }

    test("site lookup accepts only the exact https host") {
        assertEquals(CampusSites.Eeclass, CampusSites.forUrl("https://ncueeclass.ncu.edu.tw/dashboard/myTimeTable"))
        assertNull(CampusSites.forUrl("https://ncueeclass.ncu.edu.tw:8443/dashboard"))
        assertNull(CampusSites.forUrl("https://portal.ncu.edu.tw/"))
    }

    test("site paths reject queries and unusual segments") {
        assertEquals("/dashboard/myTimeTable", SitePath.parse("/dashboard/myTimeTable")?.value)
        assertNull(SitePath.parse("/dashboard?token=1"))
        assertNull(SitePath.parse("dashboard"))
        assertNull(SitePath.parse("/../admin"))
    }

    test("ee-class signed in requires same-origin dashboard links and no login form") {
        val body = """
            <html><body>
              <a href='/dashboard/myTimeTable'>課表</a>
              <a class='latest' href="https://ncueeclass.ncu.edu.tw/dashboard/latestEvent">活動</a>
            </body></html>
        """.trimIndent()

        assertEquals(SiteVerdict.SIGNED_IN, signals(200, html = body))
    }

    test("ee-class login redirects and login forms mean signed out") {
        assertEquals(SiteVerdict.SIGNED_OUT, signals(302, "/index/login?next=%2Fdashboard"))
        assertEquals(SiteVerdict.SIGNED_OUT, signals(200, html = "<form id='login_form'><input name='password'></form>"))
        assertEquals(SiteVerdict.SIGNED_OUT, signals(401))
        assertEquals(SiteVerdict.UNRECOGNIZED, signals(302, "https://ncueeclass.ncu.edu.tw:8443/index/login"))
        assertEquals(SiteVerdict.UNRECOGNIZED, signals(503, html = "unavailable"))
        assertEquals(SiteVerdict.UNRECOGNIZED, signals(403, html = "forbidden"))
    }

    test("ee-class markers in comments, scripts, and foreign links do not sign in") {
        val body = """
            <!-- <a href='/dashboard/myTimeTable'></a> -->
            <script>const x = "<a href='/dashboard/latestEvent'>";</script>
            <a href='https://evil.example/dashboard/myTimeTable'>課表</a>
            <a href='https://evil.example/dashboard/latestEvent'>活動</a>
            <a href='https://ncueeclass.ncu.edu.tw:8443/dashboard/myTimeTable'>課表</a>
            <a href='https://ncueeclass.ncu.edu.tw:8443/dashboard/latestEvent'>活動</a>
        """.trimIndent()

        assertEquals(SiteVerdict.UNRECOGNIZED, signals(200, html = body))
    }
}
