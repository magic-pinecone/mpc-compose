package org.mpc.presentation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CreditCardOff
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.mpc.data.portal.defaultPortalShortcutSections
import org.mpc.data.portal.filterPortalShortcutSections
import org.mpc.data.portal.portalLoginDestination
import org.mpc.domain.model.PortalShortcutDestination
import org.mpc.domain.model.PortalShortcutIcon
import org.mpc.domain.model.PortalShortcutItem
import org.mpc.domain.model.PortalShortcutSection

@Composable
fun PortalScreen(
    modifier: Modifier = Modifier,
    initialSearchQuery: String = "",
    sections: List<PortalShortcutSection> = defaultPortalShortcutSections,
    onOpenDestination: (PortalShortcutDestination) -> Unit,
) {
    var query by remember(initialSearchQuery) { mutableStateOf(initialSearchQuery) }
    val visibleSections = remember(sections, query) {
        filterPortalShortcutSections(sections, query)
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { onOpenDestination(portalLoginDestination()) }) {
                    Text("Portal 登入")
                }
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("搜尋 Portal 服務") },
            )
        }
        if (visibleSections.isEmpty()) {
            item {
                Text(
                    text = "找不到符合的服務",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            items(
                items = visibleSections,
                key = { section -> section.title },
            ) { section ->
                PortalShortcutSectionView(
                    section = section,
                    onOpenDestination = onOpenDestination,
                )
            }
        }
    }
}

@Composable
private fun PortalShortcutSectionView(
    section: PortalShortcutSection,
    onOpenDestination: (PortalShortcutDestination) -> Unit,
) {
    Column {
        Text(
            text = section.title,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        section.items.chunked(PORTAL_GRID_COLUMNS).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { item ->
                    PortalShortcutButton(
                        modifier = Modifier.weight(1f),
                        item = item,
                        onClick = { onOpenDestination(item.destination) },
                    )
                }
                repeat(PORTAL_GRID_COLUMNS - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PortalShortcutButton(
    modifier: Modifier,
    item: PortalShortcutItem,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilledTonalButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            onClick = onClick,
            contentPadding = PaddingValues(6.dp),
        ) {
            Icon(
                imageVector = item.icon.toImageVector(),
                contentDescription = item.label,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = item.label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun PortalShortcutIcon.toImageVector(): ImageVector = portalShortcutIcons.getValue(this)

private val portalShortcutIcons = mapOf(
    PortalShortcutIcon.ACCOUNT_BALANCE to Icons.Default.AccountBalance,
    PortalShortcutIcon.APPS to Icons.Default.Apps,
    PortalShortcutIcon.BADGE to Icons.Default.Badge,
    PortalShortcutIcon.BOOK to Icons.Default.Book,
    PortalShortcutIcon.CREDIT_CARD_OFF to Icons.Default.CreditCardOff,
    PortalShortcutIcon.EVENT to Icons.Default.Event,
    PortalShortcutIcon.GRADE to Icons.Default.Grade,
    PortalShortcutIcon.HEADSET to Icons.Default.HeadsetMic,
    PortalShortcutIcon.LOCAL_LIBRARY to Icons.Default.LocalLibrary,
    PortalShortcutIcon.MAIL to Icons.Default.Mail,
    PortalShortcutIcon.REQUEST_QUOTE to Icons.Default.RequestQuote,
    PortalShortcutIcon.ROUTER to Icons.Default.Router,
    PortalShortcutIcon.SAVINGS to Icons.Default.Savings,
    PortalShortcutIcon.SCHOOL to Icons.Default.School,
    PortalShortcutIcon.SUPPORT_AGENT to Icons.Default.SupportAgent,
    PortalShortcutIcon.WARNING to Icons.Default.WarningAmber,
    PortalShortcutIcon.WIFI to Icons.Default.Wifi,
    PortalShortcutIcon.WORKSPACES to Icons.Default.Workspaces,
    PortalShortcutIcon.WORKSPACE_PREMIUM to Icons.Default.WorkspacePremium,
)

private const val PORTAL_GRID_COLUMNS = 4
