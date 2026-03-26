package com.soprano.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.soprano.chat.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .clip(cardShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        FrostedGlass,
                        SlateGray.copy(alpha = 0.45f)
                    )
                )
            )
            .drawBehind {
                // Border highlight at top
                drawRoundRect(
                    color = FrostedBorder,
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.dp.toPx()
                    )
                )
            }
            .padding(20.dp),
        content = content
    )
}

@Composable
fun ScreenPlaceholder(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Anthracite)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = Ivory
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = SoftGray
            )
        }
    }
}
