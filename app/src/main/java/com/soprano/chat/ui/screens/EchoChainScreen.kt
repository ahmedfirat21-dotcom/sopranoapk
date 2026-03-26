package com.soprano.chat.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprano.chat.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

// ── Data ──────────────────────────────────────────────────────────
private data class EchoNode(
    val id: Int,
    val userName: String,
    val initial: Char,
    val durationSec: Int,
    val isVideo: Boolean,
    val resonanceScore: Float, // 0..1 — how much it was listened to
    val color: Color,
    val depth: Int // 0 = root, 1+ = replies
)

private val demoEchoChain = listOf(
    EchoNode(0, "Kerem",  'K', 30, false, 0.95f, Color(0xFF4A6FA5), 0),
    EchoNode(1, "Elif",   'E', 18, false, 0.80f, Color(0xFF6A5A8E), 1),
    EchoNode(2, "Zeynep", 'Z', 12, true,  0.60f, Color(0xFF8B3A5E), 1),
    EchoNode(3, "Burak",  'B', 8,  false, 0.40f, Color(0xFF4A8B6A), 2),
    EchoNode(4, "Selin",  'S', 22, false, 0.75f, Color(0xFFA07850), 1),
    EchoNode(5, "Deniz",  'D', 10, true,  0.30f, Color(0xFF5A7A8E), 2),
    EchoNode(6, "Cem",    'C', 6,  false, 0.15f, Color(0xFF7A6A50), 3),
    EchoNode(7, "Ayşe",   'A', 14, false, 0.55f, Color(0xFF6A8A5A), 2),
)

// ── Main Screen ──────────────────────────────────────────────────
@Composable
fun EchoChainScreen(
    onBack: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "echoAmbient")
    val bgDrift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bgDrift"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val orb = Brush.radialGradient(
                    colors = listOf(MatteBronze.copy(alpha = 0.04f), Color.Transparent),
                    center = Offset(size.width * (0.6f - bgDrift * 0.3f), size.height * 0.2f),
                    radius = size.maxDimension * 0.45f
                )
                onDrawBehind {
                    drawRect(color = Anthracite)
                    drawRect(brush = orb)
                }
            }
            .statusBarsPadding()
    ) {
        // ── Top Bar ──
        EchoTopBar(onBack = onBack)

        // ── Chain ──
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = 8.dp, bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(demoEchoChain.size) { index ->
                val node = demoEchoChain[index]
                val isLast = index == demoEchoChain.lastIndex
                val nextDepth = if (!isLast) demoEchoChain[index + 1].depth else -1

                EchoNodeItem(
                    node = node,
                    showBranch = !isLast,
                    nextDepth = nextDepth
                )
            }
        }

        // ── Reply Bar ──
        EchoReplyBar()
    }
}

// ── Top Bar ──────────────────────────────────────────────────────
@Composable
private fun EchoTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Geri", tint = Ivory, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = "Yankı Zinciri",
                style = MaterialTheme.typography.titleMedium,
                color = Ivory,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Derin Sohbetler · 8 yankı",
                style = MaterialTheme.typography.labelSmall,
                color = MutedGray,
                fontSize = 11.sp
            )
        }
    }
}

