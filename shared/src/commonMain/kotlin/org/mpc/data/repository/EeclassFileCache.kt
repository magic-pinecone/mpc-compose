package org.mpc.data.repository

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.buffer
import org.mpc.domain.model.EeclassCachedFile
import org.mpc.domain.session.SiteFileDestination
import org.mpc.domain.session.SiteFileInfo
import org.mpc.domain.session.SiteFileMetadata

/** Stores completed ee-class downloads under stable, token-free keys. */
internal class EeclassFileCache(
    cacheDirectory: Path,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
) {
    private val directory = cacheDirectory.resolve("eeclass").resolve("downloads")

    fun cached(key: String): EeclassCachedFile? {
        require(CACHE_KEY.matches(key))
        val folder = keyDirectory(key)
        val entry = cachedEntry(folder, readRecord(recordPath(key)))
        if (entry == null) invalidate(key)
        return entry
    }

    fun destination(key: String, fallbackName: String): CacheDestination {
        require(CACHE_KEY.matches(key))
        val folder = keyDirectory(key)
        val files = folder.resolve("files")
        val state = folder.resolve("state")
        fileSystem.createDirectories(files)
        fileSystem.createDirectories(state)
        val temporaryFile = state.resolve("download.part")
        val temporaryRecord = state.resolve("metadata.part")
        fileSystem.delete(temporaryFile, mustExist = false)
        fileSystem.delete(temporaryRecord, mustExist = false)
        return CacheDestination(
            fileSystem = fileSystem,
            filesDirectory = files,
            record = state.resolve("metadata.json"),
            temporaryFile = temporaryFile,
            temporaryRecord = temporaryRecord,
            fallbackName = fallbackName,
        )
    }

    private fun keyDirectory(key: String): Path = directory.resolve(key)

    private fun recordPath(key: String): Path = keyDirectory(key).resolve("state").resolve("metadata.json")

    private fun readRecord(path: Path): CacheRecord? {
        val source = runCatching { fileSystem.source(path).buffer() }.getOrNull() ?: return null
        return try {
            Json.decodeFromString<CacheRecord>(source.readUtf8())
        } catch (_: Exception) {
            null
        } finally {
            source.close()
        }
    }

    private fun cachedEntry(folder: Path, record: CacheRecord?): EeclassCachedFile? = record
        ?.takeIf { it.name.isNotBlank() && safeFileName(it.name, it.contentType) == it.name }
        ?.let { validRecord ->
            val file = folder.resolve("files").resolve(validRecord.name)
            fileSystem.metadataOrNull(file)
                ?.takeIf { !it.isDirectory && it.size == validRecord.size }
                ?.let {
                    EeclassCachedFile(
                        path = file.toString(),
                        name = validRecord.name,
                        contentType = validRecord.contentType,
                        size = validRecord.size,
                    )
                }
        }

    private fun invalidate(key: String) {
        fileSystem.deleteRecursively(keyDirectory(key), mustExist = false)
    }

    internal class CacheDestination(
        private val fileSystem: FileSystem,
        private val filesDirectory: Path,
        private val record: Path,
        private val temporaryFile: Path,
        private val temporaryRecord: Path,
        private val fallbackName: String,
    ) : SiteFileDestination {
        private val sink = fileSystem.sink(temporaryFile, mustCreate = true).buffer()
        private var state = State.OPEN
        private var bytesWritten = 0L
        private var fileMoved: Path? = null

        override fun write(bytes: ByteArray, offset: Int, length: Int) {
            check(state == State.OPEN)
            sink.write(bytes, offset, length)
            bytesWritten += length
        }

        override fun commit(info: SiteFileInfo): SiteFileMetadata {
            check(state == State.OPEN)
            try {
                sink.close()
                check(info.size == bytesWritten) { "Downloaded file size did not match bytes written" }
                val name = safeFileName(info.name ?: fallbackName, info.contentType)
                val finalFile = filesDirectory.resolve(name)
                val cacheRecord = CacheRecord(
                    name = name,
                    contentType = info.contentType?.takeIf(String::isNotBlank),
                    contentLength = info.contentLength,
                    size = info.size,
                )
                writeRecord(cacheRecord)

                // Publish the complete file first, then its record as the cache completion marker.
                fileSystem.delete(record, mustExist = false)
                fileSystem.delete(finalFile, mustExist = false)
                fileSystem.atomicMove(temporaryFile, finalFile)
                fileMoved = finalFile
                fileSystem.atomicMove(temporaryRecord, record)
                state = State.COMMITTED
                return SiteFileMetadata(
                    name = info.name,
                    contentType = info.contentType,
                    contentLength = info.contentLength,
                    size = info.size,
                    localPath = finalFile.toString(),
                )
            } finally {
                if (state != State.COMMITTED) abort()
            }
        }

        private fun writeRecord(record: CacheRecord) {
            val sink = fileSystem.sink(temporaryRecord, mustCreate = true).buffer()
            try {
                sink.writeUtf8(Json.encodeToString(record))
                sink.flush()
            } finally {
                sink.close()
            }
        }

        override fun abort() {
            if (state == State.ABORTED || state == State.COMMITTED) return
            state = State.ABORTED
            runCatching { sink.close() }
            runCatching { fileSystem.delete(temporaryFile, mustExist = false) }
            runCatching { fileSystem.delete(temporaryRecord, mustExist = false) }
            fileMoved?.let { runCatching { fileSystem.delete(it, mustExist = false) } }
        }

        fun isCommitted(): Boolean = state == State.COMMITTED

        private enum class State { OPEN, COMMITTED, ABORTED }
    }

    @Serializable
    private data class CacheRecord(
        val name: String,
        val contentType: String?,
        val contentLength: Long?,
        val size: Long,
    )

    private companion object {
        val CACHE_KEY = Regex("""(?:file|media)-\d+""")
    }
}

