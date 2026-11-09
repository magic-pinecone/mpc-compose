package org.mpc.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.mpc.domain.model.EeclassBulletinNumber
import org.mpc.domain.model.EeclassCachedFile
import org.mpc.domain.model.EeclassCourseNumber
import org.mpc.domain.repository.EeclassCourseRepository
import org.mpc.domain.session.SitePath
import org.mpc.presentation.model.EeclassAttachmentRow
import org.mpc.presentation.model.EeclassBulletinPage
import org.mpc.presentation.model.EeclassCoursePage
import org.mpc.presentation.model.EeclassCourses
import org.mpc.presentation.model.EeclassFileStatus
import org.mpc.presentation.model.EeclassFileTransfer
import org.mpc.presentation.model.EeclassHomeworkPage
import org.mpc.presentation.model.EeclassMaterialPage
import org.mpc.presentation.model.eeclassFileLinkRow
import org.mpc.presentation.model.toEeclassBulletinPage
import org.mpc.presentation.model.toEeclassCoursePage
import org.mpc.presentation.model.toEeclassCourses
import org.mpc.presentation.model.toEeclassHomeworkPage
import org.mpc.presentation.model.toEeclassMaterialPage
import org.mpc.presentation.state.EeclassFileDownloads

/**
 * Each navigation entry gets its own instance and uses one of the loaders. A state stays null until its
 * first load finishes, and a later load keeps showing the previous result until the new one arrives.
 */
@Inject
@ViewModelKey
@ContributesIntoMap(
    scope = AppScope::class,
    binding = binding<ViewModel>(),
)
class EeclassViewModel(
    private val repository: EeclassCourseRepository,
) : ViewModel() {
    private val _courses = MutableStateFlow<EeclassCourses?>(null)
    val courses: StateFlow<EeclassCourses?> = _courses

    private val _coursePage = MutableStateFlow<EeclassCoursePage?>(null)
    val coursePage: StateFlow<EeclassCoursePage?> = _coursePage

    private val _bulletin = MutableStateFlow<EeclassBulletinPage?>(null)
    val bulletin: StateFlow<EeclassBulletinPage?> = _bulletin

    private val _material = MutableStateFlow<EeclassMaterialPage?>(null)
    val material: StateFlow<EeclassMaterialPage?> = _material

    private val _homework = MutableStateFlow<EeclassHomeworkPage?>(null)
    val homework: StateFlow<EeclassHomeworkPage?> = _homework

    private val files = EeclassFileDownloads(repository)
    private val _openedFiles = Channel<EeclassCachedFile>(Channel.BUFFERED)
    private val _linkFailures = Channel<EeclassFileStatus>(Channel.BUFFERED)

    /** Each file a tap finished downloading, once, for the screen to open. */
    val openedFiles: Flow<EeclassCachedFile> = _openedFiles.receiveAsFlow()

    /** Why a tapped link in a page body could not open its file, since no row shows it. */
    val linkFailures: Flow<EeclassFileStatus> = _linkFailures.receiveAsFlow()

    suspend fun loadCourses() {
        _courses.value = repository.fetchEnrolledCourses().toEeclassCourses()
    }

    suspend fun loadCoursePage(course: String) {
        _coursePage.value = repository.fetchCourseContent(EeclassCourseNumber(course)).toEeclassCoursePage()
    }

    suspend fun loadBulletin(course: String, bulletin: String) {
        _bulletin.value = repository
            .fetchBulletin(EeclassCourseNumber(course), EeclassBulletinNumber(bulletin))
            .toEeclassBulletinPage()
    }

    suspend fun loadMaterial(path: String) {
        _material.value = repository.fetchMaterial(SitePath.of(path)).toEeclassMaterialPage()
    }

    suspend fun loadHomework(path: String) {
        _homework.value = repository.fetchHomework(SitePath.of(path)).toEeclassHomeworkPage()
    }

    fun fileTransfer(row: EeclassAttachmentRow): Flow<EeclassFileTransfer?> = files.transfer(row)

    fun openFile(row: EeclassAttachmentRow) {
        viewModelScope.launch {
            files.fetch(row)?.let { _openedFiles.send(it) }
        }
    }

    /** Opens the file a body [url] points to. Returns false, doing nothing, when [url] is not an ee-class file link. */
    fun openFileLink(url: String): Boolean {
        val row = eeclassFileLinkRow(url) ?: return false
        viewModelScope.launch {
            val file = files.fetch(row)
            if (file != null) {
                _openedFiles.send(file)
            } else {
                files.current(row)?.let { _linkFailures.send(it.status) }
            }
        }
        return true
    }
}
