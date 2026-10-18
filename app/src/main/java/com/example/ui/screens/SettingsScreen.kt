package com.example.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.LiquidGlassAvatar
import com.example.ui.glass.liquidGlass
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.viewmodel.AuraUiState
import com.example.viewmodel.AuraViewModel

@Composable
fun SettingsScreen(
    uiState: AuraUiState,
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    val profile = uiState.currentProfile

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .padding(bottom = 100.dp)
    ) {
        Text(
            text = "Settings",
            color = TextWhitePrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = "Account, Real Calling & Optics",
            color = TextWhiteMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(shape = RoundedCornerShape(26.dp), fillAlpha = 0.16f, borderAlpha = 0.5f)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiquidGlassAvatar(
                    avatarRes = if (profile?.isPhoneAuth == false) R.drawable.img_glass_avatar else null,
                    monogram = profile?.avatarMonogram ?: "ME",
                    size = 62.dp,
                    isOnline = true
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = profile?.displayName ?: "Allen Innocent",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = profile?.identifier ?: "alleninnocent23@gmail.com",
                        color = TextWhiteMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x24FFFFFF))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (profile?.isPhoneAuth == true) "Phone Verified" else "Google Authenticated",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Liquid Glass Optics Controls
        Text(
            text = "LIQUID GLASS OPTICS",
            color = TextWhiteMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(shape = RoundedCornerShape(22.dp), fillAlpha = 0.12f, borderAlpha = 0.4f)
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Brightness6, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Ambient Glow Intensity", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Text(text = "${(uiState.glowIntensity * 100).toInt()}%", color = TextWhiteSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = uiState.glowIntensity,
                    onValueChange = { viewModel.updateGlowIntensity(it) },
                    valueRange = 0.3f..1.8f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = Color(0x35FFFFFF)),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Fluid Atmospheric Shimmer", color = Color.White, fontSize = 14.sp)
                    }
                    Switch(
                        checked = uiState.ambientShimmerEnabled,
                        onCheckedChange = { viewModel.toggleShimmer() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextDarkPrimary,
                            checkedTrackColor = Color.White,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0x28FFFFFF)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ACCOUNT & SESSION",
            color = TextWhiteMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(shape = RoundedCornerShape(22.dp), fillAlpha = 0.12f, borderAlpha = 0.4f)
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Direct Dial
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showDirectDialDialog(true) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Direct Real Phone Dial", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Place carrier call with Android dialer", color = TextWhiteMuted, fontSize = 12.sp)
                        }
                    }
                    Text(text = "Dial →", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                // Start Real Chat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showStartChatDialog(true) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Start Real Chat", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Connect with any email or phone on Firestore", color = TextWhiteMuted, fontSize = 12.sp)
                        }
                    }
                    Text(text = "+ Chat", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                // Sign Out
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.signOut() }
                        .testTag("settings_logout_button"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFF6E6E), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Sign Out", color = Color(0xFFFF6E6E), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Clear session and return to login screen", color = TextWhiteMuted, fontSize = 12.sp)
                        }
                    }
                    Text(text = "Log Out", color = Color(0xFFFF6E6E), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AURA GLASS • REAL PERSISTENCE & CALLING",
                color = TextWhiteMuted,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Room SQLite Database • Android Photo Picker • System Dialer",
                color = Color(0x60FFFFFF),
                fontSize = 10.sp
            )
        }
    }
}
