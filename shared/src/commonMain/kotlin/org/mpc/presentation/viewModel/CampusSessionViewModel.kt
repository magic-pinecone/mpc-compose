package org.mpc.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.mpc.domain.session.CampusSessions
import org.mpc.domain.session.CampusSite
import org.mpc.domain.session.PortalStatus
import org.mpc.domain.session.SitePath
import org.mpc.domain.session.SiteStatus

@Inject
@ViewModelKey
@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<ViewModel>(),
)
class CampusSessionViewModel(
    private val sessions: CampusSessions,
) : ViewModel() {
    val portal: StateFlow<PortalStatus> = sessions.portal

    fun status(site: CampusSite): StateFlow<SiteStatus> = sessions.status(site)

    fun refresh(site: CampusSite) {
        viewModelScope.launch { sessions.refresh(site) }
    }

    /** Hands off again from a live Portal session, without signing anyone out. */
    fun reconnect(site: CampusSite) {
        viewModelScope.launch { sessions.fetch(site, site.probePath) {} }
    }

    fun browserEntry(site: CampusSite, path: SitePath): String = sessions.browserEntry(site, path)

    suspend fun browserPage(url: String, loading: Boolean) {
        sessions.browserPage(url, loading)
    }
}
