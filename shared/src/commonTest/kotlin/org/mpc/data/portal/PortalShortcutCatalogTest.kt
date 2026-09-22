package org.mpc.data.portal

import de.infix.testBalloon.framework.core.testSuite
import org.mpc.domain.model.PortalLaunchMode
import org.mpc.domain.model.PortalShortcutDestination
import kotlin.test.assertEquals
import kotlin.test.assertTrue

val portalShortcutCatalogTests by testSuite {
    test("portal paths normalize to the trusted portal host") {
        assertEquals(
            expected = "https://portal.ncu.edu.tw/system/129",
            actual = PortalShortcutDestination(
                title = "NCU Mail",
                url = org.mpc.domain.model.portalUrl("system/129"),
                launchMode = PortalLaunchMode.IN_APP,
            ).url,
        )
    }

    test("search removes empty sections and matches labels case insensitively") {
        val sections = filterPortalShortcutSections(
            sections = defaultPortalShortcutSections,
            query = "mail",
        )

        assertEquals(1, sections.size)
        assertEquals("NCU Mail", sections.single().items.single().label)
    }

    test("empty search preserves the catalog") {
        assertEquals(
            expected = defaultPortalShortcutSections,
            actual = filterPortalShortcutSections(defaultPortalShortcutSections, "  "),
        )
    }

    test("direct services remain external destinations") {
        val item = defaultPortalShortcutSections
            .first()
            .items
            .first { it.label == "新ee-class" }

        assertTrue(item.destination.launchMode == PortalLaunchMode.EXTERNAL)
        assertEquals("https://ncueeclass.ncu.edu.tw/", item.destination.url)
    }
}
