package com.example.primostaffscanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.primostaffscanner.data.model.UpdateInfo
import com.example.primostaffscanner.theme.PrimoBorder
import com.example.primostaffscanner.theme.PrimoCream
import com.example.primostaffscanner.theme.PrimoGold
import com.example.primostaffscanner.theme.PrimoGreen
import com.example.primostaffscanner.theme.PrimoInk
import com.example.primostaffscanner.theme.PrimoMuted
import com.example.primostaffscanner.theme.PrimoRed
import com.example.primostaffscanner.theme.PrimoWine
import com.example.primostaffscanner.util.OtaUpdateManager
import kotlinx.coroutines.launch

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    otaManager: OtaUpdateManager,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDownloaded by remember { mutableStateOf(false) }

    fun startDownload() {
        isDownloading = true
        errorMessage = null
        downloadProgress = 0f

        coroutineScope.launch {
            val result = otaManager.downloadApk(updateInfo.apkUrl) { progress ->
                downloadProgress = progress
            }

            isDownloading = false
            result.fold(
                onSuccess = { file ->
                    isDownloaded = true
                    otaManager.installApk(file)
                },
                onFailure = { err ->
                    errorMessage = "Download failed: ${err.localizedMessage ?: "Unknown error"}"
                }
            )
        }
    }

    Dialog(
        onDismissRequest = {
            if (!updateInfo.forceUpdate && !isDownloading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !updateInfo.forceUpdate && !isDownloading,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimoGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rocket Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrimoCream),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🚀", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "New Update Available!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimoWine,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimoCream,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
                    ) {
                        Text(
                            text = "v${otaManager.currentVersionName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = PrimoMuted,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(text = "→", fontSize = 12.sp, color = PrimoMuted)

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimoGold.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimoGold)
                    ) {
                        Text(
                            text = "v${updateInfo.versionName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimoWine,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Release Notes Card
                if (updateInfo.releaseNotes.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PrimoCream),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "What's New:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimoInk
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = updateInfo.releaseNotes,
                                fontSize = 12.sp,
                                color = PrimoMuted,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Progress Bar while downloading
                if (isDownloading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PrimoWine,
                            trackColor = PrimoCream
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Downloading update… ${(downloadProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            color = PrimoMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = PrimoRed,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Action Buttons
                if (!isDownloading) {
                    Button(
                        onClick = { startDownload() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimoWine),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isDownloaded) "Install Update" else "Update Now",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (!updateInfo.forceUpdate) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimoMuted)
                        ) {
                            Text(text = "Later", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
