package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CallEntity
import com.example.ui.components.LiquidGlassAvatar
import com.example.ui.glass.liquidGlassOrb
import com.example.ui.theme.TextDarkPrimary
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.viewmodel.AuraUiState
import com.example.viewmodel.AuraViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallsScreen(
    uiState: AuraUiState,
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Calls",
                        color = TextWhitePrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Real Phone Calls & High-Definition Audio",
                        color = TextWhiteMuted,
                        fontSize = 12.sp
                    )
                }

                // Dial phone number trigger
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x1AFFFFFF))
                        .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(16.dp))
                        .clickable { viewModel.showDirectDialDialog(true) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("calls_open_dial_dialog_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Dial", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dial Real Phone",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (uiState.roomCalls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No recent calls",
                            color = TextWhitePrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap 'Dial Real Phone' to make a cellular call to any real number.",
                            color = TextWhiteMuted,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.roomCalls) { call ->
                        LiquidRealCallRow(
                            call = call,
                            onDialPhone = { viewModel.dialCustomNumber(call.phoneNumber, call.contactName) }
                        )
                    }
                }
            }
        }

        // Active Call Modal Overlay (in-app option)
        ActiveCallOverlay(uiState = uiState, viewModel = viewModel)
    }
}

@Composable
private fun LiquidRealCallRow(
    call: CallEntity,
    onDialPhone: () -> Unit
) {
    val timeFormatted = remember(call.timestamp) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(call.timestamp))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color(0x30FFFFFF))
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x22FFFFFF), Color(0x0CFFFFFF))
                )
            )
            .border(
                1.dp,
                Color(0x35FFFFFF),
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidGlassAvatar(
                avatarRes = null,
                monogram = call.contactName.take(2).uppercase().ifBlank { "PH" },
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.contactName,
                    color = if (call.isMissed) Color.White else TextWhitePrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = call.phoneNumber,
                    color = TextWhiteMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when {
                            call.isMissed -> Icons.Default.PhoneMissed
                            call.isIncoming -> Icons.Default.CallReceived
                            else -> Icons.Default.CallMade
                        },
                        contentDescription = null,
                        tint = if (call.isMissed) Color(0xFFFF5252) else Color(0xCCFFFFFF),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$timeFormatted • ${call.durationText}",
                        color = Color(0x80FFFFFF),
                        fontSize = 11.5.sp
                    )
                }
            }

            // Real dial button
            IconButton(
                onClick = onDialPhone,
                modifier = Modifier
                    .size(38.dp)
                    .liquidGlassOrb(fillAlpha = 0.2f, borderAlpha = 0.5f)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Dial on Phone",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveCallOverlay(
    uiState: AuraUiState,
    viewModel: AuraViewModel
) {
    AnimatedVisibility(
        visible = uiState.isCallActive && uiState.activeCallUser != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        val user = uiState.activeCallUser ?: return@AnimatedVisibility

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xEE060608))
                .statusBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "AURA LIQUID CALL",
                        color = TextWhiteMuted,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = user.name,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val mins = uiState.callDurationSec / 60
                    val secs = uiState.callDurationSec % 60
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        color = TextWhiteSecondary,
                        fontSize = 15.sp
                    )
                }

                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .shadow(28.dp, CircleShape, ambientColor = Color(0x80FFFFFF))
                            .clip(CircleShape)
                            .background(Color(0x18FFFFFF))
                            .border(1.5.dp, Color(0x60FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        LiquidGlassAvatar(
                            avatarRes = user.avatarDrawableRes,
                            monogram = user.avatarMonogram,
                            size = 140.dp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(24.dp, RoundedCornerShape(32.dp), ambientColor = Color(0x50FFFFFF))
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0x28FFFFFF))
                        .border(1.2.dp, Color(0x60FFFFFF), RoundedCornerShape(32.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleCallMute() },
                            modifier = Modifier
                                .size(52.dp)
                                .liquidGlassOrb(
                                    fillAlpha = if (uiState.isCallMuted) 0.4f else 0.15f,
                                    borderAlpha = 0.5f
                                )
                        ) {
                            Icon(
                                imageVector = if (uiState.isCallMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleSpeaker() },
                            modifier = Modifier
                                .size(52.dp)
                                .liquidGlassOrb(
                                    fillAlpha = if (uiState.isSpeakerOn) 0.4f else 0.15f,
                                    borderAlpha = 0.5f
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speaker",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(14.dp, CircleShape, ambientColor = Color(0xFFFF5252))
                                .clip(CircleShape)
                                .background(Color(0xFFE53935))
                                .clickable { viewModel.endCall() }
                                .testTag("end_call_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "End Call",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
