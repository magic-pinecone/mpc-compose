package org.mpc.data.repository

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.IOException
import okio.Path
import org.mpc.domain.html.HtmlElement
import org.mpc.domain.html.HtmlParser
import org.mpc.domain.model.AcrossCourses
import org.mpc.domain.model.EeclassAttachment
import org.mpc.domain.model.EeclassBulletin
import org.mpc.domain.model.EeclassBulletinDetail
import org.mpc.domain.model.EeclassBulletinNumber
import org.mpc.domain.model.EeclassCachedFile
import org.mpc.domain.model.EeclassCourse
import org.mpc.domain.model.EeclassCourseContent
import org.mpc.domain.model.EeclassCourseItem
import org.mpc.domain.model.EeclassCourseNumber
import org.mpc.domain.model.EeclassHomework
import org.mpc.domain.model.EeclassHomeworkDetail
import org.mpc.domain.model.EeclassMaterialDetail
import org.mpc.domain.repository.EeclassCourseRepository
import org.mpc.domain.session.CampusSessions
import org.mpc.domain.session.CampusSites
import org.mpc.domain.session.EECLASS_DASHBOARD
import org.mpc.domain.session.SiteFileDestination
import org.mpc.domain.session.SiteFileMetadata
import org.mpc.domain.session.SitePath
import org.mpc.domain.session.SiteRequest
import org.mpc.domain.session.SiteResult
import org.mpc.domain.session.TransferProgress
import org.mpc.domain.session.Unavailability
import org.mpc.domain.session.eeclassBulletinDetail
import org.mpc.domain.session.eeclassBulletinDialogLink
import org.mpc.domain.session.eeclassBulletins
import org.mpc.domain.session.eeclassBulletinsPath
import org.mpc.domain.session.eeclassCourses
import org.mpc.domain.session.eeclassHomework
import org.mpc.domain.session.eeclassHomeworkDetail
import org.mpc.domain.session.eeclassHomeworkPath
import org.mpc.domain.session.eeclassMaterialDetail
import org.mpc.domain.session.eeclassMaterialDownloadRequest
import org.mpc.domain.session.eeclassMaterials
import org.mpc.domain.session.eeclassMaterialsPath

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class DefaultEeclassCourseRepository(
    private val campusSessions: CampusSessions,
    private val parser: HtmlParser,
    cacheDirectory: Path,
) : EeclassCourseRepository {
    private val fileCache = EeclassFileCache(cacheDirectory)
    private val downloadLocks = mutableMapOf<String, Mutex>()
    private val downloadLocksMutex = Mutex()

    override suspend fun fetchEnrolledCourses(): SiteResult<List<EeclassCourse>> = fetchPage(EECLASS_DASHBOARD, ::eeclassCourses)

    override suspend fun fetchCourseContent(course: EeclassCourseNumber): SiteResult<EeclassCourseContent> = fetchPage(eeclassBulletinsPath(course), ::eeclassBulletins).then { bulletins ->
        fetchPage(eeclassMaterialsPath(course), ::eeclassMaterials).then { materials ->
            fetchPage(eeclassHomeworkPath(course), ::eeclassHomework).then { homework ->
                SiteResult.Page(EeclassCourseContent(bulletins, materials, homework))
            }
        }
    }

    override suspend fun fetchBulletin(course: EeclassCourseNumber, bulletin: EeclassBulletinNumber): SiteResult<EeclassBulletinDetail> {
        val site = CampusSites.Eeclass
        val list = eeclassBulletinsPath(course)
        return campusSessions.fetchThen(site, list) { html ->
            eeclassBulletinDialogLink(parser.parse(html, site.url(list)), bulletin)?.let { link ->
                eeclassBulletinDetail(parser.parse(send(SiteRequest.Get(link)), site.baseUrl + BULLETIN_DIALOG))
            }
        }
    }

    override suspend fun fetchMaterial(path: SitePath): SiteResult<EeclassMaterialDetail> = fetchPage(path) { eeclassMaterialDetail(it, path) }

    override suspend fun fetchHomework(path: SitePath): SiteResult<EeclassHomeworkDetail> = fetchPage(path, ::eeclassHomeworkDetail)

    override suspend fun download(attachment: EeclassAttachment, onProgress: TransferProgress?): SiteResult<EeclassCachedFile> = when (attachment) {
        is EeclassAttachment.File -> downloadCached("file-${attachment.id.value}", attachment.name) { destination ->
            campusSessions.fetchFile(CampusSites.Eeclass, attachment.path, destination, onProgress)
        }

        is EeclassAttachment.Media -> downloadCached("media-${attachment.mediaId.value}", attachment.name) { destination ->
            campusSessions.fetchThen(CampusSites.Eeclass, attachment.path) { html ->
                val page = parser.parse(html, CampusSites.Eeclass.url(attachment.path))
                eeclassMaterialDownloadRequest(page, attachment.mediaId, destination, onProgress)?.let { send(it) }
            }
        }
    }

    override suspend fun fetchHomeworkAcrossCourses(): SiteResult<AcrossCourses<EeclassHomework>> = acrossCourses(::eeclassHomeworkPath, ::eeclassHomework)

    override suspend fun fetchBulletinsAcrossCourses(): SiteResult<AcrossCourses<EeclassBulletin>> = acrossCourses(::eeclassBulletinsPath, ::eeclassBulletins)

    // The site session holds a lock for each page load, so concurrent requests would only queue behind it.
    private suspend fun <T> acrossCourses(
        list: (EeclassCourseNumber) -> SitePath,
        read: (HtmlElement) -> List<T>,
    ): SiteResult<AcrossCourses<T>> = fetchEnrolledCourses().then { courses ->
        val items = mutableListOf<EeclassCourseItem<T>>()
        val unread = mutableListOf<EeclassCourse>()
        for (course in courses) {
            when (val rows = fetchPage(list(course.number), read)) {
                is SiteResult.Page -> rows.value.mapTo(items) { EeclassCourseItem(course, it) }
                is SiteResult.NeedsPortal, is SiteResult.Unavailable -> unread += course
            }
        }
        SiteResult.Page(AcrossCourses(items, unread))
    }

    private suspend fun <T> fetchPage(path: SitePath, read: (HtmlElement) -> T): SiteResult<T> {
        val site = CampusSites.Eeclass
        return campusSessions.fetch(site, path) { html -> read(parser.parse(html, site.url(path))) }
    }

    private suspend fun downloadCached(
        key: String,
        fallbackName: String,
        fetch: suspend (SiteFileDestination) -> SiteResult<SiteFileMetadata>,
    ): SiteResult<EeclassCachedFile> {
        val lock = downloadLocksMutex.withLock { downloadLocks.getOrPut(key) { Mutex() } }
        return lock.withLock {
            fileCache.cached(key)?.let { return@withLock SiteResult.Page(it) }
            val destination = try {
                fileCache.destination(key, fallbackName)
            } catch (_: IOException) {
                return@withLock SiteResult.Unavailable(Unavailability.UNEXPECTED_RESPONSE)
            }
            try {
                when (val result = fetch(destination)) {
                    is SiteResult.Page -> {
                        val cached = fileCache.cached(key)
                        if (cached == null || cached.path != result.value.localPath) {
                            SiteResult.Unavailable(Unavailability.UNEXPECTED_RESPONSE)
                        } else {
                            SiteResult.Page(cached)
                        }
                    }

                    is SiteResult.NeedsPortal -> result

                    is SiteResult.Unavailable -> result
                }
            } finally {
                destination.abort()
            }
        }
    }
}

/** Where ee-class serves announcement dialogs; relative links in a dialog resolve against it. */
private const val BULLETIN_DIALOG = "/bulletin/content/"

/** Continues with [next] on a page; a session or network failure ends the chain as it is. */
private inline fun <T, R> SiteResult<T>.then(next: (T) -> SiteResult<R>): SiteResult<R> = when (this) {
    is SiteResult.Page -> next(value)
    is SiteResult.NeedsPortal -> this
    is SiteResult.Unavailable -> this
}
