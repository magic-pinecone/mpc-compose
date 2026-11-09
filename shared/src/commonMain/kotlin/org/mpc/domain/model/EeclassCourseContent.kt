package org.mpc.domain.model

import org.mpc.domain.session.SitePath
import kotlin.jvm.JvmInline

/** The first page of an ee-class Course's announcement, material, and homework lists. */
data class EeclassCourseContent(
    val bulletins: List<EeclassBulletin>,
    val materials: List<EeclassMaterial>,
    val homework: List<EeclassHomework>,
)

// Dates and times stay as ee-class displays them: some omit the year, and materials show relative times.

/** An announcement. ee-class opens announcements in a dialog on the course's list, so it has no page of its own. */
data class EeclassBulletin(
    val number: EeclassBulletinNumber,
    val title: String,
    val author: String,
    val posted: String,
)

/** ee-class's number for an announcement, from its dialog link. */
@JvmInline
value class EeclassBulletinNumber(
    val value: String,
) {
    init {
        require(value.matches(Regex("""\d+""")))
    }
}

/** An announcement's dialog: [info] is ee-class's summary line, and [bodyHtml] is the announcement as the teacher wrote it. */
data class EeclassBulletinDetail(
    val info: String,
    val bodyHtml: String,
    val attachments: List<EeclassAttachment>,
)

sealed interface EeclassAttachment {
    val name: String
    val path: SitePath

    /** A direct `/filedownload/{id}` attachment. */
    data class File(override val name: String, override val path: SitePath) : EeclassAttachment {
        init {
            require(ATTACHMENT_ID.matches(path.value))
        }

        /** Stable, token-free ee-class file number used to identify this download in the local cache. */
        val id: EeclassAttachmentId
            get() = EeclassAttachmentId(requireNotNull(ATTACHMENT_ID.matchEntire(path.value)).groupValues[1])
    }

    /** The main media file on a material page; the one-time URL is re-read when downloading. */
    data class Media(
        override val name: String,
        override val path: SitePath,
        val mediaId: EeclassMediaId,
    ) : EeclassAttachment {
        init {
            require(MATERIAL_PATH.matches(path.value))
        }
    }
}

/** ee-class's stable numeric identifier for a file attachment. */
@JvmInline
value class EeclassAttachmentId(
    val value: String,
) {
    init {
        require(value.matches(Regex("""\d+""")))
    }
}

/** ee-class's stable numeric identifier for a material download. */
@JvmInline
value class EeclassMediaId(
    val value: String,
) {
    init {
        require(value.matches(Regex("""\d+""")))
    }
}

/** A completed ee-class file in the app cache. [path] is a local path, never a campus URL. */
data class EeclassCachedFile(
    val path: String,
    val name: String,
    val contentType: String?,
    val size: Long,
)

data class EeclassMaterial(
    val title: String,
    val author: String,
    val updated: String,
    val path: SitePath,
)

data class EeclassHomework(
    val title: String,
    val opens: String,
    val due: String,
    val score: String,
    val path: SitePath,
    val submitted: Boolean = false,
)

data class EeclassMaterialDetail(
    val title: String,
    val author: String,
    val updated: String,
    val bodyHtml: String,
    val attachments: List<EeclassAttachment>,
)

data class EeclassHomeworkDetail(
    val type: String,
    val opens: String,
    val due: String,
    val lateSubmissionAllowed: Boolean?,
    val weight: String,
    val gradingMethod: String,
    val descriptionHtml: String,
    val attachments: List<EeclassAttachment>,
    val report: EeclassHomeworkReport,
)

sealed interface EeclassHomeworkReport {
    data object None : EeclassHomeworkReport

    data object Draft : EeclassHomeworkReport

    data object Submitted : EeclassHomeworkReport
}
private val ATTACHMENT_ID = Regex("""/filedownload/(\d+)""")
private val MATERIAL_PATH = Regex("""/media/doc/\d+""")

/** One row of an enrolled course's list. */
data class EeclassCourseItem<T>(
    val course: EeclassCourse,
    val item: T,
)

/** Rows read from every enrolled course's list. [unread] names the courses whose list did not load. */
data class AcrossCourses<T>(
    val items: List<EeclassCourseItem<T>>,
    val unread: List<EeclassCourse>,
)
