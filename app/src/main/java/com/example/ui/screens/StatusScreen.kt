package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiquidGlassAvatar
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassInputField
import com.example.ui.components.LiquidGlassPillButton
import com.example.ui.theme.TextWhiteMuted
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.viewmodel.AuraUiState
import com.example.viewmodel.AuraViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusScreen(
    uiState: AuraUiState,
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    var myStatusText by remember { mutableStateOf("Online on Aura Glass.") }
    var showEditStatusDialog by remember { mutableStateOf(false) }
    var statusInput by remember { mutableStateOf("") }

    val liveStatuses = uiState.liveStatuses

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Status",
                        color = TextWhitePrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Real Live Status Updates",
                        color = TextWhiteMuted,
                        fontSize = 12.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // My Status Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = Color(0x35FFFFFF))
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0x1EFFFFFF))
                            .border(1.dp, Color(0x35FFFFFF), RoundedCornerShape(22.dp))
                            .clickable { showEditStatusDialog = true }
                            .padding(16.dp)
                            .testTag("status_my_status_card")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                LiquidGlassAvatar(
                                    avatarRes = null,
                                    monogram = uiState.currentProfile?.avatarMonogram ?: "ME",
                                    size = 54.dp
                                )
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFF0C0C10), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add status", tint = Color.Black, modifier = Modifier.size(12.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "My Status",
                                    color = TextWhitePrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = myStatusText,
                                    color = TextWhiteSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                if (liveStatuses.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x20FFFFFF))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0x12FFFFFF))
                                .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(20.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No Live Status Updates Yet",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Post an update above to broadcast to everyone on Firestore.",
                                    color = TextWhiteMuted,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "RECENT UPDATES ACROSS AURA",
                            color = TextWhiteMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                        )
                    }

                    items(liveStatuses, key = { it.id }) { item ->
                        val timeStr = remember(item.timestamp) {
                            val seconds = item.timestamp?.seconds ?: (System.currentTimeMillis() / 1000)
                            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(seconds * 1000))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x20FFFFFF))
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0x14FFFFFF))
                                .border(1.dp, Color(0x25FFFFFF), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LiquidGlassAvatar(
                                    avatarRes = null,
                                    monogram = item.authorName.take(2).uppercase().ifEmpty { "U" },
                                    size = 48.dp,
                                    hasStoryRing = true
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = item.authorName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = item.text, color = TextWhiteSecondary, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = timeStr, color = TextWhiteMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Post Status Dialog
        if (showEditStatusDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xD9000000))
                    .clickable { showEditStatusDialog = false }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(0.92f).clickable(enabled = false) {},
                    cornerRadius = 28.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Update Live Status", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showEditStatusDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LiquidGlassInputField(
                        value = statusInput,
                        onValueChange = { statusInput = it },
                        placeholder = "What's on your mind?...",
                        testTag = "input_status_update"
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    LiquidGlassPillButton(
                        text = "Publish to Firestore",
                        onClick = {
                            if (statusInput.isNotBlank()) {
                                myStatusText = statusInput.trim()
                                viewModel.publishLiveStatus(statusInput.trim())
                                showEditStatusDialog = false
                                statusInput = ""
                            }
                        },
                        testTag = "publish_status_button"
                    )
                }
            }
        }
    }
}
