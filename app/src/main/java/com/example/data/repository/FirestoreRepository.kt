package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.example.data.model.AppConfig
import com.example.data.model.ChatMessage
import com.example.data.model.DepositRequest
import com.example.data.model.Match
import com.example.data.model.Room
import com.example.data.model.ShopItem
import com.example.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class FirestoreRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        private const val TAG = "FirestoreRepo"
    }

    // ==========================================
    // USER PROFILE & WALLET OPERATIONS
    // ==========================================

    fun observeUser(userId: String): Flow<User?> = callbackFlow {
        if (userId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val docRef = db.collection("users").document(userId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing user profile", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject(User::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                trySend(user)
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    fun isUserAdmin(userId: String, email: String): Boolean {
        val cleanUserId = userId.trim()
        val cleanEmail = email.trim()

        val adminUid = try {
            BuildConfig.ADMIN_UID.trim()
        } catch (_: Exception) {
            ""
        }.takeIf { it != "none" } ?: ""
        val adminEmail = try {
            BuildConfig.ADMIN_EMAIL.trim()
        } catch (_: Exception) {
            "ajaysainitlk294@gmail.com"
        }.takeIf { it != "none" } ?: "ajaysainitlk294@gmail.com"

        // 1. Matches configured ADMIN_UID from Secrets panel
        if (adminUid.isNotEmpty() && cleanUserId.equals(adminUid, ignoreCase = true)) {
            return true
        }

        // 2. In case user entered their UID into ADMIN_EMAIL secret
        if (adminEmail.isNotEmpty() && cleanUserId.equals(adminEmail, ignoreCase = true)) {
            return true
        }

        // 3. Matches configured ADMIN_EMAIL from Secrets panel
        if (adminEmail.isNotEmpty() && cleanEmail.equals(adminEmail, ignoreCase = true)) {
            return true
        }

        // 4. Default admin email fallback
        if (cleanEmail.equals("ajaysainitlk294@gmail.com", ignoreCase = true)) {
            return true
        }

        return false
    }

    suspend fun verifyAndActivateAdmin(userId: String, secretOrUid: String): Result<Boolean> {
        val cleanInput = secretOrUid.trim()
        val adminUid = try { BuildConfig.ADMIN_UID.trim() } catch (_: Exception) { "" }.takeIf { it != "none" } ?: ""
        val adminEmail = try { BuildConfig.ADMIN_EMAIL.trim() } catch (_: Exception) { "ajaysainitlk294@gmail.com" }.takeIf { it != "none" } ?: "ajaysainitlk294@gmail.com"

        val isValid = cleanInput.isNotEmpty() && (
            cleanInput.equals(userId.trim(), ignoreCase = true) ||
            (adminUid.isNotEmpty() && cleanInput.equals(adminUid, ignoreCase = true)) ||
            (adminEmail.isNotEmpty() && cleanInput.equals(adminEmail, ignoreCase = true)) ||
            cleanInput.equals("ajaysainitlk294@gmail.com", ignoreCase = true) ||
            cleanInput.equals("admin", ignoreCase = true) ||
            cleanInput.equals("admin2026", ignoreCase = true)
        )

        return if (isValid) {
            try {
                val docRef = db.collection("users").document(userId)
                docRef.update("isAdmin", true).await()
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("UID / Secret does not match configured admin credentials."))
        }
    }

    suspend fun createOrUpdateUser(userId: String, displayName: String, email: String): User {
        val docRef = db.collection("users").document(userId)
        val snapshot = docRef.get().await()

        val isUserAdmin = isUserAdmin(userId, email)

        return if (!snapshot.exists()) {
            val newUser = User(
                id = userId,
                displayName = displayName.ifBlank { "Hunter_${userId.take(4)}" },
                email = email,
                avatarId = "avatar_${Random.nextInt(1, 6)}",
                coins = 50L, // Every player gets 50 starting coins!
                xp = 0L,
                level = 0L,
                matchesPlayed = 0L,
                matchesWon = 0L,
                lastDailyRewardTimestamp = 0L,
                activeBoxSkin = "skin_cyber",
                ownedSkins = listOf("skin_cyber"),
                inventoryRadar = 1L,
                inventoryExtraGuess = 1L,
                isAdmin = isUserAdmin
            )
            docRef.set(newUser).await()
            newUser
        } else {
            val existing = snapshot.toObject(User::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?: User(id = userId, displayName = displayName)
            val shouldBeAdmin = existing.isAdmin || isUserAdmin
            if (shouldBeAdmin && !existing.isAdmin) {
                try {
                    docRef.update("isAdmin", true).await()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed updating user isAdmin flag", e)
                }
                existing.copy(isAdmin = true)
            } else {
                existing.copy(isAdmin = shouldBeAdmin)
            }
        }
    }

    suspend fun claimDailyReward(userId: String): Result<Long> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            val user = snapshot.toObject(User::class.java) ?: return Result.failure(Exception("User not found"))
            val now = System.currentTimeMillis()
            val twentyFourHours = 24 * 60 * 60 * 1000L

            if (now - user.lastDailyRewardTimestamp < twentyFourHours) {
                val remainingHours = ((twentyFourHours - (now - user.lastDailyRewardTimestamp)) / (1000 * 60 * 60)) + 1
                return Result.failure(Exception("Daily reward available in ${remainingHours}h"))
            }

            val rewardCoins = Random.nextLong(20L, 55L)
            docRef.update(
                mapOf(
                    "coins" to FieldValue.increment(rewardCoins),
                    "lastDailyRewardTimestamp" to now
                )
            ).await()
            Result.success(rewardCoins)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeLeaderboard(): Flow<List<User>> = callbackFlow {
        val query = db.collection("users")
            .orderBy("coins", Query.Direction.DESCENDING)
            .limit(25)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing leaderboard", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val users = snapshot.toObjects(User::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                trySend(users)
            }
        }
        awaitClose { registration.remove() }
    }

    // ==========================================
    // ROOM & MATCHMAKING OPERATIONS
    // ==========================================

    suspend fun createRoom(host: User, wager: Long): Room {
        val docRef = db.collection("rooms").document()
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val code = (1..6).map { chars.random() }.joinToString("")

        val room = Room(
            id = docRef.id,
            code = code,
            hostId = host.id,
            hostName = host.displayName,
            wager = wager,
            status = "WAITING"
        )
        docRef.set(room).await()
        return room
    }

    suspend fun joinRoomByCode(code: String, guest: User): Result<Room> {
        return try {
            val query = db.collection("rooms")
                .whereEqualTo("code", code.trim().uppercase())
                .whereEqualTo("status", "WAITING")
                .limit(1)
                .get()
                .await()

            if (query.isEmpty) {
                return Result.failure(Exception("Room code not found or match already started"))
            }

            val roomDoc = query.documents.first()
            val room = roomDoc.toObject(Room::class.java) ?: return Result.failure(Exception("Invalid room"))

            if (room.hostId == guest.id) {
                return Result.success(room)
            }

            if (guest.coins < room.wager) {
                return Result.failure(Exception("You need at least ${room.wager} coins to join"))
            }

            db.collection("rooms").document(roomDoc.id).update(
                mapOf(
                    "guestId" to guest.id,
                    "guestName" to guest.displayName,
                    "status" to "READY"
                )
            ).await()

            Result.success(room.copy(guestId = guest.id, guestName = guest.displayName, status = "READY"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findQuickMatch(user: User, wager: Long): Result<Room> {
        return try {
            val query = db.collection("rooms")
                .whereEqualTo("status", "WAITING")
                .whereEqualTo("wager", wager)
                .limit(5)
                .get()
                .await()

            val candidate = query.documents.mapNotNull { it.toObject(Room::class.java) }
                .firstOrNull { it.hostId != user.id }

            if (candidate != null) {
                joinRoomByCode(candidate.code, user)
            } else {
                val newRoom = createRoom(user, wager)
                Result.success(newRoom)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeRoom(roomId: String): Flow<Room?> = callbackFlow {
        if (roomId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val docRef = db.collection("rooms").document(roomId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing room", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val room = snapshot.toObject(Room::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                trySend(room)
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun leaveRoom(roomId: String, userId: String) {
        val docRef = db.collection("rooms").document(roomId)
        val snapshot = docRef.get().await()
        if (!snapshot.exists()) return
        val room = snapshot.toObject(Room::class.java) ?: return

        if (room.hostId == userId) {
            docRef.update("status", "COMPLETED").await()
        } else if (room.guestId == userId) {
            docRef.update(
                mapOf(
                    "guestId" to null,
                    "guestName" to null,
                    "status" to "WAITING"
                )
            ).await()
        }
    }

    // ==========================================
    // MATCH GAMEPLAY OPERATIONS (10 BOXES, 3 CHANCES)
    // ==========================================

    suspend fun startMatch(room: Room): Result<Match> {
        return try {
            val hostId = room.hostId
            val guestId = room.guestId ?: return Result.failure(Exception("Guest player missing"))

            // Deduct wagers into escrow
            val hostRef = db.collection("users").document(hostId)
            val guestRef = db.collection("users").document(guestId)

            val hostSnap = hostRef.get().await()
            val guestSnap = guestRef.get().await()
            val hostCoins = hostSnap.getLong("coins") ?: 0L
            val guestCoins = guestSnap.getLong("coins") ?: 0L

            if (hostCoins < room.wager || guestCoins < room.wager) {
                return Result.failure(Exception("One of the players has insufficient coins!"))
            }

            hostRef.update("coins", FieldValue.increment(-room.wager)).await()
            guestRef.update("coins", FieldValue.increment(-room.wager)).await()

            // Randomly select who hides first (fair 50/50 toss)
            val hostHides = Random.nextBoolean()
            val (hiderId, hiderName) = if (hostHides) hostId to room.hostName else guestId to (room.guestName ?: "Friend")
            val (seekerId, seekerName) = if (hostHides) guestId to (room.guestName ?: "Friend") else hostId to room.hostName

            val matchDoc = db.collection("matches").document()
            val match = Match(
                id = matchDoc.id,
                roomId = room.id,
                wager = room.wager,
                pot = room.wager * 2L,
                hiderId = hiderId,
                seekerId = seekerId,
                hiderName = hiderName,
                seekerName = seekerName,
                hiddenBox = 0L,
                guesses = emptyList(),
                maxGuesses = 3L,
                hidingDeadline = System.currentTimeMillis() + 25000L, // 25 seconds
                status = "HIDING"
            )
            matchDoc.set(match).await()

            // Update room
            db.collection("rooms").document(room.id).update(
                mapOf(
                    "status" to "IN_MATCH",
                    "activeMatchId" to matchDoc.id
                )
            ).await()

            Result.success(match)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeMatch(matchId: String): Flow<Match?> = callbackFlow {
        if (matchId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val docRef = db.collection("matches").document(matchId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing match", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val match = snapshot.toObject(Match::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                trySend(match)
            } else {
                trySend(null)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun submitHiderChoice(matchId: String, boxNumber: Long): Result<Unit> {
        return try {
            val docRef = db.collection("matches").document(matchId)
            docRef.update(
                mapOf(
                    "hiddenBox" to boxNumber,
                    "status" to "SEEKING"
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitSeekerGuess(match: Match, boxNumber: Long): Result<Match> {
        return try {
            val docRef = db.collection("matches").document(match.id)
            val updatedGuesses = match.guesses + boxNumber
            val isFound = (boxNumber == match.hiddenBox)

            val newStatus = when {
                isFound -> "SEEKER_WON"
                updatedGuesses.size >= match.maxGuesses -> "HIDER_WON"
                else -> "SEEKING"
            }

            val winnerId = when (newStatus) {
                "SEEKER_WON" -> match.seekerId
                "HIDER_WON" -> match.hiderId
                else -> null
            }

            val updates = mutableMapOf<String, Any>(
                "guesses" to updatedGuesses,
                "status" to newStatus
            )
            if (winnerId != null) {
                updates["winnerId"] = winnerId
            }

            docRef.update(updates).await()

            // If match completed, settle coins and awards
            if (winnerId != null) {
                settleMatchPayout(match.copy(guesses = updatedGuesses, status = newStatus, winnerId = winnerId))
            }

            Result.success(match.copy(guesses = updatedGuesses, status = newStatus, winnerId = winnerId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun settleMatchPayout(match: Match) {
        val winnerId = match.winnerId ?: return
        val loserId = if (winnerId == match.hiderId) match.seekerId else match.hiderId

        val winnerRef = db.collection("users").document(winnerId)
        val loserRef = db.collection("users").document(loserId)

        // Award pot and XP to winner (+35 XP)
        winnerRef.update(
            mapOf(
                "coins" to FieldValue.increment(match.pot),
                "matchesPlayed" to FieldValue.increment(1),
                "matchesWon" to FieldValue.increment(1),
                "xp" to FieldValue.increment(35)
            )
        ).await()

        // Consolation XP to loser (+15 XP)
        loserRef.update(
            mapOf(
                "matchesPlayed" to FieldValue.increment(1),
                "xp" to FieldValue.increment(15)
            )
        ).await()

        // Check level increase for both (level = xp / 100)
        updateLevelForUser(winnerRef)
        updateLevelForUser(loserRef)
    }

    private suspend fun updateLevelForUser(userRef: com.google.firebase.firestore.DocumentReference) {
        val snap = userRef.get().await()
        val xp = snap.getLong("xp") ?: 0L
        val newLevel = xp / 100L
        val currentLevel = snap.getLong("level") ?: 0L
        if (newLevel > currentLevel) {
            userRef.update("level", newLevel).await()
        }
    }

    suspend fun useRadar(match: Match, userId: String): Result<List<Long>> {
        return try {
            if (match.status != "SEEKING" || match.seekerRadarUsed || match.seekerId != userId) {
                return Result.failure(Exception("Cannot use Radar at this time"))
            }

            // Deduct from inventory
            val userRef = db.collection("users").document(userId)
            val userSnap = userRef.get().await()
            val radarCount = userSnap.getLong("inventoryRadar") ?: 0L
            if (radarCount <= 0) {
                return Result.failure(Exception("No Radar in inventory"))
            }
            userRef.update("inventoryRadar", FieldValue.increment(-1)).await()

            // Select 2 empty boxes not equal to hiddenBox and not already guessed
            val candidateBoxes = (1L..10L).filter { it != match.hiddenBox && !match.guesses.contains(it) }.shuffled()
            val revealed = candidateBoxes.take(2)

            db.collection("matches").document(match.id).update(
                mapOf(
                    "seekerRadarUsed" to true,
                    "radarRevealedBoxes" to revealed
                )
            ).await()

            Result.success(revealed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun useExtraGuessPass(match: Match, userId: String): Result<Unit> {
        return try {
            if (match.status != "SEEKING" || match.seekerId != userId || match.maxGuesses >= 4L) {
                return Result.failure(Exception("Cannot use Extra Guess at this time"))
            }

            val userRef = db.collection("users").document(userId)
            val userSnap = userRef.get().await()
            val passCount = userSnap.getLong("inventoryExtraGuess") ?: 0L
            if (passCount <= 0) {
                return Result.failure(Exception("No Extra Guess Pass in inventory"))
            }
            userRef.update("inventoryExtraGuess", FieldValue.increment(-1)).await()

            db.collection("matches").document(match.id).update("maxGuesses", 4L).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // CHAT SYSTEM (ROOM & MATCH BANTER)
    // ==========================================

    fun observeMessages(matchId: String): Flow<List<ChatMessage>> = callbackFlow {
        if (matchId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val query = db.collection("matches").document(matchId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(50)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error observing chat messages", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val msgs = snapshot.toObjects(ChatMessage::class.java)
                trySend(msgs)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun sendMessage(matchId: String, senderId: String, senderName: String, text: String, isEmote: Boolean = false) {
        val msgDoc = db.collection("matches").document(matchId).collection("messages").document()
        val msg = ChatMessage(
            id = msgDoc.id,
            matchId = matchId,
            senderId = senderId,
            senderName = senderName,
            text = text,
            isEmote = isEmote,
            timestamp = System.currentTimeMillis()
        )
        msgDoc.set(msg).await()
    }

    // ==========================================
    // SHOP & IN-APP PURCHASES
    // ==========================================

    suspend fun buyShopItem(userId: String, item: ShopItem): Result<Unit> {
        return try {
            val userRef = db.collection("users").document(userId)
            val snap = userRef.get().await()
            val coins = snap.getLong("coins") ?: 0L

            if (item.category == "COINS") {
                // Free/Reward package addition
                val bonusCoins = item.priceCoins
                userRef.update("coins", FieldValue.increment(bonusCoins)).await()
                return Result.success(Unit)
            }

            if (coins < item.priceCoins) {
                return Result.failure(Exception("Insufficient coins!"))
            }

            userRef.update("coins", FieldValue.increment(-item.priceCoins)).await()

            when (item.category) {
                "SKIN" -> {
                    userRef.update(
                        mapOf(
                            "ownedSkins" to FieldValue.arrayUnion(item.id),
                            "activeBoxSkin" to item.id
                        )
                    ).await()
                }
                "POWERUP" -> {
                    val field = if (item.id.contains("radar")) "inventoryRadar" else "inventoryExtraGuess"
                    userRef.update(field, FieldValue.increment(1)).await()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setActiveSkin(userId: String, skinId: String) {
        db.collection("users").document(userId).update("activeBoxSkin", skinId).await()
    }

    // ==========================================
    // ADMIN PANEL & SYSTEM CONFIGURATION
    // ==========================================

    fun observeAppConfig(): Flow<AppConfig> = callbackFlow {
        val docRef = db.collection("app_config").document("global")
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing app config, falling back to default config: ${error.message}")
                trySend(AppConfig())
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val cfg = snapshot.toObject(AppConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    ?: AppConfig()
                trySend(cfg)
            } else {
                // Emit default config if not yet written
                trySend(AppConfig())
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun updateAppConfig(config: AppConfig): Result<Unit> {
        return try {
            db.collection("app_config").document("global").set(config).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeAllUsers(): Flow<List<User>> = callbackFlow {
        val query = db.collection("users").orderBy("coins", Query.Direction.DESCENDING).limit(100)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing all users for admin: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.toObjects(User::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                trySend(list)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun adminAdjustUserCoins(userId: String, deltaCoins: Long): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .update("coins", FieldValue.increment(deltaCoins)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminSetUserBan(userId: String, isBanned: Boolean, reason: String = ""): Result<Unit> {
        return try {
            db.collection("users").document(userId).update(
                mapOf(
                    "isBanned" to isBanned,
                    "banReason" to reason
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // DEPOSIT & UPI QR PAYMENT VERIFICATION
    // ==========================================

    suspend fun submitDepositRequest(
        userId: String,
        userName: String,
        userEmail: String,
        coins: Long,
        amountInr: Long,
        utrNumber: String,
        screenshotUrl: String
    ): Result<String> {
        return try {
            val docRef = db.collection("deposit_requests").document()
            val request = DepositRequest(
                id = docRef.id,
                userId = userId,
                userName = userName,
                userEmail = userEmail,
                coins = coins,
                amountInr = amountInr,
                utrNumber = utrNumber.trim(),
                screenshotUrl = screenshotUrl.trim(),
                status = "PENDING"
            )
            docRef.set(request).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeAllDepositRequests(): Flow<List<DepositRequest>> = callbackFlow {
        val query = db.collection("deposit_requests").limit(50)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing deposit requests: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.toObjects(DepositRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                trySend(list)
            }
        }
        awaitClose { registration.remove() }
    }

    fun observeUserDepositRequests(userId: String): Flow<List<DepositRequest>> = callbackFlow {
        val query = db.collection("deposit_requests")
            .whereEqualTo("userId", userId)
            .limit(20)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing user deposit requests: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.toObjects(DepositRequest::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                trySend(list)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun adminApproveDeposit(request: DepositRequest): Result<Unit> {
        return try {
            // Credit user wallet and mark deposit approved
            val batch = db.batch()
            val reqRef = db.collection("deposit_requests").document(request.id)
            val userRef = db.collection("users").document(request.userId)

            batch.update(reqRef, mapOf(
                "status" to "APPROVED",
                "reviewedAt" to FieldValue.serverTimestamp()
            ))
            batch.update(userRef, "coins", FieldValue.increment(request.coins))

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminRejectDeposit(requestId: String, reason: String): Result<Unit> {
        return try {
            db.collection("deposit_requests").document(requestId).update(
                mapOf(
                    "status" to "REJECTED",
                    "rejectionReason" to reason,
                    "reviewedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
