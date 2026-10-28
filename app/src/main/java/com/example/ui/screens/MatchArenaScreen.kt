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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.data.model.User
import com.example.ui.components.BoxVisualState
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.components.InteractiveMysteryBox
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
fun MatchArenaScreen(
    match: Match,
    currentUser: User,
    onPickHidingBox: (boxNumber: Long) -> Unit,
    onMakeGuess: (boxNumber: Long) -> Unit,
    onUseRadar: () -> Unit,
    onUseExtraGuess: () -> Unit,
    onReturnToLobby: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHider = (currentUser.id == match.hiderId)
    val isSeeker = (currentUser.id == match.seekerId)
    val isMatchFinished = (match.status == "SEEKER_WON" || match.status == "HIDER_WON" || match.status == "ABANDONED")
    val didIWin = (match.winnerId == currentUser.id)

    val remainingGuesses = (match.maxGuesses - match.guesses.size).coerceAtLeast(0L)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Arena Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (isHider) AppleBlack else AppleSurfaceVariant,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isHider) Icons.Default.Visibility else Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (isHider) ApplePureWhite else TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHider) "ROLE: HIDER" else "ROLE: SEEKER",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = if (isHider) ApplePureWhite else TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Coin Prize Pot Banner (Apple Luxury Monochrome)
            Surface(
                color = AppleSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Paid,
                        contentDescription = "Pot",
                        tint = AppleBlack,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${match.pot} COINS POT",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Phase Guidance Banner
        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (match.status) {
                    "HIDING" -> {
                        if (isHider) {
                            Text(
                                text = "SECRETLY TAP 1 OF THE 10 BOXES TO HIDE",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your friend won't see which box you pick",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        } else {
                            Text(
                                text = "FRIEND IS CHOOSING A HIDING BOX...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Get ready to hunt in 10 mystery vault boxes",
                                fontSize = 12.sp,
                                color = TextMuted,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    "SEEKING" -> {
                        if (isSeeker) {
                            Text(
                                text = "FIND YOUR FRIEND ($remainingGuesses/${match.maxGuesses} CHANCES LEFT)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Dot visualizer for chances
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                (1..match.maxGuesses).forEach { attemptIndex ->
                                    val isUsed = (attemptIndex <= match.guesses.size)
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isUsed) AppleLightGray else AppleBlack)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "FRIEND IS SEARCHING THE BOXES...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Attempts made: ${match.guesses.size}/${match.maxGuesses}. If all fail, you win!",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = if (didIWin) "VICTORY! YOU WON THE POT" else "MATCH RESOLVED",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Seeker Tactical Power-Ups Bar (Available to Seeker in SEEKING phase)
        if (isSeeker && match.status == "SEEKING") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Radar Power-up
                Surface(
                    color = AppleSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .then(
                                if (!match.seekerRadarUsed && currentUser.inventoryRadar > 0) {
                                    Modifier.clickable { onUseRadar() }
                                } else Modifier
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "Radar",
                            tint = if (!match.seekerRadarUsed && currentUser.inventoryRadar > 0) AppleBlack else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (match.seekerRadarUsed) "RADAR USED" else "RADAR (${currentUser.inventoryRadar})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!match.seekerRadarUsed && currentUser.inventoryRadar > 0) TextPrimary else TextMuted,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Extra Guess Pass Power-up
                Surface(
                    color = AppleSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .then(
                                if (match.maxGuesses < 4L && currentUser.inventoryExtraGuess > 0) {
                                    Modifier.clickable { onUseExtraGuess() }
                                } else Modifier
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Extra Chance",
                            tint = if (match.maxGuesses < 4L && currentUser.inventoryExtraGuess > 0) AppleBlack else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (match.maxGuesses >= 4L) "+1 ACTIVE" else "+1 CHANCE (${currentUser.inventoryExtraGuess})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (match.maxGuesses < 4L && currentUser.inventoryExtraGuess > 0) TextPrimary else TextMuted,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 10 Mystery Vault Boxes Grid (2 rows of 5 boxes)
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(10) { index ->
                val boxNum = (index + 1).toLong()

                val visualState = when {
                    isMatchFinished && boxNum == match.hiddenBox -> BoxVisualState.FOUND
                    match.guesses.contains(boxNum) && boxNum == match.hiddenBox -> BoxVisualState.FOUND
                    match.guesses.contains(boxNum) -> BoxVisualState.EMPTY
                    match.radarRevealedBoxes.contains(boxNum) -> BoxVisualState.RADAR_SAFE
                    isHider && match.hiddenBox == boxNum -> BoxVisualState.HIDER_PICKED
                    else -> BoxVisualState.LOCKED
                }

                val canTap = when {
                    isMatchFinished -> false
                    match.status == "HIDING" && isHider && match.hiddenBox == 0L -> true
                    match.status == "SEEKING" && isSeeker && !match.guesses.contains(boxNum) && remainingGuesses > 0 -> true
                    else -> false
                }

                InteractiveMysteryBox(
                    boxNumber = boxNum,
                    state = visualState,
                    enabled = canTap,
                    skinId = currentUser.activeBoxSkin,
                    onClick = {
                        if (match.status == "HIDING" && isHider) {
                            onPickHidingBox(boxNum)
                        } else if (match.status == "SEEKING" && isSeeker) {
                            onMakeGuess(boxNum)
                        }
                    }
                )
            }
        }

        // Live Match History Log Ticker
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppleSurface,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LOG: ",
                    color = AppleBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    softWrap = false
                )
                val logText = when {
                    match.guesses.isEmpty() -> "Match in progress. Awaiting box actions..."
                    match.status == "SEEKER_WON" -> "${match.seekerName} discovered the friend in Box #${match.hiddenBox}!"
                    match.status == "HIDER_WON" -> "All ${match.maxGuesses} guesses missed! ${match.hiderName} escaped!"
                    else -> "Box #${match.guesses.last()} was checked: EMPTY!"
                }
                Text(
                    text = logText,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // Victory / Defeat Modal
    if (isMatchFinished) {
        AlertDialog(
            onDismissRequest = onReturnToLobby,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (didIWin) Icons.Default.EmojiEvents else Icons.Default.SentimentDissatisfied,
                        contentDescription = null,
                        tint = AppleBlack,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (didIWin) "VICTORY" else "DEFEAT",
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = if (didIWin) {
                            "You outsmarted your opponent and claimed the entire ${match.pot} Coin pot (+35 XP)."
                        } else {
                            "Your friend won this round. You earned +15 XP for competing."
                        },
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = AppleSurfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Hiding Spot: Box #${match.hiddenBox}",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Guesses made: ${match.guesses.joinToString { "#$it" }}",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            },
            confirmButton = {
                CyberButton(
                    text = "RETURN TO LOBBY",
                    onClick = onReturnToLobby,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true,
                    testTag = "return_to_lobby_button"
                )
            },
            containerColor = AppleSurface
        )
    }
}
