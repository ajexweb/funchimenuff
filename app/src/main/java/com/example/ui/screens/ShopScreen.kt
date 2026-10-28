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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.ViewInAr
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShopItem
import com.example.data.model.User
import com.example.ui.components.CoinBadge
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.theme.AppleBackground
import com.example.ui.theme.AppleBlack
import com.example.ui.theme.AppleDarkGray
import com.example.ui.theme.AppleGlassBorder
import com.example.ui.theme.ApplePureWhite
import com.example.ui.theme.AppleSurface
import com.example.ui.theme.AppleSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ShopScreen(
    user: User,
    onBuyItem: (ShopItem) -> Unit,
    onEquipSkin: (String) -> Unit,
    onOpenDepositDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val boxSkins = remember {
        listOf(
            ShopItem(
                id = "skin_cyber",
                name = "Apple Matte Black",
                category = "SKIN",
                description = "Signature satin black finish with diamond-cut polished bevels.",
                priceCoins = 0L,
                previewColorHex = 0xFF000000,
                badge = "DEFAULT"
            ),
            ShopItem(
                id = "skin_gold",
                name = "Titanium Silver Vault",
                category = "SKIN",
                description = "Aerospace-grade brushed titanium with ultra-refined monochrome finish.",
                priceCoins = 100L,
                previewColorHex = 0xFF8E8E93,
                badge = "PREMIUM"
            ),
            ShopItem(
                id = "skin_void",
                name = "Ceramic Pure White",
                category = "SKIN",
                description = "Pristine zirconia ceramic casing with subtle glass reflection.",
                priceCoins = 150L,
                previewColorHex = 0xFFF2F2F7,
                badge = "LUXURY"
            ),
            ShopItem(
                id = "skin_neon_pink",
                name = "Dark Carbon Matrix",
                category = "SKIN",
                description = "High-density weave graphite carbon fiber structure.",
                priceCoins = 200L,
                previewColorHex = 0xFF1C1C1E,
                badge = "EXCLUSIVE"
            )
        )
    }

    val powerUps = remember {
        listOf(
            ShopItem(
                id = "powerup_radar",
                name = "Box Radar Scanner",
                category = "POWERUP",
                description = "Scans and reveals 2 guaranteed empty boxes when seeking, narrowing choices to 8.",
                priceCoins = 30L,
                previewColorHex = 0xFF000000,
                badge = "TACTICAL"
            ),
            ShopItem(
                id = "powerup_extra_guess",
                name = "4th Guess Emergency Pass",
                category = "POWERUP",
                description = "Grants a critical 4th attempt if your first 3 guesses fail during a seeking round.",
                priceCoins = 40L,
                previewColorHex = 0xFF000000,
                badge = "LIFE SAVER"
            )
        )
    }

    val coinPacks = remember {
        listOf(
            ShopItem(
                id = "pack_starter",
                name = "Daily Hunter Supply",
                category = "COINS",
                description = "Instant emergency reload of coins to get back in the arena.",
                priceCoins = 50L,
                previewColorHex = 0xFF000000,
                badge = "FREE"
            ),
            ShopItem(
                id = "pack_highroller",
                name = "High-Roller Vault Pack",
                category = "COINS",
                description = "Massive coin reload for hosting high-stakes 50 & 100 coin wager matches.",
                priceCoins = 250L,
                previewColorHex = 0xFF000000,
                badge = "BEST VALUE"
            )
        )
    }

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
            Column {
                Text(
                    text = "VAULT SHOP",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "Skins, Radar Power-Ups & Coin Packs",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
            }
            CoinBadge(coins = user.coins)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Inventory Status Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppleSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Radar, contentDescription = null, tint = AppleBlack, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Radars: ${user.inventoryRadar}", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = AppleBlack, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("4th Passes: ${user.inventoryExtraGuess}", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                text = { Text("BOX SKINS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (selectedTab == 0) AppleBlack else TextMuted, maxLines = 1, softWrap = false) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("POWER-UPS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (selectedTab == 1) AppleBlack else TextMuted, maxLines = 1, softWrap = false) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("COIN PACKS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (selectedTab == 2) AppleBlack else TextMuted, maxLines = 1, softWrap = false) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Items List
        val currentItems = when (selectedTab) {
            0 -> boxSkins
            1 -> powerUps
            else -> coinPacks
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (selectedTab == 2) {
                item {
                    CyberCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDepositDialog() },
                        borderColor = AppleBlack
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DEPOSIT COINS VIA UPI / QR",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "Scan QR or send via UPI & submit UTR for instant credit",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Surface(color = AppleBlack, shape = RoundedCornerShape(20.dp)) {
                                Text(
                                    text = "ADD COINS",
                                    color = ApplePureWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            items(currentItems) { item ->
                val isOwned = user.ownedSkins.contains(item.id)
                val isActive = (user.activeBoxSkin == item.id)

                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AppleSurfaceVariant)
                                    .border(1.dp, AppleGlassBorder, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (item.category == "SKIN") Icons.Default.ViewInAr else if (item.category == "POWERUP") Icons.Default.Radar else Icons.Default.Paid,
                                    contentDescription = item.name,
                                    tint = AppleBlack,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.badge.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = AppleSurfaceVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.badge,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = TextPrimary,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    lineHeight = 15.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Action Button
                        if (item.category == "SKIN") {
                            if (isActive) {
                                Surface(
                                    color = AppleSurfaceVariant,
                                    shape = RoundedCornerShape(20.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder)
                                ) {
                                    Text(
                                        text = "EQUIPPED",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            } else if (isOwned) {
                                CyberButton(
                                    text = "EQUIP",
                                    onClick = { onEquipSkin(item.id) },
                                    isPrimary = false,
                                    testTag = "equip_${item.id}"
                                )
                            } else {
                                CyberButton(
                                    text = "${item.priceCoins} COINS",
                                    onClick = { onBuyItem(item) },
                                    enabled = (user.coins >= item.priceCoins),
                                    isPrimary = true,
                                    testTag = "buy_${item.id}"
                                )
                            }
                        } else if (item.category == "POWERUP") {
                            CyberButton(
                                text = "${item.priceCoins} COINS",
                                onClick = { onBuyItem(item) },
                                enabled = (user.coins >= item.priceCoins),
                                isPrimary = true,
                                testTag = "buy_${item.id}"
                            )
                        } else {
                            // Free / Bonus Coin Pack
                            CyberButton(
                                text = "CLAIM +${item.priceCoins}",
                                onClick = { onBuyItem(item) },
                                isPrimary = true,
                                testTag = "claim_${item.id}"
                            )
                        }
                    }
                }
            }
        }
    }
}
