package com.soprano.chat.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprano.chat.ui.theme.*
import kotlin.math.*

// ── Data ──────────────────────────────────────────────────────────
private data class ConnectionNode(
    val id: String,
    val name: String,
    val initial: Char,
    val closeness: Float, // 0..1 — how close to the user (1 = closest)
    val color: Color
)

private val demoConnections = listOf(
    ConnectionNode("1", "Elif",   'E', 0.95f, Color(0xFF6A5A8E)),
    ConnectionNode("2", "Kerem",  'K', 0.85f, Color(0xFF4A6FA5)),
    ConnectionNode("3", "Zeynep", 'Z', 0.70f, Color(0xFF8B3A5E)),
    ConnectionNode("4", "Burak",  'B', 0.60f, Color(0xFF4A8B6A)),
    ConnectionNode("5", "Selin",  'S', 0.50f, Color(0xFFA07850)),
    ConnectionNode("6", "Deniz",  'D', 0.35f, Color(0xFF5A7A8E)),
    ConnectionNode("7", "Cem",    'C', 0.25f, Color(0xFF7A6A50)),
    ConnectionNode("8", "Ayşe",   'A', 0.20f, Color(0xFF6A8A5A)),
)

// ── Main ConnectionsScreen ───────────────────────────────────────
@Composable
fun ConnectionsScreen(
    onOpenWhisper: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "treeAmbient")

    // Global floating drift
    val driftPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "driftPhase"
    )

    // Track which node is "focused" (centered)
    var focusedNodeId by remember { mutableStateOf<String?>(null) }

    // Animate the focus offset transition
    val focusAnimProgress by animateFloatAsState(
        targetValue = if (focusedNodeId != null) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "focusAnim"
    )

    // Background
    val bgDrift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bgDrift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val orb = Brush.radialGradient(
                    colors = listOf(MatteBronze.copy(alpha = 0.04f), Color.Transparent),
                    center = Offset(size.width * (0.3f + bgDrift * 0.4f), size.height * 0.4f),
                    radius = size.maxDimension * 0.5f
                )
                onDrawBehind {
                    drawRect(color = Anthracite)
                    drawRect(brush = orb)
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── Header ──
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Bağlantı Ağacı",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Ivory,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Çevreni keşfet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftGray
                )
            }

            Spacer(Modifier.height(4.dp))

            // ── Legend ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendDot(color = MatteBronze, label = "Yakın Çember")
                LegendDot(color = SoftGray, label = "Uzak Bağlantı")
            }

            Spacer(Modifier.height(8.dp))

            // ── Connection Tree Canvas ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 0.dp)
            ) {
                ConnectionTreeCanvas(
                    connections = demoConnections,
                    focusedNodeId = focusedNodeId,
                    focusProgress = focusAnimProgress,
                    driftPhase = driftPhase,
                    onNodeTap = { nodeId ->
                        focusedNodeId = if (focusedNodeId == nodeId) null else nodeId
                    }
                )
            }

            // ── Bottom Info ──
            if (focusedNodeId != null) {
                val focused = demoConnections.firstOrNull { it.id == focusedNodeId }
                if (focused != null) {
                    FocusedNodeInfo(focused, onOpenWhisper = onOpenWhisper)
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

// ── Legend Dot ────────────────────────────────────────────────────
@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MutedGray,
            fontSize = 11.sp
        )
    }
}

