package com.example.primostaffscanner.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primostaffscanner.data.model.VisitorRecord
import com.example.primostaffscanner.data.repository.PrimoRepository
import com.example.primostaffscanner.theme.PrimoAmber
import com.example.primostaffscanner.theme.PrimoBorder
import com.example.primostaffscanner.theme.PrimoCream
import com.example.primostaffscanner.theme.PrimoGold
import com.example.primostaffscanner.theme.PrimoGreen
import com.example.primostaffscanner.theme.PrimoInk
import com.example.primostaffscanner.theme.PrimoMuted
import com.example.primostaffscanner.theme.PrimoWine
import kotlinx.coroutines.launch

@Composable
fun VisitorsScreen(
    repository: PrimoRepository,
    onOpenScanner: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var checkingInId by remember { mutableStateOf<String?>(null) }

    val filteredList = repository.visitors.filter { v ->
        val matchesQuery = if (searchQuery.isBlank()) true else {
            v.name.contains(searchQuery, ignoreCase = true) ||
            v.visitorId.contains(searchQuery, ignoreCase = true) ||
            v.phone.contains(searchQuery, ignoreCase = true) ||
            v.email.contains(searchQuery, ignoreCase = true) ||
            v.city.contains(searchQuery, ignoreCase = true)
        }

        val matchesFilter = when (selectedFilter) {
            "Checked In" -> v.isCheckedIn
            "Pending" -> !v.isCheckedIn
            else -> true
        }

        matchesQuery && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimoCream)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Visitors",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = PrimoWine
                )
                Text(
                    text = "Updated ${repository.lastUpdatedText}",
                    fontSize = 11.sp,
                    color = PrimoMuted
                )
            }

            Button(
                onClick = onOpenScanner,
                colors = ButtonDefaults.buttonColors(containerColor = PrimoInk),
                shape = CircleShape
            ) {
                Text(text = "Scan QR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search visitor name, phone, email or ID…", fontSize = 13.sp) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimoWine,
                unfocusedBorderColor = PrimoBorder
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Checked In", "Pending").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimoWine,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No visitors found.",
                    fontSize = 14.sp,
                    color = PrimoMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.visitorId }) { visitor ->
                    VisitorCardItem(
                        visitor = visitor,
                        isCheckingIn = checkingInId == visitor.visitorId,
                        onCheckIn = {
                            checkingInId = visitor.visitorId
                            coroutineScope.launch {
                                repository.checkInVisitor(visitor.visitorId)
                                checkingInId = null
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VisitorCardItem(
    visitor: VisitorRecord,
    isCheckingIn: Boolean,
    onCheckIn: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = visitor.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimoInk
                    )
                    Text(
                        text = "ID: ${visitor.visitorId}",
                        fontSize = 12.sp,
                        color = PrimoGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Status Badge
                Surface(
                    shape = CircleShape,
                    color = if (visitor.isCheckedIn) Color(0xFFE8F5E9) else Color(0xFFFFF8E1),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (visitor.isCheckedIn) PrimoGreen else PrimoAmber
                    )
                ) {
                    Text(
                        text = if (visitor.isCheckedIn) "Checked In" else "Pending",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (visitor.isCheckedIn) PrimoGreen else PrimoAmber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (visitor.email.isNotBlank() || visitor.phone.isNotBlank()) {
                Text(
                    text = listOf(visitor.phone, visitor.email, visitor.city)
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    fontSize = 11.sp,
                    color = PrimoMuted
                )
            }

            if (!visitor.isCheckedIn) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onCheckIn,
                    enabled = !isCheckingIn,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimoWine),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isCheckingIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Check In Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (!visitor.checkInTime.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Entered: ${visitor.checkInTime}",
                    fontSize = 11.sp,
                    color = PrimoGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
