package org.mpc.domain.portal

import de.infix.testBalloon.framework.core.testSuite
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.mpc.domain.model.AppSettings
import org.mpc.domain.model.AppThemeMode
import org.mpc.domain.model.PortalAuthenticationMode
import org.mpc.domain.model.PortalSession
import org.mpc.domain.repository.AppSettingsRepository
import kotlin.test.assertEquals

private class FakeAppSettingsRepository(
    initialSettings: AppSettings,
) : AppSettingsRepository {
    private val state = MutableStateFlow(initialSettings)

    override val settings = state

    override suspend fun setThemeMode(themeMode: AppThemeMode) {
        state.value = state.value.copy(themeMode = themeMode)
    }

    override suspend fun setPortalAuthenticationMode(authenticationMode: PortalAuthenticationMode) {
        state.value = state.value.copy(portalAuthenticationMode = authenticationMode)
    }
}

private class RecordingAuthenticator(
    private val mode: PortalAuthenticationMode,
    private val restoredSession: PortalSession? = null,
    private val authenticationFailure: Exception? = null,
    private val restoreGate: CompletableDeferred<Unit>? = null,
    private val authenticationGate: CompletableDeferred<Unit>? = null,
) : PortalSessionAuthenticator {
    var restoreCalls = 0
        private set
    var authenticateCalls = 0
        private set
    var signOutCalls = 0
        private set

    override suspend fun restore(): PortalSession? {
        restoreCalls++
        restoreGate?.await()
        return restoredSession
    }

    override suspend fun authenticate(): PortalSession {
        authenticateCalls++
        authenticationGate?.await()
        authenticationFailure?.let { throw it }
        return PortalSession(mode)
    }

    override suspend fun signOut() {
        signOutCalls++
    }
}

val portalSessionManagerTests by testSuite {
    test("restore delegates to the selected authentication mode") {
        runTest {
            val settings = FakeAppSettingsRepository(
                AppSettings(portalAuthenticationMode = PortalAuthenticationMode.SECURE_CREDENTIALS),
            )
            val secure = RecordingAuthenticator(
                mode = PortalAuthenticationMode.SECURE_CREDENTIALS,
                restoredSession = PortalSession(PortalAuthenticationMode.SECURE_CREDENTIALS),
            )
            val manual = RecordingAuthenticator(PortalAuthenticationMode.MANUAL_PORTAL)
            val manager = DefaultPortalSessionManager(settings, secure, manual)

            manager.restore()

            assertEquals(1, secure.restoreCalls)
            assertEquals(0, manual.restoreCalls)
            assertEquals(
                PortalSessionState.Authenticated(
                    PortalSession(PortalAuthenticationMode.SECURE_CREDENTIALS),
                ),
                manager.state.value,
            )
        }
    }

    test("authentication follows a changed setting") {
        runTest {
            val settings = FakeAppSettingsRepository(AppSettings())
            val secure = RecordingAuthenticator(PortalAuthenticationMode.SECURE_CREDENTIALS)
            val manual = RecordingAuthenticator(PortalAuthenticationMode.MANUAL_PORTAL)
            val manager = DefaultPortalSessionManager(settings, secure, manual)

            manager.authenticate()
            settings.setPortalAuthenticationMode(PortalAuthenticationMode.SECURE_CREDENTIALS)
            manager.authenticate()

            assertEquals(1, manual.authenticateCalls)
            assertEquals(1, secure.authenticateCalls)
            assertEquals(
                PortalSessionState.Authenticated(
                    PortalSession(PortalAuthenticationMode.SECURE_CREDENTIALS),
                ),
                manager.state.value,
            )
        }
    }

    test("sign out uses the active session adapter after the setting changes") {
        runTest {
            val settings = FakeAppSettingsRepository(AppSettings())
            val secure = RecordingAuthenticator(PortalAuthenticationMode.SECURE_CREDENTIALS)
            val manual = RecordingAuthenticator(PortalAuthenticationMode.MANUAL_PORTAL)
            val manager = DefaultPortalSessionManager(settings, secure, manual)

            manager.authenticate()
            settings.setPortalAuthenticationMode(PortalAuthenticationMode.SECURE_CREDENTIALS)
            manager.signOut()

            assertEquals(1, manual.signOutCalls)
            assertEquals(0, secure.signOutCalls)
            assertEquals(PortalSessionState.SignedOut, manager.state.value)
        }
    }

    test("adapter failures become failed state") {
        runTest {
            val settings = FakeAppSettingsRepository(AppSettings())
            val failure = IllegalStateException("adapter unavailable")
            val manager = DefaultPortalSessionManager(
                settings,
                RecordingAuthenticator(PortalAuthenticationMode.SECURE_CREDENTIALS),
                RecordingAuthenticator(PortalAuthenticationMode.MANUAL_PORTAL, authenticationFailure = failure),
            )

            manager.authenticate()

            assertEquals(
                PortalSessionState.Failed(PortalAuthenticationMode.MANUAL_PORTAL, failure),
                manager.state.value,
            )
        }
    }

    test("cancelled restore returns to its previous state") {
        runTest {
            val settings = FakeAppSettingsRepository(AppSettings())
            val manager = DefaultPortalSessionManager(
                settings,
                RecordingAuthenticator(PortalAuthenticationMode.SECURE_CREDENTIALS),
                RecordingAuthenticator(
                    PortalAuthenticationMode.MANUAL_PORTAL,
                    restoreGate = CompletableDeferred(),
                ),
            )

            val job = launch { manager.restore() }
            yield()
            assertEquals(PortalSessionState.Restoring, manager.state.value)

            job.cancelAndJoin()

            assertEquals(PortalSessionState.SignedOut, manager.state.value)
        }
    }

    test("cancelled authentication returns to its previous state") {
        runTest {
            val settings = FakeAppSettingsRepository(AppSettings())
            val manager = DefaultPortalSessionManager(
                settings,
                RecordingAuthenticator(PortalAuthenticationMode.SECURE_CREDENTIALS),
                RecordingAuthenticator(
                    PortalAuthenticationMode.MANUAL_PORTAL,
                    authenticationGate = CompletableDeferred(),
                ),
            )

            val job = launch { manager.authenticate() }
            yield()
            assertEquals(
                PortalSessionState.Authenticating(PortalAuthenticationMode.MANUAL_PORTAL),
                manager.state.value,
            )

            job.cancelAndJoin()

            assertEquals(PortalSessionState.SignedOut, manager.state.value)
        }
    }

    test("sign out clears both adapters when session ownership is unknown") {
        runTest {
            val secure = RecordingAuthenticator(PortalAuthenticationMode.SECURE_CREDENTIALS)
            val manual = RecordingAuthenticator(PortalAuthenticationMode.MANUAL_PORTAL)
            val manager = DefaultPortalSessionManager(
                FakeAppSettingsRepository(AppSettings()),
                secure,
                manual,
            )

            manager.signOut()

            assertEquals(1, secure.signOutCalls)
            assertEquals(1, manual.signOutCalls)
            assertEquals(PortalSessionState.SignedOut, manager.state.value)
        }
    }
}
