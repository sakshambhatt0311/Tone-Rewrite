package com.bhatt.tonerewriter.domain

/**
 * The four tones offered on screen. [guidance] is injected verbatim into the system
 * instruction, so tuning output quality means editing text here — not the ViewModel.
 */
enum class Tone(val label: String, val guidance: String) {
    FORMAL(
        label = "Formal",
        guidance = "Professional and businesslike. Full sentences, no slang, no contractions, " +
            "no exclamation marks. Respectful but not stiff or bureaucratic."
    ),
    FRIENDLY(
        label = "Friendly",
        guidance = "Warm, casual and human. Contractions are welcome. Sound like a colleague " +
            "the reader likes. Never gushing, never more than one exclamation mark."
    ),
    APOLOGETIC(
        label = "Apologetic",
        guidance = "Own the problem and acknowledge the impact on the reader. Apologise once, " +
            "clearly, then move to what happens next. Do not grovel or repeat the apology."
    ),
    FIRM(
        label = "Firm",
        guidance = "Direct and decisive. State the position in the first sentence. Remove hedging " +
            "words such as 'just', 'maybe' and 'sorry'. Polite but not negotiable."
    );
}

/** Slider position, mapped to how far the model may drift from the original wording. */
enum class Strength(val instruction: String) {
    SUBTLE("Keep the author's own wording wherever possible. Change only what the tone requires."),
    BALANCED("Rewrite naturally while keeping the original structure and every fact intact."),
    STRONG("Rewrite freely and confidently for maximum effect, but change no facts.");

    companion object {
        /** Maps the 0f..1f slider to a bucket. */
        fun from(value: Float): Strength = when {
            value < 0.34f -> SUBTLE
            value < 0.67f -> BALANCED
            else -> STRONG
        }
    }
}

data class RewriteRequest(
    val text: String,
    val tone: Tone,
    val strength: Strength
)
