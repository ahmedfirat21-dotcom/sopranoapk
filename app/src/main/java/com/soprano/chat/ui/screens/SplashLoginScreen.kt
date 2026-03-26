package com.soprano.chat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprano.chat.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── Combined Splash → Login Screen ───────────────────────────────
@Composable
fun SplashLoginScreen(
    onLoginSuccess: () -> Unit = {},
    onRegister: () -> Unit = {}
) {
    // Phase: splash → login
    var showLogin by remember { mutableStateOf(false) }

    // Auto-transition after 2.5s
    LaunchedEffect(Unit) {
        delay(2500)
        showLogin = true
    }

    // ── Animations ──
    val infiniteTransition = rememberInfiniteTransition(label = "splashAmbient")

    // Mesh gradient orbs
    val driftA by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "driftA"
    )
    val driftB by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(6000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "driftB"
    )

    // Logo pulse
    val logoPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "logoPulse"
    )
    val logoGlowScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "logoGlowScale"
    )

    // Logo position & size animation (splash → login transition)
    val logoOffsetY by animateFloatAsState(
        targetValue = if (showLogin) -0.28f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "logoOffsetY"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (showLogin) 0.65f else 1f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "logoScale"
    )
    val bgBlur by animateFloatAsState(
        targetValue = if (showLogin) 8f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "bgBlur"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            // ── Dynamic Mesh Gradient Background ──
            .drawWithCache {
                val orb1 = Brush.radialGradient(
                    colors = listOf(SlateGray.copy(alpha = 0.3f), Color.Transparent),
                    center = Offset(size.width * (0.3f + driftA * 0.4f), size.height * (0.2f + driftB * 0.3f)),
                    radius = size.maxDimension * 0.5f
                )
                val orb2 = Brush.radialGradient(
                    colors = listOf(MatteBronze.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * (0.7f - driftA * 0.3f), size.height * (0.6f + driftB * 0.2f)),
                    radius = size.maxDimension * 0.4f
                )
                val orb3 = Brush.radialGradient(
                    colors = listOf(DeepSlate.copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(size.width * (0.5f + driftB * 0.2f), size.height * (0.8f - driftA * 0.15f)),
                    radius = size.maxDimension * 0.35f
                )
                onDrawBehind {
                    drawRect(color = Anthracite)
                    drawRect(brush = orb1)
                    drawRect(brush = orb2)
                    drawRect(brush = orb3)
                }
            }
            .then(
                if (bgBlur > 0.5f) Modifier.blur(bgBlur.dp) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // This box just provides the blurred background
    }

    // Content on top (not blurred)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // ── Logo (always visible, animates position) ──
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = logoOffsetY * size.height
                        scaleX = logoScale
                        scaleY = logoScale
                    },
                contentAlignment = Alignment.Center
            ) {
                ResonanceLogo(
                    pulseAlpha = logoPulse,
                    glowScale = logoGlowScale
                )
            }
        }

        // ── Login Panel (slides in from below) ──
        AnimatedVisibility(
            visible = showLogin,
            enter = fadeIn(tween(600, delayMillis = 400)) +
                    slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(700, delayMillis = 300, easing = FastOutSlowInEasing)
                    ),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 80.dp)
        ) {
            LoginPanel(onLoginSuccess = onLoginSuccess, onRegister = onRegister)
        }
    }
}

// ── Resonance Logo ───────────────────────────────────────────────
@Composable
private fun ResonanceLogo(
    pulseAlpha: Float,
    glowScale: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoRings")

    // Rotating ring phase
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "ringRotation"
    )

    // Wave phase for the sound wave
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "wavePhase"
    )

    Box(
        modifier = Modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                    alpha = pulseAlpha * 0.3f
                }
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MatteBronze.copy(alpha = 0.25f),
                                RoseGold.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )

        // Outer resonance ring
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer { rotationZ = ringRotation * 0.1f }
                .drawBehind {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                MatteBronze.copy(alpha = pulseAlpha * 0.7f),
                                RoseGold.copy(alpha = pulseAlpha * 0.3f),
                                Color.Transparent,
                                MatteBronze.copy(alpha = pulseAlpha * 0.5f)
                            )
                        ),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
        )

        // Inner ring
        Box(
            modifier = Modifier
                .size(90.dp)
                .drawBehind {
                    drawCircle(
                        color = MatteBronze.copy(alpha = pulseAlpha * 0.25f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
        )

        // Central sound wave icon (drawn with Canvas)
        Box(
            modifier = Modifier
                .size(50.dp)
                .drawBehind {
                    val centerY = size.height / 2f
                    val barCount = 5
                    val barWidth = size.width * 0.08f
                    val spacing = size.width / (barCount + 1)
                    val maxH = size.height * 0.65f

                    for (i in 0 until barCount) {
                        val x = spacing * (i + 1)
                        val phaseOff = i * 0.8f
                        val amp = sin(wavePhase + phaseOff).let { (it + 1f) / 2f }
                        val barH = maxH * (0.25f + amp * 0.75f)

                        drawRoundRect(
                            color = MatteBronze.copy(alpha = 0.7f + amp * 0.3f),
                            topLeft = Offset(x - barWidth / 2, centerY - barH / 2),
                            size = androidx.compose.ui.geometry.Size(barWidth, barH),
                            cornerRadius = CornerRadius(barWidth / 2)
                        )
                    }
                }
        )

        // Brand name below logo
        Text(
            text = "SopranoChat",
            style = MaterialTheme.typography.titleLarge,
            color = Ivory,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 24.dp)
        )
    }
}

