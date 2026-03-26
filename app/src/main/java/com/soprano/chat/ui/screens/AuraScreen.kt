package com.soprano.chat.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprano.chat.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

// ── Data ──────────────────────────────────────────────────────────
private data class EchoItem(
    val title: String,
    val type: String, // "Sesli" or "Görüntülü"
    val waveColor: Color,
    val listeners: String
)

private val demoEchoes = listOf(
    EchoItem("Gece Fısıltıları",  "Sesli Yankı",    Color(0xFF6A5A8E), "124"),
    EchoItem("Işık ve Gölge",     "Görüntülü Yankı", Color(0xFF4A8B6A), "89"),
    EchoItem("Sessiz Rezonans",   "Sesli Yankı",    Color(0xFFA07850), "203")
)

// ── Main AuraScreen ──────────────────────────────────────────────
@Composable
fun AuraScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "auraProfile")

    // Aura ring breathing animation
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

    // Outer glow pulse
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowRadius"
    )

    // Background subtle drift
    val driftX by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "driftX"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val orb = Brush.radialGradient(
                    colors = listOf(MatteBronze.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * (0.5f + driftX * 0.2f), size.height * 0.25f),
                    radius = size.maxDimension * 0.5f
                )
                onDrawBehind {
                    drawRect(color = Anthracite)
                    drawRect(brush = orb)
                }
            }
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Banner Area ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .drawWithCache {
                    val bannerBrush = Brush.verticalGradient(
                        colors = listOf(
                            SlateGray.copy(alpha = 0.4f),
                            DeepSlate.copy(alpha = 0.6f),
                            Anthracite
                        )
                    )
                    onDrawBehind { drawRect(brush = bannerBrush) }
                },
            contentAlignment = Alignment.TopEnd
        ) {
            // Settings icon
            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(top = 40.dp, end = 12.dp)
                    .statusBarsPadding()
            ) {
                Icon(Icons.Outlined.Settings, contentDescription = "Ayarlar", tint = SoftGray, modifier = Modifier.size(22.dp))
            }
        }

        // ── Large Profile Avatar with Aura Ring ──
        Box(
            modifier = Modifier.offset(y = (-40).dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow orb
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = glowRadius
                        scaleY = glowRadius
                        alpha = ringAlpha * 0.3f
                    }
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MatteBronze.copy(alpha = 0.2f),
                                    RoseGold.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                    }
            )

            // Aura ring (breathing)
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .graphicsLayer {
                        scaleX = ringScale
                        scaleY = ringScale
                    }
                    .drawBehind {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    MatteBronze.copy(alpha = ringAlpha),
                                    RoseGold.copy(alpha = ringAlpha * 0.6f),
                                    MatteBronze.copy(alpha = ringAlpha * 0.3f),
                                    RoseGold.copy(alpha = ringAlpha)
                                )
                            ),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
            )

            // Avatar circle
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(DeepSlate, SlateGray)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MatteBronze,
                    modifier = Modifier.size(52.dp)
                )
            }
        }

        // ── Username & Handle ──
        Spacer(Modifier.height((-24).dp))

        Text(
            text = "Soprano",
            style = MaterialTheme.typography.headlineMedium,
            color = Ivory,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "@soprano_elite",
            style = MaterialTheme.typography.bodyMedium,
            color = MatteBronze,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(20.dp))

        // ── Aura Status (No Numbers) ──
        AuraStatusRow()

        Spacer(Modifier.height(28.dp))

        // ── Bio ──
        Text(
            text = "Seslerin arasında kaybolmayı seven bir ruh.\nRezonans arayan, sessizlikten korkmayan.",
            style = MaterialTheme.typography.bodyMedium,
            color = SoftGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(Modifier.height(32.dp))

        // ── Echo Showcase ──
        EchoShowcase()

        Spacer(Modifier.height(28.dp))

        // ── Profile Actions ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileActionButton(
                icon = Icons.Outlined.Edit,
                label = "Düzenle",
                modifier = Modifier.weight(1f)
            )
            ProfileActionButton(
                icon = Icons.Outlined.Share,
                label = "Paylaş",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Settings List ──
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SettingsRow(Icons.Outlined.Shield, "Gizlilik & Güvenlik")
            SettingsRow(Icons.Outlined.Headphones, "Ses Ayarları")
            SettingsRow(Icons.Outlined.Palette, "Görünüm")
            SettingsRow(Icons.Outlined.Info, "Hakkında")
        }
    }
}

