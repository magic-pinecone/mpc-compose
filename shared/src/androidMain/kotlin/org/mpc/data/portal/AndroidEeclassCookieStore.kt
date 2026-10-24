package org.mpc.data.portal

import android.webkit.CookieManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

internal actual fun createEeclassNativeCookieStore(): EeclassNativeCookieStore = AndroidEeclassCookieStore()

private class AndroidEeclassCookieStore : EeclassNativeCookieStore {
    private val cookieManager = CookieManager.getInstance()

    override suspend fun cookieHeader(url: String): String = withContext(Dispatchers.Main.immediate) {
        cookieManager.getCookie(url).orEmpty()
    }

    override suspend fun storeSetCookies(url: String, values: List<String>) {
        withContext(Dispatchers.Main.immediate) {
            values.forEach { value ->
                suspendCancellableCoroutine { continuation ->
                    cookieManager.setCookie(url, value) {
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                }
            }
            cookieManager.flush()
        }
    }
}
