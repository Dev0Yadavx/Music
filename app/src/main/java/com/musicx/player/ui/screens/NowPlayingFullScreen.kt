package com.musicx.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.musicx.player.data.UiTrack
import com.musicx.player.ui.components.M3ExpressiveWavyBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingFullScreen(
    title: String,
    artist: String,
    coverUrl: String,
    isPlaying: Boolean,
    progress: Float,
    lyricsText: String,
    queue: List<UiTrack>,
    onClose: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onQueueTrackSelect: (UiTrack) -> Unit
) {
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(onClick = onClose, shape = CircleShape) {
                    Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Close")
                }
                Text(
                    text = if (queue.isNotEmpty()) "UP NEXT: ${queue.size} SONGS" else "NOW PLAYING",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                FilledTonalIconButton(onClick = { showQueueSheet = true }, shape = CircleShape) {
                    Icon(Icons.Rounded.QueueMusic, contentDescription = "Queue")
                }
            }

            // Big Artwork
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(310.dp)
                    .clip(RoundedCornerShape(36.dp))
            )

            // Metadata
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Text(
                    text = artist,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Seekbar
            Column(modifier = Modifier.fillMaxWidth()) {
                M3ExpressiveWavyBar(
                    progress = progress,
                    isPlaying = isPlaying,
                    onSeek = onSeek
                )
            }

            // Playback Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = null)
                }
                FilledTonalIconButton(onClick = onPrevious, modifier = Modifier.size(56.dp), shape = CircleShape) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = null, modifier = Modifier.size(30.dp))
                }
                Button(
                    onClick = onPlayPause,
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
                FilledTonalIconButton(onClick = onNext, modifier = Modifier.size(56.dp), shape = CircleShape) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = null, modifier = Modifier.size(30.dp))
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Rounded.Repeat, contentDescription = null)
                }
            }

            // Pill Triggers
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SuggestionChip(
                    onClick = { showLyricsSheet = true },
                    label = { Text("Lyrics") },
                    icon = { Icon(Icons.Rounded.Notes, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp)
                )
                SuggestionChip(
                    onClick = { showQueueSheet = true },
                    label = { Text("Queue (${queue.size})") },
                    icon = { Icon(Icons.Rounded.QueueMusic, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }

    // 1. QUEUE BOTTOM SHEET
    if (showQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "UP NEXT RADIO QUEUE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (queue.isEmpty()) {
                    Text(
                        text = "Loading radio queue from server...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight(0.6f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(queue) { track ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onQueueTrackSelect(track)
                                        showQueueSheet = false
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = track.thumbnail,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            track.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                        Text(
                                            track.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                    Icon(
                                        Icons.Rounded.PlayCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 2. LYRICS BOTTOM SHEET
    if (showLyricsSheet) {
        ModalBottomSheet(onDismissRequest = { showLyricsSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "LYRICS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = lyricsText.ifEmpty { "Lyrics not available for this track." },
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
