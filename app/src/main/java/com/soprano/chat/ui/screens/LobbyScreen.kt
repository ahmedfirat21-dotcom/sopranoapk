package com.soprano.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soprano.chat.ui.components.GlassCard
import com.soprano.chat.ui.theme.*

@Composable
fun LobbyScreen(
    onJoinRoom: () -> Unit = {},
    onOpenEchoChain: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Anthracite)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Lobi",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Ivory,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Odaları keşfet veya oluştur",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SoftGray
                        )
                    }

                    FloatingActionButton(
                        onClick = onOpenEchoChain,
                        containerColor = MatteBronze,
                        contentColor = Ivory,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Oda Oluştur")
                    }
                }
            }

            // Category Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val categories = listOf("Tümü", "Sesli", "Görüntülü", "Podcast", "Müzik")
                    items(categories.size) { index ->
                        val isSelected = index == 0
                        FilterChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = categories[index],
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MatteBronze.copy(alpha = 0.2f),
                                selectedLabelColor = MatteBronze,
                                containerColor = SlateGray.copy(alpha = 0.5f),
                                labelColor = SoftGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = FrostedBorder,
                                selectedBorderColor = MatteBronze.copy(alpha = 0.4f),
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            // Room Cards
            items(4) { index ->
                RoomCard(index, onJoinRoom = onJoinRoom)
            }
        }
    }
}

@Composable
private fun RoomCard(index: Int, onJoinRoom: () -> Unit = {}) {
    val roomData = listOf(
        Triple("Akustik Akşam", Icons.Outlined.HeadsetMic, "Sesli Oda"),
        Triple("Ekip Toplantısı", Icons.Outlined.Videocam, "Görüntülü"),
        Triple("Açık Sahne", Icons.Outlined.Groups, "Topluluk"),
        Triple("DJ Session", Icons.Outlined.HeadsetMic, "Müzik")
    )
    val (title, icon, type) = roomData[index]
    val memberCounts = listOf(5, 12, 34, 8)

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Room icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MatteBronze.copy(alpha = 0.15f),
                                DeepSlate
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MatteBronze,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Ivory,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MatteBronze.copy(alpha = 0.6f))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "$type · ${memberCounts[index]} kişi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedGray
                    )
                }
            }

            // Join button
            TextButton(
                onClick = onJoinRoom,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MatteBronze
                )
            ) {
                Text(
                    text = "Katıl",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
