package com.soprano.chat.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprano.chat.ui.theme.*
import kotlin.math.abs
import kotlin.math.cos

// ── Data ──────────────────────────────────────────────────────────
private data class Frequency(
    val name: String,
    val subtitle: String,
    val auraColor: Color,
    val listeners: Int
)

private val frequencies = listOf(
    Frequency("Chill",                  "Sakin frekans, huzurlu anlar",       Color(0xFF4A6FA5), 42),
    Frequency("Derin Sohbetler",        "Anlamlı konuşmalar, derin bağlar",   Color(0xFF6A5A8E), 78),
    Frequency("Girişimcilik",           "Fikirler, vizyon, aksiyon",          Color(0xFFA07850), 156),
    Frequency("Gece Yarısı İtirafları", "Karanlıkta parlayan doğrular",      Color(0xFF8B3A5E), 34),
    Frequency("Rastgele Rezonans",      "Kaderle dans, sürpriz bağlantılar", Color(0xFF4A8B6A), 63)
)

// ── Main Screen ──────────────────────────────────────────────────
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DiscoverScreen(
    onJoinFrequency: () -> Unit = {}
) {
    val itemHeightDp: Dp = 80.dp
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeightDp.toPx() }

    // Padding items so the first & last real items center in the viewport
    val paddingItems = 2

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = paddingItems)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Derive selected index from scroll position
    val selectedIndex by remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            val centered = if (offset > itemHeightPx / 2) firstVisible + 1 else firstVisible
            (centered - paddingItems).coerceIn(0, frequencies.lastIndex)
        }
    }

    // Track previous selection for haptic-like animation pulse
    var prevSelected by remember { mutableIntStateOf(0) }
    val pulseScale = remember { Animatable(1f) }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != prevSelected) {
            prevSelected = selectedIndex
            pulseScale.snapTo(0.96f)
            pulseScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val currentFreq = frequencies[selectedIndex]

    // Animate background aura color
    val auraColor by animateColorAsState(
        targetValue = currentFreq.auraColor,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "aura"
    )

    // Infinite subtle background drift animation
    val infiniteTransition = rememberInfiniteTransition(label = "meshDrift")
    val driftX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftX"
    )
    val driftY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                // Dynamic mesh gradient background
                val offsetX = size.width * (0.2f + driftX * 0.6f)
                val offsetY = size.height * (0.15f + driftY * 0.5f)

                val meshBrush = Brush.radialGradient(
                    colors = listOf(
                        auraColor.copy(alpha = 0.12f),
                        Anthracite.copy(alpha = 0.95f),
                        DeepSlate
                    ),
                    center = Offset(offsetX, offsetY),
                    radius = size.maxDimension * 0.7f
                )

                onDrawBehind {
                    drawRect(color = Anthracite)
                    drawRect(brush = meshBrush)

                    // Secondary subtle gradient orb
                    val meshBrush2 = Brush.radialGradient(
                        colors = listOf(
                            auraColor.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(
                            size.width * (0.8f - driftX * 0.4f),
                            size.height * (0.7f - driftY * 0.3f)
                        ),
                        radius = size.maxDimension * 0.5f
                    )
                    drawRect(brush = meshBrush2)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Top Header ──
            Spacer(Modifier.height(32.dp))

            Text(
                text = "FREKANS",
                style = MaterialTheme.typography.labelSmall,
                color = MatteBronze,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Dalganı Seç",
                style = MaterialTheme.typography.headlineMedium,
                color = Ivory,
                fontWeight = FontWeight.Light
            )

            Spacer(Modifier.height(8.dp))

            // Subtle divider line
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(MatteBronze.copy(alpha = 0.4f))
            )

            Spacer(Modifier.weight(0.15f))

            // ── Frequency Dial (3D Cylinder Wheel) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeightDp * (paddingItems * 2 + 1))
                    .graphicsLayer {
                        scaleX = pulseScale.value
                        scaleY = pulseScale.value
                    },
                contentAlignment = Alignment.Center
            ) {
                // Center selection indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .height(itemHeightDp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    auraColor.copy(alpha = 0.08f),
                                    MatteBronze.copy(alpha = 0.06f),
                                    auraColor.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .drawBehind {
                            drawRoundRect(
                                color = MatteBronze.copy(alpha = 0.2f),
                                cornerRadius = CornerRadius(20.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx())
                            )
                        }
                )

                LazyColumn(
                    state = listState,
                    flingBehavior = snapBehavior,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeightDp * (paddingItems * 2 + 1)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    // Top padding items
                    items(paddingItems) {
                        Spacer(Modifier.height(itemHeightDp))
                    }

                    // Frequency items
                    items(frequencies.size) { index ->
                        val distanceFromCenter by remember {
                            derivedStateOf {
                                val viewportCenter = listState.layoutInfo.viewportSize.height / 2f
                                val itemInfo = listState.layoutInfo.visibleItemsInfo
                                    .firstOrNull { it.index == index + paddingItems }

                                if (itemInfo != null) {
                                    val itemCenter = itemInfo.offset + itemInfo.size / 2f
                                    val dist = (itemCenter - viewportCenter) / itemHeightPx
                                    dist.coerceIn(-2.5f, 2.5f)
                                } else {
                                    2.5f
                                }
                            }
                        }

                        FrequencyDialItem(
                            frequency = frequencies[index],
                            distanceFromCenter = distanceFromCenter,
                            isSelected = index == selectedIndex,
                            itemHeight = itemHeightDp
                        )
                    }

                    // Bottom padding items
                    items(paddingItems) {
                        Spacer(Modifier.height(itemHeightDp))
                    }
                }
            }

            Spacer(Modifier.weight(0.05f))

            // ── Selected Frequency Info ──
            AnimatedFrequencyInfo(
                frequency = currentFreq,
                auraColor = auraColor
            )

            Spacer(Modifier.weight(0.1f))

            // ── "Frekansa Katıl" Action Button ──
            JoinFrequencyButton(auraColor = auraColor, onJoinFrequency = onJoinFrequency)

            Spacer(Modifier.height(100.dp)) // space for floating nav bar
        }
    }
}

