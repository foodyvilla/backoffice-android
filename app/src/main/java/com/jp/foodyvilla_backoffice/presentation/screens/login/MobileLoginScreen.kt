package com.jp.foodyvilla_backoffice.presentation.screens.login


import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.jp.foodyvilla_backoffice.R
import com.jp.foodyvilla_backoffice.presentation.navigation.Screen
import com.jp.foodyvilla_backoffice.presentation.utils.UiState
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 1 — Welcome / Mobile Login Screen  (Restaurant Theme · Material 3)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MobileLoginScreen(
    loginViewModel: LoginViewModel,
    navController: NavController,
    onGetOtp: (String) -> Unit = {}
) {
    val mobileNumber = loginViewModel.phoneNumber.collectAsStateWithLifecycle().value
    val getOtpState = loginViewModel.getOtpState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    when (getOtpState) {
        is UiState.Error -> Toast.makeText(context, "Try Again After Sometime…", Toast.LENGTH_SHORT)
            .show()

        UiState.Loading -> Toast.makeText(context, "Sending OTP…", Toast.LENGTH_SHORT).show()
        is UiState.Success<*> -> navController.navigate(Screen.Otp)
        else -> {}
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Hero Banner ──────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    // ── ✦ LOGO AREA ───────────────────────────────────────────
                    // Replace the Icon inside ElevatedCard with your own Image():
                    //
                    //   Image(
                    //       painter = painterResource(R.drawable.ic_logo),
                    //       contentDescription = "FoodyVilla",
                    //       modifier = Modifier.size(72.dp)
                    //   )
                    //
                    // The ElevatedCard acts as the logo container/frame.
                    // ─────────────────────────────────────────────────────────
//                    ElevatedCard(
//                        modifier = Modifier.size(96.dp),
//                        shape = RoundedCornerShape(24.dp),
//                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp),
//                        colors = CardDefaults.elevatedCardColors(
//                            containerColor = MaterialTheme.colorScheme.primary
//                        )
//                    ) {
//                        Box(
//                            modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            // ↓↓↓ REPLACE THIS with your Image() ↓↓↓
//                            Icon(
//                                imageVector = Icons.Outlined.RestaurantMenu,
//                                contentDescription = "FoodyVilla Logo",
//                                tint = MaterialTheme.colorScheme.onPrimary,
//                                modifier = Modifier.size(48.dp)
//                            )
//                            // ↑↑↑ REPLACE THIS with your Image() ↑↑↑
//                        }
//                    }

//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = "FoodyVilla",
//                        style = MaterialTheme.typography.headlineMedium.copy(
//                            fontWeight = FontWeight.ExtraBold,
//                            letterSpacing = 1.sp
//                        ),
//                        color = MaterialTheme.colorScheme.onPrimaryContainer
//                    )
                    Icon(
                        painter = painterResource(R.drawable.logo_new),
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(200.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Food. Joy. Delivered.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                }
            }

            // ── Form Content ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            )
            {

                Text(
                    text = "Let's get you in! 👋",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enter your number to order delicious food",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── Phone Input Card ─────────────────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhoneAndroid,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Mobile Number",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = "🇮🇳  +91",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                },
                                modifier = Modifier.height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
//                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = Color.Transparent
                                )
                            )

                            OutlinedTextField(
                                value = mobileNumber,
                                onValueChange = {
                                    if (it.length <= 10 && it.all(Char::isDigit))
                                        loginViewModel.updatePhone(it)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                placeholder = {
                                    Text(
                                        "Enter 10-digit number",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                        }

                        AnimatedVisibility(visible = mobileNumber.isNotEmpty()) {
                            Text(
                                text = "${mobileNumber.length}/10",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (mobileNumber.length == 10)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Get OTP Button ───────────────────────────────────────────
                Button(
                    onClick = { onGetOtp(mobileNumber) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = mobileNumber.length == 10,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalDining,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Get OTP & Order Now",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.fillMaxHeight(.5f))

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "By continuing, you agree to our ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Terms",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        " & ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Privacy Policy",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// SCREEN 2 — OTP Verification Screen  (Restaurant Theme · Material 3)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OtpVerificationScreen(
    maskedPhone: String = "+91 ***** *****",
    loginViewModel: LoginViewModel,
    navController: NavController,
    onVerify: (String) -> Unit = {},
    onResendOtp: () -> Unit = {}
) {
    val otpLength = 6
    var otp by remember { mutableStateOf("") }
    val focusRequesters = remember { List(otpLength) { FocusRequester() } }
    val loginState = loginViewModel.loginUiState.collectAsStateWithLifecycle().value

    var countdown by remember { mutableIntStateOf(54) }
    var timerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (countdown > 0) {
                delay(1000L); countdown--
            }
            timerRunning = false
        }
    }

    LaunchedEffect(Unit) { focusRequesters[0].requestFocus() }

    val context = LocalContext.current
    when (loginState) {
        is UiState.Error -> Toast.makeText(context, "Try Again After Sometime…", Toast.LENGTH_SHORT)
            .show()

        UiState.Loading -> Toast.makeText(context, "Verifying…", Toast.LENGTH_SHORT).show()
        is UiState.Success<*> -> navController.navigate(Screen.Home)
        else -> {}
    }

    val timerProgress = countdown / 54f

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Hero Banner ──────────────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    // ── ✦ LOGO AREA ───────────────────────────────────────────
                    // Replace the Icon inside ElevatedCard with your own Image():
                    //
                    //   Image(
                    //       painter = painterResource(R.drawable.ic_logo),
                    //       contentDescription = "FoodyVilla",
                    //       modifier = Modifier.size(56.dp)
                    //   )
                    // ─────────────────────────────────────────────────────────
                    ElevatedCard(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // ↓↓↓ REPLACE THIS with your Image() ↓↓↓
                            Icon(
                                imageVector = Icons.Outlined.RestaurantMenu,
                                contentDescription = "FoodyVilla Logo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                            // ↑↑↑ REPLACE THIS with your Image() ↑↑↑
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Almost there! 🍽️",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "Code sent to $maskedPhone",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.PhoneAndroid,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        shape = RoundedCornerShape(50),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = Color.Transparent
                        )
                    )
                }
            }

            // ── Form Content ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "Enter the 6-digit OTP",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your food is just one step away",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // ── OTP Boxes ────────────────────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(otpLength) { index ->
                        val char = otp.getOrNull(index)?.toString() ?: ""
                        val isFilled = char.isNotEmpty()

                        BasicTextField(
                            value = char,
                            onValueChange = { input ->
                                if (input.length <= 1 && input.all { it.isDigit() }) {
                                    val newOtp = StringBuilder(otp)
                                    if (input.isNotEmpty()) {
                                        if (otp.length > index) newOtp.setCharAt(index, input[0])
                                        else newOtp.insert(index, input)
                                    } else if (otp.length > index) {
                                        newOtp.deleteCharAt(index)
                                    }
                                    otp = newOtp.toString()
                                    if (input.isNotEmpty() && index < otpLength - 1)
                                        focusRequesters[index + 1].requestFocus()
                                    if (input.isEmpty() && index > 0)
                                        focusRequesters[index - 1].requestFocus()
                                    if (otp.length == otpLength) onVerify(otp)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.85f)
                                .focusRequester(focusRequesters[index])
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    color = if (isFilled)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .border(
                                    width = if (isFilled) 2.dp else 1.dp,
                                    color = if (isFilled)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.Center) {
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { otp.length / otpLength.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── Verify Button ────────────────────────────────────────────
                Button(
                    onClick = { onVerify(otp) },
                    enabled = otp.length == otpLength,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Verify & Start Ordering",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Timer Card ───────────────────────────────────────────────
                AnimatedVisibility(
                    visible = timerRunning,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { timerProgress },
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.error,
                                    trackColor = MaterialTheme.colorScheme.errorContainer
                                )
                                Text(
                                    text = "Resend available in",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Text(
                                text = String.format("%02d:%02d", countdown / 60, countdown % 60),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                FilledTonalButton(
                    onClick = {
                        if (!timerRunning) {
                            onResendOtp()
                            countdown = 54
                            timerRunning = true
                            otp = ""
                            focusRequesters[0].requestFocus()
                        }
                    },
                    enabled = !timerRunning,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Resend OTP",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}


//import android.widget.Toast
//import androidx.compose.animation.*
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowForward
//import androidx.compose.material.icons.outlined.PhoneAndroid
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextDecoration
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.navigation.NavController
//import com.jp.foodyvilla_backoffice.presentation.navigation.Screen
//import com.jp.foodyvilla_backoffice.presentation.utils.UiState
//import kotlinx.coroutines.delay
//
//// ─────────────────────────────────────────────────────────────────────────────
//// SCREEN 1 — Welcome / Mobile Login Screen  (Material 3 Enhanced)
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Composable
//fun MobileLoginScreen(
//    loginViewModel: LoginViewModel,
//    navController: NavController,
//    onGetOtp: (String) -> Unit = {}
//) {
//    val mobileNumber = loginViewModel.phoneNumber.collectAsStateWithLifecycle().value
//    val getOtpState = loginViewModel.getOtpState.collectAsStateWithLifecycle().value
//    val context = LocalContext.current
//
//    when (getOtpState) {
//        is UiState.Error -> Toast.makeText(context, "Try Again After Sometime…", Toast.LENGTH_SHORT).show()
//        UiState.Loading -> Toast.makeText(context, "Sending OTP…", Toast.LENGTH_SHORT).show()
//        is UiState.Success<*> -> navController.navigate(Screen.Otp)
//        else -> {}
//    }
//
//    Scaffold { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(horizontal = 28.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//
//            // ── ✦ LOGO AREA ──────────────────────────────────────────────────
//            // Replace the Box below with your actual logo Image() composable.
//            // e.g: Image(painter = painterResource(R.drawable.ic_logo), …)
//            // ────────────────────────────────────────────────────────────────
//            ElevatedCard(
//                modifier = Modifier.size(110.dp),
//                shape = RoundedCornerShape(28.dp),
//                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
//                colors = CardDefaults.elevatedCardColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer
//                )
//            ) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    // ↓↓↓ REPLACE THIS with your Image() ↓↓↓
//                    Icon(
//                        imageVector = Icons.Outlined.PhoneAndroid,
//                        contentDescription = "App Logo",
//                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
//                        modifier = Modifier.size(48.dp)
//                    )
//                    // ↑↑↑ REPLACE THIS with your Image() ↑↑↑
//                }
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            // ── Heading ──────────────────────────────────────────────────────
//            Text(
//                text = "Welcome Back!",
//                style = MaterialTheme.typography.displaySmall.copy(
//                    fontWeight = FontWeight.ExtraBold
//                ),
//                color = MaterialTheme.colorScheme.onBackground
//            )
//
//            Spacer(modifier = Modifier.height(6.dp))
//
//            Text(
//                text = "Enter your mobile number to continue",
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(modifier = Modifier.height(48.dp))
//
//            // ── Phone Input Card ─────────────────────────────────────────────
//            ElevatedCard(
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(20.dp),
//                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
//                colors = CardDefaults.elevatedCardColors(
//                    containerColor = MaterialTheme.colorScheme.surface
//                )
//            ) {
//                Column(modifier = Modifier.padding(20.dp)) {
//                    Text(
//                        text = "Mobile Number",
//                        style = MaterialTheme.typography.labelLarge,
//                        color = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.padding(bottom = 12.dp)
//                    )
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        // Country Code Chip
//                        SuggestionChip(
//                            onClick = {},
//                            label = {
//                                Text(
//                                    text = "🇮🇳 +91",
//                                    style = MaterialTheme.typography.bodyLarge.copy(
//                                        fontWeight = FontWeight.Bold
//                                    )
//                                )
//                            },
//                            modifier = Modifier.height(56.dp),
//                            shape = RoundedCornerShape(12.dp),
//                            colors = SuggestionChipDefaults.suggestionChipColors(
//                                containerColor = MaterialTheme.colorScheme.secondaryContainer
//                            ),
//                            border = SuggestionChipDefaults.suggestionChipBorder(
//                                enabled = true,
//                                borderColor = Color.Transparent
//                            )
//                        )
//
//                        // Number Field
//                        OutlinedTextField(
//                            value = mobileNumber,
//                            onValueChange = {
//                                if (it.length <= 10 && it.all(Char::isDigit))
//                                    loginViewModel.updatePhone(it)
//                            },
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(56.dp),
//                            placeholder = {
//                                Text(
//                                    "Enter 10-digit number",
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    color = MaterialTheme.colorScheme.outline
//                                )
//                            },
//                            singleLine = true,
//                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
//                            shape = RoundedCornerShape(12.dp),
//                            colors = OutlinedTextFieldDefaults.colors(
//                                focusedBorderColor = MaterialTheme.colorScheme.primary,
//                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
//                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
//                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
//                            )
//                        )
//                    }
//
//                    // Character counter
//                    AnimatedVisibility(visible = mobileNumber.isNotEmpty()) {
//                        Text(
//                            text = "${mobileNumber.length}/10",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = if (mobileNumber.length == 10)
//                                MaterialTheme.colorScheme.primary
//                            else
//                                MaterialTheme.colorScheme.onSurfaceVariant,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(top = 6.dp),
//                            textAlign = TextAlign.End
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(28.dp))
//
//            // ── Get OTP Button ───────────────────────────────────────────────
//            Button(
//                onClick = { onGetOtp(mobileNumber) },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                enabled = mobileNumber.length == 10,
//                shape = RoundedCornerShape(16.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.primary,
//                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
//                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
//                ),
//                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                    contentDescription = null,
//                    modifier = Modifier.size(20.dp)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = "Get OTP",
//                    style = MaterialTheme.typography.titleMedium.copy(
//                        fontWeight = FontWeight.Bold
//                    )
//                )
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            // ── Divider with OR ──────────────────────────────────────────────
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                HorizontalDivider(modifier = Modifier.weight(1f))
//                Text(
//                    text = "  Terms apply  ",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//                HorizontalDivider(modifier = Modifier.weight(1f))
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // ── Terms ────────────────────────────────────────────────────────
//            Row(
//                horizontalArrangement = Arrangement.Center,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text(
//                    "By continuing, you agree to our ",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//                Text(
//                    "Terms",
//                    style = MaterialTheme.typography.bodySmall.copy(
//                        fontWeight = FontWeight.SemiBold,
//                        textDecoration = TextDecoration.Underline
//                    ),
//                    color = MaterialTheme.colorScheme.primary
//                )
//                Text(
//                    " & ",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//                Text(
//                    "Privacy Policy",
//                    style = MaterialTheme.typography.bodySmall.copy(
//                        fontWeight = FontWeight.SemiBold,
//                        textDecoration = TextDecoration.Underline
//                    ),
//                    color = MaterialTheme.colorScheme.primary
//                )
//            }
//        }
//    }
//}
//
//
//// ─────────────────────────────────────────────────────────────────────────────
//// SCREEN 2 — OTP Verification Screen  (Material 3 Enhanced)
//// ─────────────────────────────────────────────────────────────────────────────
//
//@Composable
//fun OtpVerificationScreen(
//    maskedPhone: String = "+91 ***** *****",
//    loginViewModel: LoginViewModel,
//    navController: NavController,
//    onVerify: (String) -> Unit = {},
//    onResendOtp: () -> Unit = {}
//) {
//    val otpLength = 6
//    var otp by remember { mutableStateOf("") }
//    val focusRequesters = remember { List(otpLength) { FocusRequester() } }
//    val loginState = loginViewModel.loginUiState.collectAsStateWithLifecycle().value
//
//    var countdown by remember { mutableIntStateOf(54) }
//    var timerRunning by remember { mutableStateOf(true) }
//
//    LaunchedEffect(timerRunning) {
//        if (timerRunning) {
//            while (countdown > 0) {
//                delay(1000L)
//                countdown--
//            }
//            timerRunning = false
//        }
//    }
//
//    LaunchedEffect(Unit) { focusRequesters[0].requestFocus() }
//
//    val context = LocalContext.current
//    when (loginState) {
//        is UiState.Error -> Toast.makeText(context, "Try Again After Sometime…", Toast.LENGTH_SHORT).show()
//        UiState.Loading -> Toast.makeText(context, "Verifying…", Toast.LENGTH_SHORT).show()
//        is UiState.Success<*> -> navController.navigate(Screen.Home)
//        else -> {}
//    }
//
//    val timerProgress = countdown / 54f
//
//    Scaffold { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .padding(horizontal = 28.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//
//            // ── ✦ LOGO AREA ──────────────────────────────────────────────────
//            // Same logo as login screen for brand consistency.
//            // Replace Icon with your Image() composable.
//            // ────────────────────────────────────────────────────────────────
//            ElevatedCard(
//                modifier = Modifier.size(110.dp),
//                shape = RoundedCornerShape(28.dp),
//                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
//                colors = CardDefaults.elevatedCardColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer
//                )
//            ) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    // ↓↓↓ REPLACE THIS with your Image() ↓↓↓
//                    Icon(
//                        imageVector = Icons.Outlined.PhoneAndroid,
//                        contentDescription = "App Logo",
//                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
//                        modifier = Modifier.size(48.dp)
//                    )
//                    // ↑↑↑ REPLACE THIS with your Image() ↑↑↑
//                }
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            // ── Title ────────────────────────────────────────────────────────
//            Text(
//                text = "Verify your number",
//                style = MaterialTheme.typography.headlineMedium.copy(
//                    fontWeight = FontWeight.ExtraBold
//                ),
//                color = MaterialTheme.colorScheme.onBackground
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Phone badge
//            AssistChip(
//                onClick = {},
//                label = {
//                    Text(
//                        text = "Sent to $maskedPhone",
//                        style = MaterialTheme.typography.bodyMedium.copy(
//                            fontWeight = FontWeight.Medium
//                        )
//                    )
//                },
//                leadingIcon = {
//                    Icon(
//                        imageVector = Icons.Outlined.PhoneAndroid,
//                        contentDescription = null,
//                        modifier = Modifier.size(16.dp)
//                    )
//                },
//                shape = RoundedCornerShape(50),
//                colors = AssistChipDefaults.assistChipColors(
//                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
//                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
//                    leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
//                ),
//                border = AssistChipDefaults.assistChipBorder(
//                    enabled = true,
//                    borderColor = Color.Transparent
//                )
//            )
//
//            Spacer(modifier = Modifier.height(44.dp))
//
//            // ── OTP Boxes ────────────────────────────────────────────────────
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(10.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                repeat(otpLength) { index ->
//                    val char = otp.getOrNull(index)?.toString() ?: ""
//                    val isFilled = char.isNotEmpty()
//
//                    BasicTextField(
//                        value = char,
//                        onValueChange = { input ->
//                            if (input.length <= 1 && input.all { it.isDigit() }) {
//                                val newOtp = StringBuilder(otp)
//                                if (input.isNotEmpty()) {
//                                    if (otp.length > index) newOtp.setCharAt(index, input[0])
//                                    else newOtp.insert(index, input)
//                                } else if (otp.length > index) {
//                                    newOtp.deleteCharAt(index)
//                                }
//                                otp = newOtp.toString()
//                                if (input.isNotEmpty() && index < otpLength - 1)
//                                    focusRequesters[index + 1].requestFocus()
//                                if (input.isEmpty() && index > 0)
//                                    focusRequesters[index - 1].requestFocus()
//                                if (otp.length == otpLength) onVerify(otp)
//                            }
//                        },
//                        modifier = Modifier
//                            .weight(1f)
//                            .aspectRatio(0.85f)
//                            .focusRequester(focusRequesters[index])
//                            .clip(RoundedCornerShape(16.dp))
//                            .background(
//                                color = if (isFilled)
//                                    MaterialTheme.colorScheme.primaryContainer
//                                else
//                                    MaterialTheme.colorScheme.surfaceContainerHigh
//                            )
//                            .border(
//                                width = if (isFilled) 2.dp else 1.dp,
//                                color = if (isFilled)
//                                    MaterialTheme.colorScheme.primary
//                                else
//                                    MaterialTheme.colorScheme.outlineVariant,
//                                shape = RoundedCornerShape(16.dp)
//                            ),
//                        singleLine = true,
//                        textStyle = LocalTextStyle.current.copy(
//                            textAlign = TextAlign.Center,
//                            fontSize = 24.sp,
//                            fontWeight = FontWeight.ExtraBold,
//                            color = MaterialTheme.colorScheme.primary
//                        ),
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        decorationBox = { innerTextField ->
//                            Box(contentAlignment = Alignment.Center) {
//                                innerTextField()
//                            }
//                        }
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // ── Progress Indicator ──────────────────────────────────────────
//            LinearProgressIndicator(
//                progress = { otp.length / otpLength.toFloat() },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(4.dp)
//                    .clip(RoundedCornerShape(2.dp)),
//                color = MaterialTheme.colorScheme.primary,
//                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
//            )
//
//            Spacer(modifier = Modifier.height(36.dp))
//
//            // ── Verify Button ────────────────────────────────────────────────
//            Button(
//                onClick = { onVerify(otp) },
//                enabled = otp.length == otpLength,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                shape = RoundedCornerShape(16.dp),
//                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
//            ) {
//                Text(
//                    text = "Verify & Proceed",
//                    style = MaterialTheme.typography.titleMedium.copy(
//                        fontWeight = FontWeight.Bold
//                    )
//                )
//            }
//
//            Spacer(modifier = Modifier.height(28.dp))
//
//            // ── Timer Card ───────────────────────────────────────────────────
//            AnimatedVisibility(
//                visible = timerRunning,
//                enter = fadeIn() + expandVertically(),
//                exit = fadeOut() + shrinkVertically()
//            ) {
//                ElevatedCard(
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(16.dp),
//                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
//                    colors = CardDefaults.elevatedCardColors(
//                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
//                    )
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 20.dp, vertical = 14.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            CircularProgressIndicator(
//                                progress = { timerProgress },
//                                modifier = Modifier.size(20.dp),
//                                strokeWidth = 2.5.dp,
//                                color = MaterialTheme.colorScheme.error,
//                                trackColor = MaterialTheme.colorScheme.errorContainer
//                            )
//                            Text(
//                                text = "Resend available in",
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.onErrorContainer
//                            )
//                        }
//                        Text(
//                            text = String.format("%02d:%02d", countdown / 60, countdown % 60),
//                            style = MaterialTheme.typography.titleMedium.copy(
//                                fontWeight = FontWeight.Bold
//                            ),
//                            color = MaterialTheme.colorScheme.error
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // ── Resend ───────────────────────────────────────────────────────
//            FilledTonalButton(
//                onClick = {
//                    if (!timerRunning) {
//                        onResendOtp()
//                        countdown = 54
//                        timerRunning = true
//                        otp = ""
//                        focusRequesters[0].requestFocus()
//                    }
//                },
//                enabled = !timerRunning,
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(14.dp)
//            ) {
//                Text(
//                    text = "Resend OTP",
//                    style = MaterialTheme.typography.titleSmall.copy(
//                        fontWeight = FontWeight.SemiBold
//                    )
//                )
//            }
//        }
//    }
//}



