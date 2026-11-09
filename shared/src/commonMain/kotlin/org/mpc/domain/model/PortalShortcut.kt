package org.mpc.domain.model

const val PORTAL_HOST = "portal.ncu.edu.tw"
const val PORTAL_HOME_URL = "https://portal.ncu.edu.tw/"

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
    SAVINGS,
    SCHOOL,
    SUPPORT_AGENT,
    WARNING,
    WIFI,
    WORKSPACES,
    WORKSPACE_PREMIUM,
}

/** How a platform opens a Portal shortcut. */
enum class PortalShortcutKind {
    /** The shortcut's [PortalShortcutDestination.url] in the in-app browser. */
    WEB,

    /** The native ee-class screens, with [PortalShortcutDestination.url] as their web fallback. */
    EECLASS_WRAPPER,
}

data class PortalShortcutDestination(
    val title: String,
    val url: String,
    val kind: PortalShortcutKind = PortalShortcutKind.WEB,
)

/** A category color. Each platform maps it to its own system palette. */
enum class PortalShortcutTint {
    BLUE,
    GREEN,
    ORANGE,
    PURPLE,
    TEAL,
}

data class PortalShortcutItem(
    val label: String,
    val icon: PortalShortcutIcon,
    val tint: PortalShortcutTint,
    val destination: PortalShortcutDestination,
) {
    /** Stable across catalog changes that keep the destination, so saved favorites survive relabeling. */
    val id: String get() = destination.url
}

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