// ── Dial Item ────────────────────────────────────────────────────
@Composable
private fun FrequencyDialItem(
    frequency: Frequency,
    distanceFromCenter: Float,
    isSelected: Boolean,
    itemHeight: Dp
) {
    val absDist = abs(distanceFromCenter)

    // 3D cylinder transforms
    val scale = (1f - absDist * 0.18f).coerceIn(0.55f, 1f)
    val alpha = (1f - absDist * 0.35f).coerceIn(0.1f, 1f)
    val rotationX = distanceFromCenter * -25f  // Tilt like a cylinder
    val translationY = distanceFromCenter * 6f  // Slight vertical compression

    val textColor by animateColorAsState(
        targetValue = if (isSelected) MatteBronze else Ivory.copy(alpha = alpha * 0.7f),
        animationSpec = tween(300),
        label = "dialTextColor"
    )

    val subtitleAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(250),
        label = "subtitleAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                this.rotationX = rotationX
                this.translationY = translationY
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = frequency.name,
                color = textColor,
                fontSize = if (isSelected) 22.sp else 18.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Light,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            if (subtitleAlpha > 0.01f) {
                Text(
                    text = frequency.subtitle,
                    color = SoftGray.copy(alpha = subtitleAlpha * 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(subtitleAlpha)
                        .padding(top = 2.dp)
                )
            }
        }
    }
}

// ── Animated Info Section ────────────────────────────────────────
@Composable
private fun AnimatedFrequencyInfo(
    frequency: Frequency,
    auraColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        // Listener count with pulsing dot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Pulsing live dot
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotPulse"
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(auraColor.copy(alpha = dotAlpha))
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${frequency.listeners} kişi bu frekansta",
                style = MaterialTheme.typography.bodySmall,
                color = SoftGray,
                fontSize = 13.sp
            )
        }
    }
}

// ── Glassmorphic Join Button ─────────────────────────────────────
@Composable
private fun JoinFrequencyButton(auraColor: Color, onJoinFrequency: () -> Unit = {}) {
    val pillShape = RoundedCornerShape(28.dp)

    // Subtle shimmer animation
    val infiniteTransition = rememberInfiniteTransition(label = "btnShimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Button(
        onClick = onJoinFrequency,
        modifier = Modifier
            .fillMaxWidth(0.72f)
            .height(56.dp)
            .drawWithCache {
                val shimmerBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    start = Offset(size.width * shimmerOffset, 0f),
                    end = Offset(size.width * (shimmerOffset + 0.5f), size.height)
                )
                onDrawBehind {
                    drawRoundRect(
                        brush = shimmerBrush,
                        cornerRadius = CornerRadius(28.dp.toPx())
                    )
                }
            },
        shape = pillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = FrostedGlass,
            contentColor = Ivory
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 2.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    FrostedBorder,
                    MatteBronze.copy(alpha = 0.3f),
                    FrostedBorder
                )
            )
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.GraphicEq,
            contentDescription = null,
            tint = MatteBronze,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Frekansa Katıl",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Ivory
        )
    }
}
