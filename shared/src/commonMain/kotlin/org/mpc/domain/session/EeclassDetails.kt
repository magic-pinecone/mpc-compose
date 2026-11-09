package org.mpc.domain.session

import org.mpc.domain.html.HtmlElement
import org.mpc.domain.model.EeclassAttachment
import org.mpc.domain.model.EeclassHomeworkDetail
import org.mpc.domain.model.EeclassHomeworkReport
import org.mpc.domain.model.EeclassMaterialDetail

private val ATTACHMENT_PATH = Regex("""/filedownload/\d+""")
private val REPORT_PATH = Regex("""/homework/report/\d+/?""")

fun eeclassMaterialDetail(page: HtmlElement): EeclassMaterialDetail {
    val header = page.select(".mod_media-docTitle .fs-page-header").firstOrNull()
    val info = header?.select(".ext2")?.firstOrNull()?.text().orEmpty()
    return EeclassMaterialDetail(
        title = header?.select("h2 .title")?.firstOrNull()?.text()?.trim().orEmpty(),
        author = info.substringAfterLast(", by ", "").trim(),
        updated = info.substringAfter("更新時間 ", "").substringBefore(", by ").trim(),
        bodyHtml = page.select(".mod_mediaDoc-show .fs-block-body").firstOrNull()?.htmlWithAbsoluteLinks().orEmpty(),
        attachments = attachments(page, ".mod_media-attachList .fs-filelist"),
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
        lateSubmissionAllowed = text("允許遲交") == "是",
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
        EeclassAttachment(link.name.removeSuffix(hint).trim(), link.path)
    }
}.distinctBy { it.path }
