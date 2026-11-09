package org.mpc.presentation.model

import org.mpc.domain.model.EeclassAttachment
import org.mpc.domain.session.eeclassFileLink

/** An attachment as native screens list it. It identifies the file, never a one-time download URL. */
class EeclassAttachmentRow internal constructor(
    val title: String,
    internal val attachment: EeclassAttachment,
) {
    val key: String = when (attachment) {
        is EeclassAttachment.File -> "file-${attachment.id.value}"
        is EeclassAttachment.Media -> "media-${attachment.mediaId.value}"
    }
}

internal fun EeclassAttachment.toRow(): EeclassAttachmentRow = EeclassAttachmentRow(name, this)

/** The row for an ee-class file link in a page body, or null when [url] is not one. */
fun eeclassFileLinkRow(url: String): EeclassAttachmentRow? = eeclassFileLink(url, "附件")?.toRow()

enum class EeclassFileStatus {
    DOWNLOADING,
    FAILED,

    /** Getting the ee-class session back needs the user to sign in to Portal. */
    NEEDS_PORTAL,
}

/** A file in flight or a failed download. [fraction] is null until the size is known. */
class EeclassFileTransfer(
    val status: EeclassFileStatus,
    val fraction: Float? = null,
)
