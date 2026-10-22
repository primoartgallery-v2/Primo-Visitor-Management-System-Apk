package com.example.primostaffscanner.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.primostaffscanner.R
import com.example.primostaffscanner.theme.PrimoBorder
import com.example.primostaffscanner.theme.PrimoCream
import com.example.primostaffscanner.theme.PrimoGold
import com.example.primostaffscanner.theme.PrimoInk
import com.example.primostaffscanner.theme.PrimoMuted
import com.example.primostaffscanner.theme.PrimoRed
import com.example.primostaffscanner.theme.PrimoWine

@Composable
fun LoginScreen(
    correctPin: String = "2026",
    onLoginSuccess: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun handleDigitPress(digit: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            errorMessage = null

            if (newPin.length == 4) {
                if (newPin == correctPin) {
                    onLoginSuccess()
                } else {
                    errorMessage = "Incorrect PIN. Please try again."
                    enteredPin = ""
                }
            }
        }
    }

    fun handleBackspace() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    fun handleClear() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        enteredPin = ""
        errorMessage = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimoCream)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Primo Art Gallery Official Colorful Logo
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(2.5.dp, PrimoGold, CircleShape)
                    .shadow(elevation = 6.dp, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_primo),
                    contentDescription = "Primo Art Gallery Official Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "PRIMO ART GALLERY",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = PrimoWine,
                letterSpacing = 2.sp
            )

            Text(
                text = "DRISHTI — A Legacy of Masters",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Serif,
                color = PrimoGold,
                letterSpacing = 1.sp
            )

            Surface(
                shape = CircleShape,
                color = PrimoWine.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, PrimoGold.copy(alpha = 0.35f)),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "STAFF SCANNER ACCESS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimoWine,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Luxury Keypad Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, PrimoGold.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter Security PIN",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        color = PrimoInk
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Enter 4-digit PIN to open scanner",
                        fontSize = 12.sp,
                        color = PrimoMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // PIN Dots Indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 4) {
                            val isFilled = i < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) PrimoWine else PrimoCream)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isFilled) PrimoWine else PrimoGold.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isFilled) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(PrimoGold)
                                    )
                                }
                            }
                        }
                    }

                    // Error Message
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimoRed.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, PrimoRed.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text(
                                text = errorMessage.orEmpty(),
                                color = PrimoRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Keypad Grid
                    KeypadGrid(
                        onDigit = { handleDigitPress(it) },
                        onBackspace = { handleBackspace() },
                        onClear = { handleClear() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Venue Footer
            Text(
                text = "🏛️ Living Traditions Centre, Bikaner House, New Delhi",
                fontSize = 11.sp,
                color = PrimoMuted,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center
            )
            Text(
                text = "DRISHTI Exhibition • 19–23 December 2026",
                fontSize = 10.sp,
                color = PrimoGold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun KeypadGrid(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        for (row in rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (key in row) {
                    KeypadButton(
                        text = key,
                        onClick = {
                            when (key) {
                                "C" -> onClear()
                                "⌫" -> onBackspace()
                                else -> onDigit(key)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    val isSpecial = text == "C" || text == "⌫"

    Surface(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        shape = CircleShape,
        color = if (isSpecial) PrimoWine.copy(alpha = 0.08f) else Color(0xFFFAFAFA),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSpecial) PrimoWine.copy(alpha = 0.3f) else PrimoBorder
        ),
        shadowElevation = if (isSpecial) 0.dp else 1.5.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = if (isSpecial) 19.sp else 23.sp,
                fontWeight = if (isSpecial) FontWeight.Bold else FontWeight.SemiBold,
                fontFamily = if (isSpecial) FontFamily.Default else FontFamily.Serif,
                color = if (isSpecial) PrimoWine else PrimoInk
            )
        }
    }
}
