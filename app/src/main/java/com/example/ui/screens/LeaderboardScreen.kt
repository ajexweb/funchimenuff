package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
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
fun LeaderboardScreen(
    players: List<User>,
    currentUser: User,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val sortedPlayers = remember(players, selectedTab) {
        if (selectedTab == 0) {
            players.sortedByDescending { it.coins }
        } else {
            players.sortedByDescending { it.matchesWon }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppleBackground)
            .padding(18.dp)
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GLOBAL RANKINGS",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = 1.sp,
                color = TextPrimary,
                maxLines = 1,
                softWrap = false
            )
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = AppleBlack,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs
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
                text = {
                    Text(
                        "TOP COINS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (selectedTab == 0) AppleBlack else TextMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "MOST WINS",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (selectedTab == 1) AppleBlack else TextMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Top 3 Podium
        if (sortedPlayers.isNotEmpty()) {
            PodiumRow(topPlayers = sortedPlayers.take(3), selectedTab = selectedTab)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Leaderboard List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(sortedPlayers) { index, player ->
                val isMe = (player.id == currentUser.id)
                LeaderboardRow(
                    rank = index + 1,
                    player = player,
                    isMe = isMe,
                    showCoins = (selectedTab == 0)
                )
            }
        }
    }
}

@Composable
private fun PodiumRow(
    topPlayers: List<User>,
    selectedTab: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd Place
        if (topPlayers.size >= 2) {
            PodiumItem(player = topPlayers[1], rank = 2, selectedTab = selectedTab)
        }
        // 1st Place
        if (topPlayers.isNotEmpty()) {
            PodiumItem(player = topPlayers[0], rank = 1, isWinner = true, selectedTab = selectedTab)
        }
        // 3rd Place
        if (topPlayers.size >= 3) {
            PodiumItem(player = topPlayers[2], rank = 3, selectedTab = selectedTab)
        }
    }
}

@Composable
private fun PodiumItem(
    player: User,
    rank: Int,
    isWinner: Boolean = false,
    selectedTab: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Icon(
            imageVector = Icons.Default.MilitaryTech,
            contentDescription = "Rank $rank",
            tint = AppleBlack,
            modifier = Modifier.size(if (isWinner) 28.dp else 22.dp)
        )
        Box(
            modifier = Modifier
                .size(if (isWinner) 54.dp else 44.dp)
                .clip(CircleShape)
                .background(if (isWinner) AppleBlack else AppleSurfaceVariant)
                .border(1.dp, AppleGlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = if (isWinner) ApplePureWhite else AppleBlack,
                modifier = Modifier.size(if (isWinner) 28.dp else 22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = player.displayName,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = TextPrimary,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (selectedTab == 0) "${player.coins} Coins" else "${player.matchesWon} Wins",
            fontSize = 10.sp,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    player: User,
    isMe: Boolean,
    showCoins: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isMe) AppleSurfaceVariant else AppleSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isMe) AppleBlack else AppleGlassBorder
        ),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = "#$rank",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    modifier = Modifier.width(32.dp),
                    maxLines = 1,
                    softWrap = false
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppleSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = AppleBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isMe) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = AppleBlack,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "YOU",
                                    color = ApplePureWhite,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                    Text(
                        text = "LVL ${player.level} • ${player.matchesPlayed} Matches",
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Stat Value
            if (showCoins) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Paid,
                        contentDescription = null,
                        tint = AppleBlack,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${player.coins}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = AppleBlack,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${player.matchesWon} W",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
