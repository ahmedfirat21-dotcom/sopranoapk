package com.soprano.chat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.VideocamOff
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprano.chat.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ── Data Models ──────────────────────────────────────────────────
private data class Participant(
    val name: String,
    val initial: Char,
    val isSpeaking: Boolean,
    val hasVideo: Boolean,
    val avatarColor: Color
)

private val demoParticipants = listOf(
    Participant("Elif",   'E', isSpeaking = true,  hasVideo = true,  avatarColor = Color(0xFF6A5A8E)),
    Participant("Kerem",  'K', isSpeaking = false, hasVideo = true,  avatarColor = Color(0xFF4A6FA5)),
    Participant("Zeynep", 'Z', isSpeaking = false, hasVideo = false, avatarColor = Color(0xFF8B3A5E)),
    Participant("Burak",  'B', isSpeaking = false, hasVideo = true,  avatarColor = Color(0xFF4A8B6A)),
    Participant("Selin",  'S', isSpeaking = true,  hasVideo = false, avatarColor = Color(0xFFA07850))
)

// ── Main VIP Lounge Screen ───────────────────────────────────────
@Composable
fun VipLoungeScreen(
    onBack: () -> Unit = {}
) {
    // ── Audio reactivity simulation ──
    val infiniteTransition = rememberInfiniteTransition(label = "roomAmbient")

    // Simulated "audio level" oscillation
    val audioLevel by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "audioLevel"
    )

    // Secondary breathe cycle
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Mesh gradient drift
    val driftA by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftA"
    )
    val driftB by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftB"
    )

    // ── Zen Mode (auto-hide controls) ──
    var showControls by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(lastInteractionTime) {
        delay(5000)
        showControls = false
    }

    // ── Aura Reveal slider ──
    var auraRevealLevel by remember { mutableFloatStateOf(0f) } // 0 = full blur, 1 = clear

    // ── Mic / Cam state ──
    var isMicOn by remember { mutableStateOf(true) }
    var isCamOn by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    showControls = true
                    lastInteractionTime = System.currentTimeMillis()
                }
            }
            // ── Dynamic Mesh Gradient Background ──
            .drawWithCache {
                val audioBoost = 0.04f + audioLevel * 0.08f
                val breatheBoost = breathe * 0.03f

                val orb1 = Brush.radialGradient(
                    colors = listOf(
                        MatteBronze.copy(alpha = audioBoost + breatheBoost),
                        Color.Transparent
                    ),
                    center = Offset(
                        size.width * (0.25f + driftA * 0.5f),
                        size.height * (0.2f + driftB * 0.3f)
                    ),
                    radius = size.maxDimension * (0.4f + audioLevel * 0.15f)
                )

                val orb2 = Brush.radialGradient(
                    colors = listOf(
                        SlateGray.copy(alpha = 0.15f + breatheBoost),
                        Color.Transparent
                    ),
                    center = Offset(
                        size.width * (0.75f - driftA * 0.3f),
                        size.height * (0.65f + driftB * 0.2f)
                    ),
                    radius = size.maxDimension * 0.5f
                )

                val orb3 = Brush.radialGradient(
                    colors = listOf(
                        Ivory.copy(alpha = 0.02f + audioLevel * 0.02f),
                        Color.Transparent
                    ),
                    center = Offset(
                        size.width * (0.5f + driftB * 0.2f),
                        size.height * (0.4f + driftA * 0.15f)
                    ),
                    radius = size.maxDimension * 0.35f
                )

                onDrawBehind {
                    drawRect(color = Anthracite)
                    drawRect(brush = orb1)
                    drawRect(brush = orb2)
                    drawRect(brush = orb3)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── Top Bar ──
            RoomTopBar(
                roomTitle = "Gece Sohbetleri",
                listenerCount = demoParticipants.size,
                showControls = showControls,
                onBack = onBack
            )

            // ── Avatar Flow Area ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AvatarFlow(
                    participants = demoParticipants,
                    audioLevel = audioLevel
                )
            }
        }

        // ── Aura Reveal Slider (right edge) ──
        AuraRevealSlider(
            level = auraRevealLevel,
            onLevelChange = {
                auraRevealLevel = it
                showControls = true
                lastInteractionTime = System.currentTimeMillis()
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        )

        // ── Zen Mode Control Panel ──
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(300)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            ZenControlPanel(
                isMicOn = isMicOn,
                onMicToggle = {
                    isMicOn = !isMicOn
                    lastInteractionTime = System.currentTimeMillis()
                },
                isCamOn = isCamOn,
                onCamToggle = {
                    isCamOn = !isCamOn
                    lastInteractionTime = System.currentTimeMillis()
                },
                onLeave = onBack
            )
        }
    }
}