// ── Aura Status Chips ────────────────────────────────────────────
@Composable
private fun AuraStatusRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AuraStatusChip(label = "Aura", value = "Akort Edilmiş")
        AuraStatusChip(label = "Rezonans", value = "Uyumlu")
        AuraStatusChip(label = "Frekans", value = "Derin")
    }
}

@Composable
private fun AuraStatusChip(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(FrostedGlass)
            .drawBehind {
                drawRoundRect(
                    color = FrostedBorder,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MutedGray,
            fontSize = 9.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MatteBronze,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

// ── Echo Showcase ────────────────────────────────────────────────
@Composable
private fun EchoShowcase() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Yankılar",
                style = MaterialTheme.typography.titleMedium,
                color = Ivory,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Tümü",
                style = MaterialTheme.typography.labelMedium,
                color = MatteBronze,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(demoEchoes.size) { index ->
                EchoCard(demoEchoes[index])
            }
        }
    }
}

@Composable
private fun EchoCard(echo: EchoItem) {
    val infiniteTransition = rememberInfiniteTransition(label = "echo_${echo.title}")

    // Wave animation phase
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_${echo.title}"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        // Circular card with abstract waveform
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            echo.waveColor.copy(alpha = 0.15f),
                            DeepSlate.copy(alpha = 0.8f)
                        )
                    )
                )
                .drawBehind {
                    // Frosted border
                    drawCircle(
                        color = FrostedBorder,
                        style = Stroke(1.dp.toPx())
                    )

                    // Abstract sound wave
                    val centerY = size.height / 2f
                    val barCount = 12
                    val barWidth = size.width * 0.035f
                    val maxBarHeight = size.height * 0.35f
                    val spacing = size.width / (barCount + 3)

                    for (i in 0 until barCount) {
                        val x = spacing * (i + 1.5f)
                        val phaseOffset = i * 0.5f
                        val amplitude = sin(wavePhase + phaseOffset).let { (it + 1f) / 2f }
                        val barHeight = maxBarHeight * (0.2f + amplitude * 0.8f)

                        drawRoundRect(
                            color = echo.waveColor.copy(alpha = 0.5f + amplitude * 0.3f),
                            topLeft = Offset(x - barWidth / 2, centerY - barHeight / 2),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2)
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // intentionally empty — waveform is drawn in drawBehind
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = echo.title,
            style = MaterialTheme.typography.labelMedium,
            color = Ivory,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 12.sp
        )
        Text(
            text = echo.type,
            style = MaterialTheme.typography.labelSmall,
            color = MutedGray,
            fontSize = 10.sp
        )
    }
}

// ── Profile Action Buttons ───────────────────────────────────────
@Composable
private fun ProfileActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(16.dp)

    Button(
        onClick = { },
        modifier = modifier.height(44.dp),
        shape = pillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = FrostedGlass,
            contentColor = Ivory
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, FrostedBorder)
    ) {
        Icon(icon, contentDescription = null, tint = MatteBronze, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Settings Row ─────────────────────────────────────────────────
@Composable
private fun SettingsRow(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FrostedGlass)
            .drawBehind {
                drawRoundRect(
                    color = FrostedBorder,
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(0.5f.dp.toPx())
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MatteBronze, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Ivory,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MutedGray, modifier = Modifier.size(18.dp))
    }
}
