package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.auth.FirebaseAuthService
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassInputField
import com.example.ui.components.LiquidGlassPillButton
import com.example.ui.components.LiquidSocialButton
import com.example.ui.glass.LiquidAmbientBackground
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.viewmodel.AuraUiState
import com.example.viewmodel.AuraViewModel

@Composable
fun LoginScreen(
    uiState: AuraUiState,
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var showCountryPicker by remember { mutableStateOf(false) }

    LiquidAmbientBackground(
        glowIntensity = uiState.glowIntensity,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.3f))

            // Centered Large Liquid Glass Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                LiquidGlassCard(
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth(),
                    cornerRadius = 32.dp
                ) {
                    // Minimalist Logo Mark (Simple White Geometric Mark)
                    LiquidMinimalistLogo()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "AURA",
                        color = TextWhitePrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )

                    Text(
                        text = "Liquid Glass Monochrome",
                        color = TextWhiteMuted,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Auth Method Switcher (Google Account vs Phone Number)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x18FFFFFF))
                            .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(16.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (uiState.authMethod == "google") Color.White else Color.Transparent)
                                .clickable { viewModel.setAuthMethod("google") }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Google / Email",
                                color = if (uiState.authMethod == "google") TextDarkPrimary else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (uiState.authMethod == "phone") Color.White else Color.Transparent)
                                .clickable { viewModel.setAuthMethod("phone") }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Phone Number",
                                color = if (uiState.authMethod == "phone") TextDarkPrimary else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Display Name (helps other real users identify you in Firestore)
                    LiquidGlassInputField(
                        value = uiState.displayNameInput,
                        onValueChange = { viewModel.updateDisplayName(it) },
                        placeholder = "Your Full Name (e.g. Allen Innocent)",
                        leadingIcon = Icons.Outlined.AccountCircle,
                        testTag = "login_name_input"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (uiState.authMethod == "google") {
                        // Google Email / Username
                        LiquidGlassInputField(
                            value = uiState.emailInput,
                            onValueChange = { viewModel.updateEmail(it) },
                            placeholder = "Email Address (e.g. allen@example.com)",
                            leadingIcon = Icons.Outlined.Mail,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            testTag = "login_email_input"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password (optional / for account security)
                        LiquidGlassInputField(
                            value = uiState.passwordInput,
                            onValueChange = { viewModel.updatePassword(it) },
                            placeholder = "Password (optional)",
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true,
                            isPasswordVisible = uiState.isPasswordVisible,
                            onTogglePasswordVisibility = { viewModel.togglePasswordVisibility() },
                            testTag = "login_password_input"
                        )
                    } else {
                        // Phone Auth Mode
                        if (!uiState.isAwaitingOtp) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Country Code selector pill
                                Box(
                                    modifier = Modifier
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color(0x1EFFFFFF))
                                        .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(18.dp))
                                        .clickable { showCountryPicker = !showCountryPicker }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = uiState.phoneCountryCode,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                LiquidGlassInputField(
                                    value = uiState.phoneNumberInput,
                                    onValueChange = { viewModel.updatePhoneNumber(it) },
                                    placeholder = "Mobile Number (e.g. 555-0199)",
                                    leadingIcon = Icons.Outlined.Phone,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.weight(1f),
                                    testTag = "login_phone_input"
                                )
                            }
                        } else {
                            // OTP Verification code input
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Enter 6-digit code for ${uiState.phoneCountryCode} ${uiState.phoneNumberInput}",
                                    color = TextWhiteSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LiquidGlassInputField(
                                    value = uiState.otpCodeInput,
                                    onValueChange = { viewModel.updateOtpCode(it) },
                                    placeholder = "Enter 6-digit verification code",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    testTag = "login_otp_input"
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Resend Code",
                                    color = TextWhiteMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable { viewModel.requestPhoneOtp() }
                                )
                            }
                        }
                    }

                    if (uiState.authError != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = uiState.authError,
                            color = Color(0xFFFF6E6E),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Primary Button: Solid White Liquid Glass Pill
                    if (uiState.isAuthenticating) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(RoundedCornerShape(27.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = TextDarkPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        val buttonText = when {
                            uiState.authMethod == "phone" && uiState.isAwaitingOtp -> "Verify & Join"
                            uiState.authMethod == "phone" -> "Continue with Phone"
                            else -> "Sign In to Aura"
                        }
                        LiquidGlassPillButton(
                            text = buttonText,
                            onClick = {
                                if (uiState.authMethod == "google" && uiState.emailInput.isBlank()) {
                                    // Try native Google Sign-In with Credential Manager
                                    FirebaseAuthService.onGoogleSignInClicked(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onAuthSuccess = { firebaseUser ->
                                            viewModel.onFirebaseAuthSuccess(firebaseUser)
                                        },
                                        onAuthError = {
                                            viewModel.signIn()
                                        },
                                        scope = coroutineScope,
                                        onAuthCancelled = {
                                            viewModel.signIn()
                                        }
                                    )
                                } else {
                                    viewModel.signIn()
                                }
                            },
                            testTag = "login_submit_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Thin Glass Divider with "or continue with" text
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, GlassBorderSubtle)
                                    )
                                )
                        )
                        Text(
                            text = "or one-tap sign in",
                            color = TextWhiteMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(GlassBorderSubtle, Color.Transparent)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Circular Glass Social-Login Buttons (Apple & Google style)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiquidSocialButton(
                            type = "google",
                            onClick = {
                                FirebaseAuthService.onGoogleSignInClicked(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onAuthSuccess = { firebaseUser ->
                                        viewModel.onFirebaseAuthSuccess(firebaseUser)
                                    },
                                    onAuthError = {
                                        val typedEmail = uiState.emailInput.ifBlank { "alleninnocent23@gmail.com" }
                                        val typedName = uiState.displayNameInput.ifBlank { "Allen Innocent" }
                                        viewModel.signInWithAccount(typedEmail, typedName)
                                    },
                                    scope = coroutineScope,
                                    onAuthCancelled = {
                                        val typedEmail = uiState.emailInput.ifBlank { "alleninnocent23@gmail.com" }
                                        val typedName = uiState.displayNameInput.ifBlank { "Allen Innocent" }
                                        viewModel.signInWithAccount(typedEmail, typedName)
                                    }
                                )
                            },
                            testTag = "social_google_button"
                        )
                        LiquidSocialButton(
                            type = "apple",
                            onClick = {
                                val typedName = uiState.displayNameInput.ifBlank { "Apple User" }
                                viewModel.signInWithAccount("user.apple@auraglass.io", typedName)
                            },
                            testTag = "social_apple_button"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))
        }

        // Country code selector dialog
        if (showCountryPicker) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xB3000000))
                    .clickable { showCountryPicker = false }
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth(0.85f), cornerRadius = 24.dp) {
                    Text(text = "Select Country Code", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(14.dp))
                    listOf(
                        "+1" to "United States / Canada",
                        "+44" to "United Kingdom",
                        "+91" to "India",
                        "+49" to "Germany",
                        "+33" to "France",
                        "+81" to "Japan",
                        "+61" to "Australia",
                        "+234" to "Nigeria"
                    ).forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.updatePhoneCountryCode(code)
                                    showCountryPicker = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = name, color = Color.White, fontSize = 13.sp)
                            Text(text = code, color = TextWhiteMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidMinimalistLogo() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = Color(0x80FFFFFF),
                spotColor = Color(0x60FFFFFF)
            )
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x35FFFFFF),
                        Color(0x10FFFFFF)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x90FFFFFF),
                        Color(0x20FFFFFF),
                        Color(0x70FFFFFF)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Simple geometric liquid drop / diamond mark
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
        )
    }
}
