package org.mpc.presentation.model

import org.mpc.domain.model.EeclassAttachment
import org.mpc.domain.model.EeclassHomeworkDetail
import org.mpc.domain.model.EeclassHomeworkReport
import org.mpc.domain.model.EeclassMaterialDetail
import org.mpc.domain.session.SitePath
import org.mpc.domain.session.SiteResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EeclassDetailPagesTest {
    private fun homework(report: EeclassHomeworkReport, late: Boolean? = false, weight: String = "10") = SiteResult.Page(
        EeclassHomeworkDetail(
            type = "個人作業",
            opens = "2026-11-01",
            due = "2026-11-20",
            lateSubmissionAllowed = late,
            weight = weight,
            gradingMethod = "",
            descriptionHtml = "<p>寫報告</p>",
            attachments = listOf(EeclassAttachment.File("spec.pdf", SitePath.of("/filedownload/1"))),
            report = report,
        ),
    )

    @Test
    fun homeworkShowsOnlyTheFieldsEeclassFilledIn() {
        val page = homework(EeclassHomeworkReport.None).toEeclassHomeworkPage()

        assertEquals(listOf("類型", "開放繳交", "繳交期限", "遲交", "成績比重"), page.fields.map { it.label })
        assertEquals("不允許", page.fields.single { it.label == "遲交" }.value)
        assertEquals("spec.pdf", page.attachments.single().title)
    }

    @Test
    fun homeworkOmitsALatePolicyEeclassDidNotState() {
        val page = homework(EeclassHomeworkReport.None, late = null).toEeclassHomeworkPage()

        assertFalse(page.fields.any { it.label == "遲交" })
    }

    @Test
    fun homeworkReportReadsAsTheStudentSeesIt() {
        assertEquals("未繳交", homework(EeclassHomeworkReport.None).toEeclassHomeworkPage().report)
        assertEquals("草稿未繳交", homework(EeclassHomeworkReport.Draft).toEeclassHomeworkPage().report)
        val submitted = homework(EeclassHomeworkReport.Submitted).toEeclassHomeworkPage()
        assertEquals("已繳交", submitted.report)
        assertTrue(submitted.submitted)
        assertFalse(homework(EeclassHomeworkReport.Draft).toEeclassHomeworkPage().submitted)
    }

    @Test
    fun materialJoinsAuthorAndUpdateTime() {
        val page = SiteResult.Page(
            EeclassMaterialDetail("第一週", "王老師", "2026-11-02", "<p>內容</p>", emptyList()),
        ).toEeclassMaterialPage()

        assertEquals("王老師 · 更新 2026-11-02", page.info)
        assertEquals("<p>內容</p>", page.bodyHtml)
    }
}
