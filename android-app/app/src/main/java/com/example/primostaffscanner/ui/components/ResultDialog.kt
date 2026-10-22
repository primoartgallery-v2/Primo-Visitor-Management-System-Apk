package com.example.primostaffscanner.ui.components

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.primostaffscanner.data.model.VisitorInfo
import com.example.primostaffscanner.theme.PrimoAmber
import com.example.primostaffscanner.theme.PrimoAmberBg
import com.example.primostaffscanner.theme.PrimoBorder
import com.example.primostaffscanner.theme.PrimoCream
import com.example.primostaffscanner.theme.PrimoDarkWine
import com.example.primostaffscanner.theme.PrimoGold
import com.example.primostaffscanner.theme.PrimoGreen
import com.example.primostaffscanner.theme.PrimoGreenBg
import com.example.primostaffscanner.theme.PrimoInk
import com.example.primostaffscanner.theme.PrimoMuted
import com.example.primostaffscanner.theme.PrimoRed
import com.example.primostaffscanner.theme.PrimoRedBg
import com.example.primostaffscanner.theme.PrimoWine

enum class ResultType {
    SUCCESS,
    ALREADY_CHECKED,
    INVALID,
    NETWORK_ERROR
}

data class ScanResultData(
    val type: ResultType,
    val visitorId: String,
    val message: String,
    val visitor: VisitorInfo? = null
)

@Composable
fun ResultDialog(
    data: ScanResultData,
    onDismiss: () -> Unit,
    onScanNext: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    val (iconText, iconColor, iconBg, titleText) = when (data.type) {
        ResultType.SUCCESS -> Quadruple("✓", PrimoGreen, PrimoGreenBg, "Visitor Checked In")
        ResultType.ALREADY_CHECKED -> Quadruple("!", PrimoAmber, PrimoAmberBg, "Already Checked In")
        ResultType.INVALID -> Quadruple("✕", PrimoRed, PrimoRedBg, "Invalid Visitor Pass")
        ResultType.NETWORK_ERROR -> Quadruple("⚠", PrimoRed, PrimoRedBg, "Connection Failed")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status Icon Circle
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(iconBg)
                        .border(2.dp, iconColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconText,
                        color = iconColor,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = titleText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimoInk,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = data.message,
                    fontSize = 13.sp,
                    color = PrimoMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Visitor Details Box (if available)
                val visitor = data.visitor
                if (visitor != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PrimoCream),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!visitor.name.isNullOrBlank()) {
                                DetailRow(label = "Name", value = visitor.name)
                            }

                            DetailRow(label = "Visitor ID", value = data.visitorId)

                            if (!visitor.city.isNullOrBlank()) {
                                DetailRow(label = "City", value = visitor.city)
                            }

                            if (!visitor.phone.isNullOrBlank()) {
                                DetailRow(label = "Phone", value = visitor.phone)
                            }

                            if (!visitor.checkInTime.isNullOrBlank()) {
                                DetailRow(label = "Check-in Time", value = visitor.checkInTime)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                } else if (data.visitorId.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimoCream)
                            .border(1.dp, PrimoBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "ID: ${data.visitorId}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimoWine
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Action Buttons
                Button(
                    onClick = onScanNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimoWine),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Scan Next Visitor",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (data.type == ResultType.NETWORK_ERROR && onRetry != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimoWine)
                    ) {
                        Text(text = "Retry Check-in", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = PrimoMuted,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = PrimoInk,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