// ── Echo Node Item ───────────────────────────────────────────────
@Composable
private fun EchoNodeItem(
    node: EchoNode,
    showBranch: Boolean,
    nextDepth: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "echo_${node.id}")

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3500 + node.id * 150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_${node.id}"
    )

    val indentDp = (node.depth * 36).dp
    val avatarSize: Dp = if (node.depth == 0) 56.dp else (44 - node.depth * 4).coerceAtLeast(32).dp
    val ringThickness = (1f + node.resonanceScore * 3f).dp
    val ringColor = if (node.resonanceScore > 0.6f) MatteBronze else if (node.resonanceScore > 0.3f) SoftGray else MutedGray
    val ringAlpha = 0.3f + node.resonanceScore * 0.5f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = indentDp),
            verticalAlignment = Alignment.Top
        ) {
            // Branch line from parent (drawn as connector line before the avatar)
            if (node.depth > 0) {
                // Vertical + curve connector
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(avatarSize + 16.dp)
                        .drawBehind {
                            val lineColor = if (node.resonanceScore > 0.5f)
                                MatteBronze.copy(alpha = 0.3f)
                            else
                                MutedGray.copy(alpha = 0.2f)

                            val lineWidth = (0.5f + node.resonanceScore * 1.5f)

                            // Curved connector
                            val path = Path().apply {
                                moveTo(0f, 0f)
                                quadraticBezierTo(
                                    0f, size.height * 0.6f,
                                    size.width, size.height * 0.5f
                                )
                            }
                            drawPath(
                                path = path,
                                color = lineColor,
                                style = Stroke(width = lineWidth.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                )
            }

            // Avatar with resonance ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                // Resonance ring (outer)
                Box(
                    modifier = Modifier
                        .size(avatarSize + ringThickness * 2 + 4.dp)
                        .drawBehind {
                            drawCircle(
                                color = ringColor.copy(alpha = ringAlpha),
                                style = Stroke(width = ringThickness.toPx())
                            )
                        }
                )

                // Avatar
                Box(
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(node.color.copy(alpha = 0.35f), DeepSlate)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = node.initial.toString(),
                        color = Ivory.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (node.depth == 0) 22.sp else (16 - node.depth).coerceAtLeast(12).sp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Content card
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp)
            ) {
                // Name + type badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = node.userName,
                        style = MaterialTheme.typography.labelLarge,
                        color = Ivory,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (node.depth == 0) 15.sp else 13.sp
                    )
                    Spacer(Modifier.width(6.dp))

                    if (node.isVideo) {
                        Icon(
                            Icons.Outlined.Videocam,
                            contentDescription = null,
                            tint = MatteBronze.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                    }

                    Text(
                        text = "${node.durationSec}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedGray,
                        fontSize = 10.sp
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Waveform card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (node.depth == 0) 44.dp else 34.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FrostedGlass)
                        .drawBehind {
                            drawRoundRect(
                                color = if (node.resonanceScore > 0.6f)
                                    MatteBronze.copy(alpha = 0.15f)
                                else
                                    FrostedBorder,
                                cornerRadius = CornerRadius(14.dp.toPx()),
                                style = Stroke(0.5f.dp.toPx())
                            )
                        }
                        .drawWithCache {
                            onDrawBehind {
                                val barCount = if (node.depth == 0) 32 else 20
                                val barWidth = size.width / (barCount * 2.5f)
                                val maxH = size.height * 0.7f
                                val spacing = size.width / (barCount + 1)

                                for (i in 0 until barCount) {
                                    val x = spacing * (i + 0.5f)
                                    val phaseOff = i * 0.35f
                                    val amp = sin(wavePhase + phaseOff).let { (it + 1f) / 2f }
                                    val barH = maxH * (0.12f + amp * 0.88f)

                                    val barColor = if (node.resonanceScore > 0.5f)
                                        MatteBronze.copy(alpha = 0.3f + amp * 0.4f)
                                    else
                                        SoftGray.copy(alpha = 0.2f + amp * 0.25f)

                                    drawRoundRect(
                                        color = barColor,
                                        topLeft = Offset(x, (size.height - barH) / 2f),
                                        size = Size(barWidth, barH),
                                        cornerRadius = CornerRadius(barWidth / 2f)
                                    )
                                }
                            }
                        }
                        .padding(8.dp)
                )

                Spacer(Modifier.height(4.dp))

                // Resonance label
                val resonanceLabel = when {
                    node.resonanceScore > 0.8f -> "Güçlü Rezonans"
                    node.resonanceScore > 0.5f -> "Aktif Dinleniyor"
                    node.resonanceScore > 0.2f -> "Keşfediliyor"
                    else -> "Yeni Yankı"
                }
                Text(
                    text = resonanceLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (node.resonanceScore > 0.5f) MatteBronze.copy(alpha = 0.7f) else MutedGray,
                    fontSize = 10.sp
                )
            }
        }

        // Vertical branch line downward (connecting to next sibling/child)
        if (showBranch && node.depth <= nextDepth) {
            val branchIndent = if (nextDepth > node.depth) {
                indentDp + 36.dp // indent for child
            } else {
                indentDp // same level
            }

            Box(
                modifier = Modifier
                    .padding(start = branchIndent)
                    .width(2.dp)
                    .height(20.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MutedGray.copy(alpha = 0.25f),
                                MutedGray.copy(alpha = 0.08f)
                            )
                        )
                    )
            )
        } else if (showBranch) {
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ── Reply Bar ────────────────────────────────────────────────────
@Composable
private fun EchoReplyBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .navigationBarsPadding()
            .clip(RoundedCornerShape(24.dp))
            .background(FrostedGlass)
            .drawBehind {
                drawRoundRect(
                    color = FrostedBorder,
                    cornerRadius = CornerRadius(24.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Yankına cevap ver...",
            style = MaterialTheme.typography.bodySmall,
            color = MutedGray,
            modifier = Modifier.weight(1f)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MatteBronze.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Outlined.GraphicEq, contentDescription = "Sesli Yanıt", tint = MatteBronze, modifier = Modifier.size(20.dp))
            }
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MatteBronze.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Outlined.Videocam, contentDescription = "Görüntülü Yanıt", tint = MatteBronze, modifier = Modifier.size(20.dp))
            }
        }
    }
}
