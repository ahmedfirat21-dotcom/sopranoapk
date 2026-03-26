package com.soprano.chat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprano.chat.ui.theme.*
import kotlinx.coroutines.delay

// ── Search Result Data ───────────────────────────────────────────
private data class SearchResult(
    val name: String,
    val initial: Char,
    val type: String, // "Frekans" or "Aura"
    val subtitle: String,
    val color: Color
)

private val allResults = listOf(
    SearchResult("Derin Sohbetler", 'D', "Frekans", "78 kişi çevrimiçi", Color(0xFF6A5A8E)),
    SearchResult("Elif", 'E', "Aura", "Çok Yakın Rezonans", Color(0xFF6A5A8E)),
    SearchResult("Gece Yarısı İtirafları", 'G', "Frekans", "34 kişi çevrimiçi", Color(0xFF8B3A5E)),
    SearchResult("Kerem", 'K', "Aura", "Uyumlu Bağlantı", Color(0xFF4A6FA5)),
    SearchResult("Chill", 'C', "Frekans", "42 kişi çevrimiçi", Color(0xFF4A6FA5)),
    SearchResult("Zeynep", 'Z', "Aura", "Tanıdık Frekans", Color(0xFF8B3A5E)),
    SearchResult("Girişimcilik", 'G', "Frekans", "156 kişi çevrimiçi", Color(0xFFA07850)),
    SearchResult("Selin", 'S', "Aura", "Uzak Yankı", Color(0xFFA07850)),
)

// ── Search Overlay Screen ────────────────────────────────────────
@Composable
fun SearchOverlayScreen(
    onDismiss: () -> Unit = {},
    onResultClick: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Auto-focus on entry
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }

    // Filter results
    val filteredResults = remember(query) {
        if (query.isBlank()) emptyList()
        else allResults.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.type.contains(query, ignoreCase = true)
        }
    }

    // Background animation
    val infiniteTransition = rememberInfiniteTransition(label = "searchBg")
    val bgDrift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bgDrift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val orb = Brush.radialGradient(
                    colors = listOf(SlateGray.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(size.width * (0.4f + bgDrift * 0.2f), size.height * 0.15f),
                    radius = size.maxDimension * 0.4f
                )
                onDrawBehind {
                    drawRect(color = Anthracite.copy(alpha = 0.97f))
                    drawRect(brush = orb)
                }
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* consume clicks */ }
        ) {
            Spacer(Modifier.height(12.dp))

            // ── Search Bar ──
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onDismiss = onDismiss,
                focusRequester = focusRequester
            )

            Spacer(Modifier.height(8.dp))

            // ── Results ──
            if (query.isNotBlank() && filteredResults.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.SearchOff,
                            contentDescription = null,
                            tint = MutedGray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Sonuç bulunamadı",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Show all results when query matches, or popular when empty
                    val display = if (query.isBlank()) allResults.take(4) else filteredResults
                    
                    if (query.isBlank() && display.isNotEmpty()) {
                        item {
                            Text(
                                text = "Popüler",
                                style = MaterialTheme.typography.labelMedium,
                                color = MatteBronze,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                            )
                        }
                    }

                    items(display.size) { index ->
                        SearchResultPill(
                            result = display[index],
                            onClick = onResultClick
                        )
                    }
                }
            }
        }
    }
}

// ── Search Bar ───────────────────────────────────────────────────
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    focusRequester: FocusRequester
) {
    val pillShape = RoundedCornerShape(26.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(pillShape)
            .background(FrostedGlass)
            .drawBehind {
                drawRoundRect(
                    color = FrostedBorder,
                    cornerRadius = CornerRadius(26.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Search,
            contentDescription = null,
            tint = MutedGray,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(12.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "Bir Frekans veya Aura Ara...",
                    color = MutedGray,
                    fontSize = 14.sp
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    color = Ivory,
                    fontSize = 14.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(MatteBronze)
            )
        }

        if (query.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Temizle",
                    tint = MutedGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = "İptal",
            style = MaterialTheme.typography.labelMedium,
            color = MatteBronze,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() }
        )
    }
}

// ── Search Result Pill Card ──────────────────────────────────────
@Composable
private fun SearchResultPill(
    result: SearchResult,
    onClick: () -> Unit
) {
    val pillShape = RoundedCornerShape(22.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(pillShape)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(FrostedGlass, SlateGray.copy(alpha = 0.4f))
                )
            )
            .drawBehind {
                drawRoundRect(
                    color = FrostedBorder,
                    cornerRadius = CornerRadius(22.dp.toPx()),
                    style = Stroke(0.5f.dp.toPx())
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar / Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(result.color.copy(alpha = 0.35f), DeepSlate)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (result.type == "Frekans") {
                Icon(
                    Icons.Outlined.GraphicEq,
                    contentDescription = null,
                    tint = MatteBronze,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = result.initial.toString(),
                    color = Ivory.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.name,
                style = MaterialTheme.typography.titleSmall,
                color = Ivory,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = result.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MutedGray,
                fontSize = 11.sp
            )
        }

        // Type badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (result.type == "Frekans") MatteBronze.copy(alpha = 0.12f)
                    else SlateGray.copy(alpha = 0.5f)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = result.type,
                style = MaterialTheme.typography.labelSmall,
                color = if (result.type == "Frekans") MatteBronze else SoftGray,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            )
        }
    }
}
