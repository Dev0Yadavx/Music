package com.musicx.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.musicx.player.data.NetworkClient
import com.musicx.player.data.UiTrack
import com.musicx.player.ui.screens.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        exoPlayer = ExoPlayer.Builder(this).build()

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScaffold(exoPlayer)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}

@Composable
fun AppScaffold(exoPlayer: ExoPlayer) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showFullPlayer by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    var currentTrack by remember {
        mutableStateOf(
            UiTrack(
                videoId = "kJQP7kiw5Fk",
                title = "Kesariya",
                artist = "Arijit Singh",
                thumbnail = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500"
            )
        )
    }
    var currentQueue by remember { mutableStateOf<List<UiTrack>>(emptyList()) }
    var lyricsText by remember { mutableStateOf("Loading...") }

    val scope = rememberCoroutineScope()

    fun playTrack(track: UiTrack) {
        currentTrack = track
        scope.launch {
            try {
                // 1. Fetch direct playable stream URL
                val streamRes = NetworkClient.fetchStream(track.videoId)
                val url = streamRes.get("stream_url")?.asString

                // 2. Fetch watch queue for Up Next drawer
                launch {
                    val queue = NetworkClient.fetchWatchQueueTracks(track.videoId)
                    currentQueue = queue
                }

                // 3. Fetch Lyrics
                launch {
                    try {
                        val lyricsRes = NetworkClient.api.getLyrics(track.videoId)
                        lyricsText = lyricsRes.get("lyrics")?.asString ?: "Lyrics not found"
                    } catch (_: Exception) {
                        lyricsText = "Lyrics not found"
                    }
                }

                if (!url.isNullOrEmpty()) {
                    val item = MediaItem.fromUri(url)
                    exoPlayer.setMediaItem(item)
                    exoPlayer.prepare()
                    exoPlayer.play()
                    isPlaying = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Auto-advance next song from queue when current track ends
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED && currentQueue.isNotEmpty()) {
                    val nextTrack = currentQueue.first()
                    currentQueue = currentQueue.drop(1)
                    playTrack(nextTrack)
                }
            }
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    // Update progress periodically during playback
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val duration = exoPlayer.duration
            val current = exoPlayer.currentPosition
            if (duration > 0) {
                progress = (current.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            }
            delay(500)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Column {
                    // Floating Mini Player
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .clickable { showFullPlayer = true },
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = currentTrack.thumbnail,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    currentTrack.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    currentTrack.artist,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = {
                                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                            }) {
                                Icon(
                                    if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Bottom Navigation
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
                            label = { Text("Home") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            label = { Text("Search") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Rounded.LibraryMusic, contentDescription = null) },
                            label = { Text("Library") }
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (selectedTab) {
                    0 -> HomeScreen { id, title, artist ->
                        playTrack(UiTrack(id, title, artist, currentTrack.thumbnail))
                    }
                    1 -> SearchScreen { id, title, artist ->
                        playTrack(UiTrack(id, title, artist, currentTrack.thumbnail))
                    }
                    2 -> LibraryScreen()
                }
            }
        }

        if (showFullPlayer) {
            NowPlayingFullScreen(
                title = currentTrack.title,
                artist = currentTrack.artist,
                coverUrl = currentTrack.thumbnail,
                isPlaying = isPlaying,
                progress = progress,
                lyricsText = lyricsText,
                queue = currentQueue,
                onClose = { showFullPlayer = false },
                onPlayPause = { if (isPlaying) exoPlayer.pause() else exoPlayer.play() },
                onNext = {
                    if (currentQueue.isNotEmpty()) {
                        val next = currentQueue.first()
                        currentQueue = currentQueue.drop(1)
                        playTrack(next)
                    }
                },
                onPrevious = {},
                onSeek = { factor ->
                    val duration = exoPlayer.duration
                    if (duration > 0) {
                        val pos = (duration * factor).toLong()
                        exoPlayer.seekTo(pos)
                        progress = factor
                    }
                },
                onQueueTrackSelect = { selected -> playTrack(selected) }
            )
        }
    }
}
