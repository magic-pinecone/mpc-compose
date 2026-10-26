package org.mpc.domain.session

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
}
