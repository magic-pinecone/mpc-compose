package org.mpc.bridge

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import org.mpc.domain.model.EeclassCachedFile
import org.mpc.domain.repository.EeclassCourseRepository
import org.mpc.presentation.model.EeclassAttachmentRow
import org.mpc.presentation.model.EeclassFileTransfer
import org.mpc.presentation.model.eeclassFileLinkRow
import org.mpc.presentation.state.EeclassFileDownloads

/** What native ee-class screens read and do to open attachments: each row's progress and the download itself. */
class EeclassFilesBridge internal constructor(
    repository: EeclassCourseRepository,
) {
    private val downloads = EeclassFileDownloads(repository)

    fun transfer(row: EeclassAttachmentRow): Flow<EeclassFileTransfer?> = downloads.transfer(row)

    fun current(row: EeclassAttachmentRow): EeclassFileTransfer? = downloads.current(row)

    /** The row for an ee-class file link in a page body, or null when [url] is not one. */
    fun linkRow(url: String): EeclassAttachmentRow? = eeclassFileLinkRow(url)

    /** The cached file for [row], downloading it first when needed. Null when the download failed. */
    @Throws(CancellationException::class)
    suspend fun fetch(row: EeclassAttachmentRow): EeclassCachedFile? = downloads.fetch(row)
}
