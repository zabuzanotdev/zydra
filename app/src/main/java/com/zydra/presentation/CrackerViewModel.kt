package com.zydra.presentation

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zydra.R
import com.zydra.data.di.AppContainer
import com.zydra.domain.model.CrackFailure
import com.zydra.domain.model.CrackTarget
import com.zydra.domain.model.CrackingProgress
import com.zydra.domain.repository.WordlistRepository
import com.zydra.presentation.model.BruteForceOptions
import com.zydra.presentation.model.CrackerUiState
import com.zydra.presentation.model.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CrackerViewModel(
    application: Application,
    private val container: AppContainer
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<CrackerUiState>(CrackerUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private var crackingJob: Job? = null

    fun startDictionaryCracking(targetUri: Uri, targetName: String, wordlistUri: Uri) {
        crackingJob?.cancel()
        crackingJob = viewModelScope.launch {
            _uiState.value = CrackerUiState.Analyzing
            val target = container.detectTarget.invoke(targetName)
            if (target is CrackTarget.Unsupported) {
                _uiState.value = CrackerUiState.Error(
                    UiText.Resource(R.string.error_unsupported_format, listOf(targetName))
                )
                return@launch
            }

            val targetFile = try {
                container.fileResolver.resolve(targetName) {
                    openStream(targetUri)
                }
            } catch (_: Exception) {
                _uiState.value = CrackerUiState.Error(UiText.Resource(R.string.error_file_unreadable))
                return@launch
            }

            val passwords = try {
                container.wordlistRepository.readPasswords { openStream(wordlistUri) }
            } catch (error: WordlistRepository.WordlistException) {
                _uiState.value = CrackerUiState.Error(mapWordlistError(error))
                return@launch
            }

            container.crackWithDictionary.invoke(target, targetFile, passwords).collect { progress ->
                _uiState.value = mapProgress(progress, target)
            }
        }
    }

    fun startBruteForceCracking(targetUri: Uri, targetName: String, options: BruteForceOptions) {
        crackingJob?.cancel()
        crackingJob = viewModelScope.launch {
            _uiState.value = CrackerUiState.Analyzing
            val target = container.detectTarget.invoke(targetName)
            if (target is CrackTarget.Unsupported) {
                _uiState.value = CrackerUiState.Error(
                    UiText.Resource(R.string.error_unsupported_format, listOf(targetName))
                )
                return@launch
            }

            val targetFile = try {
                container.fileResolver.resolve(targetName) {
                    openStream(targetUri)
                }
            } catch (_: Exception) {
                _uiState.value = CrackerUiState.Error(UiText.Resource(R.string.error_file_unreadable))
                return@launch
            }

            container.crackWithBruteForce.invoke(
                target = target,
                targetFile = targetFile,
                charsetKeys = options.charsetKeys,
                minLength = options.minLength,
                maxLength = options.maxLength
            ).collect { progress ->
                _uiState.value = mapProgress(progress, target)
            }
        }
    }

    fun stopCracking() {
        crackingJob?.cancel()
        crackingJob = null
        _uiState.value = CrackerUiState.Idle
    }

    fun isCracking(): Boolean {
        val state = _uiState.value
        return state is CrackerUiState.Analyzing || state is CrackerUiState.Cracking
    }

    private fun openStream(uri: Uri) = try {
        getApplication<Application>().contentResolver.openInputStream(uri)
    } catch (_: Exception) {
        null
    }

    private fun mapProgress(progress: CrackingProgress, target: CrackTarget): CrackerUiState {
        return when (progress) {
            is CrackingProgress.Trying -> CrackerUiState.Cracking(
                target = target,
                triedCount = progress.triedCount,
                totalCount = progress.totalCount,
                progress = progress.fraction,
                speedPerSecond = progress.speedPerSecond,
                etaSeconds = progress.etaSeconds
            )
            is CrackingProgress.Found -> CrackerUiState.Success(
                password = progress.password,
                matchedDetail = progress.matchedDetail
            )
            is CrackingProgress.Failed -> CrackerUiState.Error(mapFailure(progress.failure))
        }
    }

    private fun mapFailure(failure: CrackFailure): UiText {
        return when (failure) {
            CrackFailure.PasswordNotFound -> UiText.Resource(R.string.error_no_match)
            is CrackFailure.UnsupportedFormat ->
                UiText.Resource(R.string.error_unsupported_format, listOf(failure.displayName))
            CrackFailure.Rar5NotSupported -> UiText.Resource(R.string.error_rar5_unsupported)
            CrackFailure.EmptyWordlist -> UiText.Resource(R.string.error_empty_wordlist)
            is CrackFailure.WordlistTooLarge ->
                UiText.Resource(
                    R.string.error_wordlist_too_large,
                    listOf(NumberFormat.getInstance(Locale.US).format(failure.maxAllowed))
                )
            CrackFailure.FileUnreadable -> UiText.Resource(R.string.error_file_unreadable)
            CrackFailure.NoCharsetSelected -> UiText.Resource(R.string.error_no_charset)
            CrackFailure.InvalidLengthRange -> UiText.Resource(R.string.error_bad_range)
            is CrackFailure.TooManyCombinations ->
                UiText.Resource(
                    R.string.error_too_many_combinations,
                    listOf(NumberFormat.getInstance(Locale.US).format(failure.total))
                )
            CrackFailure.NoCandidate -> UiText.Resource(R.string.error_no_candidate)
        }
    }

    private fun mapWordlistError(error: WordlistRepository.WordlistException): UiText {
        return when (error) {
            is WordlistRepository.WordlistException.Empty ->
                UiText.Resource(R.string.error_empty_wordlist)
            is WordlistRepository.WordlistException.TooLarge ->
                UiText.Resource(
                    R.string.error_wordlist_too_large,
                    listOf(NumberFormat.getInstance(Locale.US).format(error.maxAllowed))
                )
            is WordlistRepository.WordlistException.Unreadable ->
                UiText.Resource(R.string.error_file_unreadable)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val application: Application,
        private val container: AppContainer
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CrackerViewModel(application, container) as T
        }
    }
}
