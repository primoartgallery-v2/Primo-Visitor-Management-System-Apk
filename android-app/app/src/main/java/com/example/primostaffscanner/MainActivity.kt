package com.example.primostaffscanner

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primostaffscanner.data.api.PrimoApiClient
import com.example.primostaffscanner.data.model.UpdateInfo
import com.example.primostaffscanner.data.repository.PrimoRepository
import com.example.primostaffscanner.theme.PrimoBorder
import com.example.primostaffscanner.theme.PrimoCream
import com.example.primostaffscanner.theme.PrimoGold
import com.example.primostaffscanner.theme.PrimoInk
import com.example.primostaffscanner.theme.PrimoMuted
import com.example.primostaffscanner.theme.PrimoStaffScannerTheme
import com.example.primostaffscanner.theme.PrimoWine
import com.example.primostaffscanner.ui.components.ResultDialog
import com.example.primostaffscanner.ui.components.ScanResultData
import com.example.primostaffscanner.ui.components.UpdateDialog
import com.example.primostaffscanner.ui.screens.DashboardScreen
import com.example.primostaffscanner.ui.screens.ExhibitionManagementScreen
import com.example.primostaffscanner.ui.screens.HistoryScreen
import com.example.primostaffscanner.ui.screens.LoginScreen
import com.example.primostaffscanner.ui.screens.ManualSearchScreen
import com.example.primostaffscanner.ui.screens.ScannerScreen
import com.example.primostaffscanner.ui.screens.VisitorsScreen
import com.example.primostaffscanner.util.FeedbackManager
import com.example.primostaffscanner.util.OtaUpdateManager
import kotlinx.coroutines.launch

enum class AppTab {
    DASHBOARD,
    SCANNER,
    VISITORS,
    EXHIBITIONS
}

class MainActivity : ComponentActivity() {

    private lateinit var feedbackManager: FeedbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedbackManager = FeedbackManager(this)

        enableEdgeToEdge()
        setContent {
            PrimoStaffScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PrimoAppRoot(feedbackManager = feedbackManager)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        feedbackManager.release()
    }
}

@Composable
fun PrimoAppRoot(feedbackManager: FeedbackManager) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val otaManager = remember { OtaUpdateManager(context) }
    var availableUpdate by remember { mutableStateOf<UpdateInfo?>(null) }

    var isLoggedIn by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }

    val apiClient = remember { PrimoApiClient() }
    val repository = remember { PrimoRepository(apiClient) }

    var isManualSearchOpen by remember { mutableStateOf(false) }
    var isHistoryOpen by remember { mutableStateOf(false) }
    var pendingDialogResult by remember { mutableStateOf<ScanResultData?>(null) }

    // Check for OTA updates automatically on startup
    LaunchedEffect(Unit) {
        otaManager.checkForUpdate().onSuccess { update ->
            if (update != null) {
                availableUpdate = update
            }
        }
    }

    if (!isLoggedIn) {
        LoginScreen(
            correctPin = "2026",
            onLoginSuccess = { isLoggedIn = true }
        )
        return
    }

    // Main App with Bottom Navigation Bar
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(PrimoCream)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            when {
                isManualSearchOpen -> {
                    BackHandler { isManualSearchOpen = false }
                    ManualSearchScreen(
                        apiClient = apiClient,
                        onBackToScanner = { isManualSearchOpen = false },
                        onShowResult = { data ->
                            pendingDialogResult = data
                        }
                    )
                }

                isHistoryOpen -> {
                    BackHandler { isHistoryOpen = false }
                    HistoryScreen(
                        historyItems = repository.latestCheckIns,
                        onBack = { isHistoryOpen = false },
                        onClear = { repository.latestCheckIns.clear() }
                    )
                }

                currentTab == AppTab.DASHBOARD -> {
                    DashboardScreen(
                        repository = repository,
                        appVersion = otaManager.currentVersionName,
                        onCheckForUpdates = {
                            coroutineScope.launch {
                                val result = otaManager.checkForUpdate()
                                result.fold(
                                    onSuccess = { update ->
                                        if (update != null) {
                                            availableUpdate = update
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "You are using the latest version (v${otaManager.currentVersionName})",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(
                                            context,
                                            "Update check error: ${err.localizedMessage ?: "Connection error"}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        },
                        onOpenScanner = { currentTab = AppTab.SCANNER },
                        onLogout = { isLoggedIn = false }
                    )
                }

                currentTab == AppTab.SCANNER -> {
                    ScannerScreen(
                        apiClient = apiClient,
                        feedbackManager = feedbackManager,
                        onOpenManualSearch = { isManualSearchOpen = true },
                        onOpenHistory = { isHistoryOpen = true },
                        onLogout = { isLoggedIn = false },
                        onVisitorCheckedIn = { item ->
                            repository.latestCheckIns.add(0, item)
                        }
                    )
                }

                currentTab == AppTab.VISITORS -> {
                    VisitorsScreen(
                        repository = repository,
                        onOpenScanner = { currentTab = AppTab.SCANNER }
                    )
                }

                currentTab == AppTab.EXHIBITIONS -> {
                    ExhibitionManagementScreen(
                        repository = repository
                    )
                }
            }

            // Global Result Dialog if opened from manual search
            val dialogData = pendingDialogResult
            if (dialogData != null) {
                ResultDialog(
                    data = dialogData,
                    onDismiss = { pendingDialogResult = null },
                    onScanNext = {
                        pendingDialogResult = null
                        isManualSearchOpen = false
                        currentTab = AppTab.SCANNER
                    }
                )
            }

            // OTA Update Dialog Popup
            val update = availableUpdate
            if (update != null) {
                UpdateDialog(
                    updateInfo = update,
                    otaManager = otaManager,
                    onDismiss = { availableUpdate = null }
                )
            }
        }

        // Bottom Navigation Bar
        if (!isManualSearchOpen && !isHistoryOpen) {
            HorizontalDivider(color = PrimoBorder, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    icon = "📊",
                    label = "Dashboard",
                    isSelected = currentTab == AppTab.DASHBOARD,
                    onClick = { currentTab = AppTab.DASHBOARD }
                )

                BottomNavItem(
                    icon = "📷",
                    label = "Scanner",
                    isSelected = currentTab == AppTab.SCANNER,
                    onClick = { currentTab = AppTab.SCANNER }
                )

                BottomNavItem(
                    icon = "👥",
                    label = "Visitors",
                    isSelected = currentTab == AppTab.VISITORS,
                    onClick = { currentTab = AppTab.VISITORS }
                )

                BottomNavItem(
                    icon = "🏛️",
                    label = "Exhibitions",
                    isSelected = currentTab == AppTab.EXHIBITIONS,
                    onClick = { currentTab = AppTab.EXHIBITIONS }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PrimoWine else PrimoMuted
        )
    }
}
