package com.example.primostaffscanner.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primostaffscanner.R
import com.example.primostaffscanner.data.model.Exhibition
import com.example.primostaffscanner.data.repository.PrimoRepository
import com.example.primostaffscanner.theme.PrimoBorder
import com.example.primostaffscanner.theme.PrimoCardBg
import com.example.primostaffscanner.theme.PrimoCream
import com.example.primostaffscanner.theme.PrimoGold
import com.example.primostaffscanner.theme.PrimoGreen
import com.example.primostaffscanner.theme.PrimoInk
import com.example.primostaffscanner.theme.PrimoMuted
import com.example.primostaffscanner.theme.PrimoWine
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    repository: PrimoRepository,
    appVersion: String = "1.0",
    onCheckForUpdates: () -> Unit,
    onOpenScanner: () -> Unit,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedExhibition by remember { mutableStateOf(repository.currentExhibition) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimoCream)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Topbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo_primo),
                    contentDescription = "Primo Art Gallery Logo",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, PrimoGold, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Primo Art Gallery",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = PrimoWine
                    )
                    Text(
                        text = "VISITOR MANAGEMENT DASHBOARD",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimoMuted,
                        letterSpacing = 1.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live Data Pill
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(PrimoGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "LIVE DATA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimoInk
                        )
                    }
                }

                // Scan Visitor Button
                Button(
                    onClick = onOpenScanner,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimoInk),
                    shape = CircleShape,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Scan Visitor",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Current Exhibition Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Exhibition",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = PrimoInk
                    )

                    Text(
                        text = repository.currentExhibition.dateRangeText,
                        fontSize = 11.sp,
                        color = PrimoGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selector Box
                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDropdownExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        color = PrimoCream,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedExhibition.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimoInk,
                                modifier = Modifier.weight(1f)
                            )
                            Text(text = "▼", fontSize = 10.sp, color = PrimoMuted)
                        }
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        repository.exhibitions.forEach { exh ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = exh.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = exh.code, fontSize = 11.sp, color = PrimoMuted)
                                    }
                                },
                                onClick = {
                                    selectedExhibition = exh
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = repository.currentExhibition.venue,
                        fontSize = 11.sp,
                        color = PrimoMuted,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            repository.selectExhibition(selectedExhibition)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimoInk),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Set Current Exhibition",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4 Metric Cards (2x2 Grid)
        val metrics = repository.metrics

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "TOTAL VISITORS",
                value = metrics.totalVisitors.toString(),
                subtext = "Registered visitors"
            )

            MetricCard(
                modifier = Modifier.weight(1f),
                label = "CHECKED IN",
                value = metrics.checkedIn.toString(),
                subtext = "Visitors entered"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "PENDING",
                value = metrics.pending.toString(),
                subtext = "Not checked in"
            )

            MetricCard(
                modifier = Modifier.weight(1f),
                label = "CHECK-IN RATE",
                value = "${metrics.checkInRate}%",
                subtext = "Overall attendance"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Attendance Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Attendance",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = PrimoInk
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Check-in progress",
                        fontSize = 12.sp,
                        color = PrimoMuted,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${metrics.checkInRate}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimoInk
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (metrics.checkInRate / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PrimoGold,
                    trackColor = PrimoCream,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                repository.refreshData()
                            }
                        },
                        enabled = !repository.isRefreshing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A6055)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (repository.isRefreshing) "Refreshing…" else "Refresh",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onOpenScanner,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimoCream),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Open Scanner",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimoInk
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Latest Check-ins Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Latest Check-ins",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = PrimoInk
                    )

                    Text(
                        text = "Live",
                        fontSize = 11.sp,
                        color = PrimoGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val recents = repository.latestCheckIns
                if (recents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No check ins yet.",
                            fontSize = 13.sp,
                            color = PrimoMuted
                        )
                    }
                } else {
                    recents.take(5).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimoInk
                                )
                                Text(
                                    text = "${item.visitorId} · ${item.timestamp}",
                                    fontSize = 11.sp,
                                    color = PrimoMuted
                                )
                            }

                            Text(
                                text = "Checked In",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimoGreen
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer & Version with OTA Check
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Primo Art Gallery · v$appVersion · ",
                fontSize = 11.sp,
                color = PrimoMuted
            )

            Text(
                text = "Check for Updates 🚀",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PrimoWine,
                modifier = Modifier
                    .clickable { onCheckForUpdates() }
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtext: String
) {
    Card(
        modifier = modifier.height(140.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PrimoMuted,
                letterSpacing = 1.sp
            )

            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Serif,
                color = PrimoInk
            )

            Text(
                text = subtext,
                fontSize = 11.sp,
                color = PrimoMuted
            )
        }
    }
}
