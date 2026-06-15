package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
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
    val isPlaylistSyncing by viewModel.isPlaylistSyncing.collectAsState()
    val playlistSyncResult by viewModel.playlistSyncResult.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    var isFullscreen by remember { mutableStateOf(false) }
    var selectedResolution by remember { mutableStateOf("Auto") }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    // Split loaded channels into country headers and FIFA group
    val fifaChannels = remember(channels) { channels.filter { it.category.contains("FIFA", ignoreCase = true) } }
    val bangladeshChannels = remember(channels) { channels.filter { it.country == "Bangladesh" && !it.category.contains("FIFA", ignoreCase = true) } }
    val indiaChannels = remember(channels) { channels.filter { it.country == "India" && !it.category.contains("FIFA", ignoreCase = true) } }
    val globalChannels = remember(channels) { channels.filter { it.country != "Bangladesh" && it.country != "India" && !it.category.contains("FIFA", ignoreCase = true) } }

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
                    selectedResolution = selectedResolution,
                    modifier = Modifier.fillMaxSize(),
                    isFullscreen = true
                )

                // High-End HUD Stream Info Label (Fullscreen Overlay)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(24.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = selectedChannel!!.name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Quality: $selectedResolution",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

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
                    onToggleFullscreen = { isFullscreen = !isFullscreen }
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
                            Column {
                                VideoPlayerBox(
                                    channel = channel,
                                    selectedResolution = selectedResolution,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                                // Interactive Resolution Switcher bar
                                ResolutionSelectorRow(
                                    selectedResolution = selectedResolution,
                                    onResolutionSelected = { selectedResolution = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
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



                        // Channels Listings lazy stacked column
                        ChannelsLayout(
                            fifaList = fifaChannels,
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
                                Column(modifier = Modifier.weight(1f)) {
                                    VideoPlayerBox(
                                        channel = channel,
                                        selectedResolution = selectedResolution,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ResolutionSelectorRow(
                                        selectedResolution = selectedResolution,
                                        onResolutionSelected = { selectedResolution = it },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
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
                                fifaList = fifaChannels,
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
    onToggleFullscreen: () -> Unit
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
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = com.example.R.drawable.neo_tv_logo,
                    contentDescription = "NEO TV PRO Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "NEO",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = " TV PRO",
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                color = Color(0xFF38BDF8),
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
            }
        }
    }
}

@Composable
fun VideoPlayerBox(
    channel: Channel,
    selectedResolution: String = "Auto",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        VideoPlayer(
            url = channel.url,
            selectedResolution = selectedResolution,
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
                    .background(if (isActive) Color(0xFF38BDF8) else Color(0xFF233044))
                    .clickable { onCategorySelected(key) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
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
    fifaList: List<Channel>,
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
        // Section FIFA: Sports / World cup
        if (fifaList.isNotEmpty()) {
            item {
                SectionHeader(title = "⚽ FIFA World Cup Live", count = fifaList.size)
            }
            itemsIndexed(fifaList, key = { _, ch -> ch.url }) { index, channel ->
                ChannelItemRow(
                    channel = channel,
                    displayNumber = if (channel.channelNumber > 0) channel.channelNumber else (index + 1),
                    isSelected = selectedChannel?.url == channel.url,
                    onSelect = { onChannelSelected(channel) }
                )
            }
        }

        // Section 1: Bangladesh
        if (bangladeshList.isNotEmpty()) {
            item {
                SectionHeader(title = "🇧🇩 Bangladesh Live", count = bangladeshList.size)
            }
            itemsIndexed(bangladeshList, key = { _, ch -> ch.url }) { index, channel ->
                ChannelItemRow(
                    channel = channel,
                    displayNumber = if (channel.channelNumber > 0) channel.channelNumber else (index + 1),
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
            itemsIndexed(indiaList, key = { _, ch -> ch.url }) { index, channel ->
                ChannelItemRow(
                    channel = channel,
                    displayNumber = if (channel.channelNumber > 0) channel.channelNumber else (index + 1),
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
            itemsIndexed(globalList, key = { _, ch -> ch.url }) { index, channel ->
                ChannelItemRow(
                    channel = channel,
                    displayNumber = if (channel.channelNumber > 0) channel.channelNumber else (index + 1),
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
            color = Color(0xFF38BDF8)
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1E293B))
                .padding(horizontal = 10.dp, vertical = 4.dp)
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
    displayNumber: Int,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val indicatorColor = if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF131D31) else Color(0xFF0F172A))
            .border(1.dp, indicatorColor, RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(vertical = 12.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Channel Index Number Box
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0B0F19)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$displayNumber",
                color = Color.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. Channel Logo Card
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (channel.logo.isNotEmpty()) {
                AsyncImage(
                    model = channel.logo,
                    contentDescription = "${channel.name} Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // If logo is empty, render a dark card matching screenshot star news style
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E293B))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 3. Information Details Column
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

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Tag/Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = channel.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray
                    )
                }

                // Country Text
                Text(
                    text = channel.country,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 4. Quality selection / settings pill right side
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = channel.resolution,
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Config gear",
                    tint = Color.Gray,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
fun ResolutionSelectorRow(
    selectedResolution: String,
    onResolutionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("Auto", "4K", "1080p", "720p", "480p", "360p")
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📺 রেজোলিউশন (Quality Selection)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (selectedResolution == "Auto") "Auto (Network adaptive High)" else "Manual: $selectedResolution",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                options.forEach { option ->
                    val isSelected = selectedResolution == option
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E293B))
                            .clickable { onResolutionSelected(option) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("res_$option")
                    ) {
                        Text(
                            text = option,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}


