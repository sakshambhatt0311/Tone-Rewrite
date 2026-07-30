package com.bhatt.tonerewriter.ui.rewrite

import com.bhatt.tonerewriter.domain.Strength
import com.bhatt.tonerewriter.domain.Tone

/** Everything the screen renders. One immutable object, one source of truth. */
data class RewriteUiState(
    val input: String = "",
    val tone: Tone = Tone.FORMAL,
    val strength: Float = 0.6f,
    val result: ResultState = ResultState.Idle,
    val history: List<HistoryEntry> = emptyList()
) {
    val charCount: Int get() = input.length
    val strengthBucket: Strength get() = Strength.from(strength)
    val isLoading: Boolean get() = result is ResultState.Loading
    val canRewrite: Boolean get() = input.isNotBlank() && !isLoading

    companion object {
        const val MAX_CHARS = 800
    }
}

sealed interface ResultState {

    data object Idle : ResultState

    /** [source] is shown greyed above the spinner so the screen doesn't jump on completion. */
    data class Loading(val source: String, val tone: Tone) : ResultState

    data class Success(
        val source: String,
        val rewrite: String,
        val tone: Tone,
        val strength: Strength
    ) : ResultState

    data class Error(val message: String, val retryable: Boolean) : ResultState
}

data class HistoryEntry(
    val source: String,
    val rewrite: String,
    val tone: Tone
)
