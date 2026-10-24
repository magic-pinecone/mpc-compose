package org.mpc.domain.portal

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.http.Url
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

const val EECLASS_BASE_URL = "https://ncueeclass.ncu.edu.tw"
const val EECLASS_DASHBOARD_URL = "$EECLASS_BASE_URL/dashboard"
const val EECLASS_TIMETABLE_PATH = "/dashboard/myTimeTable"
internal const val HTTPS_DEFAULT_PORT = 443

sealed interface EeclassSessionState {
    data object Unknown : EeclassSessionState
    data object Checking : EeclassSessionState
    data object SignedOut : EeclassSessionState
    data object Authenticated : EeclassSessionState
    data class Unavailable(val reason: EeclassUnavailableReason) : EeclassSessionState
}

enum class EeclassUnavailableReason {
    NETWORK,
    UNEXPECTED_RESPONSE,
}

sealed interface EeclassSessionCheck {
    data object Authenticated : EeclassSessionCheck
    data object SignedOut : EeclassSessionCheck
    data class Unavailable(val reason: EeclassUnavailableReason) : EeclassSessionCheck
}

interface EeclassSessionChecker {
    suspend fun check(): EeclassSessionCheck
}

interface EeclassSessionManager {
    val state: StateFlow<EeclassSessionState>
    val pendingTaskPath: StateFlow<String?>

    suspend fun restore()
    suspend fun browserNavigationStarted()
    suspend fun browserNavigationFinished(url: String)
    fun requestTask(path: String)
    fun pendingTaskUrlIfAuthenticated(): String?
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
internal class DefaultEeclassSessionManager(
    private val checker: EeclassSessionChecker,
) : EeclassSessionManager {
    private val mutex = Mutex()
    private val _state = MutableStateFlow<EeclassSessionState>(EeclassSessionState.Unknown)
    private val _pendingTaskPath = MutableStateFlow<String?>(null)

    override val state: StateFlow<EeclassSessionState> = _state
    override val pendingTaskPath: StateFlow<String?> = _pendingTaskPath

    override suspend fun restore() {
        verifySession(completedUrl = null)
    }

    override suspend fun browserNavigationFinished(url: String) {
        verifySession(completedUrl = url)
    }

    private suspend fun verifySession(completedUrl: String?) {
        mutex.withLock {
            val previousState = _state.value
            _state.value = EeclassSessionState.Checking
            try {
                val result = checkSafely()
                _state.value = when (result) {
                    EeclassSessionCheck.Authenticated -> EeclassSessionState.Authenticated
                    EeclassSessionCheck.SignedOut -> EeclassSessionState.SignedOut
                    is EeclassSessionCheck.Unavailable -> EeclassSessionState.Unavailable(result.reason)
                }
                if (result == EeclassSessionCheck.Authenticated && completedUrl != null) {
                    val pendingPath = _pendingTaskPath.value
                    if (pendingPath != null && isEeclassTaskUrl(completedUrl, pendingPath)) {
                        _pendingTaskPath.value = null
                    }
                }
            } catch (cancellation: CancellationException) {
                _state.value = previousState
                throw cancellation
            }
        }
    }

    override suspend fun browserNavigationStarted() {
        mutex.withLock {
            _state.value = EeclassSessionState.Checking
        }
    }

    override fun requestTask(path: String) {
        require(path.startsWith("/dashboard/"))
        require(path.split('/').drop(1).all { segment -> segment.matches(TASK_PATH_SEGMENT) })
        _pendingTaskPath.value = path
    }

    override fun pendingTaskUrlIfAuthenticated(): String? {
        if (_state.value != EeclassSessionState.Authenticated) return null
        return _pendingTaskPath.value?.let { path -> EECLASS_BASE_URL + path }
    }

    private suspend fun checkSafely(): EeclassSessionCheck = try {
        checker.check()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        EeclassSessionCheck.Unavailable(EeclassUnavailableReason.NETWORK)
    }
}

private val TASK_PATH_SEGMENT = Regex("[A-Za-z0-9_-]+")

private fun isEeclassTaskUrl(value: String, path: String): Boolean {
    val url = parseHttpsUrl(value) ?: return false
    return url.host.equals("ncueeclass.ncu.edu.tw", ignoreCase = true) && url.encodedPath == path
}

internal fun isEeclassBrowserFlowUrl(value: String): Boolean {
    val url = parseHttpsUrl(value) ?: return false
    return url.host.equals("ncueeclass.ncu.edu.tw", ignoreCase = true) ||
        url.host.equals("portal.ncu.edu.tw", ignoreCase = true)
}

internal fun isEeclassFlowStartUrl(value: String): Boolean {
    val url = parseHttpsUrl(value) ?: return false
    if (url.host.equals("ncueeclass.ncu.edu.tw", ignoreCase = true)) return true
    return url.host.equals("portal.ncu.edu.tw", ignoreCase = true) &&
        url.encodedPath == "/system/ncueeclass"
}

internal fun isEeclassJavascriptAllowed(value: String): Boolean = isEeclassFlowStartUrl(value)

private fun parseHttpsUrl(value: String): Url? = runCatching { Url(value) }
    .getOrNull()
    ?.takeIf { it.protocol.name == "https" && it.port == HTTPS_DEFAULT_PORT }
