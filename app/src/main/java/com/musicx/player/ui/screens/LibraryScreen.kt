package com.musicx.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.musicx.player.data.NetworkClient
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen() {
    var tunnelInput by remember { mutableStateOf(NetworkClient.streamTunnelUrl) }
    var pingStatus by remember { mutableStateOf("Ready to ping stream tunnel") }
    var isPinging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Header Profile
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = "https://avatars.githubusercontent.com/u/257059002?v=4",
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Music X Engine",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Base: music-x-api-docs.onrender.com",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    FloatingActionButton(
                        onClick = {},
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Create Playlist")
                    }
                }
            }

            // DEDICATED STREAM & WATCH TUNNEL SWITCHER BOX
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Sensors,
                                contentDescription = null,
                                tint = Color(0xFFFFD60A)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STREAM & WATCH TUNNEL ROUTE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Text(
                            text = "General API uses Render. Only /stream and /watch route to this custom link.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = tunnelInput,
                            onValueChange = { tunnelInput = it },
                            label = { Text("Stream/Watch Tunnel URL") },
                            placeholder = { Text("http://127.0.0.1:8000 or https://xyz.trycloudflare.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pingStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = {
                                    isPinging = true
                                    pingStatus = "Verifying tunnel link..."
                                    NetworkClient.setStreamTunnel(tunnelInput)
                                    scope.launch {
                                        try {
                                            val res = NetworkClient.fetchStream("kJQP7kiw5Fk")
                                            val streamUrl = res.get("stream_url")?.asString
                                            pingStatus = if (!streamUrl.isNullOrEmpty()) "Connected! Stream link verified" else "Failed: Empty stream URL"
                                        } catch (e: Exception) {
                                            pingStatus = "Failed: ${e.localizedMessage}"
                                        } finally {
                                            isPinging = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isPinging
                            ) {
                                Text("Apply & Test")
                            }
                        }
                    }
                }
            }

            // Cloud Collections (Mapped directly to Render backend)
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "CLOUD COLLECTIONS (ONRENDER)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.2.sp
                    )

                    LibraryTileItem(Icons.Rounded.Favorite, Color(0xFFFF2A44), "Liked Songs", "Render • /library/songs")
                    LibraryTileItem(Icons.Rounded.History, Color(0xFFFFD60A), "Listening History", "Render • /library/history")
                    LibraryTileItem(Icons.Rounded.CloudDone, Color(0xFF00E676), "Cloud Uploads", "Render • /library/uploads")
                    LibraryTileItem(Icons.Rounded.Subscriptions, Color(0xFF29B6F6), "Subscribed Artists", "Render • /library/artists")
                }
            }
        }
    }
}

@Composable
fun LibraryTileItem(icon: ImageVector, tint: Color, title: String, subtitle: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
