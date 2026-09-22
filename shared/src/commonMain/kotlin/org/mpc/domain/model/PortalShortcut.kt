package org.mpc.domain.model

const val PORTAL_HOST = "portal.ncu.edu.tw"
const val PORTAL_HOME_URL = "https://portal.ncu.edu.tw/"

enum class PortalLaunchMode {
    IN_APP,
    EXTERNAL,
}

enum class PortalShortcutIcon {
    ACCOUNT_BALANCE,
    APPS,
    BADGE,
    BOOK,
    CREDIT_CARD_OFF,
    EVENT,
    GRADE,
    HEADSET,
    LOCAL_LIBRARY,
    MAIL,
    REQUEST_QUOTE,
    ROUTER,
    SAVINGS,
    SCHOOL,
    SUPPORT_AGENT,
    WARNING,
    WIFI,
    WORKSPACES,
    WORKSPACE_PREMIUM,
}

data class PortalShortcutDestination(
    val title: String,
    val url: String,
    val launchMode: PortalLaunchMode,
)

data class PortalShortcutItem(
    val label: String,
    val icon: PortalShortcutIcon,
    val destination: PortalShortcutDestination,
)

data class PortalShortcutSection(
    val title: String,
    val items: List<PortalShortcutItem>,
)

fun portalUrl(path: String): String {
    val normalizedPath = path.trim().let { value ->
        when {
            value.isEmpty() -> "/"
            value.startsWith("/") -> value
            else -> "/$value"
        }
    }
    return "https://$PORTAL_HOST$normalizedPath"
}
