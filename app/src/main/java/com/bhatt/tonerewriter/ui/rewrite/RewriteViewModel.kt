package com.bhatt.tonerewriter.ui.rewrite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bhatt.tonerewriter.data.FirebaseAiService
import com.bhatt.tonerewriter.data.RewriteError
import com.bhatt.tonerewriter.data.ToneRewriteService
import com.bhatt.tonerewriter.domain.RewriteRequest
import com.bhatt.tonerewriter.domain.Strength
import com.bhatt.tonerewriter.domain.Tone
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns all screen state and the only coroutine that talks to the model.
 *
 * It knows nothing about Firebase, HTTP or prompts — only [ToneRewriteService] — which is
 * what makes the AI layer swappable and this class testable with a fake service.
 */
class RewriteViewModel(
    private val service: ToneRewriteService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewriteUiState())
    val uiState: StateFlow<RewriteUiState> = _uiState.asStateFlow()

    /** Tracked so a new request cancels the one still in flight instead of racing it. */
    private var rewriteJob: Job? = null

    fun onInputChange(value: String) {
        if (value.length > RewriteUiState.MAX_CHARS) return
        _uiState.update { it.copy(input = value) }
    }

    /** Chip tap: select the tone, and if a result is already on screen, refresh it in place. */
    fun onToneSelected(tone: Tone) {
        _uiState.update { it.copy(tone = tone) }
        if (_uiState.value.result !is ResultState.Idle) rewrite()
    }

    fun onStrengthChange(value: Float) {
        _uiState.update { it.copy(strength = value) }
    }

    fun onClearInput() {
        rewriteJob?.cancel()
        _uiState.update { it.copy(input = "", result = ResultState.Idle) }
    }

    fun onHistorySelected(entry: HistoryEntry) {
        rewriteJob?.cancel()
        _uiState.update {
            it.copy(
                input = entry.source,
                tone = entry.tone,
                result = ResultState.Success(
                    source = entry.source,
                    rewrite = entry.rewrite,
                    tone = entry.tone,
                    strength = it.strengthBucket
                )
            )
        }
    }

    fun rewrite() {
        val state = _uiState.value
        val source = state.input.trim()
        if (source.isEmpty()) return

        rewriteJob?.cancel()
        rewriteJob = viewModelScope.launch {
            _uiState.update { it.copy(result = ResultState.Loading(source, state.tone)) }

            val request = RewriteRequest(
                text = source,
                tone = state.tone,
                strength = state.strengthBucket
            )

            service.rewrite(request).fold(
                onSuccess = { rewrite -> onRewriteSuccess(source, rewrite, request) },
                onFailure = { error -> onRewriteFailure(error) }
            )
        }
    }

    private fun onRewriteSuccess(source: String, rewrite: String, request: RewriteRequest) {
        _uiState.update { state ->
            state.copy(
                result = ResultState.Success(
                    source = source,
                    rewrite = rewrite,
                    tone = request.tone,
                    strength = request.strength
                ),
                history = state.history.prepend(HistoryEntry(source, rewrite, request.tone))
            )
        }
    }

    private fun onRewriteFailure(error: Throwable) {
        val rewriteError = error as? RewriteError
        _uiState.update {
            it.copy(
                result = ResultState.Error(
                    message = rewriteError?.message ?: "Something went wrong. Try again.",
                    retryable = rewriteError?.retryable ?: true
                )
            )
        }
    }

    private fun List<HistoryEntry>.prepend(entry: HistoryEntry): List<HistoryEntry> =
        (listOf(entry) + filterNot { it.source == entry.source && it.tone == entry.tone })
            .take(MAX_HISTORY)

    override fun onCleared() {
        rewriteJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val MAX_HISTORY = 5

        /** Hand-rolled factory: one dependency doesn't justify a DI framework yet. */
        val Factory = viewModelFactory {
            initializer { RewriteViewModel(service = FirebaseAiService()) }
        }
    }
}

/** Used by the UI to label the slider without duplicating the bucket logic. */
val Strength.label: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }
