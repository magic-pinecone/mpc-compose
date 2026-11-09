package org.mpc.presentation

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import org.mpc.domain.model.EeclassCachedFile
import java.io.File

private const val GENERIC_MIME_TYPE = "application/octet-stream"

/** The `FileProvider` authority that serves the ee-class download cache. */
internal fun Context.eeclassFileAuthority(): String = "$packageName.eeclassfiles"

internal fun eeclassFileMimeType(file: EeclassCachedFile): String {
    val declared = file.contentType?.takeIf { it.isNotBlank() && it != GENERIC_MIME_TYPE }
    val byExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.name.substringAfterLast('.', "").lowercase())
    return declared ?: byExtension ?: GENERIC_MIME_TYPE
}

/** Opens [file] in the viewer the user has for its type, or offers the share sheet when no app can show it. */
internal fun Context.openEeclassFile(file: EeclassCachedFile) {
    val uri = FileProvider.getUriForFile(this, eeclassFileAuthority(), File(file.path))
    val mimeType = eeclassFileMimeType(file)
    val view = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, mimeType)
        .grantingRead(uri)
    try {
        startActivity(view)
    } catch (_: ActivityNotFoundException) {
        val send = Intent(Intent.ACTION_SEND)
            .setType(mimeType)
            .putExtra(Intent.EXTRA_STREAM, uri)
            .grantingRead(uri)
        startActivity(Intent.createChooser(send, file.name))
    }
}

private fun Intent.grantingRead(uri: Uri): Intent = addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    .apply { clipData = ClipData.newRawUri("", uri) }
