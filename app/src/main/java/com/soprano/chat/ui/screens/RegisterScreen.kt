package com.soprano.chat.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprano.chat.ui.theme.*
import kotlinx.coroutines.delay

// ── Register & Onboarding Screen ─────────────────────────────────
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    // ── Form State ──
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var usernameFocused by remember { mutableStateOf(false) }
    var emailFocused by remember { mutableStateOf(false) }
    var passwordFocused by remember { mutableStateOf(false) }

    // Simulated username availability (available if 3+ chars)
    val usernameAvailable = username.length >= 3

    // ── Agreement State ──
    var agreementAccepted by remember { mutableStateOf(false) }

    // ── Photo State ──
    var hasPhoto by remember { mutableStateOf(false) }

    // ── Background Animation ──
    val infiniteTransition = rememberInfiniteTransition(label = "registerBg")
    val driftA by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "driftA"
    )
    val driftB by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(7000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "driftB"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val orb1 = Brush.radialGradient(
                    colors = listOf(SlateGray.copy(alpha = 0.2f), Color.Transparent),
                    center = Offset(size.width * (0.3f + driftA * 0.4f), size.height * (0.15f + driftB * 0.2f)),
                    radius = size.maxDimension * 0.5f
                )
                val orb2 = Brush.radialGradient(
                    colors = listOf(MatteBronze.copy(alpha = 0.04f), Color.Transparent),
                    center = Offset(size.width * (0.7f - driftA * 0.3f), size.height * (0.7f + driftB * 0.15f)),
                    radius = size.maxDimension * 0.4f
                )
                onDrawBehind {
                    drawRect(color = Anthracite)
                    drawRect(brush = orb1)
                    drawRect(brush = orb2)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Back Arrow ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Outlined.ArrowBack,
                        contentDescription = "Geri",
                        tint = Ivory,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Title ──
            Text(
                text = "Auranı Oluştur",
                style = MaterialTheme.typography.headlineMedium,
                color = MatteBronze,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Frekansını bul, bağlan",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedGray
            )

            Spacer(Modifier.height(28.dp))

            // ── Profile Photo Ring ──
            AuraPhotoRing(
                hasPhoto = hasPhoto,
                onPickPhoto = { hasPhoto = true }
            )

            Spacer(Modifier.height(32.dp))

            // ── Form Card ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Username
                RegisterPillField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = "Kullanıcı Adı",
                    leadingIcon = Icons.Outlined.AlternateEmail,
                    isFocused = usernameFocused,
                    onFocusChange = { usernameFocused = it },
                    trailingContent = {
                        if (username.isNotEmpty()) {
                            UsernameAvailabilityIcon(available = usernameAvailable)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(Modifier.height(14.dp))

                // Email
                RegisterPillField(
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

                // Password
                RegisterPillField(
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

                // ── Premium Agreement Ring ──
                PremiumAgreementRow(
                    accepted = agreementAccepted,
                    onToggle = { agreementAccepted = !agreementAccepted }
                )

                Spacer(Modifier.height(32.dp))

                // ── "Frekansa Katıl" Button ──
                JoinButton(
                    enabled = agreementAccepted && username.length >= 3 && email.isNotEmpty() && password.length >= 6,
                    onClick = onRegisterSuccess
                )
            }
        }
    }
}

// ── Aura Photo Ring ──────────────────────────────────────────────
@Composable
private fun AuraPhotoRing(
    hasPhoto: Boolean,
    onPickPhoto: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "photoRing")

    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe"
    )
    val breatheAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breatheAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onPickPhoto() }
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(130.dp)
                .graphicsLayer {
                    scaleX = breatheScale
                    scaleY = breatheScale
                    alpha = breatheAlpha * 0.4f
                }
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MatteBronze.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                }
        )

        // Breathing ring border
        Box(
            modifier = Modifier
                .size(110.dp)
                .graphicsLayer {
                    scaleX = breatheScale
                    scaleY = breatheScale
                }
                .drawBehind {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                MatteBronze.copy(alpha = breatheAlpha),
                                RoseGold.copy(alpha = breatheAlpha * 0.5f),
                                MatteBronze.copy(alpha = breatheAlpha * 0.3f),
                                RoseGold.copy(alpha = breatheAlpha)
                            )
                        ),
                        style = Stroke(width = 2.5f.dp.toPx())
                    )
                }
        )

        // Inner circle
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(DeepSlate, SlateGray.copy(alpha = 0.8f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (hasPhoto) {
                // Simulated photo — gradient placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MatteBronze.copy(alpha = 0.3f),
                                    SlateGray.copy(alpha = 0.6f),
                                    DeepSlate
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MatteBronze,
                        modifier = Modifier.size(44.dp)
                    )
                }
            } else {
                Icon(
                    Icons.Outlined.CameraAlt,
                    contentDescription = "Fotoğraf Seç",
                    tint = MutedGray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Tiny "+" badge
        if (!hasPhoto) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-8).dp, y = (-8).dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MatteBronze),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Add,
                    contentDescription = null,
                    tint = Ivory,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ── Username Availability Icon ───────────────────────────────────
