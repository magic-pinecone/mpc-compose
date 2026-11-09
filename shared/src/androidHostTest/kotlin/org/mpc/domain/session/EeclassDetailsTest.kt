package org.mpc.domain.session

import de.infix.testBalloon.framework.core.testSuite
import org.mpc.data.html.JsoupHtmlParser
import org.mpc.domain.model.EeclassAttachment
import org.mpc.domain.model.EeclassHomeworkReport
import kotlin.test.assertEquals

private fun materialDetailPage(html: String) = JsoupHtmlParser().parse(html, "https://ncueeclass.ncu.edu.tw/media/doc/123")

private fun homeworkDetailPage(html: String) = JsoupHtmlParser().parse(html, "https://ncueeclass.ncu.edu.tw/course/homework/77")

private fun homeworkDetail(reportMarkup: String = "", due: String = "<span class='fs-text-bold fs-text-notice'>2026-11-08</span>") = """
    <div class="module app-course_homework app-course_homework-show">
      <div class="fs-description"><dl class="dl-horizontal">
        <dt>類型</dt><dd>個人作業</dd>
        <dt>開放繳交</dt><dd>2026-11-01 08:00</dd>
        <dt>繳交期限</dt><dd>$due</dd>
        <dt>已繳交</dt><dd>3人</dd>
        <dt>允許遲交</dt><dd>否</dd>
        <dt>成績比重</dt><dd>20%</dd>
        <dt>評分方式</dt><dd>直接打分數</dd>
        <dt>說明</dt><dd><div><p>請完成<strong>練習</strong>，並<a href="/course/40001">參考課程頁面</a>。</p></div></dd>
        <dt>附件</dt><dd><div class="fs-list fs-filelist"><div class="caption"></div><ul>
          <li class="clearfix"><div class="text"><a href="/filedownload/456" target="_blank" rel="noopener noreferrer">
            <span class="text">作業說明.pdf<span class="fs-hint">(1.2 MB)</span></span>
          </a></div></li>
        </ul></div></dd>
      </dl></div>
      $reportMarkup
    </div>
""".trimIndent()

val eeclassDetailsTests by testSuite {
    test("a material detail reads its summary, rich body, and filename without attachment size") {
        val html = """
            <div class="module mod_media mod_media-docTitle">
              <div class="fs-page-header hidden-xs hidden-sm">
                <h2><div class="title pull-left">第一章 講義</div></h2>
                <div class="ext2 fs-hint">7 觀看數, 更新時間 11-08, by 林老師</div>
              </div>
            </div>
            <div class="module mod_mediaDoc mod_mediaDoc-show"><div class="fs-block"><div class="fs-block-body list-margin">
              <p>請閱讀<strong>本週講義</strong>，並<a href="/course/40001">查看課程</a>。</p>
            </div></div></div>
            <div class="module mod_media mod_media-attachList"><div class="fs-list fs-filelist">
              <div class="caption"><div class="caption">附件:</div></div><ul><li class="clearfix"><div class="text">
                <a href="/filedownload/123" target="_blank" rel="noopener noreferrer"><span class="text">講義.pdf<span class="fs-hint">(2.4 MB)</span></span></a>
              </div></li><li><div class="text"><a href="/filedownload/124"><span class="text">習題.pdf<span class="fs-hint">(3 MB)</span></span></a></div></li></ul>
            </div></div>
        """.trimIndent()

        val detail = eeclassMaterialDetail(materialDetailPage(html))

        assertEquals("第一章 講義", detail.title)
        assertEquals("林老師", detail.author)
        assertEquals("11-08", detail.updated)
        assertEquals(true, detail.bodyHtml.contains("<strong>本週講義</strong>"))
        assertEquals(true, detail.bodyHtml.contains("href=\"https://ncueeclass.ncu.edu.tw/course/40001\""))
        assertEquals(
            listOf(EeclassAttachment.File("講義.pdf", SitePath.of("/filedownload/123")), EeclassAttachment.File("習題.pdf", SitePath.of("/filedownload/124"))),
            detail.attachments,
        )
    }

    test("a homework draft preserves a date-only deadline and rich assignment details") {
        val html = homeworkDetail(
            reportMarkup = """
                <div class="fs-text-notice text-center">(作業編輯中，尚未繳交)</div>
                <div class="text-center fs-margin-default"><a href="/homework/report/456/?ajaxAuth=TOKEN" class="btn btn-primary">檢視 / 修改我的作業</a></div>
            """.trimIndent(),
        )

        val detail = eeclassHomeworkDetail(homeworkDetailPage(html))

        assertEquals("個人作業", detail.type)
        assertEquals("2026-11-01 08:00", detail.opens)
        assertEquals("2026-11-08", detail.due)
        assertEquals(false, detail.lateSubmissionAllowed)
        assertEquals("20%", detail.weight)
        assertEquals("直接打分數", detail.gradingMethod)
        assertEquals(true, detail.descriptionHtml.contains("<strong>練習</strong>"))
        assertEquals(true, detail.descriptionHtml.contains("href=\"https://ncueeclass.ncu.edu.tw/course/40001\""))
        assertEquals(listOf(EeclassAttachment.File("作業說明.pdf", SitePath.of("/filedownload/456"))), detail.attachments)
        assertEquals(EeclassHomeworkReport.Draft, detail.report)
    }

    test("a homework report link without draft status means submitted") {
        val html = homeworkDetail(
            reportMarkup = "<div class='text-center'><a href='/homework/report/789/?ajaxAuth=TOKEN'>檢視 / 修改我的作業</a></div>",
            due = "2026-11-08 23:59",
        )

        assertEquals(EeclassHomeworkReport.Submitted, eeclassHomeworkDetail(homeworkDetailPage(html)).report)
    }

    test("a homework without a report has none status and retains its date-only deadline") {
        val detail = eeclassHomeworkDetail(homeworkDetailPage(homeworkDetail()))

        assertEquals("2026-11-08", detail.due)
        assertEquals(EeclassHomeworkReport.None, detail.report)
    }
}