// ── Room Top Bar ─────────────────────────────────────────────────
@Composable
private fun RoomTopBar(
    roomTitle: String,
    listenerCount: Int,
    showControls: Boolean,
    onBack: () -> Unit
) {
    AnimatedVisibility(
        visible = showControls,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(300))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = roomTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ivory,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MatteBronze)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "$listenerCount kişi",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftGray,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(onClick = { }) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "Seçenekler",
                    tint = Ivory,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Avatar Flow ──────────────────────────────────────────────────
@Composable
private fun AvatarFlow(
    participants: List<Participant>,
    audioLevel: Float
) {
    // Lay avatars out in an organic circular/scattered pattern
    val positions = remember {
        listOf(
            Offset(0f, -0.22f),   // top center
            Offset(-0.28f, 0f),   // left
            Offset(0.28f, 0.02f), // right
            Offset(-0.14f, 0.25f),// bottom-left
            Offset(0.16f, 0.27f)  // bottom-right
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        participants.forEachIndexed { index, participant ->
            val pos = positions.getOrElse(index) { Offset(0f, 0f) }
            val size = if (participant.isSpeaking) 82.dp else 68.dp

            FloatingAvatar(
                participant = participant,
                audioLevel = audioLevel,
                avatarSize = size,
                modifier = Modifier
                    .offset(
                        x = (pos.x * 300).dp,
                        y = (pos.y * 300).dp
                    )
            )
        }
    }
}

@Composable
private fun FloatingAvatar(
    participant: Participant,
    audioLevel: Float,
    avatarSize: Dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_${participant.name}")

    // Gentle floating motion
    val floatY by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3000 + (participant.name.hashCode() % 1500).let { if (it < 0) -it else it },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY_${participant.name}"
    )

    // Pulsing ring for speaker
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (participant.isSpeaking) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_${participant.name}"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = if (participant.isSpeaking) 0.7f else 0f,
        targetValue = if (participant.isSpeaking) 0.2f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha_${participant.name}"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                translationY = floatY
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Outer pulsing ring (speaker only)
            if (participant.isSpeaking) {
                val ringSize = avatarSize + 18.dp
                Box(
                    modifier = Modifier
                        .size(ringSize)
                        .graphicsLayer {
                            scaleX = ringScale
                            scaleY = ringScale
                            alpha = ringAlpha * audioLevel
                        }
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        MatteBronze.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                        }
                )

                // Inner glow ring
                Box(
                    modifier = Modifier
                        .size(avatarSize + 6.dp)
                        .drawBehind {
                            drawCircle(
                                color = MatteBronze.copy(alpha = 0.4f),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 2.dp.toPx()
                                )
                            )
                        }
                )
            } else {
                // Subtle border for non-speakers
                Box(
                    modifier = Modifier
                        .size(avatarSize + 4.dp)
                        .drawBehind {
                            drawCircle(
                                color = FrostedBorder,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 1.dp.toPx()
                                )
                            )
                        }
                )
            }

            // Avatar circle
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                participant.avatarColor.copy(alpha = 0.4f),
                                DeepSlate
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participant.initial.toString(),
                    fontSize = if (participant.isSpeaking) 26.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ivory.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // Name label
        Text(
            text = participant.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (participant.isSpeaking) MatteBronze else SoftGray,
            fontWeight = if (participant.isSpeaking) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 11.sp
        )
    }
}

