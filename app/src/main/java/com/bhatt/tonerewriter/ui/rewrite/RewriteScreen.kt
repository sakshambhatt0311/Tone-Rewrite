package com.bhatt.tonerewriter.ui.rewrite

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bhatt.tonerewriter.domain.Strength
import com.bhatt.tonerewriter.domain.Tone
import com.bhatt.tonerewriter.ui.theme.OnResultButton
import com.bhatt.tonerewriter.ui.theme.OnResultTonalContainer
import com.bhatt.tonerewriter.ui.theme.ResultButton
import com.bhatt.tonerewriter.ui.theme.ResultHighlight
import com.bhatt.tonerewriter.ui.theme.ResultOnSurface
import com.bhatt.tonerewriter.ui.theme.ResultSurface
import com.bhatt.tonerewriter.ui.theme.ResultTonalContainer
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Rewrite",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            InputCard(
                value = state.input,
                charCount = state.charCount,
                onValueChange = onInputChange,
                onPaste = { context.readClipboard()?.let(onInputChange) }
            )
            Spacer(Modifier.height(20.dp))

            SectionLabel("Tone")
            Spacer(Modifier.height(10.dp))
            ToneChips(selected = state.tone, onSelect = onToneSelected)
            Spacer(Modifier.height(16.dp))

            StrengthSlider(value = state.strength, onChange = onStrengthChange)
            Spacer(Modifier.height(20.dp))

            RewriteButton(enabled = state.canRewrite, onClick = onRewrite)

            AnimatedVisibility(
                visible = state.result !is ResultState.Idle,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(24.dp))
                    SectionLabel("Result")
                    Spacer(Modifier.height(10.dp))

                    when (val result = state.result) {
                        is ResultState.Idle -> Unit

                        is ResultState.Loading -> LoadingCard(result.tone)

                        is ResultState.Error -> ErrorCard(
                            message = result.message,
                            retryable = result.retryable,
                            onRetry = onRewrite
                        )

                        is ResultState.Success -> {
                            ResultCard(
                                result = result,
                                onCopy = {
                                    context.copyToClipboard(result.rewrite)
                                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                },
                                onShare = { context.share(result.rewrite) },
                                onRegenerate = onRewrite
                            )
                            Spacer(Modifier.height(16.dp))
                            TryAnotherTone(current = result.tone, onSelect = onToneSelected)
                        }
                    }
                }
            }

            if (state.history.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                HistoryList(entries = state.history, onSelect = onHistorySelected)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun InputCard(
    value: String,
    charCount: Int,
    onValueChange: (String) -> Unit,
    onPaste: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Box(Modifier.heightIn(min = 96.dp)) {
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
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))

            val fraction = (charCount.toFloat() / RewriteUiState.MAX_CHARS).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { fraction },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
            )

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$charCount / ${RewriteUiState.MAX_CHARS}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Outlined.ContentPaste,
                    contentDescription = "Paste from clipboard",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onPaste)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ToneChips(selected: Tone, onSelect: (Tone) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Tone.entries.forEach { tone ->
            val isSelected = tone == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(tone) },
                label = { Text(tone.label) },
                leadingIcon = {
                    Icon(
                        imageVector = if (isSelected) Icons.Outlined.Check else tone.icon,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun StrengthSlider(value: Float, onChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "subtle",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = value,
            onValueChange = onChange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )
        Text(
            text = "strong",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RewriteButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(vertical = 18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Rewrite",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LoadingCard(tone: Tone) {
    Surface(color = ResultSurface, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            CircularProgressIndicator(
                color = ResultButton,
                strokeWidth = 3.dp,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = "Rewriting in ${tone.label.lowercase()}…",
                color = ResultOnSurface,
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
    // Diff is pure and input-capped, so recompute only when the pair actually changes.
    val highlighted = remember(result.source, result.rewrite) {
        highlightChanges(result.source, result.rewrite, ResultHighlight)
    }

    Surface(color = ResultSurface, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text(result.tone.label) },
                    leadingIcon = {
                        Icon(
                            imageVector = result.tone.icon,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        disabledContainerColor = ResultTonalContainer,
                        disabledLabelColor = OnResultTonalContainer,
                        disabledLeadingIconContentColor = OnResultTonalContainer
                    )
                )
                Text(
                    text = "· ${result.strength.label.lowercase()}",
                    color = ResultOnSurface.copy(alpha = 0.55f),
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = highlighted,
                color = ResultOnSurface,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onCopy,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ResultButton,
                        contentColor = OnResultButton
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Copy", fontWeight = FontWeight.SemiBold)
                }
                ResultIconAction(Icons.Outlined.Share, "Share rewrite", onShare)
                ResultIconAction(Icons.Outlined.Refresh, "Rewrite again", onRegenerate)
            }
        }
    }
}

@Composable
private fun ResultIconAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = ResultTonalContainer,
            contentColor = OnResultTonalContainer
        ),
        modifier = Modifier.size(52.dp)
    ) {
        Icon(imageVector = icon, contentDescription = description, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ErrorCard(message: String, retryable: Boolean, onRetry: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(24.dp),
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
                    Text("Try again", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TryAnotherTone(current: Tone, onSelect: (Tone) -> Unit) {
    Column {
        Text(
            text = "Try another tone",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Tone.entries.filter { it != current }.forEach { tone ->
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable { onSelect(tone) }
                ) {
                    Text(
                        text = tone.label,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(entry) }
                    .padding(vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = entry.source.take(48).let { if (it.length < entry.source.length) "$it…" else it },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = entry.tone.label,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

private val Tone.icon: ImageVector
    get() = when (this) {
        Tone.FORMAL -> Icons.Outlined.Work
        Tone.FRIENDLY -> Icons.Outlined.SentimentSatisfiedAlt
        Tone.APOLOGETIC -> Icons.Outlined.VolunteerActivism
        Tone.FIRM -> Icons.Outlined.Bolt
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

@Preview(name = "Dark", showBackground = true, backgroundColor = 0xFF141017)
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

@Preview(name = "Light", showBackground = true, backgroundColor = 0xFFFDF6FA)
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
    )
)
