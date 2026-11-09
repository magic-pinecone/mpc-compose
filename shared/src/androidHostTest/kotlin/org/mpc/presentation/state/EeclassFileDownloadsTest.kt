package org.mpc.presentation.state

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.mpc.domain.model.AcrossCourses
import org.mpc.domain.model.EeclassAttachment
import org.mpc.domain.model.EeclassBulletin
import org.mpc.domain.model.EeclassBulletinDetail
import org.mpc.domain.model.EeclassBulletinNumber
import org.mpc.domain.model.EeclassCachedFile
import org.mpc.domain.model.EeclassCourse
import org.mpc.domain.model.EeclassCourseContent
import org.mpc.domain.model.EeclassCourseNumber
import org.mpc.domain.model.EeclassHomework
import org.mpc.domain.model.EeclassHomeworkDetail
import org.mpc.domain.model.EeclassMaterialDetail
import org.mpc.domain.repository.EeclassCourseRepository
import org.mpc.domain.session.PortalNeed
import org.mpc.domain.session.SitePath
import org.mpc.domain.session.SiteResult
import org.mpc.domain.session.TransferProgress
import org.mpc.domain.session.Unavailability
import org.mpc.presentation.model.EeclassFileStatus
import org.mpc.presentation.model.eeclassFileLinkRow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EeclassFileDownloadsTest {
    private val file = EeclassCachedFile("/cache/eeclass/downloads/file-7/files/講義.pdf", "講義.pdf", "application/pdf", 12)
    private val row = assertNotNull(eeclassFileLinkRow("https://ncueeclass.ncu.edu.tw/filedownload/7"))

    @Test
    fun aFinishedDownloadReturnsTheFileAndClearsTheRow() = runTest {
        val downloads = EeclassFileDownloads(
            FakeRepository { onProgress ->
                onProgress?.invoke(6, 12)
                SiteResult.Page(file)
            },
        )

        assertEquals(file, downloads.fetch(row))
        assertNull(downloads.transfer(row).first())
    }

    @Test
    fun progressIsAFractionOnceTheSizeIsKnown() = runTest {
        var seen: Float? = null
        lateinit var downloads: EeclassFileDownloads
        downloads = EeclassFileDownloads(
            FakeRepository { onProgress ->
                onProgress?.invoke(3, 12)
                seen = downloads.current(row)?.fraction
                SiteResult.Page(file)
            },
        )

        downloads.fetch(row)

        assertEquals(0.25f, seen)
    }

    @Test
    fun aFailureKeepsTheRowFailedUntilTheNextTap() = runTest {
        var attempts = 0
        val downloads = EeclassFileDownloads(
            FakeRepository {
                attempts += 1
                if (attempts == 1) SiteResult.Unavailable(Unavailability.NETWORK) else SiteResult.Page(file)
            },
        )

        assertNull(downloads.fetch(row))
        assertEquals(EeclassFileStatus.FAILED, downloads.current(row)?.status)
        assertEquals(file, downloads.fetch(row))
        assertNull(downloads.current(row))
    }

    @Test
    fun aSignedOutSessionAsksForPortal() = runTest {
        val downloads = EeclassFileDownloads(FakeRepository { SiteResult.NeedsPortal(PortalNeed.MANUAL_SIGN_IN) })

        assertNull(downloads.fetch(row))
        assertEquals(EeclassFileStatus.NEEDS_PORTAL, downloads.current(row)?.status)
    }

    @Test
    fun onlyEeclassFileDownloadLinksAreFiles() {
        assertEquals("file-7", eeclassFileLinkRow("https://ncueeclass.ncu.edu.tw/filedownload/7/")?.key)
        assertNull(eeclassFileLinkRow("https://ncueeclass.ncu.edu.tw/media/doc/7"))
        assertNull(eeclassFileLinkRow("https://example.com/filedownload/7"))
        assertNull(eeclassFileLinkRow("http://ncueeclass.ncu.edu.tw/filedownload/7"))
    }

    private class FakeRepository(
        private val answer: (TransferProgress?) -> SiteResult<EeclassCachedFile>,
    ) : EeclassCourseRepository {
        override suspend fun download(attachment: EeclassAttachment, onProgress: TransferProgress?) = answer(onProgress)

        override suspend fun fetchEnrolledCourses(): SiteResult<List<EeclassCourse>> = error("unused")

        override suspend fun fetchCourseContent(course: EeclassCourseNumber): SiteResult<EeclassCourseContent> = error("unused")

        override suspend fun fetchBulletin(course: EeclassCourseNumber, bulletin: EeclassBulletinNumber): SiteResult<EeclassBulletinDetail> = error("unused")

        override suspend fun fetchMaterial(path: SitePath): SiteResult<EeclassMaterialDetail> = error("unused")

        override suspend fun fetchHomework(path: SitePath): SiteResult<EeclassHomeworkDetail> = error("unused")

        override suspend fun fetchHomeworkAcrossCourses(): SiteResult<AcrossCourses<EeclassHomework>> = error("unused")

        override suspend fun fetchBulletinsAcrossCourses(): SiteResult<AcrossCourses<EeclassBulletin>> = error("unused")
    }
}
