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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primostaffscanner.data.api.PrimoApiClient
import com.example.primostaffscanner.data.model.VisitorInfo
import com.example.primostaffscanner.theme.PrimoAmber
import com.example.primostaffscanner.theme.PrimoBorder
import com.example.primostaffscanner.theme.PrimoCream
import com.example.primostaffscanner.theme.PrimoDarkWine
import com.example.primostaffscanner.theme.PrimoGold
import com.example.primostaffscanner.theme.PrimoGreen
import com.example.primostaffscanner.theme.PrimoInk
import com.example.primostaffscanner.theme.PrimoMuted
import com.example.primostaffscanner.theme.PrimoRed
import com.example.primostaffscanner.theme.PrimoWine
import com.example.primostaffscanner.ui.components.ResultType
import com.example.primostaffscanner.ui.components.ScanResultData
import kotlinx.coroutines.launch

@Composable
fun ManualSearchScreen(
    apiClient: PrimoApiClient,
    onBackToScanner: () -> Unit,
    onShowResult: (ScanResultData) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var inputQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var lookupVisitor by remember { mutableStateOf<VisitorInfo?>(null) }
    var searchError by remember { mutableStateOf<String?>(null) }

    fun normalizeId(query: String): String {
        val trimmed = query.trim().uppercase()
        if (trimmed.startsWith("PAG-")) {
            return trimmed
        }
        // If staff typed just a number like "1" or "0001", format to PAG-2026-0001
        val num = trimmed.toIntOrNull()
        if (num != null) {
            return "PAG-2026-${num.toString().padStart(4, '0')}"
        }
        return trimmed
    }

    fun performLookup() {
        val visitorId = normalizeId(inputQuery)
        if (visitorId.isBlank()) {
            searchError = "Please enter a Visitor ID."
            return
        }

        isLoading = true
        searchError = null
        lookupVisitor = null

        coroutineScope.launch {
            val result = apiClient.lookup(visitorId)
            isLoading = false
            result.fold(
                onSuccess = { response ->
                    if (response.status == "success" && response.visitor != null) {
                        lookupVisitor = response.visitor
                    } else {
                        searchError = response.message ?: "Visitor ID not found."
                    }
                },
                onFailure = { err ->
                    searchError = "Connection error: ${err.localizedMessage}"
                }
            )
        }
    }

    fun performDirectCheckIn() {
        val visitorId = normalizeId(inputQuery)
        if (visitorId.isBlank()) {
            searchError = "Please enter a Visitor ID."
            return
        }

        isLoading = true
        searchError = null

        coroutineScope.launch {
            val result = apiClient.checkIn(visitorId)
            isLoading = false
            result.fold(
                onSuccess = { response ->
                    val statusText = (response.status ?: "").lowercase()
                    val type = when {
                        statusText == "success" -> ResultType.SUCCESS
                        statusText.contains("already") -> ResultType.ALREADY_CHECKED
                        else -> ResultType.INVALID
                    }
                    onShowResult(
                        ScanResultData(
                            type = type,
                            visitorId = visitorId,
                            message = response.message ?: "Checked in.",
                            visitor = response.visitor
                        )
                    )
                },
                onFailure = { err ->
                    onShowResult(
                        ScanResultData(
                            type = ResultType.NETWORK_ERROR,
                            visitorId = visitorId,
                            message = "Network error: ${err.localizedMessage}"
                        )
                    )
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimoCream)
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Manual Search",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimoWine
            )

            OutlinedButton(
                onClick = onBackToScanner,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimoWine)
            ) {
                Text(text = "Back to Camera")
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Enter Visitor ID (e.g. PAG-2026-0001 or just 1)",
            fontSize = 13.sp,
            color = PrimoMuted
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                OutlinedTextField(
                    value = inputQuery,
                    onValueChange = {
                        inputQuery = it
                        searchError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Visitor ID") },
                    placeholder = { Text("PAG-2026-0001") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimoWine,
                        unfocusedBorderColor = PrimoBorder
                    )
                )

                if (searchError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = searchError.orEmpty(),
                        color = PrimoRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { performLookup() },
                        enabled = !isLoading && inputQuery.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimoWine)
                    ) {
                        Text(text = "Lookup Info", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { performDirectCheckIn() },
                        enabled = !isLoading && inputQuery.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimoWine)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = "Check In", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Found Visitor Card
        val visitor = lookupVisitor
        if (visitor != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimoGold)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = visitor.name ?: "Unknown Visitor",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimoInk
                        )
                        val status = visitor.checkInStatus ?: "Pending"
                        val isChecked = status.lowercase().contains("checked")
                        Text(
                            text = status,
                            color = if (isChecked) PrimoGreen else PrimoAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "ID: ${visitor.visitorId ?: ""}", fontSize = 13.sp, color = PrimoMuted)
                    if (!visitor.city.isNullOrBlank()) {
                        Text(text = "City: ${visitor.city}", fontSize = 13.sp, color = PrimoMuted)
                    }
                    if (!visitor.phone.isNullOrBlank()) {
                        Text(text = "Phone: ${visitor.phone}", fontSize = 13.sp, color = PrimoMuted)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { performDirectCheckIn() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimoWine)
                    ) {
                        Text(text = "Confirm Check In", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
