package com.soprano.chat.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprano.chat.ui.theme.*

// ── Incoming Call / Frequency Screen ─────────────────────────────
@Composable
fun IncomingCallScreen(
    callerName: String = "Elif",
    callerInitial: Char = 'E',
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "callAmbient")

    // ── Mesh gradient breathing ──
    val breatheA by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breatheA"
    )
    val breatheB by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(6000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breatheB"
    )

    // ── Ripple ring animations (3 concentric, staggered) ──
    val ripple1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "ripple1"
    )
    val ripple2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = LinearEasing),
            RepeatMode.Restart,
            initialStartOffset = StartOffset(800)
        ),
        label = "ripple2"
    )
    val ripple3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(2400, easing = LinearEasing),
            RepeatMode.Restart,
            initialStartOffset = StartOffset(1600)
        ),
        label = "ripple3"
    )

    // ── Aura ring breathing ──
    val auraRingScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "auraRing"
    )
    val auraRingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "auraAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            // ── Breathing Mesh Gradient Background ──
            .drawWithCache {
                val orb1 = Brush.radialGradient(
                    colors = listOf(SlateGray.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(size.width * (0.25f + breatheA * 0.5f), size.height * (0.2f + breatheB * 0.2f)),
                    radius = size.maxDimension * 0.5f
                )
                val orb2 = Brush.radialGradient(
                    colors = listOf(MatteBronze.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * (0.7f - breatheA * 0.3f), size.height * (0.5f + breatheB * 0.2f)),
                    radius = size.maxDimension * 0.45f
                )
                val orb3 = Brush.radialGradient(
                    colors = listOf(DeepSlate.copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(size.width * (0.5f + breatheB * 0.2f), size.height * (0.8f - breatheA * 0.1f)),
                    radius = size.maxDimension * 0.35f
                )
                onDrawBehind {
                    drawRect(color = Anthracite)
                    drawRect(brush = orb1)
                    drawRect(brush = orb2)
                    drawRect(brush = orb3)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // ── Top Label ──
            Text(
                text = "GELEN FREKANS",
                style = MaterialTheme.typography.labelSmall,
                color = MatteBronze,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                fontSize = 11.sp
            )

            Spacer(Modifier.weight(0.2f))

            // ── Caller Aura with Ripple Rings ──
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // Ripple ring 1
                RippleRing(progress = ripple1, maxSize = 280.dp)
                // Ripple ring 2
                RippleRing(progress = ripple2, maxSize = 260.dp)
                // Ripple ring 3
                RippleRing(progress = ripple3, maxSize = 240.dp)

                // Aura breathing ring
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer {
                            scaleX = auraRingScale
                            scaleY = auraRingScale
                        }
                        .drawBehind {
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        MatteBronze.copy(alpha = auraRingAlpha),
                                        RoseGold.copy(alpha = auraRingAlpha * 0.5f),
                                        MatteBronze.copy(alpha = auraRingAlpha * 0.3f),
                                        RoseGold.copy(alpha = auraRingAlpha)
                                    )
                                ),
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                )

                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF6A5A8E).copy(alpha = 0.4f),
                                    DeepSlate
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = callerInitial.toString(),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ivory.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Caller Info ──
            Text(
                text = callerName,
                style = MaterialTheme.typography.headlineMedium,
                color = Ivory,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Frekansa davet ediyor...",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedGray,
                fontSize = 14.sp
            )

            Spacer(Modifier.weight(0.4f))

            // ── Bottom Action Buttons ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Reject
                Button(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FrostedGlass,
                        contentColor = SoftGray
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, FrostedBorder
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 1.dp
                    )
                ) {
                    Icon(
                        Icons.Outlined.CallEnd,
                        contentDescription = null,
                        tint = Color(0xFF8B3A3A).copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Reddet",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = SoftGray
                    )
                }

                // Accept
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .drawWithCache {
                            val shimmerBrush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height)
                            )
                            onDrawBehind {
                                drawRoundRect(
                                    brush = shimmerBrush,
                                    cornerRadius = CornerRadius(28.dp.toPx())
                                )
                            }
                        },
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MatteBronze,
                        contentColor = Ivory
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 10.dp,
                        pressedElevation = 3.dp
                    )
                ) {
                    Icon(
                        Icons.Outlined.GraphicEq,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Katıl",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// ── Ripple Ring Composable ────────────────────────────────────────
@Composable
private fun RippleRing(
    progress: Float,
    maxSize: androidx.compose.ui.unit.Dp
) {
    val scale = 0.5f + progress * 0.5f
    val alpha = (1f - progress).coerceIn(0f, 0.5f)

    Box(
        modifier = Modifier
            .size(maxSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .drawBehind {
                drawCircle(
                    color = MatteBronze,
                    style = Stroke(width = 1.5f.dp.toPx())
                )
            }
    )
}
