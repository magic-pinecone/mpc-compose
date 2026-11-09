package org.mpc.data.repository

import de.infix.testBalloon.framework.core.testSuite
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import org.mpc.data.html.JsoupHtmlParser
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
import org.mpc.domain.model.EeclassMediaId
import org.mpc.domain.repository.EeclassCourseRepository
import org.mpc.domain.session.CampusSessions
import org.mpc.domain.session.CampusSite
import org.mpc.domain.session.PortalNeed
import org.mpc.domain.session.PortalSignIn
import org.mpc.domain.session.PortalStatus
import org.mpc.domain.session.SiteFileDestination
import org.mpc.domain.session.SiteFileInfo
import org.mpc.domain.session.SiteFileMetadata
import org.mpc.domain.session.SiteFollowUp
import org.mpc.domain.session.SitePath
import org.mpc.domain.session.SiteRequest
import org.mpc.domain.session.SiteResult
import org.mpc.domain.session.SiteStatus
import org.mpc.domain.session.TransferProgress
import org.mpc.domain.session.Unavailability
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

private const val TOKEN = "ephemeral-token-should-not-leak"
private const val MATERIAL_PATH = "/media/doc/14"

private class RepositorySessions : CampusSessions {
    override val portal = MutableStateFlow<PortalStatus>(PortalStatus.Unknown)
    private val siteStatus = MutableStateFlow<SiteStatus>(SiteStatus.Unknown)
    var materialHtml = ""
    var content = "file-content".encodeToByteArray()
    var metadata = SiteFileInfo("lecture.pdf", "application/pdf", content.size.toLong(), content.size.toLong())
    var failAfterWrite = false
    var fetchFileCalls = 0
    var fetchThenCalls = 0
    var followUpWasOpen = false
    var downloadGate: CompletableDeferred<Unit>? = null
    var downloadStarted: CompletableDeferred<Unit>? = null
    val fetchedPaths = mutableListOf<String>()
    val tokenLinks = mutableListOf<String>()

    override fun status(site: CampusSite): StateFlow<SiteStatus> = siteStatus

    override suspend fun refreshPortal() = Unit

    override suspend fun <T> fetch(site: CampusSite, path: SitePath, parse: (String) -> T): SiteResult<T> = SiteResult.Page(parse(materialHtml))

    override suspend fun fetchFile(
        site: CampusSite,
        path: SitePath,
        destination: SiteFileDestination,
        onProgress: TransferProgress?,
    ): SiteResult<SiteFileMetadata> {
        fetchFileCalls++
        fetchedPaths += path.value
        downloadStarted?.complete(Unit)
        downloadGate?.await()
        destination.write(content, 0, content.size)
        if (failAfterWrite) return SiteResult.Unavailable(Unavailability.NETWORK)
        return SiteResult.Page(destination.commit(metadata))
    }

    override suspend fun <T> fetchThen(
        site: CampusSite,
        path: SitePath,
        then: suspend SiteFollowUp.(String) -> T?,
    ): SiteResult<T> {
        fetchThenCalls++
        fetchedPaths += path.value
        val followUp = object : SiteFollowUp {
            override suspend fun <R> send(request: SiteRequest<R>): R {
                check(followUpWasOpen)
                check(request is SiteRequest.Download)
                tokenLinks += request.link
                request.destination.write(content, 0, content.size)
                val completed = request.destination.commit(metadata)
                @Suppress("UNCHECKED_CAST")
                return requireNotNull(request.reply(org.mpc.domain.session.SiteResponse(200, null, "", completed))) as R
            }
        }
        followUpWasOpen = true
        val result = try {
            with(followUp) { then(materialHtml) }
        } finally {
            followUpWasOpen = false
        }
        return result?.let { SiteResult.Page(it) } ?: SiteResult.Unavailable(Unavailability.UNEXPECTED_RESPONSE)
    }

    override suspend fun refresh(site: CampusSite) = Unit

    override fun browserEntry(url: String): String = url

    override suspend fun browserPage(url: String, loading: Boolean) = Unit

    override suspend fun signInToPortal(): PortalSignIn = PortalSignIn.OpenBrowser("https://portal.ncu.edu.tw/login")

    override suspend fun signOut(site: CampusSite) = Unit

    override suspend fun signOutEverywhere() = Unit
}

private fun testCacheDirectory(): Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve("mpc-eeclass-${Random.nextLong()}")

private fun repository(sessions: RepositorySessions, cacheDirectory: Path) = DefaultEeclassCourseRepository(sessions, JsoupHtmlParser(), cacheDirectory)

private fun mediaPage(token: String = TOKEN) = """
    <div class="mod_media-docTitle"><a href="https://ncueeclass.ncu.edu.tw/media/download/22/?auth=$token">Lecture.pdf</a></div>
    <ul class="mod_media-attachList"><li><a href="/filedownload/44">Notes.pdf</a></li></ul>
""".trimIndent()

