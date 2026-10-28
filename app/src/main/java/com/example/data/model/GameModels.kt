package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class User(
    @DocumentId val id: String = "",
    val displayName: String = "Hunter",
    val email: String = "",
    val avatarId: String = "avatar_1",
    val coins: Long = 50L,
    val xp: Long = 0L,
    val level: Long = 0L,
    val matchesPlayed: Long = 0L,
    val matchesWon: Long = 0L,
    val lastDailyRewardTimestamp: Long = 0L,
    val activeBoxSkin: String = "skin_cyber",
    val ownedSkins: List<String> = listOf("skin_cyber"),
    val inventoryRadar: Long = 1L,
    val inventoryExtraGuess: Long = 1L,
    val isAdmin: Boolean = false,
    val isBanned: Boolean = false,
    val banReason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Room(
    @DocumentId val id: String = "",
    val code: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val guestId: String? = null,
    val guestName: String? = null,
    val wager: Long = 25L,
    val status: String = "WAITING", // WAITING, READY, IN_MATCH, COMPLETED
    val activeMatchId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Match(
    @DocumentId val id: String = "",
    val roomId: String = "",
    val wager: Long = 25L,
    val pot: Long = 50L,
    val hiderId: String = "",
    val seekerId: String = "",
    val hiderName: String = "",
    val seekerName: String = "",
    val hiddenBox: Long = 0L, // 1..10, 0 means not yet selected
    val guesses: List<Long> = emptyList(),
    val maxGuesses: Long = 3L,
    val hidingDeadline: Long = 0L,
    val status: String = "HIDING", // HIDING, SEEKING, SEEKER_WON, HIDER_WON, ABANDONED
    val winnerId: String? = null,
    val seekerRadarUsed: Boolean = false,
    val radarRevealedBoxes: List<Long> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val matchId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val isEmote: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class ShopItem(
    val id: String,
    val name: String,
    val category: String, // "SKIN" or "POWERUP" or "COINS"
    val description: String,
    val priceCoins: Long,
    val previewColorHex: Long,
    val badge: String = ""
)

@IgnoreExtraProperties
data class AppConfig(
    @DocumentId val id: String = "global",
    val currentVersion: String = "1.1",
    val latestApkUrl: String = "",
    val updateTitle: String = "New App Version Available",
    val updateMessage: String = "Download the latest APK update for new features and optimizations.",
    val forceUpdate: Boolean = false,
    val broadcastNotice: String = "",
    val showNotice: Boolean = false,
    val upiId: String = "boxhunter@upi",
    val qrImageUrl: String = "",
    val minDepositCoins: Long = 50L,
    val coinRateInr: Long = 1L // 1 Rupee = 1 Coin
)

@IgnoreExtraProperties
data class DepositRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val coins: Long = 50L,
    val amountInr: Long = 50L,
    val utrNumber: String = "",
    val screenshotUrl: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val rejectionReason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val reviewedAt: Timestamp? = null
)
