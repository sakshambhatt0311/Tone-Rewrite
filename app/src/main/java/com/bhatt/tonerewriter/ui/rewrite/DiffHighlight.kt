package com.bhatt.tonerewriter.ui.rewrite

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * Highlights the words the model actually changed, so the rewrite is scannable at a glance
 * instead of forcing a re-read.
 *
 * Word-level longest-common-subsequence: tokens of [rewrite] that survive the LCS with
 * [source] are untouched, everything else is new and gets a background. Input is capped at
 * 800 characters, so the O(n*m) table is a few thousand cells — cheap enough for recomposition.
 */
fun highlightChanges(
    source: String,
    rewrite: String,
    highlight: Color
): AnnotatedString {
    val sourceWords = source.tokenize()
    val rewriteWords = rewrite.tokenize()
    if (sourceWords.isEmpty()) return AnnotatedString(rewrite)

    val kept = longestCommonSubsequenceMask(
        sourceWords.map { it.normalized },
        rewriteWords.map { it.normalized }
    )

    return buildAnnotatedString {
        val changedStyle = SpanStyle(background = highlight)
        var index = 0
        while (index < rewriteWords.size) {
            if (kept[index]) {
                append(rewriteWords[index].raw)
                index++
            } else {
                // Merge the run of changed words so the background is one continuous block.
                val start = index
                while (index < rewriteWords.size && !kept[index]) index++
                val run = rewriteWords.subList(start, index).joinToString("") { it.raw }
                withStyle(changedStyle) { append(run.trimEnd()) }
                append(run.takeLast(run.length - run.trimEnd().length))
            }
        }
    }
}

private data class Token(val raw: String, val normalized: String)

/** Keeps trailing whitespace on each token so re-joining reproduces the original text. */
private fun String.tokenize(): List<Token> =
    Regex("\\S+\\s*").findAll(this).map { match ->
        Token(
            raw = match.value,
            normalized = match.value.trim().lowercase().trim('.', ',', '!', '?', ';', ':', '"', '\'')
        )
    }.toList()

/** Returns, for every token in [b], whether it is part of the common subsequence with [a]. */
private fun longestCommonSubsequenceMask(a: List<String>, b: List<String>): BooleanArray {
    val table = Array(a.size + 1) { IntArray(b.size + 1) }
    for (i in a.indices.reversed()) {
        for (j in b.indices.reversed()) {
            table[i][j] = if (a[i] == b[j]) {
                table[i + 1][j + 1] + 1
            } else {
                maxOf(table[i + 1][j], table[i][j + 1])
            }
        }
    }

    val mask = BooleanArray(b.size)
    var i = 0
    var j = 0
    while (i < a.size && j < b.size) {
        when {
            a[i] == b[j] -> {
                mask[j] = true
                i++
                j++
            }

            table[i + 1][j] >= table[i][j + 1] -> i++
            else -> j++
        }
    }
    return mask
}
