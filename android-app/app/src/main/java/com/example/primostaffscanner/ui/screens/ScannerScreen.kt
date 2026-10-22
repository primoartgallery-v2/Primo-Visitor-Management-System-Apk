package com.example.primostaffscanner.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.primostaffscanner.data.api.PrimoApiClient
import com.example.primostaffscanner.data.model.ScanHistoryItem
import com.example.primostaffscanner.theme.PrimoBorder
import com.example.primostaffscanner.theme.PrimoCream
import com.example.primostaffscanner.theme.PrimoDarkWine
import com.example.primostaffscanner.theme.PrimoGold
import com.example.primostaffscanner.theme.PrimoGreen
import com.example.primostaffscanner.theme.PrimoInk
import com.example.primostaffscanner.theme.PrimoLightGold
import com.example.primostaffscanner.theme.PrimoMuted
import com.example.primostaffscanner.theme.PrimoWine
import com.example.primostaffscanner.ui.components.ResultDialog
import com.example.primostaffscanner.ui.components.ResultType
import com.example.primostaffscanner.ui.components.ScanResultData
import com.example.primostaffscanner.util.FeedbackManager
import com.example.primostaffscanner.util.QrCodeAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    apiClient: PrimoApiClient,
    feedbackManager: FeedbackManager,
    onOpenManualSearch: () -> Unit,
    onOpenHistory: () -> Unit,
    onLogout: () -> Unit,
    onVisitorCheckedIn: (ScanHistoryItem) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var isCheckingIn by remember { mutableStateOf(false) }
    var activeResultData by remember { mutableStateOf<ScanResultData?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val qrAnalyzer = remember {
        QrCodeAnalyzer { rawText, parsedId ->
            val visitorId = parsedId ?: rawText.trim()
            if (!isCheckingIn && activeResultData == null && visitorId.isNotBlank()) {
                coroutineScope.launch(Dispatchers.Main) {
                    isCheckingIn = true

                    val result = apiClient.checkIn(visitorId)
                    isCheckingIn = false

                    result.fold(
                        onSuccess = { response ->
                            val statusText = (response.status ?: "").lowercase()
                            val isSuccess = statusText == "success"
                            val isAlready = statusText.contains("already")

                            val resultType = when {
                                isSuccess -> {
                                    feedbackManager.playSuccess()
                                    ResultType.SUCCESS
                                }
                                isAlready -> {
                                    feedbackManager.playAlreadyChecked()
                                    ResultType.ALREADY_CHECKED
                                }
                                else -> {
                                    feedbackManager.playError()
                                    ResultType.INVALID
                                }
                            }

                            val timeNow = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                            val visitorName = response.visitor?.name ?: "Visitor"

                            onVisitorCheckedIn(
                                ScanHistoryItem(
                                    visitorId = visitorId,
                                    name = visitorName,
                                    status = response.status ?: "Scanned",
                                    timestamp = timeNow,
                                    checkInTime = response.visitor?.checkInTime
                                )
                            )

                            activeResultData = ScanResultData(
                                type = resultType,
                                visitorId = visitorId,
                                message = response.message ?: "Check-in completed.",
                                visitor = response.visitor
                            )
                        },
                        onFailure = { err ->
                            feedbackManager.playError()
                            activeResultData = ScanResultData(
                                type = ResultType.NETWORK_ERROR,
                                visitorId = visitorId,
                                message = "Network error: ${err.localizedMessage ?: "Failed to connect"}"
                            )
                        }
                    )
                }
            }
        }
    }

    // Toggle analyzer state based on dialog visibility
    LaunchedEffect(activeResultData, isCheckingIn) {
        qrAnalyzer.setScanningEnabled(activeResultData == null && !isCheckingIn)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, qrAnalyzer)
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraInstance = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            // Scanning Overlay
            ScannerOverlay(isScanning = activeResultData == null && !isCheckingIn)

        } else {
            // Permission Request Card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PrimoCream)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimoBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Camera Permission Required",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimoWine
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "To scan visitor QR passes, the app needs access to your device camera.",
                            fontSize = 13.sp,
                            color = PrimoMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimoWine),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Grant Permission", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Top Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Status Indicator Pill
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, PrimoGold.copy(alpha = 0.6f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isCheckingIn) PrimoGold else PrimoGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCheckingIn) "Checking in…" else "Scanner Active",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Quick Actions: Torch, Search, History, Logout
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Torch Toggle
                TopBarCircleButton(
                    text = if (isTorchOn) "⚡" else "💡",
                    onClick = {
                        val newState = !isTorchOn
                        isTorchOn = newState
                        cameraInstance?.cameraControl?.enableTorch(newState)
                    }
                )

                // Manual Search
                TopBarCircleButton(
                    text = "🔍",
                    onClick = onOpenManualSearch
                )

                // History
                TopBarCircleButton(
                    text = "📋",
                    onClick = onOpenHistory
                )

                // Logout
                TopBarCircleButton(
                    text = "✕",
                    onClick = onLogout
                )
            }
        }

        // Bottom Instruction Pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.72f))
                .border(1.dp, PrimoGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Align visitor pass QR code inside the frame",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Checking In Loader Overlay
        if (isCheckingIn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = PrimoWine,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Verifying pass with server…",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimoInk
                        )
                    }
                }
            }
        }

        // Result Dialog Popup
        val resultData = activeResultData
        if (resultData != null) {
            ResultDialog(
                data = resultData,
                onDismiss = { activeResultData = null },
                onScanNext = { activeResultData = null },
                onRetry = {
                    val retryId = resultData.visitorId
                    activeResultData = null
                    isCheckingIn = true
                    coroutineScope.launch {
                        val retryResult = apiClient.checkIn(retryId)
                        isCheckingIn = false
                        retryResult.fold(
                            onSuccess = { res ->
                                activeResultData = ScanResultData(
                                    type = if (res.status == "success") ResultType.SUCCESS else ResultType.ALREADY_CHECKED,
                                    visitorId = retryId,
                                    message = res.message ?: "Checked in.",
                                    visitor = res.visitor
                                )
                            },
                            onFailure = {
                                activeResultData = ScanResultData(
                                    type = ResultType.NETWORK_ERROR,
                                    visitorId = retryId,
                                    message = "Retry failed. Check your internet connection."
                                )
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun TopBarCircleButton(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.65f),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimoGold.copy(alpha = 0.5f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun ScannerOverlay(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Target Box
        Box(
            modifier = Modifier
                .size(260.dp)
                .border(2.dp, PrimoGold.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
        ) {
            // Animated Laser Line
            if (isScanning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .offset(y = laserPosition.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    PrimoLightGold,
                                    Color.White,
                                    PrimoLightGold,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}
