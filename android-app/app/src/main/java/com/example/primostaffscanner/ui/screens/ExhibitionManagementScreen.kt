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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primostaffscanner.data.model.Exhibition
import com.example.primostaffscanner.data.repository.PrimoRepository
import com.example.primostaffscanner.theme.PrimoBorder
import com.example.primostaffscanner.theme.PrimoCream
import com.example.primostaffscanner.theme.PrimoGold
import com.example.primostaffscanner.theme.PrimoGreen
import com.example.primostaffscanner.theme.PrimoInk
import com.example.primostaffscanner.theme.PrimoMuted
import com.example.primostaffscanner.theme.PrimoRed
import com.example.primostaffscanner.theme.PrimoWine

@Composable
fun ExhibitionManagementScreen(
    repository: PrimoRepository
) {
    val scrollState = rememberScrollState()

    var editingId by remember { mutableStateOf<String?>(null) }
    var exhibitionName by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var formMessage by remember { mutableStateOf<String?>(null) }

    fun clearForm() {
        editingId = null
        exhibitionName = ""
        venue = ""
        startDate = ""
        endDate = ""
        formMessage = null
    }

    fun startEdit(exhibition: Exhibition) {
        editingId = exhibition.id
        exhibitionName = exhibition.name
        venue = exhibition.venue
        startDate = exhibition.startDate
        endDate = exhibition.endDate
        formMessage = "Editing ${exhibition.code}"
    }

    fun handleSave() {
        if (exhibitionName.isBlank() || venue.isBlank()) {
            formMessage = "Please enter exhibition name and venue."
            return
        }

        repository.saveExhibition(
            id = editingId,
            name = exhibitionName.trim(),
            venue = venue.trim(),
            startDate = startDate.ifBlank { "19 Dec 2026" }.trim(),
            endDate = endDate.ifBlank { "23 Dec 2026" }.trim()
        )

        clearForm()
        formMessage = "Exhibition saved successfully!"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimoCream)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Card: Exhibition Management
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
                        text = "Exhibition Management",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = PrimoInk
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimoCream,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
                    ) {
                        Text(
                            text = "Admin",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimoMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = exhibitionName,
                    onValueChange = { exhibitionName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Exhibition name", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimoWine,
                        unfocusedBorderColor = PrimoBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Venue", fontSize = 13.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimoWine,
                        unfocusedBorderColor = PrimoBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("dd-mm-yyyy", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimoWine,
                            unfocusedBorderColor = PrimoBorder
                        )
                    )

                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("dd-mm-yyyy", fontSize = 12.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimoWine,
                            unfocusedBorderColor = PrimoBorder
                        )
                    )
                }

                if (formMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formMessage.orEmpty(),
                        fontSize = 11.sp,
                        color = PrimoWine,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { handleSave() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimoInk),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (editingId != null) "Update Exhibition" else "Save Exhibition",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { clearForm() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimoCream),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
                    ) {
                        Text(
                            text = "New",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimoInk
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Existing Exhibitions List
        Text(
            text = "Active & Configured Exhibitions",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = PrimoInk,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        repository.exhibitions.forEach { exhibition ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (repository.currentExhibition.id == exhibition.id) PrimoGold else PrimoBorder
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = exhibition.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimoWine,
                            modifier = Modifier.weight(1f)
                        )

                        if (repository.currentExhibition.id == exhibition.id) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PrimoGold.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimoGold)
                            ) {
                                Text(
                                    text = "Current",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimoGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${exhibition.code} · ${exhibition.venue} · ${exhibition.dateRangeText} · ${if (exhibition.isActive) "Active" else "Inactive"}",
                        fontSize = 11.sp,
                        color = PrimoMuted,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { startEdit(exhibition) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimoCream),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Edit", fontSize = 11.sp, color = PrimoInk, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = { repository.toggleExhibitionStatus(exhibition.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimoCream),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (exhibition.isActive) "Deactivate" else "Activate",
                                fontSize = 11.sp,
                                color = if (exhibition.isActive) PrimoRed else PrimoGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
