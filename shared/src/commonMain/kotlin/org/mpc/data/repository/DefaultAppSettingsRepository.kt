package org.mpc.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.mpc.domain.model.AppSettings
import org.mpc.domain.model.AppThemeMode
import org.mpc.domain.model.PortalAuthenticationMode
import org.mpc.domain.repository.AppSettingsRepository

@ContributesBinding(AppScope::class)
@Inject
internal class DefaultAppSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : AppSettingsRepository {
    override val settings: Flow<AppSettings> = dataStore.data
        .map { preferences -> preferences.toAppSettings() }
        .distinctUntilChanged()

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        dataStore.edit { preferences ->
            preferences[themeModeKey] = themeMode.name
        }
    }

    override suspend fun setPortalAuthenticationMode(authenticationMode: PortalAuthenticationMode) {
        dataStore.edit { preferences ->
            preferences[portalAuthenticationModeKey] = authenticationMode.name
        }
    }
}

private val themeModeKey = stringPreferencesKey("appearance.theme_mode")
private val portalAuthenticationModeKey = stringPreferencesKey("portal.authentication_mode")

private fun Preferences.toAppSettings(): AppSettings = AppSettings(
    themeMode = this[themeModeKey].toEnumOrDefault(AppThemeMode.SYSTEM),
    portalAuthenticationMode = this[portalAuthenticationModeKey]
        .toEnumOrDefault(PortalAuthenticationMode.MANUAL_PORTAL),
)

private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T = this?.let { value ->
    enumValues<T>().firstOrNull { it.name == value }
} ?: default
