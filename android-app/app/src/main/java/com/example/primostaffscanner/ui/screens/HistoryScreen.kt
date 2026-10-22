package com.example.primostaffscanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primostaffscanner.data.model.ScanHistoryItem
import com.example.primostaffscanner.theme.PrimoAmber
import com.example.primostaffscanner.theme.PrimoBorder
import com.example.primostaffscanner.theme.PrimoCream
import com.example.primostaffscanner.theme.PrimoGreen
import com.example.primostaffscanner.theme.PrimoInk
import com.example.primostaffscanner.theme.PrimoMuted
import com.example.primostaffscanner.theme.PrimoRed
import com.example.primostaffscanner.theme.PrimoWine

@Composable
fun HistoryScreen(
    historyItems: List<ScanHistoryItem>,
    onBack: () -> Unit,
    onClear: () -> Unit
) {
    val totalCount = historyItems.size
    val successCount = historyItems.count { it.status.lowercase().contains("success") || it.status.lowercase().contains("checked in") }
    val alreadyCount = historyItems.count { it.status.lowercase().contains("already") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimoCream)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scan Activity",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimoWine
            )

            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimoWine)
            ) {
                Text(text = "Back")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Summary Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(label = "Total Scanned", value = totalCount.toString(), color = PrimoInk)
                StatItem(label = "Checked In", value = successCount.toString(), color = PrimoGreen)
                StatItem(label = "Already In", value = alreadyCount.toString(), color = PrimoAmber)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (historyItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No visitors scanned in this session yet.",
                    color = PrimoMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(historyItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.name.ifBlank { item.visitorId },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimoInk
                                )
                                Text(
                                    text = item.visitorId,
                                    fontSize = 12.sp,
                                    color = PrimoMuted
                                )
                                Text(
                                    text = item.timestamp,
                                    fontSize = 11.sp,
                                    color = PrimoMuted
                                )
                            }

                            val isSuccess = item.status.lowercase().contains("success") || item.status.lowercase().contains("checked in")
                            val isAlready = item.status.lowercase().contains("already")
                            val tagColor = when {
                                isSuccess -> PrimoGreen
                                isAlready -> PrimoAmber
                                else -> PrimoRed
                            }

                            Text(
                                text = if (isSuccess) "Checked In" else if (isAlready) "Already In" else "Invalid",
                                color = tagColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimoRed)
            ) {
                Text(text = "Clear History", color = PrimoRed)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = PrimoMuted)
    }
}
