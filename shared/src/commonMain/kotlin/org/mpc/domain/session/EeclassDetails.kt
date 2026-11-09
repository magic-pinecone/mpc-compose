package org.mpc.domain.session

import org.mpc.domain.html.HtmlElement
import org.mpc.domain.model.EeclassAttachment
import org.mpc.domain.model.EeclassHomeworkDetail
import org.mpc.domain.model.EeclassHomeworkReport
import org.mpc.domain.model.EeclassMaterialDetail
import org.mpc.domain.model.EeclassMediaId

private val ATTACHMENT_PATH = Regex("""/filedownload/\d+""")
private val REPORT_PATH = Regex("""/homework/report/\d+/?""")

fun eeclassMaterialDetail(page: HtmlElement, hostPath: SitePath? = null): EeclassMaterialDetail {
    val header = page.select(".mod_media-docTitle .fs-page-header").firstOrNull()
    val info = header?.select(".ext2")?.firstOrNull()?.text().orEmpty()
    val media = page.select(".mod_media-docTitle[href], .mod_media-docTitle a[href]")
        .firstNotNullOfOrNull { element -> element.materialDownloadLink()?.second?.let { element to it } }
    val mainFile = if (hostPath != null && media != null) {
        listOf(EeclassAttachment.Media(media.first.text().trim().ifBlank { header?.text().orEmpty() }, hostPath, media.second))
    } else {
        emptyList()
    }
    return EeclassMaterialDetail(
        title = header?.select("h2 .title")?.firstOrNull()?.text()?.trim().orEmpty(),
        author = info.substringAfterLast(", by ", "").trim(),
        updated = info.substringAfter("更新時間 ", "").substringBefore(", by ").trim(),
        bodyHtml = page.select(".mod_mediaDoc-show .fs-block-body").firstOrNull()?.htmlWithAbsoluteLinks().orEmpty(),
        attachments = mainFile + attachments(page, ".mod_media-attachList"),
    )
}

fun eeclassHomeworkDetail(page: HtmlElement): EeclassHomeworkDetail {
    val section = page.select(".app-course_homework-show").firstOrNull() ?: page
    val labels = section.select(".fs-description dt")
    val values = section.select(".fs-description dt + dd")
    fun field(label: String): HtmlElement? = labels.indexOfFirst { it.text().trim() == label }
        .takeIf { it >= 0 }?.let(values::getOrNull)
    fun text(label: String): String = field(label)?.text()?.trim().orEmpty()
    val hasReport = section.select("a[href]").any { eeclassPath(it)?.matches(REPORT_PATH) == true }
    val report = when {
        !hasReport -> EeclassHomeworkReport.None
        section.text().contains("作業編輯中，尚未繳交") -> EeclassHomeworkReport.Draft
        else -> EeclassHomeworkReport.Submitted
    }
    return EeclassHomeworkDetail(
        type = text("類型"),
        opens = text("開放繳交"),
        due = text("繳交期限"),
        lateSubmissionAllowed = text("允許遲交").takeIf(String::isNotEmpty)?.let { it == "是" },
        weight = text("成績比重"),
        gradingMethod = text("評分方式"),
        descriptionHtml = field("說明")?.htmlWithAbsoluteLinks().orEmpty(),
        attachments = field("附件")?.let { attachments(it, ".fs-filelist") }.orEmpty(),
        report = report,
    )
}

private fun attachments(page: HtmlElement, selector: String): List<EeclassAttachment> = page.select(selector).flatMap { list ->
    list.select("a[href]").mapNotNull { anchor ->
        val link = anchor.siteLink(ATTACHMENT_PATH) ?: return@mapNotNull null
        val hint = anchor.select(".fs-hint").lastOrNull()?.text().orEmpty()
        EeclassAttachment.File(link.name.removeSuffix(hint).trim(), link.path)
    }
}.distinctBy { it.path }

private val MATERIAL_DOWNLOAD_PATH = Regex("""/media/download/(\d+)/?""")

/** Builds the token-bearing request only for immediate use by [CampusSessions.fetchThen]. */
internal fun eeclassMaterialDownloadRequest(
    page: HtmlElement,
    mediaId: EeclassMediaId,
    destination: SiteFileDestination,
    onProgress: TransferProgress?,
): SiteRequest<SiteFileMetadata>? {
    val link = page.select(".mod_media-docTitle[href], .mod_media-docTitle a[href]")
        .firstNotNullOfOrNull { element ->
            element.materialDownloadLink()?.takeIf { it.second == mediaId }?.first
        }
        ?: return null
    return SiteRequest.Download(link, destination, onProgress = onProgress)
}

private fun HtmlElement.materialDownloadLink(): Pair<String, EeclassMediaId>? {
    val url = absUrl("href")?.let(::parseHttpsUrl) ?: return null
    if (!url.host.equals(CampusSites.Eeclass.host, ignoreCase = true)) return null
    val pathId = MATERIAL_DOWNLOAD_PATH.matchEntire(url.encodedPath)?.groupValues?.get(1) ?: return null
    val authValues = url.parameters.getAll("auth").orEmpty()
    if (url.parameters.names() != setOf("auth") || authValues.size != 1 || authValues.single().isBlank()) return null
    return url.toString() to EeclassMediaId(pathId)
}
