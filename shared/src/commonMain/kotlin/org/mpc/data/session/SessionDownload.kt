package org.mpc.data.session

import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.prepareRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.decodeURLPart
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import org.mpc.domain.session.SiteFileDestination
import org.mpc.domain.session.SiteFileInfo
import org.mpc.domain.session.SiteRequest
import org.mpc.domain.session.SiteResponse
import org.mpc.domain.session.Unavailability

private const val HTTP_STATUS_OK = 200
private const val DOWNLOAD_BUFFER_SIZE = 64 * 1024
private const val TRANSFER_TIMEOUT_MILLIS = 300_000L
private const val TRANSFER_IDLE_TIMEOUT_MILLIS = 60_000L

private sealed interface DownloadExecution {
    data class Response(val response: SiteResponse) : DownloadExecution

    data class Failed(val reason: Unavailability) : DownloadExecution
}

private class DownloadContext(
    val request: SiteRequest.Download,
    val cookieJar: PlatformCookieJar,
    val storeResponseCookies: suspend (String, HttpResponse) -> Unit,
) {
    val url = request.link
    var cookiesBeforeRequest: CookieHeader? = null
    var destinationStarted = false
    var destinationCommitted = false
}

internal suspend fun sendDownload(
    client: HttpClient,
    cookieJar: PlatformCookieJar,
    request: SiteRequest.Download,
    storeResponseCookies: suspend (String, HttpResponse) -> Unit,
): TransportResult {
    val context = DownloadContext(request, cookieJar, storeResponseCookies)
    try {
        context.cookiesBeforeRequest = cookieJar.cookieHeader(context.url)
        val execution = client.prepareRequest(context.url) {
            context.cookiesBeforeRequest?.let { header(HttpHeaders.Cookie, it.value) }
            request.headers.forEach { (name, value) -> header(name.wireName, value) }
            method = HttpMethod.Get
            transferTimeout()
        }.execute { response ->
            downloadResponse(response, context)
        }
        return when (execution) {
            is DownloadExecution.Response -> TransportResult.Response(execution.response)
            is DownloadExecution.Failed -> TransportResult.Failed(execution.reason)
        }
    } catch (cancellation: CancellationException) {
        abortPartialDestination(context)
        throw cancellation
    } catch (_: Exception) {
        abortPartialDestination(context)
        return TransportResult.Failed(Unavailability.NETWORK)
    }
}

private suspend fun downloadResponse(response: HttpResponse, context: DownloadContext): DownloadExecution {
    val status = response.status.value
    val location = response.headers[HttpHeaders.Location]
    val isHtml = response.contentType()?.match(ContentType.Text.Html) == true
    if (status != HTTP_STATUS_OK || isHtml) {
        val html = if (status == HTTP_STATUS_OK && isHtml) response.bodyAsText() else ""
        return pageResponse(response, context, status, location, html)
    }
    return fileResponse(response, context, status, location)
}

private suspend fun pageResponse(
    response: HttpResponse,
    context: DownloadContext,
    status: Int,
    location: String?,
    html: String,
): DownloadExecution = if (context.cookieJar.cookieHeader(context.url) != context.cookiesBeforeRequest) {
    DownloadExecution.Failed(Unavailability.UNEXPECTED_RESPONSE)
} else {
    context.storeResponseCookies(context.url, response)
    DownloadExecution.Response(SiteResponse(status, location, html))
}

private suspend fun fileResponse(
    response: HttpResponse,
    context: DownloadContext,
    status: Int,
    location: String?,
): DownloadExecution {
    context.destinationStarted = true
    val contentLength = contentLength(response)
    val size = streamToDestination(response.bodyAsChannel(), context.request, contentLength)
    val hasExpectedSize = contentLength == null || contentLength == size
    val cookiesAreCurrent = context.cookieJar.cookieHeader(context.url) == context.cookiesBeforeRequest
    return if (!hasExpectedSize || !cookiesAreCurrent) {
        abortDestination(context.request.destination)
        context.destinationStarted = false
        DownloadExecution.Failed(Unavailability.UNEXPECTED_RESPONSE)
    } else {
        context.storeResponseCookies(context.url, response)
        val info = siteFileInfo(response, contentLength, size)
        val metadata = context.request.destination.commit(info)
        context.destinationCommitted = true
        context.destinationStarted = false
        DownloadExecution.Response(SiteResponse(status, location, "", metadata))
    }
}

private suspend fun streamToDestination(
    channel: ByteReadChannel,
    request: SiteRequest.Download,
    total: Long?,
): Long {
    var transferred = 0L
    request.onProgress?.invoke(transferred, total)
    val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
    while (true) {
        val read = channel.readAvailable(buffer, 0, buffer.size)
        if (read > 0) {
            request.destination.write(buffer, 0, read)
            transferred += read
            request.onProgress?.invoke(transferred, total)
        }
        if (read < 0) break
    }
    return transferred
}

private fun contentLength(response: HttpResponse): Long? = response.headers[HttpHeaders.ContentLength]
    ?.toLongOrNull()
    ?.takeIf { it >= 0 }

private fun siteFileInfo(response: HttpResponse, contentLength: Long?, size: Long): SiteFileInfo {
    val contentType = response.contentType()
    return SiteFileInfo(
        name = response.headers[HttpHeaders.ContentDisposition]?.let(::dispositionFileName),
        contentType = contentType?.withoutParameters()?.toString(),
        contentLength = contentLength,
        size = size,
    )
}

private fun abortDestination(destination: SiteFileDestination) {
    runCatching { destination.abort() }
}

private fun abortPartialDestination(context: DownloadContext) {
    if (context.destinationStarted && !context.destinationCommitted) abortDestination(context.request.destination)
}

private fun HttpRequestBuilder.transferTimeout() = timeout {
    requestTimeoutMillis = TRANSFER_TIMEOUT_MILLIS
    socketTimeoutMillis = TRANSFER_IDLE_TIMEOUT_MILLIS
}

internal fun dispositionFileName(value: String): String? {
    val disposition = runCatching { ContentDisposition.parse(value) }.getOrNull() ?: return null
    val extended = disposition.parameter("filename*")
        ?.takeIf { it.startsWith("UTF-8''", ignoreCase = true) }
        ?.let { runCatching { it.substringAfter("''").decodeURLPart() }.getOrNull() }
    return (extended ?: disposition.parameter(ContentDisposition.Parameters.FileName))?.takeIf { it.isNotBlank() }
}
