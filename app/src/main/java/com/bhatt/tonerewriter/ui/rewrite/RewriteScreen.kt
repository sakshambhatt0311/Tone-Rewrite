package com.bhatt.tonerewriter.ui.rewrite

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhatt.tonerewriter.domain.Strength
import com.bhatt.tonerewriter.domain.Tone
import com.bhatt.tonerewriter.ui.theme.ToneRewriterTheme

/**
 * Stateful entry point. The only place that knows a ViewModel exists — everything below is
 * a pure function of [RewriteUiState], so it previews and tests without Firebase.
 */
@Composable
fun RewriteRoute(
    modifier: Modifier = Modifier,
    viewModel: RewriteViewModel = viewModel(factory = RewriteViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    RewriteScreen(
        state = state,
        onInputChange = viewModel::onInputChange,
        onToneSelected = viewModel::onToneSelected,
        onStrengthChange = viewModel::onStrengthChange,
        onRewrite = viewModel::rewrite,
        onHistorySelected = viewModel::onHistorySelected,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewriteScreen(
    state: RewriteUiState,
    onInputChange: (String) -> Unit,
    onToneSelected: (Tone) -> Unit,
    onStrengthChange: (Float) -> Unit,
    onRewrite: () -> Unit,
    onHistorySelected: (HistoryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { BrandBar(scrollBehavior = scrollBehavior) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Headline()
            Spacer(Modifier.height(20.dp))

            InputCard(
                value = state.input,
                charCount = state.charCount,
                onValueChange = onInputChange,
                onPaste = { context.readClipboard()?.let(onInputChange) }
            )
            Spacer(Modifier.height(12.dp))

            ToneTiles(selected = state.tone, onSelect = onToneSelected)
            Spacer(Modifier.height(14.dp))

            StrengthToggle(selected = state.strengthBucket, onChange = onStrengthChange)

            RewriteButton(enabled = state.canRewrite, onClick = onRewrite)

            AnimatedVisibility(
                visible = state.result !is ResultState.Idle,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(18.dp))

                    when (val result = state.result) {
                        is ResultState.Idle -> Unit

                        is ResultState.Loading -> LoadingCard(result.tone)

                        is ResultState.Error -> ErrorCard(
                            message = result.message,
                            retryable = result.retryable,
                            onRetry = onRewrite
                        )

                        is ResultState.Success -> ResultCard(
                            result = result,
                            onCopy = {
                                context.copyToClipboard(result.rewrite)
                                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                            },
                            onShare = { context.share(result.rewrite) },
                            onRegenerate = onRewrite
                        )
                    }
                }
            }

            if (state.history.isNotEmpty()) {
                Spacer(Modifier.height(26.dp))
                SectionLabel("Recent")
                Spacer(Modifier.height(4.dp))
                HistoryList(entries = state.history, onSelect = onHistorySelected)
            }
        }
    }
}

/** Small bar: brand mark plus the app name, so the screen's own headline can carry the weight. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandBar(scrollBehavior: TopAppBarScrollBehavior) {
    TopAppBar(
        title = {
            Text(
                text = "Tone Rewriter",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        navigationIcon = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = CircleShape,
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}

@Composable
private fun Headline() {
    Column {
        Text(
            text = buildAnnotatedString {
                append("Say it ")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("better.")
                }
            },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Paste anything. Pick a tone. Ship it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InputCard(
    value: String,
    charCount: Int,
    onValueChange: (String) -> Unit,
    onPaste: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            // Roughly two lines. Enough that an empty field still reads as a text area, low
            // enough that the footer sits right under the text instead of floating below a gap.
            Box(Modifier.heightIn(min = 52.dp)) {
                if (value.isEmpty()) {
                    Text(
                        text = "Paste or type what you want to say…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onPaste,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentPaste,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Text("Paste", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(Modifier.weight(1f))

                // The counter is the only cap warning left, so it turns red as the limit nears.
                val nearLimit = charCount > RewriteUiState.MAX_CHARS * 0.9f
                Text(
                    text = "$charCount / ${RewriteUiState.MAX_CHARS}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (nearLimit) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * Four tones as a 2×2 grid of described tiles. Laid out by hand rather than with a lazy grid
 * because the whole screen is already inside a vertical scroll.
 */
@Composable
private fun ToneTiles(selected: Tone, onSelect: (Tone) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Tone.entries.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { tone ->
                    ToneTile(
                        tone = tone,
                        selected = tone == selected,
                        onClick = { onSelect(tone) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToneTile(
    tone: Tone,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        selected = selected,
        onClick = onClick,
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainer,
        contentColor = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (selected) scheme.primary else scheme.outlineVariant),
        modifier = modifier
    ) {
        // Icon inline with the label rather than stacked above it — the stacked version spent a
        // whole row on a 21dp glyph, which is most of what made these tiles tall.
        Column(Modifier.padding(horizontal = 13.dp, vertical = 11.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(tone.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = tone.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = tone.blurb,
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) {
                    scheme.onPrimaryContainer.copy(alpha = 0.8f)
                } else {
                    scheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * The three [Strength] buckets, shown directly instead of behind a continuous slider. The
 * ViewModel still takes a 0f..1f value, so each option emits the midpoint of its bucket.
 */
@Composable
private fun StrengthToggle(selected: Strength, onChange: (Float) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = CircleShape,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(4.dp)) {
            Strength.entries.forEach { strength ->
                val isSelected = strength == selected
                Surface(
                    selected = isSelected,
                    onClick = { onChange(strength.sliderValue) },
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    contentColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    shape = CircleShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = strength.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RewriteButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(vertical = 17.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
    ) {
        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Rewrite",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/** Shared shell for the three result states, so they can't drift apart visually. */
@Composable
private fun ResultShell(content: @Composable () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
        content = content
    )
}

@Composable
private fun LoadingCard(tone: Tone) {
    ResultShell {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Rewriting in ${tone.label.lowercase()}…",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun ResultCard(
    result: ResultState.Success,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRegenerate: () -> Unit
) {
    val highlight = MaterialTheme.colorScheme.primaryContainer
    // Diff is pure and input-capped, so recompute only when the pair actually changes.
    val highlighted = remember(result.source, result.rewrite, highlight) {
        highlightChanges(result.source, result.rewrite, highlight)
    }

    ResultShell {
        Column(Modifier.padding(17.dp)) {
            Text(
                text = "${result.tone.label} · ${result.strength.label}".uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.8.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = highlighted,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                lineHeight = 25.sp
            )

            Spacer(Modifier.height(14.dp))

            ChangeLegend(changedRuns = highlighted.spanStyles.size, swatch = highlight)

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onCopy,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(vertical = 13.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Copy", fontWeight = FontWeight.Medium)
                }
                ResultIconAction(Icons.Outlined.Share, "Share rewrite", onShare)
                ResultIconAction(Icons.Outlined.Refresh, "Rewrite again", onRegenerate)
            }
        }
    }
}

/** Tells the reader what the highlighted spans mean, and how much actually moved. */
@Composable
private fun ChangeLegend(changedRuns: Int, swatch: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier
                .size(11.dp)
                .background(swatch, RoundedCornerShape(3.dp))
        )
        Text(
            text = when (changedRuns) {
                0 -> "No changes needed"
                1 -> "1 phrase rewritten"
                else -> "$changedRuns phrases rewritten"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResultIconAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.size(48.dp)
    ) {
        Icon(imageVector = icon, contentDescription = description, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ErrorCard(message: String, retryable: Boolean, onRetry: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            if (retryable) {
                TextButton(onClick = onRetry, contentPadding = PaddingValues(0.dp)) {
                    Text(
                        text = "Try again",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryList(entries: List<HistoryEntry>, onSelect: (HistoryEntry) -> Unit) {
    Column {
        entries.forEach { entry ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(entry) }
                    .padding(vertical = 12.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(11.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(entry.tone.icon, contentDescription = null, modifier = Modifier.size(17.dp))
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = entry.source,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = entry.tone.label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private val Tone.icon: ImageVector
    get() = when (this) {
        Tone.FORMAL -> Icons.Outlined.Work
        Tone.FRIENDLY -> Icons.Outlined.SentimentSatisfiedAlt
        Tone.APOLOGETIC -> Icons.Outlined.VolunteerActivism
        Tone.FIRM -> Icons.Outlined.Bolt
    }

/** Midpoint of each bucket, so [Strength.from] round-trips the value the toggle emits. */
private val Strength.sliderValue: Float
    get() = when (this) {
        Strength.SUBTLE -> 0.15f
        Strength.BALANCED -> 0.5f
        Strength.STRONG -> 0.85f
    }

private fun Context.clipboard(): ClipboardManager? =
    getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

private fun Context.copyToClipboard(text: String) {
    clipboard()?.setPrimaryClip(ClipData.newPlainText("Rewrite", text))
}

private fun Context.readClipboard(): String? =
    clipboard()?.primaryClip?.takeIf { it.itemCount > 0 }
        ?.getItemAt(0)?.coerceToText(this)?.toString()
        ?.take(RewriteUiState.MAX_CHARS)

private fun Context.share(text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, "Share rewrite"))
}

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF17130E)
@Composable
private fun RewriteScreenPreviewDark() {
    ToneRewriterTheme {
        RewriteScreen(
            state = previewState,
            onInputChange = {},
            onToneSelected = {},
            onStrengthChange = {},
            onRewrite = {},
            onHistorySelected = {}
        )
    }
}

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFFFF8F3)
@Composable
private fun RewriteScreenPreviewLight() {
    ToneRewriterTheme {
        RewriteScreen(
            state = previewState,
            onInputChange = {},
            onToneSelected = {},
            onStrengthChange = {},
            onRewrite = {},
            onHistorySelected = {}
        )
    }
}

private val previewState = RewriteUiState(
    input = "hey so i cant do the friday deadline, need more time, sorry",
    tone = Tone.FORMAL,
    result = ResultState.Success(
        source = "hey so i cant do the friday deadline, need more time, sorry",
        rewrite = "Hi — I won't be able to meet the Friday deadline and would " +
            "appreciate a short extension. Apologies for the inconvenience.",
        tone = Tone.FORMAL,
        strength = Strength.BALANCED
    ),
    history = listOf(
        HistoryEntry(
            source = "thanks for covering my shift yesterday",
            rewrite = "Thank you for covering my shift yesterday.",
            tone = Tone.FRIENDLY
        )
    )
)
