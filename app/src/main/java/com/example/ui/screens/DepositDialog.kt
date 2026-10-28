package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppConfig
import com.example.data.model.DepositRequest
import com.example.data.model.User
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.theme.AppleBlack
import com.example.ui.theme.AppleGlassBorder
import com.example.ui.theme.ApplePureWhite
import com.example.ui.theme.AppleSurface
import com.example.ui.theme.AppleSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DepositDialog(
    user: User,
    appConfig: AppConfig,
    myRequests: List<DepositRequest>,
    onSubmitRequest: (coins: Long, amountInr: Long, utr: String, proofUrl: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedCoinAmount by remember { mutableStateOf(50L) }
    var utrInput by remember { mutableStateOf("") }
    var proofUrlInput by remember { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }

    val upiId = appConfig.upiId.ifBlank { "boxhunter@upi" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showHistory) "DEPOSIT HISTORY" else "ADD COINS (UPI / QR)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
                Surface(
                    color = AppleSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
                    modifier = Modifier.clickable { showHistory = !showHistory }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showHistory) "NEW" else "STATUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        },
        text = {
            if (showHistory) {
                // History List
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (myRequests.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No recent deposit requests.", color = TextMuted, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(myRequests) { req ->
                                Surface(
                                    color = AppleSurfaceVariant,
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "+${req.coins} Coins (Rs. ${req.amountInr})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Surface(
                                                color = when (req.status) {
                                                    "APPROVED" -> AppleBlack
                                                    "REJECTED" -> AppleSurface
                                                    else -> AppleSurface
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder)
                                            ) {
                                                Text(
                                                    text = req.status,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (req.status == "APPROVED") ApplePureWhite else TextPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "UTR: ${req.utrNumber}",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        if (req.rejectionReason.isNotBlank()) {
                                            Text(
                                                text = "Reason: ${req.rejectionReason}",
                                                fontSize = 11.sp,
                                                color = TextMuted,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Deposit Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // UPI ID & QR Card
                    Surface(
                        color = AppleSurfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppleGlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "OFFICIAL PAYMENT UPI ID",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppleSurface)
                                    .border(1.dp, AppleGlassBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", upiId))
                                        Toast.makeText(context, "UPI ID Copied: $upiId", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = upiId,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppleBlack, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "1 INR = 1 Game Coin • Verified by Admin",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "SELECT AMOUNT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(50L, 100L, 200L, 500L).forEach { coins ->
                            val isSelected = (selectedCoinAmount == coins)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) AppleBlack else AppleGlassBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedCoinAmount = coins },
                                color = if (isSelected) AppleBlack else AppleSurfaceVariant
                            ) {
                                Text(
                                    text = "${coins}C",
                                    color = if (isSelected) ApplePureWhite else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = utrInput,
                        onValueChange = { utrInput = it },
                        label = { Text("12-Digit UTR / Transaction No.") },
                        placeholder = { Text("e.g. 428910284729") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppleBlack,
                            unfocusedBorderColor = AppleGlassBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = proofUrlInput,
                        onValueChange = { proofUrlInput = it },
                        label = { Text("Screenshot URL (Optional / Supabase)") },
                        placeholder = { Text("https://...") },
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
            }
        },
        confirmButton = {
            if (!showHistory) {
                CyberButton(
                    text = "SUBMIT VERIFICATION",
                    onClick = {
                        if (utrInput.length < 6 && proofUrlInput.isBlank()) {
                            Toast.makeText(context, "Please enter a valid UTR number or screenshot link", Toast.LENGTH_SHORT).show()
                            return@CyberButton
                        }
                        onSubmitRequest(selectedCoinAmount, selectedCoinAmount, utrInput, proofUrlInput)
                        Toast.makeText(context, "Deposit request submitted! Admin will verify and credit coins.", Toast.LENGTH_LONG).show()
                        onDismiss()
                    },
                    isPrimary = true
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = TextSecondary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = AppleSurface
    )
}
