package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppConfig
import com.example.data.model.DepositRequest
import com.example.data.model.User
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.theme.AppleBackground
import com.example.ui.theme.AppleBlack
import com.example.ui.theme.AppleGlassBorder
import com.example.ui.theme.AppleLightGray
import com.example.ui.theme.ApplePureWhite
import com.example.ui.theme.AppleSurface
import com.example.ui.theme.AppleSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AdminPanelScreen(
    appConfig: AppConfig,
    allUsers: List<User>,
    allDeposits: List<DepositRequest>,
    onUpdateAppConfig: (AppConfig) -> Unit,
    onAdjustUserCoins: (userId: String, delta: Long) -> Unit,
    onSetUserBan: (userId: String, isBanned: Boolean, reason: String) -> Unit,
    onApproveDeposit: (DepositRequest) -> Unit,
    onRejectDeposit: (requestId: String, reason: String) -> Unit,
    onExitAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    // Reject Dialog State
    var rejectingRequestId by remember { mutableStateOf<String?>(null) }
    var rejectReasonInput by remember { mutableStateOf("Payment not credited or invalid UTR") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(18.dp)
    ) {
        // Admin Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onExitAdmin) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "ADMIN CONTROL PANEL",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Full system, updates & wallet management",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Surface(
                color = AppleBlack,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "MASTER",
                    color = ApplePureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Admin Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = AppleSurface,
            contentColor = AppleBlack,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = AppleBlack
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("USERS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (selectedTab == 0) AppleBlack else TextMuted, maxLines = 1, softWrap = false) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    val pendingCount = allDeposits.count { it.status == "PENDING" }
                    val label = if (pendingCount > 0) "PAYMENTS ($pendingCount)" else "PAYMENTS"
                    Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (selectedTab == 1) AppleBlack else TextMuted, maxLines = 1, softWrap = false)
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("APP SETTINGS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (selectedTab == 2) AppleBlack else TextMuted, maxLines = 1, softWrap = false) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> UsersManagementTab(
                users = allUsers,
                onAdjustCoins = onAdjustUserCoins,
                onSetBan = onSetUserBan
            )
            1 -> DepositsManagementTab(
                deposits = allDeposits,
                onApprove = onApproveDeposit,
                onPromptReject = { rejectingRequestId = it }
            )
            2 -> AppSettingsTab(
                config = appConfig,
                onSave = { updated ->
                    onUpdateAppConfig(updated)
                    Toast.makeText(context, "System configuration updated successfully", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Rejection Reason Dialog
    if (rejectingRequestId != null) {
        AlertDialog(
            onDismissRequest = { rejectingRequestId = null },
            title = {
                Text(text = "Reject Payment Proof", fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, softWrap = false)
            },
            text = {
                Column {
                    Text(
                        text = "Provide rejection reason for the player:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = rejectReasonInput,
                        onValueChange = { rejectReasonInput = it },
                        singleLine = false,
                        maxLines = 3,
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
                    text = "CONFIRM REJECT",
                    onClick = {
                        val id = rejectingRequestId
                        if (id != null) {
                            onRejectDeposit(id, rejectReasonInput)
                            rejectingRequestId = null
                        }
                    },
                    isPrimary = true
                )
            },
            dismissButton = {
                TextButton(onClick = { rejectingRequestId = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = AppleSurface
        )
    }
}

@Composable
private fun UsersManagementTab(
    users: List<User>,
    onAdjustCoins: (userId: String, delta: Long) -> Unit,
    onSetBan: (userId: String, isBanned: Boolean, reason: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter { it.displayName.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search users by name or email...", color = TextMuted, fontSize = 12.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppleBlack,
                unfocusedBorderColor = AppleGlassBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered) { user ->
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (user.isAdmin) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(color = AppleBlack, shape = RoundedCornerShape(4.dp)) {
                                            Text("ADMIN", color = ApplePureWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                    if (user.isBanned) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(color = AppleSurfaceVariant, shape = RoundedCornerShape(4.dp), border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder)) {
                                            Text("BANNED", color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                }
                                Text(
                                    text = user.email.ifBlank { "ID: ${user.id.take(8)}" },
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = "${user.coins} COINS",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls: Adjust Wallet & Ban/Unban
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = AppleSurfaceVariant,
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
                                    modifier = Modifier.clickable { onAdjustCoins(user.id, 50L) }
                                ) {
                                    Text("+50 Coins", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                                }
                                Surface(
                                    color = AppleSurfaceVariant,
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
                                    modifier = Modifier.clickable { onAdjustCoins(user.id, 200L) }
                                ) {
                                    Text("+200 Coins", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                                }
                                Surface(
                                    color = AppleSurfaceVariant,
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
                                    modifier = Modifier.clickable { onAdjustCoins(user.id, -50L) }
                                ) {
                                    Text("-50 Coins", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                                }
                            }

                            // Ban Toggle
                            Surface(
                                color = if (user.isBanned) AppleBlack else AppleSurfaceVariant,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
                                modifier = Modifier.clickable {
                                    onSetBan(user.id, !user.isBanned, if (!user.isBanned) "Violated terms / Anti-cheat" else "")
                                }
                            ) {
                                Text(
                                    text = if (user.isBanned) "UNBAN" else "BAN USER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (user.isBanned) ApplePureWhite else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DepositsManagementTab(
    deposits: List<DepositRequest>,
    onApprove: (DepositRequest) -> Unit,
    onPromptReject: (requestId: String) -> Unit
) {
    if (deposits.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No payment deposit requests yet.", color = TextMuted, fontSize = 13.sp)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(deposits) { req ->
            val isPending = (req.status == "PENDING")

            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = req.userName.ifBlank { "User ${req.userId.take(6)}" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = req.userEmail.ifBlank { "UTR: ${req.utrNumber}" },
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Surface(
                            color = when (req.status) {
                                "APPROVED" -> AppleBlack
                                "REJECTED" -> AppleSurfaceVariant
                                else -> AppleSurfaceVariant
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder)
                        ) {
                            Text(
                                text = req.status,
                                color = if (req.status == "APPROVED") ApplePureWhite else TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Payment details
                    Surface(
                        color = AppleSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Requested: ${req.coins} Coins (Rs. ${req.amountInr})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "12-Digit UTR: ${req.utrNumber.ifBlank { "None submitted" }}",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                            if (req.screenshotUrl.isNotBlank()) {
                                Text(
                                    text = "Screenshot/Proof: ${req.screenshotUrl}",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (req.rejectionReason.isNotBlank()) {
                                Text(
                                    text = "Rejection Reason: ${req.rejectionReason}",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    if (isPending) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CyberButton(
                                text = "APPROVE & CREDIT COINS",
                                onClick = { onApprove(req) },
                                modifier = Modifier.weight(1f),
                                isPrimary = true
                            )
                            CyberButton(
                                text = "REJECT",
                                onClick = { onPromptReject(req.id) },
                                modifier = Modifier.weight(0.6f),
                                isPrimary = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppSettingsTab(
    config: AppConfig,
    onSave: (AppConfig) -> Unit
) {
    var versionInput by remember { mutableStateOf(config.currentVersion) }
    var apkUrlInput by remember { mutableStateOf(config.latestApkUrl) }
    var forceUpdateToggle by remember { mutableStateOf(config.forceUpdate) }
    var noticeInput by remember { mutableStateOf(config.broadcastNotice) }
    var showNoticeToggle by remember { mutableStateOf(config.showNotice) }
    var upiIdInput by remember { mutableStateOf(config.upiId) }
    var qrImageUrlInput by remember { mutableStateOf(config.qrImageUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "APP UPDATES & APK DOWNLOAD",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = versionInput,
                    onValueChange = { versionInput = it },
                    label = { Text("Current App Version") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppleBlack, unfocusedBorderColor = AppleGlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apkUrlInput,
                    onValueChange = { apkUrlInput = it },
                    label = { Text("Latest APK Download Direct URL") },
                    placeholder = { Text("https://example.com/latest.apk") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppleBlack, unfocusedBorderColor = AppleGlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Force Mandatory Update", fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = forceUpdateToggle,
                        onCheckedChange = { forceUpdateToggle = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ApplePureWhite, checkedTrackColor = AppleBlack)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "GLOBAL NOTICE & BROADCAST",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = noticeInput,
                    onValueChange = { noticeInput = it },
                    label = { Text("Broadcast Message to All Users") },
                    placeholder = { Text("Server maintenance tonight at 11 PM...") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppleBlack, unfocusedBorderColor = AppleGlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Display Notice Banner on Home", fontSize = 13.sp, color = TextPrimary)
                    Switch(
                        checked = showNoticeToggle,
                        onCheckedChange = { showNoticeToggle = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ApplePureWhite, checkedTrackColor = AppleBlack)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PAYMENT UPI & QR CONFIGURATION",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = upiIdInput,
                    onValueChange = { upiIdInput = it },
                    label = { Text("Your Payment UPI ID") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppleBlack, unfocusedBorderColor = AppleGlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = qrImageUrlInput,
                    onValueChange = { qrImageUrlInput = it },
                    label = { Text("Payment QR Code Image URL (Supabase/Hosted)") },
                    placeholder = { Text("https://your-bucket.supabase.co/qr.png") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppleBlack, unfocusedBorderColor = AppleGlassBorder),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        CyberButton(
            text = "SAVE ALL CONFIGURATIONS",
            onClick = {
                val updated = config.copy(
                    currentVersion = versionInput.trim(),
                    latestApkUrl = apkUrlInput.trim(),
                    forceUpdate = forceUpdateToggle,
                    broadcastNotice = noticeInput.trim(),
                    showNotice = showNoticeToggle,
                    upiId = upiIdInput.trim(),
                    qrImageUrl = qrImageUrlInput.trim()
                )
                onSave(updated)
            },
            modifier = Modifier.fillMaxWidth(),
            isPrimary = true
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}
