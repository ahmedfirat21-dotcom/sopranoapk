package com.soprano.chat.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprano.chat.ui.theme.*

@Composable
fun FloatingBottomBar(
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 24.dp,
                    shape = pillShape,
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .clip(pillShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            FrostedGlass,
                            SlateGray.copy(alpha = 0.7f)
                        )
                    )
                )
                .drawBehind {
                    // Top highlight for glassmorphism
                    drawRoundRect(
                        color = FrostedHighlight,
                        cornerRadius = CornerRadius(32.dp.toPx())
                    )
                }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { screen ->
                val isSelected = currentRoute == screen.route

                NavBarItem(
                    screen = screen,
                    isSelected = isSelected,
                    onClick = { onItemClick(screen) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MatteBronze else SoftGray,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "iconColor"
    )

    val labelColor by animateColorAsState(
        targetValue = if (isSelected) MatteBronze else MutedGray,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "labelColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.25f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "glow"
    )

    Column(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Bronze glow behind active icon
            if (glowAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .graphicsLayer { alpha = glowAlpha }
                        .background(
                            color = BronzeGlow,
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }

            Icon(
                imageVector = screen.icon!!,
                contentDescription = screen.title,
                tint = iconColor,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = screen.title,
            color = labelColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}
