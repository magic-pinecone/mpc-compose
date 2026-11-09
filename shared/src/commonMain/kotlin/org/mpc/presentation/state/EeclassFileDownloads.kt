package org.mpc.presentation.state

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.mpc.domain.model.EeclassCachedFile
import org.mpc.domain.repository.EeclassCourseRepository
import org.mpc.domain.session.SiteResult
import org.mpc.presentation.model.EeclassAttachmentRow
import org.mpc.presentation.model.EeclassFileStatus
import org.mpc.presentation.model.EeclassFileTransfer

/** Downloads attachments for native screens and keeps each row's progress or failure until its next tap. */
class EeclassFileDownloads(
    private val repository: EeclassCourseRepository,
) {
    private val transfers = MutableStateFlow<Map<String, EeclassFileTransfer>>(emptyMap())

    /** The row's download in flight or last failure. Null once it has finished or when nothing was tried. */
    fun transfer(row: EeclassAttachmentRow): Flow<EeclassFileTransfer?> = transfers.map { it[row.key] }.distinctUntilChanged()

    fun current(row: EeclassAttachmentRow): EeclassFileTransfer? = transfers.value[row.key]

    /** The cached file for [row], downloading it first when needed. Null when it failed or is already downloading. */
    suspend fun fetch(row: EeclassAttachmentRow): EeclassCachedFile? {
        if (current(row)?.status == EeclassFileStatus.DOWNLOADING) return null
        set(row, EeclassFileTransfer(EeclassFileStatus.DOWNLOADING))
        val result = try {
            repository.download(row.attachment) { sent, total ->
                val fraction = total?.takeIf { it > 0 }?.let { (sent.toFloat() / it).coerceIn(0f, 1f) }
                set(row, EeclassFileTransfer(EeclassFileStatus.DOWNLOADING, fraction))
            }
        } catch (cancellation: CancellationException) {
            clear(row)
            throw cancellation
        }
        return when (result) {
            is SiteResult.Page -> result.value.also { clear(row) }
            is SiteResult.NeedsPortal -> null.also { set(row, EeclassFileTransfer(EeclassFileStatus.NEEDS_PORTAL)) }
            is SiteResult.Unavailable -> null.also { set(row, EeclassFileTransfer(EeclassFileStatus.FAILED)) }
        }
    }

    private fun set(row: EeclassAttachmentRow, transfer: EeclassFileTransfer) = transfers.update { it + (row.key to transfer) }

    private fun clear(row: EeclassAttachmentRow) = transfers.update { it - row.key }
}
