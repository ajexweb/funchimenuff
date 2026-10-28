package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppConfig
import com.example.data.model.ChatMessage
import com.example.data.model.DepositRequest
import com.example.data.model.Match
import com.example.data.model.Room
import com.example.data.model.ShopItem
import com.example.data.model.User
import com.example.data.repository.FirestoreRepository
import com.example.ui.auth.AuthManager
import com.example.ui.components.CyberButton
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.DepositDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.MatchArenaScreen
import com.example.ui.screens.RoomScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.TutorialDialog
import com.example.ui.theme.AppleBackground
import com.example.ui.theme.AppleBlack
import com.example.ui.theme.AppleLightGray
import com.example.ui.theme.ApplePureWhite
import com.example.ui.theme.AppleSurface
import com.example.ui.theme.BoxHunterTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        val authManager = AuthManager(this)
        val repository = FirestoreRepository(this)

        setContent {
            BoxHunterTheme {
                BoxHunterApp(
                    authManager = authManager,
                    repository = repository
                )
            }
        }
    }
}

enum class MainTab {
    LOBBY,
    LEADERBOARD,
    SHOP
}

@Composable
fun BoxHunterApp(
    authManager: AuthManager,
    repository: FirestoreRepository
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Auth State
    val firebaseUser by authManager.observeAuthState().collectAsState(initial = authManager.currentUser)
    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    // Navigation and Active Game Session
    var currentTab by remember { mutableStateOf(MainTab.LOBBY) }
    var currentRoomId by remember { mutableStateOf<String?>(null) }
    var activeMatchId by remember { mutableStateOf<String?>(null) }
    var showTutorial by remember { mutableStateOf(false) }
    var showAdminPanel by remember { mutableStateOf(false) }
    var showDepositDialog by remember { mutableStateOf(false) }

    // User Profile
    var currentUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(firebaseUser) {
        val user = firebaseUser
        if (user != null) {
            val userProfile = repository.createOrUpdateUser(
                userId = user.uid,
                displayName = user.displayName ?: "Hunter_${user.uid.take(4)}",
                email = user.email ?: ""
            )
            val isAdm = userProfile.isAdmin || repository.isUserAdmin(user.uid, user.email ?: "")
            currentUser = userProfile.copy(isAdmin = isAdm)

            // Listen to real-time user updates
            repository.observeUser(user.uid).collect { updated ->
                if (updated != null) {
                    val isUpdatedAdm = updated.isAdmin || repository.isUserAdmin(user.uid, user.email ?: "")
                    currentUser = updated.copy(isAdmin = isUpdatedAdm)
                }
            }
        } else {
            currentUser = null
            currentRoomId = null
            activeMatchId = null
            showAdminPanel = false
        }
    }

    // App Global Config (Updates, Notices, UPI QR)
    val appConfig by repository.observeAppConfig().collectAsState(initial = AppConfig())

    // Active Room State
    val activeRoom by remember(currentRoomId) {
        currentRoomId?.let { repository.observeRoom(it) } ?: emptyFlow()
    }.collectAsState(initial = null)

    // Listen for room match transitions
    LaunchedEffect(activeRoom?.activeMatchId, activeRoom?.status) {
        val matchId = activeRoom?.activeMatchId
        if (activeRoom?.status == "IN_MATCH" && matchId != null) {
            activeMatchId = matchId
        }
    }

    // Active Match State
    val activeMatch by remember(activeMatchId) {
        activeMatchId?.let { repository.observeMatch(it) } ?: emptyFlow()
    }.collectAsState(initial = null)

    // Match Chat
    val matchChatMessages by remember(activeMatchId) {
        activeMatchId?.let { repository.observeMessages(it) } ?: emptyFlow()
    }.collectAsState(initial = emptyList())

    // Leaderboard
    val leaderboardPlayers by remember(currentUser) {
        if (currentUser != null) repository.observeLeaderboard() else emptyFlow()
    }.collectAsState(initial = emptyList())

    // User's own deposit requests
    val myDepositRequests by remember(currentUser) {
        if (currentUser != null) repository.observeUserDepositRequests(currentUser!!.id) else emptyFlow()
    }.collectAsState(initial = emptyList())

    // Admin data (only observed if currentUser.isAdmin)
    val allAdminUsers by remember(currentUser?.isAdmin) {
        if (currentUser?.isAdmin == true) repository.observeAllUsers() else emptyFlow()
    }.collectAsState(initial = emptyList())

    val allAdminDeposits by remember(currentUser?.isAdmin) {
        if (currentUser?.isAdmin == true) repository.observeAllDepositRequests() else emptyFlow()
    }.collectAsState(initial = emptyList())

    // Mandatory APK Update Dialog
    val isAppOutdated = remember(appConfig) {
        appConfig.forceUpdate && appConfig.latestApkUrl.isNotBlank() && appConfig.currentVersion != BuildConfig.VERSION_NAME
    }

    // UI Composition Root
    if (firebaseUser == null) {
        SignInScreen(
            isLoading = isSigningIn,
            errorMessage = authError,
            onSignInClick = {
                isSigningIn = true
                authError = null
                coroutineScope.launch {
                    val result = authManager.signInWithGoogle()
                    isSigningIn = false
                    result.onFailure {
                        authError = it.message ?: "Sign-in failed. Please try again."
                    }
                }
            }
        )
    } else {
        val user = currentUser
        if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppleBackground),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppleBlack, strokeWidth = 3.dp)
            }
        } else {
            // Handle Back Button Navigation
            BackHandler(enabled = (showAdminPanel || activeMatchId != null || currentRoomId != null || currentTab != MainTab.LOBBY)) {
                when {
                    showAdminPanel -> {
                        showAdminPanel = false
                    }
                    activeMatchId != null -> {
                        activeMatchId = null
                    }
                    currentRoomId != null -> {
                        coroutineScope.launch {
                            repository.leaveRoom(currentRoomId!!, user.id)
                            currentRoomId = null
                        }
                    }
                    currentTab != MainTab.LOBBY -> {
                        currentTab = MainTab.LOBBY
                    }
                }
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (!showAdminPanel && activeMatchId == null && currentRoomId == null) {
                        NavigationBar(
                            containerColor = AppleSurface,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = currentTab == MainTab.LOBBY,
                                onClick = { currentTab = MainTab.LOBBY },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Lobby") },
                                label = { Text("Lobby", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AppleBlack,
                                    selectedTextColor = AppleBlack,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                    indicatorColor = AppleLightGray
                                )
                            )
                            NavigationBarItem(
                                selected = currentTab == MainTab.LEADERBOARD,
                                onClick = { currentTab = MainTab.LEADERBOARD },
                                icon = { Icon(Icons.Default.Leaderboard, contentDescription = "Ranks") },
                                label = { Text("Leaderboard", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AppleBlack,
                                    selectedTextColor = AppleBlack,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                    indicatorColor = AppleLightGray
                                )
                            )
                            NavigationBarItem(
                                selected = currentTab == MainTab.SHOP,
                                onClick = { currentTab = MainTab.SHOP },
                                icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Shop") },
                                label = { Text("Shop", fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, softWrap = false) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AppleBlack,
                                    selectedTextColor = AppleBlack,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                    indicatorColor = AppleLightGray
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when {
                        // 1. Admin Control Panel
                        showAdminPanel && user.isAdmin -> {
                            AdminPanelScreen(
                                appConfig = appConfig,
                                allUsers = allAdminUsers,
                                allDeposits = allAdminDeposits,
                                onUpdateAppConfig = { updated ->
                                    coroutineScope.launch {
                                        repository.updateAppConfig(updated)
                                    }
                                },
                                onAdjustUserCoins = { targetUid, delta ->
                                    coroutineScope.launch {
                                        repository.adminAdjustUserCoins(targetUid, delta)
                                        Toast.makeText(context, "Wallet updated ($delta coins)", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onSetUserBan = { targetUid, isBanned, reason ->
                                    coroutineScope.launch {
                                        repository.adminSetUserBan(targetUid, isBanned, reason)
                                        Toast.makeText(context, if (isBanned) "User banned" else "User unbanned", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onApproveDeposit = { depositReq ->
                                    coroutineScope.launch {
                                        val res = repository.adminApproveDeposit(depositReq)
                                        res.onSuccess {
                                            Toast.makeText(context, "Approved! +${depositReq.coins} Coins credited to player", Toast.LENGTH_LONG).show()
                                        }.onFailure {
                                            Toast.makeText(context, it.message ?: "Failed to approve", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onRejectDeposit = { reqId, reason ->
                                    coroutineScope.launch {
                                        repository.adminRejectDeposit(reqId, reason)
                                        Toast.makeText(context, "Deposit request rejected", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onExitAdmin = { showAdminPanel = false }
                            )
                        }

                        // 2. Live Match Arena
                        activeMatchId != null && activeMatch != null -> {
                            MatchArenaScreen(
                                match = activeMatch!!,
                                currentUser = user,
                                onPickHidingBox = { boxNum ->
                                    coroutineScope.launch {
                                        repository.submitHiderChoice(activeMatch!!.id, boxNum)
                                    }
                                },
                                onMakeGuess = { boxNum ->
                                    coroutineScope.launch {
                                        repository.submitSeekerGuess(activeMatch!!, boxNum)
                                    }
                                },
                                onUseRadar = {
                                    coroutineScope.launch {
                                        val res = repository.useRadar(activeMatch!!, user.id)
                                        res.onFailure {
                                            Toast.makeText(context, it.message ?: "Failed to use Radar", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onUseExtraGuess = {
                                    coroutineScope.launch {
                                        val res = repository.useExtraGuessPass(activeMatch!!, user.id)
                                        res.onFailure {
                                            Toast.makeText(context, it.message ?: "Failed to use pass", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onReturnToLobby = {
                                    activeMatchId = null
                                    currentRoomId = null
                                }
                            )
                        }

                        // 3. Room Screen (Waiting with Friend / Code)
                        currentRoomId != null && activeRoom != null -> {
                            RoomScreen(
                                room = activeRoom!!,
                                currentUser = user,
                                chatMessages = matchChatMessages,
                                onSendMessage = { text, isEmote ->
                                    coroutineScope.launch {
                                        val matchTarget = activeMatchId ?: activeRoom!!.id
                                        repository.sendMessage(matchTarget, user.id, user.displayName, text, isEmote)
                                    }
                                },
                                onStartMatch = {
                                    coroutineScope.launch {
                                        val result = repository.startMatch(activeRoom!!)
                                        result.onSuccess { match ->
                                            activeMatchId = match.id
                                        }.onFailure { err ->
                                            Toast.makeText(context, err.message ?: "Cannot start match", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onLeaveRoom = {
                                    coroutineScope.launch {
                                        repository.leaveRoom(currentRoomId!!, user.id)
                                        currentRoomId = null
                                    }
                                }
                            )
                        }

                        // 4. Tab Screens
                        else -> {
                            when (currentTab) {
                                MainTab.LOBBY -> {
                                    HomeScreen(
                                        user = user,
                                        appConfig = appConfig,
                                        onCreateRoom = { wager ->
                                            if (user.coins < wager) {
                                                Toast.makeText(context, "Insufficient coins! You need $wager coins.", Toast.LENGTH_SHORT).show()
                                                return@HomeScreen
                                            }
                                            coroutineScope.launch {
                                                val room = repository.createRoom(user, wager)
                                                currentRoomId = room.id
                                            }
                                        },
                                        onJoinRoom = { code ->
                                            coroutineScope.launch {
                                                val result = repository.joinRoomByCode(code, user)
                                                result.onSuccess { room ->
                                                    currentRoomId = room.id
                                                }.onFailure { err ->
                                                    Toast.makeText(context, err.message ?: "Failed to join room", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        onQuickMatch = { wager ->
                                            if (user.coins < wager) {
                                                Toast.makeText(context, "Insufficient coins! You need $wager coins.", Toast.LENGTH_SHORT).show()
                                                return@HomeScreen
                                            }
                                            coroutineScope.launch {
                                                val result = repository.findQuickMatch(user, wager)
                                                result.onSuccess { room ->
                                                    currentRoomId = room.id
                                                }.onFailure { err ->
                                                    Toast.makeText(context, err.message ?: "Failed to find match", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        onClaimDailyReward = {
                                            coroutineScope.launch {
                                                val result = repository.claimDailyReward(user.id)
                                                result.onSuccess { bonus ->
                                                    Toast.makeText(context, "Claimed +$bonus Daily Coins", Toast.LENGTH_LONG).show()
                                                }.onFailure { err ->
                                                    Toast.makeText(context, err.message ?: "Reward not ready", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        onOpenTutorial = { showTutorial = true },
                                        onOpenAdminPanel = { showAdminPanel = true },
                                        onOpenDepositDialog = { showDepositDialog = true },
                                        onActivateAdmin = { secretKey ->
                                            coroutineScope.launch {
                                                val result = repository.verifyAndActivateAdmin(user.id, secretKey)
                                                result.onSuccess {
                                                    currentUser = currentUser?.copy(isAdmin = true)
                                                    showAdminPanel = true
                                                    Toast.makeText(context, "Admin verified! Welcome Admin.", Toast.LENGTH_SHORT).show()
                                                }.onFailure { err ->
                                                    Toast.makeText(context, err.message ?: "Verification failed", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                }
                                MainTab.LEADERBOARD -> {
                                    LeaderboardScreen(
                                        players = leaderboardPlayers,
                                        currentUser = user
                                    )
                                }
                                MainTab.SHOP -> {
                                    ShopScreen(
                                        user = user,
                                        onBuyItem = { item ->
                                            coroutineScope.launch {
                                                val res = repository.buyShopItem(user.id, item)
                                                res.onSuccess {
                                                    Toast.makeText(context, "Purchased ${item.name}", Toast.LENGTH_SHORT).show()
                                                }.onFailure { err ->
                                                    Toast.makeText(context, err.message ?: "Purchase failed", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        onEquipSkin = { skinId ->
                                            coroutineScope.launch {
                                                repository.setActiveSkin(user.id, skinId)
                                                Toast.makeText(context, "Skin equipped", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onOpenDepositDialog = { showDepositDialog = true }
                                    )
                                }
                            }
                        }
                    }

                    // Tutorial Modal
                    if (showTutorial) {
                        TutorialDialog(onDismiss = { showTutorial = false })
                    }

                    // Deposit / Add Coins Dialog
                    if (showDepositDialog) {
                        DepositDialog(
                            user = user,
                            appConfig = appConfig,
                            myRequests = myDepositRequests,
                            onSubmitRequest = { coins, inr, utr, proofUrl ->
                                coroutineScope.launch {
                                    repository.submitDepositRequest(
                                        userId = user.id,
                                        userName = user.displayName,
                                        userEmail = user.email,
                                        coins = coins,
                                        amountInr = inr,
                                        utrNumber = utr,
                                        screenshotUrl = proofUrl
                                    )
                                }
                            },
                            onDismiss = { showDepositDialog = false }
                        )
                    }

                    // Mandatory Update Dialog
                    if (isAppOutdated) {
                        AlertDialog(
                            onDismissRequest = {},
                            title = {
                                Text(text = appConfig.updateTitle, fontWeight = FontWeight.Bold, color = TextPrimary)
                            },
                            text = {
                                Text(text = appConfig.updateMessage, fontSize = 13.sp, color = TextSecondary)
                            },
                            confirmButton = {
                                CyberButton(
                                    text = "DOWNLOAD APK UPDATE",
                                    onClick = {
                                        try {
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(appConfig.latestApkUrl))
                                            context.startActivity(browserIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    isPrimary = true
                                )
                            },
                            containerColor = AppleSurface
                        )
                    }
                }
            }
        }
    }
}
