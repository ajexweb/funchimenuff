package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppConfig
import com.example.data.model.User
import com.example.ui.components.CoinBadge
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.components.LevelBadge
import com.example.ui.theme.AppleBackground
import com.example.ui.theme.AppleBlack
import com.example.ui.theme.AppleGlassBorder
import com.example.ui.theme.ApplePureWhite
import com.example.ui.theme.AppleSurface
import com.example.ui.theme.AppleSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    user: User,
    appConfig: AppConfig,
    onCreateRoom: (wager: Long) -> Unit,
    onJoinRoom: (code: String) -> Unit,
    onQuickMatch: (wager: Long) -> Unit,
    onClaimDailyReward: () -> Unit,
    onOpenTutorial: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onOpenDepositDialog: () -> Unit,
    onActivateAdmin: (secretOrUid: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showAdminAuthDialog by remember { mutableStateOf(false) }
    var adminKeyInput by remember { mutableStateOf("") }
    var selectedWager by remember { mutableStateOf(25L) }
    var roomCodeInput by remember { mutableStateOf("") }

    val now = System.currentTimeMillis()
    val canClaimDaily = (now - user.lastDailyRewardTimestamp >= 24 * 60 * 60 * 1000L)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        // Top Player Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(AppleSurfaceVariant)
                        .border(1.dp, AppleGlassBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = AppleBlack,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = user.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    LevelBadge(level = user.level, xp = user.xp)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoinBadge(coins = user.coins)
                Spacer(modifier = Modifier.width(8.dp))
                // Add Coins Deposit Button
                Surface(
                    color = AppleBlack,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { onOpenDepositDialog() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Coins",
                            tint = ApplePureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Admin Access Button (Shown to Admin user or with unlock option for developer)
        if (user.isAdmin) {
            Spacer(modifier = Modifier.height(14.dp))
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = AppleBlack
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenAdminPanel() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin",
                            tint = AppleBlack,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ADMIN CONTROL PANEL",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "Manage users, wallet, updates & verify payments",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Surface(
                        color = AppleBlack,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "OPEN",
                            color = ApplePureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        } else {
            // Admin Verification / Unlock button for the Owner
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = AppleSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAdminAuthDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Admin Unlock",
                            tint = AppleBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ADMIN ACCESS (OWNER LOGIN)",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "Tap to verify UID or enter admin passkey",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    Surface(
                        color = AppleBlack,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "UNLOCK",
                            color = ApplePureWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // Global Broadcast Notice Banner (If enabled by Admin)
        if (appConfig.showNotice && appConfig.broadcastNotice.isNotBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = AppleSurfaceVariant,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Notice",
                        tint = AppleBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = appConfig.broadcastNotice,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }

        // Banned Notice Overlay (if player is banned by admin)
        if (user.isBanned) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = AppleSurfaceVariant,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, AppleBlack),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACCOUNT SUSPENDED",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your account has been restricted by administrator. Reason: ${user.banReason.ifBlank { "Anti-cheat detection or terms violation." }}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Reward Banner
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AppleGlassBorder
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (canClaimDaily) AppleBlack else AppleSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Daily Reward",
                            tint = if (canClaimDaily) ApplePureWhite else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Coin Bonus",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = if (canClaimDaily) "Ready to collect (+20-55 Coins)" else "Available tomorrow",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                if (canClaimDaily) {
                    CyberButton(
                        text = "CLAIM",
                        onClick = onClaimDailyReward,
                        isPrimary = true,
                        testTag = "claim_daily_button"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Match Hero Card
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AppleBlack
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppleBlack)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUICK MATCH 1v1",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = ApplePureWhite,
                            maxLines = 1,
                            softWrap = false
                        )
                        Surface(
                            color = AppleSurfaceVariant,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "25 WAGER",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Instantly pair with an opponent. Winner takes the full 50-coin pot.",
                        fontSize = 13.sp,
                        color = Color(0xFFD1D1D6),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    CyberButton(
                        text = "FIND OPPONENT NOW",
                        onClick = { onQuickMatch(25L) },
                        enabled = !user.isBanned,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.PlayArrow,
                        isPrimary = false,
                        testTag = "quick_match_button"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Play with Friends Section
        Text(
            text = "PLAY WITH FRIENDS",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TextSecondary,
            maxLines = 1,
            softWrap = false
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Create Room Card
            CyberCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !user.isBanned) { showCreateDialog = true }
                    .testTag("create_room_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppleSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Room",
                            tint = AppleBlack,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Create Room",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pick wager & invite",
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Join Room Card
            CyberCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !user.isBanned) { showJoinDialog = true }
                    .testTag("join_room_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppleSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "Join Room",
                            tint = AppleBlack,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Join Room",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter 6-digit code",
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Match Statistics Card
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "YOUR BATTLE RECORD",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(title = "Matches", value = "${user.matchesPlayed}")
                    StatItem(title = "Victories", value = "${user.matchesWon}")
                    val winRate = if (user.matchesPlayed > 0) ((user.matchesWon * 100) / user.matchesPlayed) else 0
                    StatItem(title = "Win Rate", value = "$winRate%")
                    StatItem(title = "Total XP", value = "${user.xp}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tutorial CTA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AppleSurface)
                .border(1.dp, AppleGlassBorder, RoundedCornerShape(16.dp))
                .clickable { onOpenTutorial() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.HelpOutline,
                contentDescription = "How to play",
                tint = AppleBlack,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "How to Play (10 Boxes & 3 Chances Rule)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                softWrap = false
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Create Room Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(text = "Choose Match Wager", fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, softWrap = false)
            },
            text = {
                Column {
                    Text(
                        text = "Both you and your opponent will put this amount into the pot. Winner takes all.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(10L, 25L, 50L, 100L).forEach { amount ->
                            val isSelected = (selectedWager == amount)
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) AppleBlack else AppleGlassBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedWager = amount },
                                color = if (isSelected) AppleBlack else AppleSurfaceVariant
                            ) {
                                Text(
                                    text = "$amount",
                                    color = if (isSelected) ApplePureWhite else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                CyberButton(
                    text = "CREATE (${selectedWager * 2} POT)",
                    onClick = {
                        showCreateDialog = false
                        onCreateRoom(selectedWager)
                    },
                    isPrimary = true,
                    testTag = "confirm_create_room"
                )
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = AppleSurface
        )
    }

    // Join Room Dialog
    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = {
                Text(text = "Enter Room Code", fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, softWrap = false)
            },
            text = {
                Column {
                    Text(
                        text = "Ask your friend for the 6-character room invite code:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = roomCodeInput,
                        onValueChange = { if (it.length <= 6) roomCodeInput = it.uppercase() },
                        singleLine = true,
                        placeholder = { Text("e.g. VAULT7", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleBlack,
                            unfocusedBorderColor = AppleGlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("room_code_input")
                    )
                }
            },
            confirmButton = {
                CyberButton(
                    text = "JOIN MATCH",
                    onClick = {
                        if (roomCodeInput.isNotBlank()) {
                            showJoinDialog = false
                            onJoinRoom(roomCodeInput)
                        }
                    },
                    enabled = roomCodeInput.length >= 4,
                    isPrimary = true,
                    testTag = "confirm_join_room"
                )
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = AppleSurface
        )
    }

    if (showAdminAuthDialog) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showAdminAuthDialog = false },
            title = {
                Text(
                    text = "ADMIN VERIFICATION",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Verify your Admin credentials using your Firebase UID or secret key:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = AppleSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "YOUR AUTH UID",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted
                                )
                                Text(
                                    text = user.id,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = {
                                    val clip = ClipData.newPlainText("Auth UID", user.id)
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(clip)
                                    Toast.makeText(context, "UID Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy UID",
                                    tint = AppleBlack,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adminKeyInput,
                        onValueChange = { adminKeyInput = it },
                        label = { Text("Admin Secret / UID (or leave empty to check your UID)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleBlack,
                            unfocusedBorderColor = AppleGlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                CyberButton(
                    text = "CLAIM ADMIN ACCESS",
                    onClick = {
                        val key = if (adminKeyInput.isNotBlank()) adminKeyInput else user.id
                        showAdminAuthDialog = false
                        onActivateAdmin(key)
                    },
                    isPrimary = true
                )
            },
            dismissButton = {
                TextButton(onClick = { showAdminAuthDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = AppleSurface
        )
    }
}

@Composable
private fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = TextPrimary,
            maxLines = 1,
            softWrap = false
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            color = TextMuted,
            maxLines = 1,
            softWrap = false
        )
    }
}
