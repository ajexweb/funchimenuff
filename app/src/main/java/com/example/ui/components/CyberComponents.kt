package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleBlack
import com.example.ui.theme.AppleDarkGray
import com.example.ui.theme.AppleGlassBorder
import com.example.ui.theme.AppleGlassBorderStrong
import com.example.ui.theme.AppleLightGray
import com.example.ui.theme.ApplePureWhite
import com.example.ui.theme.AppleSurface
import com.example.ui.theme.AppleSurfaceVariant
import com.example.ui.theme.BoxActiveBlack
import com.example.ui.theme.BoxEmptyGray
import com.example.ui.theme.BoxGlassBase
import com.example.ui.theme.BoxGlassLid
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

fun triggerHapticFeedback(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
        }
    } catch (_: Exception) {}
}

@Composable
fun CoinBadge(
    coins: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(1.dp, AppleGlassBorder, RoundedCornerShape(24.dp)),
        color = AppleSurface,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(AppleBlack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Paid,
                    contentDescription = "Coins",
                    tint = ApplePureWhite,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = "$coins COINS",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun LevelBadge(
    level: Long,
    xp: Long,
    modifier: Modifier = Modifier
) {
    val progress = ((xp % 100) / 100f).coerceIn(0.08f, 1f)
    Surface(
        modifier = modifier
            .border(1.dp, AppleGlassBorder, RoundedCornerShape(12.dp)),
        color = AppleSurfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LVL $level",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(3.dp))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AppleLightGray)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(fraction = progress)
                        .background(AppleBlack)
                )
            }
        }
    }
}

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = AppleGlassBorder,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x0F000000)),
        colors = CardDefaults.cardColors(containerColor = AppleSurface),
        shape = RoundedCornerShape(20.dp)
    ) {
        content()
    }
}

@Composable
fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    testTag: String = "cyber_button"
) {
    val context = LocalContext.current
    val containerBg = when {
        !enabled -> AppleSurfaceVariant
        isPrimary -> AppleBlack
        else -> ApplePureWhite
    }
    val contentColor = when {
        !enabled -> TextMuted
        isPrimary -> ApplePureWhite
        else -> TextPrimary
    }
    val borderStroke = if (!isPrimary && enabled) AppleGlassBorderStrong else Color.Transparent

    Button(
        onClick = {
            triggerHapticFeedback(context)
            onClick()
        },
        enabled = enabled,
        modifier = modifier
            .height(50.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerBg,
            contentColor = contentColor,
            disabledContainerColor = AppleSurfaceVariant,
            disabledContentColor = TextMuted
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderStroke),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = contentColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

enum class BoxVisualState {
    LOCKED,
    HIDER_PICKED,
    EMPTY,
    FOUND,
    RADAR_SAFE
}

@Composable
fun InteractiveMysteryBox(
    boxNumber: Long,
    state: BoxVisualState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    skinId: String = "skin_cyber"
) {
    val context = LocalContext.current
    val scaleAnim by animateFloatAsState(
        targetValue = when (state) {
            BoxVisualState.FOUND -> 1.05f
            BoxVisualState.HIDER_PICKED -> 1.03f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "box_scale"
    )

    val (bgCol, borderCol, iconTint) = when (state) {
        BoxVisualState.LOCKED -> Triple(AppleSurface, AppleGlassBorder, AppleDarkGray)
        BoxVisualState.HIDER_PICKED -> Triple(AppleBlack, AppleBlack, ApplePureWhite)
        BoxVisualState.EMPTY -> Triple(AppleSurfaceVariant, AppleGlassBorder, TextMuted)
        BoxVisualState.FOUND -> Triple(AppleBlack, AppleBlack, ApplePureWhite)
        BoxVisualState.RADAR_SAFE -> Triple(AppleSurfaceVariant, AppleDarkGray, AppleBlack)
    }

    Box(
        modifier = modifier
            .scale(scaleAnim)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(bgCol)
            .border(
                width = if (state == BoxVisualState.HIDER_PICKED || state == BoxVisualState.FOUND) 2.dp else 1.dp,
                color = borderCol,
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(
                elevation = if (state == BoxVisualState.LOCKED) 2.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x0D000000)
            )
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                triggerHapticFeedback(context)
                onClick()
            }
            .testTag("box_$boxNumber"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (state) {
                BoxVisualState.LOCKED -> {
                    Text(
                        text = "$boxNumber",
                        color = AppleBlack,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                }
                BoxVisualState.HIDER_PICKED -> {
                    Text(
                        text = "$boxNumber",
                        color = ApplePureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Hidden",
                        tint = ApplePureWhite,
                        modifier = Modifier.size(14.dp)
                    )
                }
                BoxVisualState.EMPTY -> {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Empty",
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "EMPTY",
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                BoxVisualState.FOUND -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Found",
                        tint = ApplePureWhite,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "FOUND",
                        color = ApplePureWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                BoxVisualState.RADAR_SAFE -> {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "Clear",
                        tint = AppleBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "CLEAR",
                        color = AppleBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
