package org.mpc.data.portal

import org.mpc.domain.model.PORTAL_HOME_URL
import org.mpc.domain.model.PortalLaunchMode
import org.mpc.domain.model.PortalShortcutDestination
import org.mpc.domain.model.PortalShortcutIcon
import org.mpc.domain.model.PortalShortcutItem
import org.mpc.domain.model.PortalShortcutSection
import org.mpc.domain.model.portalUrl

val defaultPortalShortcutSections: List<PortalShortcutSection> = listOf(
    PortalShortcutSection(
        title = "常用服務",
        items = listOf(
            externalShortcut("新ee-class", PortalShortcutIcon.BOOK, "https://ncueeclass.ncu.edu.tw/"),
            externalShortcut("選課系統", PortalShortcutIcon.EVENT, "https://cis.ncu.edu.tw/Course/"),
            portalShortcut("服務櫃台", PortalShortcutIcon.SUPPORT_AGENT, "/system/incu"),
            portalShortcut("NCU Mail", PortalShortcutIcon.MAIL, "/system/129"),
            portalShortcut("成績查詢", PortalShortcutIcon.GRADE, "/system/incu-studentscore"),
            portalShortcut("人事系統", PortalShortcutIcon.BADGE, "/system/humanoauth"),
            portalShortcut("圖書館服務平台", PortalShortcutIcon.LOCAL_LIBRARY, "/system/library-cloud-services"),
            portalShortcut("宿舍網路系統", PortalShortcutIcon.WIFI, "/system/dormnet"),
        ),
    ),
    PortalShortcutSection(
        title = "課務相關",
        items = listOf(
            externalShortcut("新ee-class", PortalShortcutIcon.BOOK, "https://ncueeclass.ncu.edu.tw/"),
            externalShortcut("選課系統", PortalShortcutIcon.EVENT, "https://cis.ncu.edu.tw/Course/"),
            portalShortcut("成績查詢", PortalShortcutIcon.GRADE, "/system/incu-studentscore"),
            portalShortcut("期中預警查詢", PortalShortcutIcon.WARNING, "/system/incu-ewarningstudent"),
        ),
    ),
    PortalShortcutSection(
        title = "學務相關",
        items = listOf(
            portalShortcut("學生證掛失", PortalShortcutIcon.CREDIT_CARD_OFF, "/system/32"),
            portalShortcut("學籍系統", PortalShortcutIcon.ACCOUNT_BALANCE, "/system/incu-registrationflow"),
            portalShortcut("畢業資格審查", PortalShortcutIcon.SCHOOL, "/system/incu-graduate-2"),
            portalShortcut("學費繳費證明", PortalShortcutIcon.REQUEST_QUOTE, "/system/tuition"),
        ),
    ),
    PortalShortcutSection(
        title = "可利用資源",
        items = listOf(
            portalShortcut("個別諮商", PortalShortcutIcon.SUPPORT_AGENT, "/system/consult"),
            portalShortcut("圖書館服務平台", PortalShortcutIcon.LOCAL_LIBRARY, "/system/library-cloud-services"),
        ),
    ),
    PortalShortcutSection(
        title = "電算中心",
        items = listOf(
            portalShortcut("宿舍網路", PortalShortcutIcon.ROUTER, "/system/dormnet"),
            portalShortcut("客服中心", PortalShortcutIcon.HEADSET, "/system/sdsystem"),
            portalShortcut("Office365", PortalShortcutIcon.WORKSPACES, "/system/office365"),
            portalShortcut("G Suite", PortalShortcutIcon.APPS, "/system/gsuite"),
        ),
    ),
    PortalShortcutSection(
        title = "財務相關",
        items = listOf(
            portalShortcut("學費繳費單", PortalShortcutIcon.REQUEST_QUOTE, "/system/82"),
            portalShortcut("學費繳費證明", PortalShortcutIcon.REQUEST_QUOTE, "/system/tuition"),
            portalShortcut("人事系統", PortalShortcutIcon.BADGE, "/system/humanoauth"),
            portalShortcut("獎助學金暨工讀管理系統", PortalShortcutIcon.WORKSPACE_PREMIUM, "/system/134"),
            portalShortcut("就學補助系統", PortalShortcutIcon.SAVINGS, "/system/42"),
            portalShortcut("撥帳系統", PortalShortcutIcon.ACCOUNT_BALANCE, "/system/46"),
        ),
    ),
)

fun filterPortalShortcutSections(
    sections: List<PortalShortcutSection>,
    query: String,
): List<PortalShortcutSection> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) return sections

    return sections.mapNotNull { section ->
        val filteredItems = section.items.filter { item ->
            item.label.contains(normalizedQuery, ignoreCase = true)
        }
        section.takeIf { filteredItems.isNotEmpty() }?.copy(items = filteredItems)
    }
}

fun portalLoginDestination(): PortalShortcutDestination = PortalShortcutDestination(
    title = "Portal 登入",
    url = PORTAL_HOME_URL,
    launchMode = PortalLaunchMode.IN_APP,
)

private fun portalShortcut(
    label: String,
    icon: PortalShortcutIcon,
    path: String,
): PortalShortcutItem = PortalShortcutItem(
    label = label,
    icon = icon,
    destination = PortalShortcutDestination(
        title = label,
        url = portalUrl(path),
        launchMode = PortalLaunchMode.IN_APP,
    ),
)

private fun externalShortcut(
    label: String,
    icon: PortalShortcutIcon,
    url: String,
): PortalShortcutItem = PortalShortcutItem(
    label = label,
    icon = icon,
    destination = PortalShortcutDestination(
        title = label,
        url = url,
        launchMode = PortalLaunchMode.EXTERNAL,
    ),
)
