package com.soprano.chat.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprano.chat.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

// ── Data ──────────────────────────────────────────────────────────
private data class WhisperMessage(
    val id: Int,
    val isMine: Boolean,
    val durationSec: Int,
    val timestamp: String,
    val isVideo: Boolean,
    val isResonated: Boolean // "read receipt" — listened/watched
)

private val demoMessages = listOf(
    WhisperMessage(1, false, 12, "21:04", false, true),
    WhisperMessage(2, true,  8,  "21:05", false, true),
    WhisperMessage(3, false, 22, "21:07", true,  true),
    WhisperMessage(4, true,  5,  "21:08", false, false),
    WhisperMessage(5, false, 15, "21:12", false, true),
    WhisperMessage(6, true,  30, "21:14", true,  true),
    WhisperMessage(7, false, 9,  "21:18", false, false),
    WhisperMessage(8, true,  18, "21:20", false, true),
)

// ── Main WhisperScreen ───────────────────────────────────────────
@Composable
fun WhisperScreen(
    contactName: String = "Elif",
    onBack: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "whisperAmbient")
    val bgDrift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bgDrift"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val orb = Brush.radialGradient(
                    colors = listOf(SlateGray.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * (0.3f + bgDrift * 0.4f), size.height * 0.3f),
                    radius = size.maxDimension * 0.5f
                )
                onDrawBehind {
                    drawRect(color = Anthracite)
                    drawRect(brush = orb)
                }
            }
            .statusBarsPadding()
    ) {
        // ── Top Bar ──
        WhisperTopBar(contactName = contactName, onBack = onBack)

        // ── Messages ──
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            reverseLayout = true
        ) {
            items(demoMessages.size) { index ->
                val msg = demoMessages[demoMessages.lastIndex - index]
                VoiceCard(msg)
            }
        }

        // ── Input Bar ──
        WhisperInputBar()
    }
}

// ── Top Bar ──────────────────────────────────────────────────────
@Composable
private fun WhisperTopBar(contactName: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Geri", tint = Ivory, modifier = Modifier.size(20.dp))
        }

        // Contact avatar
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(DeepSlate, SlateGray))),
            contentAlignment = Alignment.Center
        ) {
            Text(contactName.first().toString(), color = MatteBronze, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(contactName, style = MaterialTheme.typography.titleSmall, color = Ivory, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MatteBronze.copy(alpha = 0.7f)))
                Spacer(Modifier.width(5.dp))
                Text("Rezonans Aktif", style = MaterialTheme.typography.labelSmall, color = MutedGray, fontSize = 10.sp)
            }
        }
    }
}

// ── Voice Card (Pill-shaped message) ─────────────────────────────
@Composable
private fun VoiceCard(msg: WhisperMessage) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_${msg.id}")

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000 + msg.id * 200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase_${msg.id}"
    )

    // Resonated glow animation
    val resonatedAlpha by infiniteTransition.animateFloat(
        initialValue = if (msg.isResonated) 0.3f else 0f,
        targetValue = if (msg.isResonated) 0.6f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "resonated_${msg.id}"
    )

    val pillShape = RoundedCornerShape(24.dp)
    val alignment = if (msg.isMine) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .clip(pillShape)
                .then(
                    if (msg.isResonated) {
                        Modifier.border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    MatteBronze.copy(alpha = resonatedAlpha),
                                    RoseGold.copy(alpha = resonatedAlpha * 0.7f),
                                    MatteBronze.copy(alpha = resonatedAlpha)
                                )
                            ),
                            shape = pillShape
                        )
                    } else {
                        Modifier.border(1.dp, FrostedBorder, pillShape)
                    }
                )
                .background(
                    if (msg.isMine) {
                        Brush.horizontalGradient(
                            listOf(SlateGray.copy(alpha = 0.5f), FrostedGlass)
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(FrostedGlass, DeepSlate.copy(alpha = 0.6f))
                        )
                    }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Video badge
            if (msg.isVideo) {
                Icon(
                    Icons.Outlined.Videocam,
                    contentDescription = null,
                    tint = if (msg.isResonated) MatteBronze else MutedGray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
            }

            // Waveform
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .drawWithCache {
                        onDrawBehind {
                            val barCount = 28
                            val barWidth = size.width / (barCount * 2.2f)
                            val maxH = size.height * 0.85f
                            val spacing = size.width / barCount

                            for (i in 0 until barCount) {
                                val x = spacing * i + spacing * 0.25f
                                val phaseOff = i * 0.4f
                                val amplitude = sin(wavePhase + phaseOff).let { (it + 1f) / 2f }
                                val barH = maxH * (0.15f + amplitude * 0.85f)

                                val barColor = if (msg.isResonated) {
                                    MatteBronze.copy(alpha = 0.4f + amplitude * 0.4f)
                                } else {
                                    SoftGray.copy(alpha = 0.25f + amplitude * 0.3f)
                                }

                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(x, (size.height - barH) / 2f),
                                    size = Size(barWidth, barH),
                                    cornerRadius = CornerRadius(barWidth / 2f)
                                )
                            }
                        }
                    }
            )

            Spacer(Modifier.width(10.dp))

            // Duration + time
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${msg.durationSec}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (msg.isResonated) MatteBronze else SoftGray,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
                Text(
                    text = msg.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedGray,
                    fontSize = 9.sp
                )
            }
        }
    }
}

// ── Input Bar ────────────────────────────────────────────────────
@Composable
private fun WhisperInputBar() {
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val micRing by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micRing"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Keyboard icon
        IconButton(
            onClick = { },
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(FrostedGlass)
                .drawBehind {
                    drawCircle(color = FrostedBorder, style = Stroke(1.dp.toPx()))
                }
        ) {
            Icon(Icons.Outlined.Keyboard, contentDescription = "Klavye", tint = SoftGray, modifier = Modifier.size(20.dp))
        }

        // Central mic button
        Box(contentAlignment = Alignment.Center) {
            // Outer pulse ring
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .graphicsLayer {
                        scaleX = micRing
                        scaleY = micRing
                        alpha = 2f - micRing // fades as it expands
                    }
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(MatteBronze.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                    }
            )

            // Main mic button
            FloatingActionButton(
                onClick = { },
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                containerColor = MatteBronze,
                contentColor = Ivory,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 12.dp,
                    pressedElevation = 4.dp
                )
            ) {
                Icon(Icons.Outlined.Mic, contentDescription = "Kayıt", modifier = Modifier.size(28.dp))
            }
        }

        // Camera icon
        IconButton(
            onClick = { },
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(FrostedGlass)
                .drawBehind {
                    drawCircle(color = FrostedBorder, style = Stroke(1.dp.toPx()))
                }
        ) {
            Icon(Icons.Outlined.Videocam, contentDescription = "Video", tint = SoftGray, modifier = Modifier.size(20.dp))
        }
    }
}
