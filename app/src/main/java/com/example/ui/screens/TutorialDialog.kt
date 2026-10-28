package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberButton
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
fun TutorialDialog(
    onDismiss: () -> Unit
) {
    var stepIndex by remember { mutableIntStateOf(0) }

    val steps = listOf(
        TutorialStep(
            title = "1. EQUAL COIN WAGER",
            desc = "Both players wager an equal amount of coins (e.g. 25 + 25 = 50 Coins). All coins enter the protected match escrow pot.",
            icon = Icons.Default.Paid
        ),
        TutorialStep(
            title = "2. SECRET 10-BOX HIDING",
            desc = "The Hider secretly selects 1 of the 10 mystery vault boxes to conceal their avatar. The Seeker cannot see this pick.",
            icon = Icons.Default.Visibility
        ),
        TutorialStep(
            title = "3. 3 CHANCES TO FIND",
            desc = "The Seeker has exactly 3 attempts to find which box holds their friend. Use tactical power-ups like Radar if needed.",
            icon = Icons.Default.Bolt
        ),
        TutorialStep(
            title = "4. WINNER TAKES ALL",
            desc = "If found within 3 tries, the Seeker takes the entire pot! If all 3 guesses fail, the Hider takes all coins.",
            icon = Icons.Default.EmojiEvents
        )
    )

    val currentStep = steps[stepIndex]

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HOW TO PLAY",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "${stepIndex + 1} / 4",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(AppleBlack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentStep.icon,
                        contentDescription = null,
                        tint = ApplePureWhite,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = currentStep.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentStep.desc,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == stepIndex) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (index == stepIndex) AppleBlack else AppleLightGray)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (stepIndex < steps.size - 1) {
                CyberButton(
                    text = "NEXT",
                    onClick = { stepIndex++ },
                    isPrimary = true
                )
            } else {
                CyberButton(
                    text = "GOT IT",
                    onClick = onDismiss,
                    isPrimary = true
                )
            }
        },
        dismissButton = {
            if (stepIndex > 0) {
                TextButton(onClick = { stepIndex-- }) {
                    Text("BACK", color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = AppleSurface
    )
}

private data class TutorialStep(
    val title: String,
    val desc: String,
    val icon: ImageVector
)