private fun safeFileName(value: String, contentType: String? = null): String {
    val cleaned = sanitizeFileName(value)
    val hasExtension = cleaned.substringAfterLast('.', "").isNotEmpty()
    val extension = MIME_EXTENSIONS[contentType?.substringBefore(';')?.lowercase()]
    return if (hasExtension || extension == null) cleaned else "$cleaned.$extension"
}

private fun sanitizeFileName(value: String): String {
    val cleaned = value.map(::sanitizeFileNameCharacter).joinToString("")
        .trim()
        .trimEnd('.')
        .take(180)
    return if (cleaned.isEmpty() || cleaned == "." || cleaned == "..") "download" else cleaned
}

private fun sanitizeFileNameCharacter(character: Char): Char = when {
    character == '/' || character == '\\' -> '_'
    character in RESERVED_FILE_NAME_CHARACTERS -> '_'
    character.code < FIRST_NON_CONTROL_ASCII_CODE -> '_'
    character.code in FIRST_DELETE_CONTROL_ASCII_CODE..LAST_C1_CONTROL_ASCII_CODE -> '_'
    else -> character
}

private const val RESERVED_FILE_NAME_CHARACTERS = "<>:\"|?*"
private const val FIRST_NON_CONTROL_ASCII_CODE = 0x20
private const val FIRST_DELETE_CONTROL_ASCII_CODE = 0x7f
private const val LAST_C1_CONTROL_ASCII_CODE = 0x9f
private val MIME_EXTENSIONS = mapOf(
    "application/pdf" to "pdf",
    "application/zip" to "zip",
    "image/jpeg" to "jpg",
    "image/png" to "png",
    "image/gif" to "gif",
    "text/plain" to "txt",
    "text/csv" to "csv",
    "application/msword" to "doc",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to "docx",
    "application/vnd.ms-excel" to "xls",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" to "xlsx",
    "application/vnd.ms-powerpoint" to "ppt",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation" to "pptx",
)
