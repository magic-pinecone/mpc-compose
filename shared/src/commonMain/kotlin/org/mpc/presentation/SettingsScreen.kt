package org.mpc.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.mpc.domain.model.AppSettings
import org.mpc.domain.model.AppThemeMode
import org.mpc.domain.model.PortalAuthenticationMode

@Composable
fun SettingsScreen(
    settings: AppSettings,
    modifier: Modifier = Modifier,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onPortalAuthenticationModeSelected: (PortalAuthenticationMode) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SettingsSectionTitle(
            title = "外觀",
            description = "選擇 App 使用系統、淺色或深色配色。",
        )
        SettingsChoiceRow(
            title = "跟隨系統",
            description = "使用裝置目前的外觀設定",
            selected = settings.themeMode == AppThemeMode.SYSTEM,
            onClick = { onThemeModeSelected(AppThemeMode.SYSTEM) },
        )
        SettingsChoiceRow(
            title = "淺色",
            description = "固定使用淺色配色",
            selected = settings.themeMode == AppThemeMode.LIGHT,
            onClick = { onThemeModeSelected(AppThemeMode.LIGHT) },
        )
        SettingsChoiceRow(
            title = "深色",
            description = "固定使用深色配色",
            selected = settings.themeMode == AppThemeMode.DARK,
            onClick = { onThemeModeSelected(AppThemeMode.DARK) },
        )

        HorizontalDivider()

        SettingsSectionTitle(
            title = "Portal 登入方式",
            description = "選擇 Portal session 由哪一種 adapter 管理。",
        )
        SettingsChoiceRow(
            title = "安全儲存登入資訊",
            description = "iOS 使用 Keychain；Android 使用 Keystore-backed storage",
            selected = settings.portalAuthenticationMode == PortalAuthenticationMode.SECURE_CREDENTIALS,
            onClick = {
                onPortalAuthenticationModeSelected(PortalAuthenticationMode.SECURE_CREDENTIALS)
            },
        )
        SettingsChoiceRow(
            title = "Portal 手動登入",
            description = "在 App 內的 Portal WebView 完成登入",
            selected = settings.portalAuthenticationMode == PortalAuthenticationMode.MANUAL_PORTAL,
            onClick = {
                onPortalAuthenticationModeSelected(PortalAuthenticationMode.MANUAL_PORTAL)
            },
        )
    }
}

@Composable
private fun SettingsSectionTitle(
    title: String,
    description: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsChoiceRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
