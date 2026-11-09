package org.mpc.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import org.mpc.presentation.model.EeclassItemRow
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
    LaunchedEffect(viewModel, path) {
        if (viewModel.material.value == null) viewModel.loadMaterial(path)
    }

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
                EeclassHtmlBody(loaded.bodyHtml, onOpenLink)
            }
            EeclassAttachmentList(loaded.attachments, onOpenPage)
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
    LaunchedEffect(viewModel, path) {
        if (viewModel.homework.value == null) viewModel.loadHomework(path)
    }

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
                    EeclassHtmlBody(loaded.descriptionHtml, onOpenLink, topPadding = 0)
                }
            }
            EeclassAttachmentList(loaded.attachments, onOpenPage)
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

@Composable
internal fun EeclassAttachmentList(attachments: List<EeclassItemRow>, onOpenPage: (title: String, path: String) -> Unit) {
    if (attachments.isEmpty()) return
    EeclassSectionHeader("附件")
    attachments.forEach { attachment ->
        ListItem(
            headlineContent = { Text(attachment.title) },
            modifier = Modifier.clickable { onOpenPage(attachment.title, attachment.path) },
        )
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
