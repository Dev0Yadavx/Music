package com.musicx.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.PlayArrow
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

@Composable
fun HomeScreen(onSongSelect: (String, String, String) -> Unit) {
    val moods = listOf("Energize", "Workout", "Relax", "Focus", "Party", "Romance")
    val charts = listOf("Top 50 India", "Viral Hits 2026", "Bollywood Hotlist", "Punjabi Pop")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("LISTEN NOW", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                    Text("Music X Studio", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
                }
                FilledTonalIconButton(onClick = {}) {
                    Icon(Icons.Rounded.ElectricBolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Mood Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moods.forEachIndexed { i, mood ->
                    FilterChip(
                        selected = i == 0,
                        onClick = {},
                        label = { Text(mood) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        // Horizontal Charts Shelf
        item {
            Text("Trending Charts (India)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(charts) { title ->
                    ElevatedCard(
                        modifier = Modifier.width(150.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500",
                                contentDescription = null,
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(24.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(10.dp))
                        }
                    }
                }
            }
        }

        // Quick Picks Vertical List
        item {
            Text("Quick Picks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 20.dp, top = 28.dp, bottom = 12.dp))
        }

        items(5) { idx ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { onSongSelect("kJQP7kiw5Fk", "Despacito", "Luis Fonsi") },
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=200",
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Trending Hit #$idx", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("Studio Artist • 03:40", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onSongSelect("kJQP7kiw5Fk", "Track #$idx", "Artist") }) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
