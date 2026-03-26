package com.soprano.chat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprano.chat.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

// ── Studio Recording Screen ──────────────────────────────────────
@Composable
fun StudioScreen(
    onBack: () -> Unit = {},
    onSend: () -> Unit = {}
) {
    var isRecording by remember { mutableStateOf(false) }
    var recordingDone by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var cancelDragOffset by remember { mutableFloatStateOf(0f) }

    // Timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    // ── Animations ──
    val infiniteTransition = rememberInfiniteTransition(label = "studioAmbient")

    val bgDriftA by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bgA"
    )
    val bgDriftB by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(6000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bgB"
    )

    // Simulated audio level
    val audioWave by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "audioWave"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label = "wavePhase"
    )

    // Ring pulse while recording
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ringPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            // ── Heavy blur background ──
            .drawWithCache {
                val orb1 = Brush.radialGradient(
                    colors = listOf(SlateGray.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(size.width * (0.3f + bgDriftA * 0.4f), size.height * 0.3f),
                    radius = size.maxDimension * 0.5f
                )
                val orb2 = Brush.radialGradient(
                    colors = listOf(MatteBronze.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * (0.7f - bgDriftA * 0.3f), size.height * 0.6f),
                    radius = size.maxDimension * 0.4f
                )
                onDrawBehind {
                    drawRect(color = Anthracite)
                    drawRect(brush = orb1)
                    drawRect(brush = orb2)
                }
            }
            .blur(16.dp),
        contentAlignment = Alignment.Center
    ) {}

    // Content layer (not blurred)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Geri",
                        tint = Ivory,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Stüdyo",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ivory,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.size(48.dp)) // balance
            }

            Spacer(Modifier.weight(0.3f))

            // ── Recording Circle ──
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer fluid waveform (only during recording)
                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .graphicsLayer {
                                scaleX = ringPulse
                                scaleY = ringPulse
                            }
                            .drawWithCache {
                                onDrawBehind {
                                    // Organic waveform ring
                                    val centerX = size.width / 2f
                                    val centerY = size.height / 2f
                                    val baseRadius = size.minDimension / 2f - 8f
                                    val segments = 72

                                    val path = androidx.compose.ui.graphics.Path()
                                    for (i in 0..segments) {
                                        val angle = (2.0 * PI * i / segments).toFloat()
                                        val waveAmp = audioWave * 12f
                                        val wave = sin(angle * 6f + wavePhase) * waveAmp +
                                                sin(angle * 3f + wavePhase * 1.5f) * waveAmp * 0.5f
                                        val r = baseRadius + wave

                                        val x = centerX + kotlin.math.cos(angle) * r
                                        val y = centerY + sin(angle) * r

                                        if (i == 0) path.moveTo(x, y)
                                        else path.lineTo(x, y)
                                    }
                                    path.close()

                                    drawPath(
                                        path = path,
                                        brush = Brush.sweepGradient(
                                            colors = listOf(
                                                MatteBronze.copy(alpha = 0.7f),
                                                RoseGold.copy(alpha = 0.4f),
                                                MatteBronze.copy(alpha = 0.5f),
                                                RoseGold.copy(alpha = 0.7f)
                                            )
                                        ),
                                        style = Stroke(width = 3.dp.toPx())
                                    )
                                }
                            }
                    )
                }

                // Main circle
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    SlateGray.copy(alpha = 0.4f),
                                    DeepSlate.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .drawBehind {
                            drawCircle(
                                color = if (isRecording) MatteBronze.copy(alpha = 0.4f) else FrostedBorder,
                                style = Stroke(width = 2.dp.toPx())
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isRecording) {
                        // Timer
                        val mins = recordingSeconds / 60
                        val secs = recordingSeconds % 60
                        Text(
                            text = "%02d:%02d".format(mins, secs),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Ivory,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 2.sp
                        )
                    } else if (!recordingDone) {
                        Icon(
                            Icons.Outlined.Mic,
                            contentDescription = null,
                            tint = MatteBronze,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MatteBronze,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Status Text ──
            if (isRecording) {
                Text(
                    text = "İptal etmek için kaydır",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGray,
                    fontSize = 13.sp
                )
            } else if (!recordingDone) {
                Text(
                    text = "Kayıt başlatmak için dokun",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedGray,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.weight(0.2f))

            // ── Preview Card (after recording) ──
            AnimatedVisibility(
                visible = recordingDone,
                enter = fadeIn(tween(400)) + slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                ),
                exit = fadeOut(tween(200))
            ) {
                RecordingPreviewCard(
                    seconds = recordingSeconds,
                    wavePhase = wavePhase,
                    onSend = onSend
                )
            }

            Spacer(Modifier.weight(0.1f))

            // ── Bottom Controls ──
            if (!recordingDone) {
                RecordButton(
                    isRecording = isRecording,
                    audioLevel = audioWave,
                    onToggle = {
                        if (isRecording) {
                            isRecording = false
                            recordingDone = true
                        } else {
                            isRecording = true
                            recordingDone = false
                        }
                    }
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Record Button ────────────────────────────────────────────────
@Composable
private fun RecordButton(
    isRecording: Boolean,
    audioLevel: Float,
    onToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recBtn")
    val pulseRing by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer pulse ring
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer {
                        scaleX = pulseRing
                        scaleY = pulseRing
                        alpha = 2f - pulseRing
                    }
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF8B3A3A).copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                    }
            )
        }

        FloatingActionButton(
            onClick = onToggle,
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            containerColor = if (isRecording) Color(0xFF8B3A3A) else MatteBronze,
            contentColor = Ivory,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 12.dp,
                pressedElevation = 4.dp
            )
        ) {
            Icon(
                if (isRecording) Icons.Outlined.Stop else Icons.Outlined.Mic,
                contentDescription = if (isRecording) "Durdur" else "Kaydet",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// ── Recording Preview Card ───────────────────────────────────────
@Composable
private fun RecordingPreviewCard(
    seconds: Int,
    wavePhase: Float,
    onSend: () -> Unit
) {
    val pillShape = RoundedCornerShape(22.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .clip(pillShape)
            .background(FrostedGlass)
            .drawBehind {
                drawRoundRect(
                    color = FrostedBorder,
                    cornerRadius = CornerRadius(22.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Önizleme",
            style = MaterialTheme.typography.labelMedium,
            color = MatteBronze,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(12.dp))

        // Waveform preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DeepSlate.copy(alpha = 0.6f))
                .drawWithCache {
                    onDrawBehind {
                        val barCount = 36
                        val barWidth = size.width / (barCount * 2.2f)
                        val maxH = size.height * 0.75f
                        val spacing = size.width / barCount

                        for (i in 0 until barCount) {
                            val x = spacing * i + spacing * 0.25f
                            val phaseOff = i * 0.4f
                            val amp = sin(wavePhase + phaseOff).let { (it + 1f) / 2f }
                            val barH = maxH * (0.15f + amp * 0.85f)

                            drawRoundRect(
                                color = MatteBronze.copy(alpha = 0.4f + amp * 0.4f),
                                topLeft = Offset(x, (size.height - barH) / 2f),
                                size = Size(barWidth, barH),
                                cornerRadius = CornerRadius(barWidth / 2f)
                            )
                        }
                    }
                }
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${seconds}s",
            style = MaterialTheme.typography.labelSmall,
            color = SoftGray
        )

        Spacer(Modifier.height(16.dp))

        // Send button
        Button(
            onClick = onSend,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MatteBronze,
                contentColor = Ivory
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 2.dp
            )
        ) {
            Icon(Icons.Outlined.GraphicEq, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Yankıla", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}
