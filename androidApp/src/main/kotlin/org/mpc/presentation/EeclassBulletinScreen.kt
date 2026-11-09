package org.mpc.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.presentation.viewModel.EeclassViewModel

/** One announcement: its title, ee-class's summary line, the body as rich text, and its attachments. */
@Composable
fun EeclassBulletinScreen(
    course: String,
    number: String,
    title: String,
    onOpenPage: (title: String, path: String) -> Unit,
    onOpenLink: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EeclassViewModel = metroViewModel(),
) {
    val page by viewModel.bulletin.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, course, number) {
        if (viewModel.bulletin.value == null) viewModel.loadBulletin(course, number)
    }

    EeclassPageContent(
        status = page?.status,
        unavailableTitle = "無法載入公告",
        onRefresh = { viewModel.loadBulletin(course, number) },
        modifier = modifier,
    ) {
        val loaded = page ?: return@EeclassPageContent
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                EeclassDetailHeader(title = title, subtitle = loaded.info)
                EeclassHtmlBody(loaded.bodyHtml, onOpenLink)
            }
            EeclassAttachmentList(loaded.attachments, onOpenPage)
        }
    }
}
