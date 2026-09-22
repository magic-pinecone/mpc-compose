package org.mpc.domain.model

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

enum class PortalAuthenticationMode {
    SECURE_CREDENTIALS,
    MANUAL_PORTAL,
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val portalAuthenticationMode: PortalAuthenticationMode = PortalAuthenticationMode.MANUAL_PORTAL,
)
