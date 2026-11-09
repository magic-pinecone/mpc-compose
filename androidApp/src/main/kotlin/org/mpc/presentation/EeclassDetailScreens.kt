package org.mpc.presentation

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.presentation.model.EeclassAttachmentRow
import org.mpc.presentation.model.EeclassFileStatus
import org.mpc.presentation.model.EeclassFileTransfer
import org.mpc.presentation.theme.MpcTheme
import org.mpc.presentation.viewModel.EeclassViewModel

/** One material: its title, who posted it and when, the body as rich text, and its attachments. */
@Composable
fun EeclassMaterialScreen(
    title: String,
    path: String,
    onOpenPage: (title: String, path: String) -> Unit,
    onOpenLink: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EeclassViewModel = metroViewModel(),
) {
    val page by viewModel.material.collectAsStateWithLifecycle()
    val openLink = eeclassFileAwareLinkHandler(viewModel, onOpenLink)
    LaunchedEffect(viewModel, path) {
        if (viewModel.material.value == null) viewModel.loadMaterial(path)
    }

    EeclassFileEffects(viewModel)
    EeclassPageContent(
        status = page?.status,
        unavailableTitle = "無法載入教材",
        onRefresh = { viewModel.loadMaterial(path) },
        modifier = modifier,
    ) {
        val loaded = page ?: return@EeclassPageContent
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                EeclassDetailHeader(title = title, subtitle = loaded.info)
                EeclassHtmlBody(loaded.bodyHtml, openLink)
            }
            EeclassAttachmentList(loaded.attachments, viewModel)
            EeclassOpenOnWebButton { onOpenPage(title, path) }
        }
    }
}

/** One homework: its dates, late policy, weight, grading, description, attachments, and whether you handed it in. */
@Composable
fun EeclassHomeworkScreen(
    title: String,
    path: String,
    onOpenPage: (title: String, path: String) -> Unit,
    onOpenLink: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EeclassViewModel = metroViewModel(),
) {
    val page by viewModel.homework.collectAsStateWithLifecycle()
    val openLink = eeclassFileAwareLinkHandler(viewModel, onOpenLink)
    LaunchedEffect(viewModel, path) {
        if (viewModel.homework.value == null) viewModel.loadHomework(path)
    }

    EeclassFileEffects(viewModel)
    EeclassPageContent(
        status = page?.status,
        unavailableTitle = "無法載入作業",
        onRefresh = { viewModel.loadHomework(path) },
        modifier = modifier,
    ) {
        val loaded = page ?: return@EeclassPageContent
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                EeclassDetailHeader(title = title, subtitle = loaded.report, highlight = loaded.submitted)
            }
            loaded.fields.forEach { field ->
                ListItem(
                    overlineContent = { Text(field.label) },
                    headlineContent = { Text(field.value) },
                )
            }
            if (loaded.descriptionHtml.isNotBlank()) {
                EeclassSectionHeader("說明")
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EeclassHtmlBody(loaded.descriptionHtml, openLink, topPadding = 0)
                }
            }
            EeclassAttachmentList(loaded.attachments, viewModel)
            EeclassOpenOnWebButton { onOpenPage(title, path) }
        }
    }
}

@Composable
internal fun EeclassDetailHeader(title: String, subtitle: String, highlight: Boolean = false) {
    Text(
        text = title,
        style = MpcTheme.typography.sectionTitle,
        modifier = Modifier.semantics { heading() },
    )
    if (subtitle.isNotEmpty()) {
        Text(
            text = subtitle,
            style = MpcTheme.typography.supporting,
            color = if (highlight) MpcTheme.colors.accent else MpcTheme.colors.labelSecondary,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/** An HTML fragment as selectable rich text. [onOpenLink] receives the URL of a tapped link. */
@Composable
internal fun EeclassHtmlBody(html: String, onOpenLink: (url: String) -> Unit, topPadding: Int = 16) {
    val openLink by rememberUpdatedState(onOpenLink)
    val body = remember(html) {
        AnnotatedString.fromHtml(html) { link -> (link as? LinkAnnotation.Url)?.let { openLink(it.url) } }
    }
    if (body.isNotBlank()) {
        SelectionContainer {
            Text(text = body, style = MpcTheme.typography.body, modifier = Modifier.padding(top = topPadding.dp))
        }
    }
}

/** Opens each file a tap finishes downloading, and tells the user why a tapped body link's file did not open. */
@Composable
internal fun EeclassFileEffects(viewModel: EeclassViewModel) {
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.openedFiles.collect { file ->
            try {
                context.openEeclassFile(file)
            } catch (_: IllegalArgumentException) {
                Toast.makeText(context, "無法開啟檔案", Toast.LENGTH_SHORT).show()
            }
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.linkFailures.collect { status ->
            val message = if (status == EeclassFileStatus.NEEDS_PORTAL) "需要登入 Portal" else "無法下載檔案"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}

/** [onOpenLink] for page bodies: an ee-class file link downloads and opens natively, any other link goes on. */
@Composable
internal fun eeclassFileAwareLinkHandler(viewModel: EeclassViewModel, onOpenLink: (url: String) -> Unit): (String) -> Unit {
    val fallback by rememberUpdatedState(onOpenLink)
    return remember(viewModel) { { url -> if (!viewModel.openFileLink(url)) fallback(url) } }
}

@Composable
internal fun EeclassAttachmentList(attachments: List<EeclassAttachmentRow>, viewModel: EeclassViewModel) {
    if (attachments.isEmpty()) return
    EeclassSectionHeader("附件")
    attachments.forEach { attachment -> EeclassAttachmentItem(attachment, viewModel) }
}

@Composable
private fun EeclassAttachmentItem(attachment: EeclassAttachmentRow, viewModel: EeclassViewModel) {
    val transfer by produceState<EeclassFileTransfer?>(null, viewModel, attachment) {
        viewModel.fileTransfer(attachment).collect { value = it }
    }
    val status = transfer?.status
    ListItem(
        leadingContent = { Icon(Icons.Outlined.AttachFile, contentDescription = null) },
        headlineContent = { Text(attachment.title) },
        supportingContent = transfer?.let { current -> { EeclassTransferStatus(current) } },
        trailingContent = if (status == EeclassFileStatus.FAILED || status == EeclassFileStatus.NEEDS_PORTAL) {
            { TextButton(onClick = { viewModel.openFile(attachment) }) { Text("重試") } }
        } else {
            null
        },
        modifier = Modifier.clickable(enabled = status != EeclassFileStatus.DOWNLOADING) { viewModel.openFile(attachment) },
    )
}

@Composable
private fun EeclassTransferStatus(transfer: EeclassFileTransfer) {
    when (transfer.status) {
        EeclassFileStatus.DOWNLOADING -> {
            val fraction = transfer.fraction
            if (fraction == null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
            }
        }

        EeclassFileStatus.FAILED -> Text("下載失敗")

        EeclassFileStatus.NEEDS_PORTAL -> Text("需要登入 Portal，請在 Portal 分頁登入")
    }
}

@Composable
private fun EeclassOpenOnWebButton(onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Text("在網頁開啟")
    }
}
