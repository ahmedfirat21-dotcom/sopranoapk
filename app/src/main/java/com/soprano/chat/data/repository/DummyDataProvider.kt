package com.soprano.chat.data.repository

import com.soprano.chat.data.network.ConnectionNode
import com.soprano.chat.data.network.EchoNode
import com.soprano.chat.data.network.RoomNode
import com.soprano.chat.data.network.UserProfile

object DummyDataProvider {

    // ── 1. Bağlantı Ağacı Fallback ──
    fun getEmptyStateConnections(): List<ConnectionNode> {
        val dummyProfile = UserProfile(
            id = "dummy_connection",
            username = "Gizemli Yankı",
            displayName = "Keşfedilmeyi Bekliyor",
            avatarUrl = null,
            auraColor = "#3A3A4A", // Soluk mat gri
            resonanceScore = 0,
            isOnline = false
        )
        return listOf(
            ConnectionNode(id = "conn_1", user = dummyProfile, connectionWeight = 0.1f, interactionCount = 0),
            ConnectionNode(id = "conn_2", user = dummyProfile, connectionWeight = 0.1f, interactionCount = 0),
            ConnectionNode(id = "conn_3", user = dummyProfile, connectionWeight = 0.1f, interactionCount = 0)
        )
    }

    // ── 2. Yankı Zinciri Fallback ──
    fun getWelcomeEcho(): List<EchoNode> {
        val founderProfile = UserProfile(
            id = "founder_1",
            username = "soprano_founder",
            displayName = "Soprano Founder",
            avatarUrl = null, 
            auraColor = "#B8926A", // Bronz rezonans halkası
            resonanceScore = 999,
            isOnline = true
        )
        return listOf(
            EchoNode(
                id = "welcome_echo_1",
                author = founderProfile,
                mediaUrl = "android.resource://com.soprano.chat/raw/welcome_audio", // Local dummy ses/lottie referansı
                mediaType = "AUDIO",
                duration = 15 // 15 saniyelik Hoş Geldin Yankısı
            )
        )
    }

    // ── 3. Lobi/Odalar Fallback ──
    fun getLoungeFallback(): List<RoomNode> {
        val botProfile = UserProfile(
            id = "bot_1",
            username = "soprano_bot",
            displayName = "Soprano Bot",
            avatarUrl = null,
            auraColor = "#6A5A8E", 
            resonanceScore = 100,
            isOnline = true
        )
        return listOf(
            RoomNode(
                id = "lounge_1",
                title = "Soprano Lounge (Sessiz Frekans)",
                livekitRoomName = "soprano_lounge_default",
                host = botProfile,
                participantCount = 1
            )
        )
    }
}