// ── Canvas Tree ──────────────────────────────────────────────────
@Composable
private fun ConnectionTreeCanvas(
    connections: List<ConnectionNode>,
    focusedNodeId: String?,
    focusProgress: Float,
    driftPhase: Float,
    onNodeTap: (String) -> Unit
) {
    val density = LocalDensity.current

    // Precompute node positions in [0,1] normalized space
    val nodeAngles = remember(connections.size) {
        connections.mapIndexed { i, _ ->
            (2.0 * PI * i / connections.size).toFloat()
        }
    }

    // Store tap positions for hit-testing
    val nodePositions = remember { mutableMapOf<String, Offset>() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(connections) {
                detectTapGestures { tapOffset ->
                    val hitRadius = 60f
                    nodePositions.entries.minByOrNull {
                        (tapOffset - it.value).getDistance()
                    }?.let { (id, pos) ->
                        if ((tapOffset - pos).getDistance() < hitRadius) {
                            onNodeTap(id)
                        }
                    }
                }
            }
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxRadius = minOf(size.width, size.height) * 0.40f

        // ── Draw "root" (self) node ──
        val rootRadius = 28f
        val rootCenter = Offset(centerX, centerY)

        // ── Compute node positions ──
        val positions = connections.mapIndexed { index, node ->
            val angle = nodeAngles[index]

            // Distance from center based on closeness (inverted: closer = nearer to center)
            val distFactor = 1f - node.closeness * 0.65f
            val baseDistance = maxRadius * (0.35f + distFactor * 0.65f)

            // Underwater floating drift
            val driftStrength = 8f + (1f - node.closeness) * 12f
            val nodePhaseOffset = index * 1.3f
            val floatX = sin(driftPhase + nodePhaseOffset) * driftStrength
            val floatY = cos(driftPhase * 0.7f + nodePhaseOffset * 1.1f) * driftStrength * 0.7f

            // Focus offset: if a node is focused, shift the tree so that node goes to center
            var x = centerX + cos(angle) * baseDistance + floatX
            var y = centerY + sin(angle) * baseDistance + floatY

            if (focusedNodeId != null && focusProgress > 0f) {
                if (node.id == focusedNodeId) {
                    // Move focused node toward center
                    x = x + (centerX - x) * focusProgress * 0.7f
                    y = y + (centerY - y) * focusProgress * 0.7f
                } else {
                    // Push others slightly outward
                    val dx = x - centerX
                    val dy = y - centerY
                    x += dx * focusProgress * 0.15f
                    y += dy * focusProgress * 0.15f
                }
            }

            val position = Offset(x, y)
            nodePositions[node.id] = position
            position to node
        }

        // ── Draw branches (lines from root to each node) ──
        positions.forEach { (pos, node) ->
            val lineAlpha = (node.closeness * 0.6f + 0.1f).coerceIn(0.1f, 0.7f)
            val lineWidth = (node.closeness * 3f + 0.5f).coerceIn(0.5f, 3.5f)

            val lineColor = if (node.closeness > 0.6f) {
                MatteBronze.copy(alpha = lineAlpha)
            } else {
                MutedGray.copy(alpha = lineAlpha * 0.6f)
            }

            // Curved branch using quadratic bezier
            val midX = (rootCenter.x + pos.x) / 2f + sin(driftPhase * 0.5f) * 15f
            val midY = (rootCenter.y + pos.y) / 2f + cos(driftPhase * 0.3f) * 10f

            val path = Path().apply {
                moveTo(rootCenter.x, rootCenter.y)
                quadraticBezierTo(midX, midY, pos.x, pos.y)
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = lineWidth, cap = StrokeCap.Round)
            )
        }

        // ── Draw root node ──
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(MatteBronze.copy(alpha = 0.4f), DeepSlate),
                center = rootCenter,
                radius = rootRadius
            ),
            center = rootCenter,
            radius = rootRadius
        )
        drawCircle(
            color = MatteBronze.copy(alpha = 0.5f),
            center = rootCenter,
            radius = rootRadius,
            style = Stroke(width = 2f)
        )

        // Root initial
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(230, 245, 240, 232) // Ivory
                textSize = 20f * density.density
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                isAntiAlias = true
            }
            drawText("S", centerX, centerY + 7f * density.density, paint)
        }

        // ── Draw connection nodes ──
        positions.forEach { (pos, node) ->
            val nodeSize = (14f + node.closeness * 16f).coerceIn(14f, 30f)
            val isFocused = node.id == focusedNodeId

            val effectiveSize = if (isFocused) nodeSize * 1.3f else nodeSize

            // Glow for close nodes
            if (node.closeness > 0.6f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MatteBronze.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = pos,
                        radius = effectiveSize * 2f
                    ),
                    center = pos,
                    radius = effectiveSize * 2f
                )
            }

            // Node circle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        node.color.copy(alpha = 0.4f),
                        DeepSlate.copy(alpha = 0.9f)
                    ),
                    center = pos,
                    radius = effectiveSize
                ),
                center = pos,
                radius = effectiveSize
            )

            // Border
            val borderColor = if (node.closeness > 0.6f) {
                MatteBronze.copy(alpha = 0.5f)
            } else {
                FrostedBorder
            }
            val borderWidth = if (isFocused) 2.5f else if (node.closeness > 0.6f) 1.5f else 0.8f

            drawCircle(
                color = borderColor,
                center = pos,
                radius = effectiveSize,
                style = Stroke(width = borderWidth)
            )

            // Initial text
            drawContext.canvas.nativeCanvas.apply {
                val textSizePx = (8f + node.closeness * 8f) * density.density
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.argb(
                        (180 + node.closeness * 75).toInt().coerceAtMost(255),
                        245, 240, 232
                    )
                    textSize = textSizePx
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                    isAntiAlias = true
                }
                drawText(
                    node.initial.toString(),
                    pos.x,
                    pos.y + textSizePx * 0.35f,
                    paint
                )
            }

            // Name label for close nodes or focused
            if (node.closeness > 0.5f || isFocused) {
                drawContext.canvas.nativeCanvas.apply {
                    val labelSize = 10f * density.density
                    val paint = android.graphics.Paint().apply {
                        color = if (node.closeness > 0.6f) {
                            android.graphics.Color.argb(200, 184, 146, 106) // MatteBronze
                        } else {
                            android.graphics.Color.argb(150, 160, 160, 176) // SoftGray
                        }
                        textSize = labelSize
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                        isAntiAlias = true
                    }
                    drawText(
                        node.name,
                        pos.x,
                        pos.y + effectiveSize + labelSize + 4f * density.density,
                        paint
                    )
                }
            }
        }
    }
}

// ── Focused Node Info Panel ──────────────────────────────────────
@Composable
private fun FocusedNodeInfo(node: ConnectionNode, onOpenWhisper: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onOpenWhisper() }
            .background(FrostedGlass)
            .drawBehind {
                drawRoundRect(
                    color = FrostedBorder,
                    cornerRadius = CornerRadius(18.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
            }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(
                    RoundedCornerShape(50)
                )
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(node.color.copy(alpha = 0.4f), DeepSlate)
                    )
                )
                .drawBehind {
                    drawCircle(
                        color = MatteBronze.copy(alpha = 0.4f),
                        style = Stroke(1.5f.dp.toPx())
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = node.initial.toString(),
                color = Ivory,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = node.name,
                style = MaterialTheme.typography.titleSmall,
                color = Ivory,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))

            val closenessLabel = when {
                node.closeness > 0.8f -> "Çok Yakın Rezonans"
                node.closeness > 0.5f -> "Uyumlu Bağlantı"
                node.closeness > 0.3f -> "Tanıdık Frekans"
                else -> "Uzak Yankı"
            }
            Text(
                text = closenessLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MatteBronze,
                fontSize = 11.sp
            )
        }

        // Closeness indicator
        val closenessText = "${(node.closeness * 100).toInt()}%"
        Text(
            text = closenessText,
            style = MaterialTheme.typography.titleMedium,
            color = MatteBronze.copy(alpha = 0.8f),
            fontWeight = FontWeight.Bold
        )
    }
}
