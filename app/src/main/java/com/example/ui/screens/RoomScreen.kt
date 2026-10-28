package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.Room
import com.example.data.model.User
import com.example.ui.components.CoinBadge
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
fun RoomScreen(
    room: Room,
    currentUser: User,
    chatMessages: List<ChatMessage>,
    onSendMessage: (text: String, isEmote: Boolean) -> Unit,
    onStartMatch: () -> Unit,
    onLeaveRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isHost = (currentUser.id == room.hostId)
    val hasGuest = (room.guestId != null)
    var chatInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(18.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onLeaveRoom,
                modifier = Modifier.testTag("leave_room_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Leave Room",
                    tint = TextPrimary
                )
            }

            Text(
                text = "MATCH ROOM",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                letterSpacing = 1.sp,
                color = TextPrimary,
                maxLines = 1,
                softWrap = false
            )

            CoinBadge(coins = room.wager * 2L)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Room Code Box
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SHARE THIS CODE WITH YOUR FRIEND",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppleSurfaceVariant)
                        .border(1.dp, AppleGlassBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("BoxHunter Room Code", room.code)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Room Code Copied: ${room.code}", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = room.code,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = AppleBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Wager per player: ${room.wager} Coins • Winner takes: ${room.wager * 2} Coins",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Player Versus Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Host Player
            PlayerSlot(
                name = room.hostName,
                isHost = true,
                isReady = true,
                modifier = Modifier.weight(1f)
            )

            // Guest Player
            if (hasGuest) {
                PlayerSlot(
                    name = room.guestName ?: "Challenger",
                    isHost = false,
                    isReady = true,
                    modifier = Modifier.weight(1f)
                )
            } else {
                WaitingSlot(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start Match Action (Host only)
        if (isHost) {
            CyberButton(
                text = if (hasGuest) "START MATCH (${room.wager * 2} POT)" else "WAITING FOR FRIEND TO JOIN...",
                onClick = onStartMatch,
                enabled = hasGuest,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = true,
                testTag = "start_match_button"
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppleSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder)
            ) {
                Text(
                    text = "Waiting for Host to start the battle...",
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Emotes Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Ready!", "Can't find me!", "Easy coins", "I'm coming!").forEach { emote ->
                Surface(
                    color = AppleSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
                    modifier = Modifier.clickable { onSendMessage(emote, true) }
                ) {
                    Text(
                        text = emote,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // In-Room Chat List
        CyberCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = "ROOM CHAT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    reverseLayout = true
                ) {
                    items(chatMessages.reversed()) { msg ->
                        ChatMessageItem(msg = msg, isMe = (msg.senderId == currentUser.id))
                    }
                }

                // Chat Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatInput,
                        onValueChange = { chatInput = it },
                        placeholder = { Text("Send message...", color = TextMuted, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleBlack,
                            unfocusedBorderColor = AppleGlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = AppleBlack
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("chat_input_field")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (chatInput.isNotBlank()) {
                                onSendMessage(chatInput.trim(), false)
                                chatInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AppleBlack)
                            .testTag("send_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = ApplePureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerSlot(
    name: String,
    isHost: Boolean,
    isReady: Boolean,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier,
        borderColor = if (isReady) AppleBlack else AppleGlassBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AppleSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = AppleBlack,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (isHost) "HOST" else "CHALLENGER",
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Ready",
                    tint = AppleBlack,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "READY",
                    fontSize = 10.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun WaitingSlot(modifier: Modifier = Modifier) {
    CyberCard(
        modifier = modifier,
        borderColor = AppleGlassBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = AppleBlack,
                modifier = Modifier.size(34.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Waiting...",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = "Share code above",
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun ChatMessageItem(msg: ChatMessage, isMe: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isMe) AppleBlack else AppleSurfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isMe) AppleBlack else AppleGlassBorder)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                if (!isMe) {
                    Text(
                        text = msg.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Text(
                    text = msg.text,
                    fontSize = 13.sp,
                    color = if (isMe) ApplePureWhite else TextPrimary
                )
            }
        }
    }
}