// ── Login Panel ──────────────────────────────────────────────────
@Composable
private fun LoginPanel(onLoginSuccess: () -> Unit, onRegister: () -> Unit = {}) {
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }

    val pillShape = RoundedCornerShape(28.dp)
    val fieldShape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .clip(pillShape)
            .background(FrostedGlass)
            .drawBehind {
                drawRoundRect(
                    color = FrostedBorder,
                    cornerRadius = CornerRadius(28.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hoş Geldin",
            style = MaterialTheme.typography.headlineSmall,
            color = Ivory,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Frekansına bağlan",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedGray
        )

        Spacer(Modifier.height(28.dp))

        // ── Email Field ──
        PillTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "E-posta",
            leadingIcon = Icons.Outlined.Email,
            isFocused = emailFocused,
            onFocusChange = { emailFocused = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(Modifier.height(14.dp))

        // ── Password Field ──
        PillTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Şifre",
            leadingIcon = Icons.Outlined.Lock,
            isFocused = passwordFocused,
            onFocusChange = { passwordFocused = it },
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Spacer(Modifier.height(24.dp))

        // ── Login Button ──
        val infiniteTransition = rememberInfiniteTransition(label = "loginBtn")
        val shimmer by infiniteTransition.animateFloat(
            initialValue = -0.5f, targetValue = 1.5f,
            animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Restart),
            label = "shimmer"
        )

        Button(
            onClick = onLoginSuccess,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .drawWithCache {
                    val shimmerBrush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        start = Offset(size.width * shimmer, 0f),
                        end = Offset(size.width * (shimmer + 0.4f), size.height)
                    )
                    onDrawBehind {
                        drawRoundRect(
                            brush = shimmerBrush,
                            cornerRadius = CornerRadius(26.dp.toPx())
                        )
                    }
                },
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MatteBronze,
                contentColor = Ivory
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                text = "Giriş Yap",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Register Link ──
        Text(
            text = "Auranı henüz oluşturmadın mı? ",
            style = MaterialTheme.typography.bodySmall,
            color = MutedGray,
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
        Text(
            text = "Kayıt Ol",
            style = MaterialTheme.typography.bodySmall,
            color = RoseGold,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onRegister() }
        )
    }
}

// ── Pill Text Field ──────────────────────────────────────────────
@Composable
private fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val fieldShape = RoundedCornerShape(24.dp)

    val borderBrush = if (isFocused) {
        Brush.horizontalGradient(
            listOf(MatteBronze.copy(alpha = 0.6f), RoseGold.copy(alpha = 0.4f), MatteBronze.copy(alpha = 0.6f))
        )
    } else {
        Brush.horizontalGradient(listOf(FrostedBorder, FrostedBorder))
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { onFocusChange(it.isFocused) }
            .border(1.dp, borderBrush, fieldShape),
        shape = fieldShape,
        singleLine = true,
        placeholder = {
            Text(placeholder, color = MutedGray, fontSize = 14.sp)
        },
        leadingIcon = {
            Icon(
                leadingIcon,
                contentDescription = null,
                tint = if (isFocused) MatteBronze else MutedGray,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = "Şifre görünürlüğü",
                        tint = MutedGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Ivory,
            unfocusedTextColor = SoftGray,
            cursorColor = MatteBronze,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = SlateGray.copy(alpha = 0.3f),
            unfocusedContainerColor = SlateGray.copy(alpha = 0.2f)
        )
    )
}
