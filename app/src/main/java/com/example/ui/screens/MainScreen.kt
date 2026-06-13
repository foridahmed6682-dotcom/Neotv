package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.ui.components.AdminPanel
import com.example.ui.components.SponsorMarquee
import com.example.ui.components.VideoPlayer
import com.example.ui.viewmodel.IptvViewModel

@Composable
fun MainScreen(
    viewModel: IptvViewModel,
    modifier: Modifier = Modifier
) {
    val channels by viewModel.channels.collectAsState()
    val activeSponsors by viewModel.activeSponsors.collectAsState()
    val selectedChannel by viewModel.selectedChannel.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isValidating by viewModel.isValidating.collectAsState()

    var showAdminPanel by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    // Split loaded channels into country headers
    val bangladeshChannels = remember(channels) { channels.filter { it.country == "Bangladesh" } }
    val indiaChannels = remember(channels) { channels.filter { it.country == "India" } }
    val globalChannels = remember(channels) { channels.filter { it.country != "Bangladesh" && it.country != "India" } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF090D16), // Deeper cosmic slate black background
    ) { innerPadding ->
        if (isFullscreen && selectedChannel != null) {
            // Full Screen Mode Overlay (Removes all side cards, status bar, and header)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                VideoPlayer(
                    url = selectedChannel!!.url,
                    modifier = Modifier.fillMaxSize(),
                    isFullscreen = true
                )

                // Minimize Overlay controller
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { isFullscreen = false }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Exit Full Screen",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Standard Dashboard Mode (Responsive Grid/Column)
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // Branded Header Row
                HeaderBar(
                    selectedChannel = selectedChannel,
                    isValidating = isValidating,
                    isFullscreen = isFullscreen,
                    onToggleFullscreen = { isFullscreen = !isFullscreen },
                    onOpenAdmin = { showAdminPanel = !showAdminPanel }
                )

                // Responsive Layout splitter
                if (isPortrait) {
                    // Mobile Portrait Mode Structure
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Main active stream screen bounds
                        selectedChannel?.let { channel ->
                            VideoPlayerBox(
                                channel = channel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        } ?: Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No Channel Selected. Touch below to watch.",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }

                        // Sponsor scrolling ticker
                        SponsorMarquee(
                            sponsors = activeSponsors,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )

                        // Categories selectors row
                        CategoryRow(
                            selectedCategory = selectedCategory,
                            onCategorySelected = { viewModel.setCategory(it) },
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        // Collapsible admin panel sheet drawer
                        if (showAdminPanel) {
                            AdminPanel(
                                sponsors = activeSponsors,
                                onSaveSponsor = { viewModel.saveSponsor(it) },
                                onDeleteSponsor = { viewModel.deleteSponsor(it) },
                                onClose = { showAdminPanel = false },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }

                        // Channels Listings lazy stacked column
                        ChannelsLayout(
                            bangladeshList = bangladeshChannels,
                            indiaList = indiaChannels,
                            globalList = globalChannels,
                            selectedChannel = selectedChannel,
                            onChannelSelected = { viewModel.selectChannel(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Tablet Landscape Side-by-Side Structure
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Left Pane: Stream Player + Sponsor Ticker + Admin Control
                        Column(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                                .padding(12.dp)
                        ) {
                            selectedChannel?.let { channel ->
                                VideoPlayerBox(
                                    channel = channel,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            } ?: Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No Channel Selected.",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            SponsorMarquee(sponsors = activeSponsors)

                            if (showAdminPanel) {
                                Spacer(modifier = Modifier.height(8.dp))
                                AdminPanel(
                                    sponsors = activeSponsors,
                                    onSaveSponsor = { viewModel.saveSponsor(it) },
                                    onDeleteSponsor = { viewModel.deleteSponsor(it) },
                                    onClose = { showAdminPanel = false }
                                )
                            }
                        }

                        // Right Pane: Active Category and Channel Flow list
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFF0F172A))
                                .padding(vertical = 12.dp)
                        ) {
                            CategoryRow(
                                selectedCategory = selectedCategory,
                                onCategorySelected = { viewModel.setCategory(it) }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            ChannelsLayout(
                                bangladeshList = bangladeshChannels,
                                indiaList = indiaChannels,
                                globalList = globalChannels,
                                selectedChannel = selectedChannel,
                                onChannelSelected = { viewModel.selectChannel(it) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderBar(
    selectedChannel: Channel?,
    isValidating: Boolean,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Brand Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Live Icon logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SLATE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = "IPTV",
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            // Spinner to indicate silent automatic latency validations!
            if (isValidating) {
                Spacer(modifier = Modifier.width(12.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 1.5.dp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "HEALING...",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (selectedChannel != null) {
                // Instantly clickable fullscreen trigger
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .clickable { onToggleFullscreen() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "ফুল স্ক্রিন (FullScreen)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Quick Floating Config Settings trigger
            IconButton(
                onClick = onOpenAdmin,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Admin trigger settings",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun VideoPlayerBox(
    channel: Channel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        VideoPlayer(
            url = channel.url,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
        )
    }
}

@Composable
fun CategoryRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        "All" to "All (সব চ্যানেল)",
        "FIFA" to "FIFA (ফিফা)",
        "News" to "News (খবর)",
        "Entertainment" to "Entertainment (বিনোদন)"
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (key, title) ->
            val isActive = selectedCategory == key
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isActive) MaterialTheme.colorScheme.primary else Color(0xFF1E293B))
                    .clickable { onCategorySelected(key) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("category_$key")
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.Black else Color.White
                )
            }
        }
    }
}

@Composable
fun ChannelsLayout(
    bangladeshList: List<Channel>,
    indiaList: List<Channel>,
    globalList: List<Channel>,
    selectedChannel: Channel?,
    onChannelSelected: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section 1: Bangladesh
        if (bangladeshList.isNotEmpty()) {
            item {
                SectionHeader(title = "🇧🇩 Bangladesh Live", count = bangladeshList.size)
            }
            items(bangladeshList, key = { it.url }) { channel ->
                ChannelItemRow(
                    channel = channel,
                    isSelected = selectedChannel?.url == channel.url,
                    onSelect = { onChannelSelected(channel) }
                )
            }
        }

        // Section 2: India
        if (indiaList.isNotEmpty()) {
            item {
                SectionHeader(title = "🇮🇳 India Live", count = indiaList.size)
            }
            items(indiaList, key = { it.url }) { channel ->
                ChannelItemRow(
                    channel = channel,
                    isSelected = selectedChannel?.url == channel.url,
                    onSelect = { onChannelSelected(channel) }
                )
            }
        }

        // Section 3: Global
        if (globalList.isNotEmpty()) {
            item {
                SectionHeader(title = "🌐 Global Live", count = globalList.size)
            }
            items(globalList, key = { it.url }) { channel ->
                ChannelItemRow(
                    channel = channel,
                    isSelected = selectedChannel?.url == channel.url,
                    onSelect = { onChannelSelected(channel) }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E293B))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "$count CH",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun ChannelItemRow(
    channel: Channel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val indicatorColor = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E293B)
    
    // Latency visualizer label
    val latencyLabel = when {
        channel.responseTimeMs < 250L -> "⚡ Super Fast (${channel.responseTimeMs}ms)"
        channel.responseTimeMs < 500L -> "⚡ Fast (${channel.responseTimeMs}ms)"
        channel.responseTimeMs < 1000L -> "⚡ Good (${channel.responseTimeMs}ms)"
        else -> "📶 Med (${channel.responseTimeMs}ms)"
    }
    
    val latencyColor = when {
        channel.responseTimeMs < 250L -> Color(0xFF22C55E) // Green
        channel.responseTimeMs < 500L -> Color(0xFF38BDF8) // Cyan
        channel.responseTimeMs < 1000L -> Color(0xFFEAB308) // Yellow
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A))
            .border(1.dp, indicatorColor, RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel Logo Frame with Failover placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = channel.logo,
                contentDescription = "${channel.name} Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = channel.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = channel.category,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "•",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                // Live Latency Metric Badge
                Text(
                    text = latencyLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = latencyColor
                )
            }
        }

        // Small indicator pill
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▶",
                fontSize = 10.sp,
                color = if (isSelected) Color.Black else Color.White,
                modifier = Modifier.offset(x = 1.dp)
            )
        }
    }
}
