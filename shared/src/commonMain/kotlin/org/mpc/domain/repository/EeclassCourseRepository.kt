package org.mpc.domain.repository

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
import org.mpc.domain.session.SitePath
import org.mpc.domain.session.SiteResult
import org.mpc.domain.session.TransferProgress

interface EeclassCourseRepository {
    /** The student's enrolled courses from the ee-class dashboard, recovering the session when it can. */
    suspend fun fetchEnrolledCourses(): SiteResult<List<EeclassCourse>>

    /** The first page of [course]'s announcements, materials, and homework; fails if any one list fails. */
    suspend fun fetchCourseContent(course: EeclassCourseNumber): SiteResult<EeclassCourseContent>

    /** One announcement's dialog, read through [course]'s announcement list, which holds the dialog's token. */
    suspend fun fetchBulletin(course: EeclassCourseNumber, bulletin: EeclassBulletinNumber): SiteResult<EeclassBulletinDetail>

    suspend fun fetchMaterial(path: SitePath): SiteResult<EeclassMaterialDetail>

    suspend fun fetchHomework(path: SitePath): SiteResult<EeclassHomeworkDetail>

    /** Downloads and caches a direct file attachment or a material's media reference. */
    suspend fun download(attachment: EeclassAttachment, onProgress: TransferProgress? = null): SiteResult<EeclassCachedFile>

    /** The first page of every enrolled course's homework list. */
    suspend fun fetchHomeworkAcrossCourses(): SiteResult<AcrossCourses<EeclassHomework>>

    /** The first page of every enrolled course's announcement list. */
    suspend fun fetchBulletinsAcrossCourses(): SiteResult<AcrossCourses<EeclassBulletin>>
}