val eeclassRepositoryTests by testSuite {
    test("direct downloads cache bytes under a sanitized filename with its extension") {
        runTest {
            val root = testCacheDirectory()
            FileSystem.SYSTEM.createDirectories(root)
            try {
                val sessions = RepositorySessions().apply {
                    content = "pdf".encodeToByteArray()
                    metadata = SiteFileInfo("../metadata.json", "application/pdf", 3, 3)
                }
                val repository = repository(sessions, root)
                val attachment = EeclassAttachment.File("Lecture.pdf", SitePath.of("/filedownload/5"))

                val first = assertIs<SiteResult.Page<EeclassCachedFile>>(repository.download(attachment)).value
                assertEquals(".._metadata.json", first.name)
                assertTrue(first.path.endsWith("/file-5/files/.._metadata.json"))
                assertEquals("pdf", FileSystem.SYSTEM.source(first.path.toPath()).buffer().readUtf8())
                assertEquals(1, sessions.fetchFileCalls)

                sessions.content = "changed".encodeToByteArray()
                val second = assertIs<SiteResult.Page<EeclassCachedFile>>(repository.download(attachment)).value
                assertEquals(first, second)
                assertEquals(1, sessions.fetchFileCalls, "a complete cache entry should be reused")
            } finally {
                FileSystem.SYSTEM.deleteRecursively(root, mustExist = false)
            }
        }
    }

    test("media details expose only a host path and stable media id, and downloads send the fresh token in fetchThen") {
        runTest {
            val root = testCacheDirectory()
            FileSystem.SYSTEM.createDirectories(root)
            try {
                val sessions = RepositorySessions().apply {
                    materialHtml = mediaPage()
                    metadata = SiteFileInfo("Lecture.pdf", "application/pdf", content.size.toLong(), content.size.toLong())
                }
                val repository = repository(sessions, root)

                val detail = assertIs<SiteResult.Page<org.mpc.domain.model.EeclassMaterialDetail>>(
                    repository.fetchMaterial(SitePath.of(MATERIAL_PATH)),
                ).value
                assertEquals(EeclassMediaId("22"), (detail.attachments.first() as EeclassAttachment.Media).mediaId)
                assertEquals(listOf("Lecture.pdf", "Notes.pdf"), detail.attachments.map(EeclassAttachment::name))
                assertFalse(detail.toString().contains(TOKEN))

                val downloaded = assertIs<SiteResult.Page<EeclassCachedFile>>(
                    repository.download(detail.attachments.first()),
                ).value
                assertTrue(downloaded.path.endsWith("/media-22/files/Lecture.pdf"))
                assertEquals(listOf("https://ncueeclass.ncu.edu.tw/media/download/22/?auth=$TOKEN"), sessions.tokenLinks)
                assertFalse(downloaded.toString().contains(TOKEN))
                assertFalse(detail.toString().contains("/media/download/22"))
                assertTrue(sessions.followUpWasOpen.not())

                repository.download(detail.attachments.first())
                assertEquals(1, sessions.tokenLinks.size, "a completed media download should be cached")
            } finally {
                FileSystem.SYSTEM.deleteRecursively(root, mustExist = false)
            }
        }
    }

    test("same-key downloads serialize and share the completed cache entry") {
        runTest {
            val root = testCacheDirectory()
            FileSystem.SYSTEM.createDirectories(root)
            try {
                val sessions = RepositorySessions().apply {
                    downloadStarted = CompletableDeferred()
                    downloadGate = CompletableDeferred()
                }
                val repository = repository(sessions, root)
                val attachment = EeclassAttachment.File("Lecture.pdf", SitePath.of("/filedownload/6"))
                val downloadStarted = requireNotNull(sessions.downloadStarted)
                val downloadGate = requireNotNull(sessions.downloadGate)
                var first: SiteResult<EeclassCachedFile>? = null
                var second: SiteResult<EeclassCachedFile>? = null
                val firstDownload = launch { first = repository.download(attachment) }
                downloadStarted.await()
                val secondDownload = launch { second = repository.download(attachment) }
                yield()
                downloadGate.complete(Unit)
                firstDownload.join()
                secondDownload.join()

                assertIs<SiteResult.Page<EeclassCachedFile>>(first)
                assertIs<SiteResult.Page<EeclassCachedFile>>(second)
                assertEquals(1, sessions.fetchFileCalls)
                assertEquals((first as SiteResult.Page).value.path, (second as SiteResult.Page).value.path)
            } finally {
                FileSystem.SYSTEM.deleteRecursively(root, mustExist = false)
            }
        }
    }

    test("an unsuccessful partial download is aborted and cannot become a cache hit") {
        runTest {
            val root = testCacheDirectory()
            FileSystem.SYSTEM.createDirectories(root)
            try {
                val sessions = RepositorySessions().apply { failAfterWrite = true }
                val repository = repository(sessions, root)
                val attachment = EeclassAttachment.File("failed.pdf", SitePath.of("/filedownload/7"))

                assertIs<SiteResult.Unavailable>(repository.download(attachment))
                assertEquals(null, EeclassFileCache(root).cached("file-7"))
                assertFalse(FileSystem.SYSTEM.exists(root.resolve("eeclass/downloads/file-7/files/download.part")))
                assertFalse(FileSystem.SYSTEM.exists(root.resolve("eeclass/downloads/file-7/state/metadata.part")))
            } finally {
                FileSystem.SYSTEM.deleteRecursively(root, mustExist = false)
            }
        }
    }
}
