package org.mpc.domain.session

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

val campusSitesTests by testSuite {
    test("JavaScript stays scoped to https campus hosts") {
        assertTrue(CampusSites.allowsJavascript("https://portal.ncu.edu.tw/login"))
        assertTrue(CampusSites.allowsJavascript("https://portal.ncu.edu.tw/system/129"))
        assertTrue(CampusSites.allowsJavascript("https://ncueeclass.ncu.edu.tw/dashboard"))
        assertTrue(CampusSites.allowsJavascript("https://NCU.EDU.TW/"))
        assertFalse(CampusSites.allowsJavascript("https://portal.ncu.edu.tw.evil.example/system/ncueeclass"))
        assertFalse(CampusSites.allowsJavascript("https://evilncu.edu.tw/"))
        assertFalse(CampusSites.allowsJavascript("http://ncueeclass.ncu.edu.tw/dashboard"))
        assertFalse(CampusSites.allowsJavascript("https://ncueeclass.ncu.edu.tw:8443/dashboard"))
    }

    test("Portal launch confirmations continue to their /go page") {
        assertEquals("https://portal.ncu.edu.tw/system/129/go", portalLaunchUrl("https://portal.ncu.edu.tw/system/129"))
        assertEquals(
            "https://portal.ncu.edu.tw/system/incu-studentscore/go",
            portalLaunchUrl("https://portal.ncu.edu.tw/system/incu-studentscore?from=menu"),
        )
        assertNull(portalLaunchUrl("https://portal.ncu.edu.tw/system/129/go"))
        assertNull(portalLaunchUrl("https://portal.ncu.edu.tw/login"))
        assertNull(portalLaunchUrl("https://portal.ncu.edu.tw/system/"))
        assertNull(portalLaunchUrl("https://ncueeclass.ncu.edu.tw/system/129"))
        assertNull(portalLaunchUrl("http://portal.ncu.edu.tw/system/129"))
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
}