// ── Aura Reveal Slider ───────────────────────────────────────────
@Composable
private fun AuraRevealSlider(
    level: Float,
    onLevelChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val trackHeight = 180.dp
    val trackHeightPx = with(density) { trackHeight.toPx() }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label
        Text(
            text = "AURA",
            style = MaterialTheme.typography.labelSmall,
            color = MatteBronze.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            fontSize = 9.sp
        )
        Spacer(Modifier.height(8.dp))

        // Vertical slider track
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(trackHeight)
                .clip(RoundedCornerShape(16.dp))
                .background(FrostedGlass)
                .drawBehind {
                    drawRoundRect(
                        color = FrostedBorder,
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newLevel = (level - dragAmount.y / trackHeightPx)
                            .coerceIn(0f, 1f)
                        onLevelChange(newLevel)
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Fill bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = level.coerceIn(0.02f, 1f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MatteBronze.copy(alpha = 0.6f),
                                MatteBronze.copy(alpha = 0.2f)
                            )
                        )
                    )
            )

            // Thumb indicator
            Box(
                modifier = Modifier
                    .offset(y = -(trackHeight * level) + (trackHeight / 2))
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MatteBronze)
                    .drawBehind {
                        drawCircle(
                            color = RoseGold.copy(alpha = 0.4f),
                            radius = size.minDimension * 0.8f
                        )
                    }
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = "SEVİYE",
            style = MaterialTheme.typography.labelSmall,
            color = MutedGray,
            fontSize = 8.sp,
            letterSpacing = 1.5.sp
        )
    }
}

// ── Zen Control Panel ────────────────────────────────────────────
@Composable
private fun ZenControlPanel(
    isMicOn: Boolean,
    onMicToggle: () -> Unit,
    isCamOn: Boolean,
    onCamToggle: () -> Unit,
    onLeave: () -> Unit
) {
    val pillShape = RoundedCornerShape(28.dp)

    Row(
        modifier = Modifier
            .clip(pillShape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        FrostedGlass,
                        SlateGray.copy(alpha = 0.6f),
                        FrostedGlass
                    )
                )
            )
            .drawBehind {
                drawRoundRect(
                    color = FrostedBorder,
                    cornerRadius = CornerRadius(28.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                )
            }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mic
        ZenButton(
            icon = if (isMicOn) Icons.Outlined.Mic else Icons.Outlined.MicOff,
            isActive = isMicOn,
            onClick = onMicToggle,
            contentDescription = "Mikrofon"
        )

        // Camera
        ZenButton(
            icon = if (isCamOn) Icons.Outlined.Videocam else Icons.Outlined.VideocamOff,
            isActive = isCamOn,
            onClick = onCamToggle,
            contentDescription = "Kamera"
        )

        // Leave
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8B3A3A).copy(alpha = 0.6f),
                            Color(0xFF5A2020).copy(alpha = 0.4f)
                        )
                    )
                )
                .drawBehind {
                    drawCircle(
                        color = Color(0xFF8B3A3A).copy(alpha = 0.3f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onLeave) {
                Icon(
                    Icons.Outlined.CallEnd,
                    contentDescription = "Ayrıl",
                    tint = Ivory,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ZenButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    contentDescription: String
) {
    val bgColor = if (isActive) MatteBronze.copy(alpha = 0.15f) else SlateGray.copy(alpha = 0.3f)
    val iconColor = if (isActive) MatteBronze else MutedGray

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bgColor)
            .drawBehind {
                drawCircle(
                    color = if (isActive) MatteBronze.copy(alpha = 0.25f) else FrostedBorder,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