@Composable
private fun UsernameAvailabilityIcon(available: Boolean) {
    val iconColor by animateColorAsState(
        targetValue = if (available) MatteBronze else Color(0xFF8B3A3A).copy(alpha = 0.7f),
        animationSpec = tween(300),
        label = "availColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (available) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "availScale"
    )

    Icon(
        imageVector = if (available) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
        contentDescription = if (available) "Uygun" else "Kullanımda",
        tint = iconColor,
        modifier = Modifier
            .size(20.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    )
}

// ── Premium Agreement Row (Animated Ring) ────────────────────────
@Composable
private fun PremiumAgreementRow(
    accepted: Boolean,
    onToggle: () -> Unit
) {
    // Fill animation
    val fillProgress by animateFloatAsState(
        targetValue = if (accepted) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ringFill"
    )

    val ringColor by animateColorAsState(
        targetValue = if (accepted) MatteBronze else MutedGray.copy(alpha = 0.5f),
        animationSpec = tween(400),
        label = "ringColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onToggle() },
        verticalAlignment = Alignment.Top
    ) {
        // ── Custom Animated Ring ──
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp)
                .drawBehind {
                    // Outer ring
                    drawCircle(
                        color = ringColor,
                        style = Stroke(width = 1.5f.dp.toPx())
                    )
                    // Fill circle (liquid fill animation)
                    if (fillProgress > 0f) {
                        clipRect(
                            top = size.height * (1f - fillProgress),
                            bottom = size.height
                        ) {
                            drawCircle(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        RoseGold.copy(alpha = 0.8f),
                                        MatteBronze
                                    )
                                ),
                                radius = size.minDimension / 2f - 2.dp.toPx()
                            )
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Check icon when filled
            if (fillProgress > 0.8f) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Ivory,
                    modifier = Modifier
                        .size(13.dp)
                        .graphicsLayer { alpha = (fillProgress - 0.8f) * 5f }
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // ── Agreement Text ──
        val agreementText = buildAnnotatedString {
            withStyle(SpanStyle(color = SoftGray, fontSize = 12.sp)) {
                append("SopranoChat ")
            }
            withStyle(SpanStyle(color = RoseGold, fontSize = 12.sp, fontWeight = FontWeight.Medium)) {
                append("Topluluk Rezonans Kuralları")
            }
            withStyle(SpanStyle(color = SoftGray, fontSize = 12.sp)) {
                append(" ve ")
            }
            withStyle(SpanStyle(color = RoseGold, fontSize = 12.sp, fontWeight = FontWeight.Medium)) {
                append("Gizlilik Politikası")
            }
            withStyle(SpanStyle(color = SoftGray, fontSize = 12.sp)) {
                append("'nı kabul ediyorum.")
            }
        }

        Text(
            text = agreementText,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Join Button ──────────────────────────────────────────────────
@Composable
private fun JoinButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "joinBtn")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -0.5f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "shimmer"
    )

    val buttonAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.4f,
        animationSpec = tween(400),
        label = "btnAlpha"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer { alpha = buttonAlpha }
            .then(
                if (enabled) {
                    Modifier.drawWithCache {
                        val shimmerBrush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            start = Offset(size.width * shimmer, 0f),
                            end = Offset(size.width * (shimmer + 0.4f), size.height)
                        )
                        onDrawBehind {
                            drawRoundRect(
                                brush = shimmerBrush,
                                cornerRadius = CornerRadius(28.dp.toPx())
                            )
                        }
                    }
                } else Modifier
            ),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MatteBronze,
            contentColor = Ivory,
            disabledContainerColor = MatteBronze.copy(alpha = 0.3f),
            disabledContentColor = Ivory.copy(alpha = 0.5f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (enabled) 10.dp else 0.dp,
            pressedElevation = 3.dp,
            disabledElevation = 0.dp
        )
    ) {
        Icon(
            Icons.Outlined.GraphicEq,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Frekansa Katıl",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
    }
}

// ── Pill Text Field (Register variant) ───────────────────────────
@Composable
private fun RegisterPillField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
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
            when {
                trailingContent != null -> trailingContent()
                isPassword -> {
                    IconButton(onClick = { onTogglePassword?.invoke() }) {
                        Icon(
                            if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = "Şifre görünürlüğü",
                            tint = MutedGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
