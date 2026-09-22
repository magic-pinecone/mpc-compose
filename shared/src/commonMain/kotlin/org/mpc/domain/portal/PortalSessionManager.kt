package org.mpc.domain.portal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.mpc.domain.model.PortalAuthenticationMode
import org.mpc.domain.model.PortalSession
import org.mpc.domain.repository.AppSettingsRepository

interface PortalSessionAuthenticator {
    suspend fun restore(): PortalSession?

    suspend fun authenticate(): PortalSession

    suspend fun signOut()
}

class PortalSessionException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

interface PortalSessionManager {
    val state: StateFlow<PortalSessionState>

    suspend fun restore()

    suspend fun authenticate()

    suspend fun signOut()
}

sealed interface PortalSessionState {
    data object SignedOut : PortalSessionState

    data object Restoring : PortalSessionState

    data class RequiresAuthentication(
        val mode: PortalAuthenticationMode,
    ) : PortalSessionState

    data class Authenticating(
        val mode: PortalAuthenticationMode,
    ) : PortalSessionState

    data class Authenticated(
        val session: PortalSession,
    ) : PortalSessionState

    data class Failed(
        val mode: PortalAuthenticationMode,
        val cause: Throwable,
    ) : PortalSessionState
}

/**
 * Owns session lifecycle while leaving credential storage and browser UI to platform adapters.
 *
 * The two adapters are deliberately explicit: secure credentials can use Keychain/Keystore,
 * while the manual adapter can keep the authenticated Calf WebView session in its own cookie
 * store. No shared Ktor/WebView cookie state is assumed here.
 */
// Platform adapters can fail with different exception types; preserve them in Failed state.
@Suppress("TooGenericExceptionCaught")
class DefaultPortalSessionManager(
    private val settingsRepository: AppSettingsRepository,
    private val secureCredentialsAuthenticator: PortalSessionAuthenticator,
    private val manualPortalAuthenticator: PortalSessionAuthenticator,
) : PortalSessionManager {
    private val mutex = Mutex()
    private val _state = MutableStateFlow<PortalSessionState>(PortalSessionState.SignedOut)
    private var activeSessionMode: PortalAuthenticationMode? = null

    override val state: StateFlow<PortalSessionState> = _state

    override suspend fun restore() {
        mutex.withLock {
            val mode = selectedMode()
            _state.value = PortalSessionState.Restoring
            try {
                val session = authenticator(mode).restore()
                activeSessionMode = session?.authenticationMode
                _state.value = session?.let(PortalSessionState::Authenticated)
                    ?: PortalSessionState.RequiresAuthentication(mode)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (cause: Exception) {
                _state.value = PortalSessionState.Failed(mode, cause)
            }
        }
    }

    override suspend fun authenticate() {
        mutex.withLock {
            val mode = selectedMode()
            _state.value = PortalSessionState.Authenticating(mode)
            try {
                val session = authenticator(mode).authenticate()
                activeSessionMode = session.authenticationMode
                _state.value = PortalSessionState.Authenticated(session)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (cause: Exception) {
                _state.value = PortalSessionState.Failed(mode, cause)
            }
        }
    }

    override suspend fun signOut() {
        mutex.withLock {
            val mode = activeSessionMode ?: selectedMode()
            try {
                authenticator(mode).signOut()
                activeSessionMode = null
                _state.value = PortalSessionState.SignedOut
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (cause: Exception) {
                _state.value = PortalSessionState.Failed(mode, cause)
            }
        }
    }

    private suspend fun selectedMode(): PortalAuthenticationMode = settingsRepository.settings.first().portalAuthenticationMode

    private fun authenticator(mode: PortalAuthenticationMode): PortalSessionAuthenticator = when (mode) {
        PortalAuthenticationMode.SECURE_CREDENTIALS -> secureCredentialsAuthenticator
        PortalAuthenticationMode.MANUAL_PORTAL -> manualPortalAuthenticator
    }
}
