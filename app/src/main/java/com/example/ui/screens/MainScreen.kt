package com.example.ui.screens

import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DesktopMac
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.ui.components.AdminPanelDialog
import com.example.ui.components.SponsorMarquee
import com.example.ui.components.VideoPlayer
import com.example.ui.viewmodel.IptvUiState
import com.example.ui.viewmodel.IptvViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    viewModel: IptvViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val activeSponsors by viewModel.activeSponsors.collectAsState()
    val channels by viewModel.filteredChannels.collectAsState()

    var showCustomUrlInput by remember { mutableStateOf(false) }
    var inputUrl by remember { mutableStateOf(state.playlistUrl) }

    val focusManager = LocalFocusManager.current
    val rootFocusRequester = remember { FocusRequester() }

    // Preloaded playlist shortcuts
    val playlistPresets = listOf(
        Pair("🇧🇩 BD Channels", "https://iptv-org.github.io/iptv/countries/bd.m3u"),
        Pair("🏆 Sports Live", "https://iptv-org.github.io/iptv/categories/sports.m3u"),
        Pair("📰 World News", "https://iptv-org.github.io/iptv/categories/news.m3u"),
        Pair("🌐 Global Mixed", "https://iptv-org.github.io/iptv/index.m3u")
    )

    // Request initial focus on key layout to receive hotkeys
    LaunchedEffect(Unit) {
        rootFocusRequester.requestFocus()
    }

    // Capture standard short toast messages
    LaunchedEffect(state.message) {
        if (state.message != null) {
            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    // Root interceptor Box for TV Remote control numeric hotkeys
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19)) // Premium Deep Dark Slate
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    val keyCode = keyEvent.key.keyCode
                    // Map Kotlin external code to native keycodes
                    val nativeCode = keyEvent.nativeKeyEvent.keyCode
                    if (nativeCode in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9) {
                        val digit = nativeCode - KeyEvent.KEYCODE_0
                        viewModel.onNumericKeyPress(digit)
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
            .focusRequester(rootFocusRequester)
            .focusable()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Premium Header Tab
            HeaderSection(
                onAdminClick = { viewModel.toggleAdminChallenge(true) },
                onToggleCustomUrl = { showCustomUrlInput = !showCustomUrlInput }
            )

            // Collapsible Playlist Downloader / Settings Row
            AnimatedVisibility(
                visible = showCustomUrlInput,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Download Custom Live IPTV Playlist",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        SpacerHeight(8)

                        // URL input field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputUrl,
                                onValueChange = { inputUrl = it },
                                placeholder = { Text("Paste M3U raw playlist link...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_m3u_input_field"),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            SpacerWidth(8)
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    viewModel.loadPlaylist(inputUrl)
                                    showCustomUrlInput = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("submit_m3u_button")
                            ) {
                                Text("Sync Playlist")
                            }
                        }

                        SpacerHeight(12)

                        // Preset playlist buttons
                        Text(
                            text = "Preset Portals:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.LightGray
                        )
                        SpacerHeight(6)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            playlistPresets.forEach { preset ->
                                Button(
                                    onClick = {
                                        inputUrl = preset.second
                                        viewModel.loadPlaylist(preset.second)
                                        showCustomUrlInput = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (state.playlistUrl == preset.second) MaterialTheme.colorScheme.primary else Color(0xFF334155)
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.testTag("preset_${preset.first}")
                                ) {
                                    Text(preset.first, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // TV Split-Screen Layout or standard Mobile vertical stack
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val isWide = maxWidth > 750.dp

                if (isWide) {
                    // Wide/TV Optimized Landscape Split View
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left Pane: Video Player + Media Controls
                        Column(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                                .padding(16.dp)
                        ) {
                            // Video player takes core focus
                            VideoPlayerBox(
                                state = state,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16 / 9f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                            )

                            SpacerHeight(12)

                            // Continuous flow sponsors marquee
                            SponsorMarquee(
                                sponsors = activeSponsors,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Right Pane: Filters + Interactive Channels List
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFF0F172A).copy(alpha = 0.5f))
                                .border(
                                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                                    shape = RoundedCornerShape(topStart = 16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            CategoryRow(
                                selected = state.selectedCategory,
                                onSelect = { viewModel.selectCategory(it) }
                            )

                            SpacerHeight(12)

                            ChannelSectionList(
                                channels = channels,
                                currentSelected = state.selectedChannel,
                                onSelectChannel = { viewModel.selectChannel(it) },
                                isLoading = state.isLoadingChannels
                            )
                        }
                    }
                } else {
                    // Mobile Vertical Stack View
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top segment: player
                        VideoPlayerBox(
                            state = state,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16 / 9f)
                        )

                        // Sponsors Marquee
                        SponsorMarquee(
                            sponsors = activeSponsors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )

                        // Category pillows
                        CategoryRow(
                            selected = state.selectedCategory,
                            onSelect = { viewModel.selectCategory(it) },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        SpacerHeight(8)

                        // Channels scroll feed
                        ChannelSectionList(
                            channels = channels,
                            currentSelected = state.selectedChannel,
                            onSelectChannel = { viewModel.selectChannel(it) },
                            isLoading = state.isLoadingChannels,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // TV Remote Hotkey overlay switch prompt
        if (state.digitSwitchChannelName != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.widthIn(max = 300.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Switching to",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "CHANNEL ${state.digitSwitchCode}",
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            text = state.digitSwitchChannelName ?: "Searching target...",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        SpacerHeight(12)
                        CircularProgressIndicator(
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Secure Sponser Admin Overlay Panel
        if (state.adminPasswordChallengeActive || state.isAdminLoggedIn) {
            AdminPanelDialog(
                state = state,
                onDismiss = { viewModel.toggleAdminChallenge(false); viewModel.logoutAdmin() },
                onPasswordSubmit = { viewModel.challengeAdminPassword(it) },
                onLogout = { viewModel.logoutAdmin() },
                onSaveSponsor = { viewModel.saveSponsor(it) },
                onDeleteSponsor = { viewModel.deleteSponsor(it) },
                onEditSponsorClick = { viewModel.setEditingSponsor(it) },
                onAddAdmin = { viewModel.addAdmin(it) },
                onRemoveAdmin = { viewModel.removeAdmin(it) }
            )
        }
    }
}

@Composable
fun VideoPlayerBox(
    state: IptvUiState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        if (state.selectedChannel != null) {
            VideoPlayer(channel = state.selectedChannel, modifier = Modifier.fillMaxSize())
        } else {
            // Elegant player placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = "Placeholder screen",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(64.dp)
                    )
                    SpacerHeight(12)
                    Text(
                        text = "No Live Stream Selected",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Pick an IPTV stream from the catalog below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    onAdminClick: () -> Unit,
    onToggleCustomUrl: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A)) // Premium Dark Slate base
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Styled Branded Theme App Label
        Icon(
            imageVector = Icons.Default.DesktopMac,
            contentDescription = "Live Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        SpacerWidth(8)
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
            Text(
                text = "Live Stream Hybrid TV Portal",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

        // Sync Playlist Trigger
        IconButton(
            onClick = onToggleCustomUrl,
            modifier = Modifier.testTag("toggle_custom_playlist_button")
        ) {
            Icon(
                imageVector = Icons.Default.Input,
                contentDescription = "IPTV Input Link",
                tint = Color.LightGray
            )
        }

        SpacerWidth(4)

        // Security code entry
        IconButton(
            onClick = onAdminClick,
            modifier = Modifier.testTag("admin_portal_button")
        ) {
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = "Admin Portal",
                tint = Color.LightGray
            )
        }
    }
}

@Composable
fun CategoryRow(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "News", "Sports", "Entertainment", "Movies", "Music")

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(categories) { category ->
            CategoryPill(
                title = category,
                isSelected = category == selected,
                onClick = { onSelect(category) }
            )
        }
    }
}

@Composable
fun CategoryPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(CircleShape) // Circular pill-shaped design
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else if (isFocused) Color(0xFF334155)
                else Color(0xFF1E293B)
            )
            .border(
                width = 1.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("category_pill_$title"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF0F172A) else Color.White
        )
    }
}

@Composable
fun ChannelSectionList(
    channels: List<Channel>,
    currentSelected: Channel?,
    onSelectChannel: (Channel) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                SpacerHeight(12)
                Text("Caching Live Channels feed dynamically...", color = Color.Gray, fontSize = 12.sp)
            }
        }
        return
    }

    if (channels.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No channels in this category",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Try syncing playlists using the input at the top right.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        return
    }

    // Sort into Bangladesh, India and Global groupings for clean headers
    val bangladeshChannels = remember(channels) { channels.filter { it.country == "Bangladesh" } }
    val indiaChannels = remember(channels) { channels.filter { it.country == "India" } }
    val globalChannels = remember(channels) { channels.filter { it.country == "Global" } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (bangladeshChannels.isNotEmpty()) {
            item {
                CountrySectionHeader(name = "🇧🇩 Bangladesh Live", count = bangladeshChannels.size)
            }
            items(bangladeshChannels) { item ->
                ChannelRowItem(
                    channel = item,
                    isSelected = currentSelected?.url == item.url,
                    onClick = { onSelectChannel(item) }
                )
            }
        }

        if (indiaChannels.isNotEmpty()) {
            item {
                CountrySectionHeader(name = "🇮🇳 India Live", count = indiaChannels.size)
            }
            items(indiaChannels) { item ->
                ChannelRowItem(
                    channel = item,
                    isSelected = currentSelected?.url == item.url,
                    onClick = { onSelectChannel(item) }
                )
            }
        }

        if (globalChannels.isNotEmpty()) {
            item {
                CountrySectionHeader(name = "🌐 Global Live Broadcasts", count = globalChannels.size)
            }
            items(globalChannels) { item ->
                ChannelRowItem(
                    channel = item,
                    isSelected = currentSelected?.url == item.url,
                    onClick = { onSelectChannel(item) }
                )
            }
        }
    }
}

@Composable
fun CountrySectionHeader(
    name: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.5.sp
        )
        SpacerWidth(8)
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
fun ChannelRowItem(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .testTag("channel_card_${channel.channelNumber}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E293B)
            else if (isFocused) Color(0xFF334155).copy(alpha = 0.6f)
            else Color(0xFF1E293B).copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else if (isFocused) Color(0xFF38BDF8).copy(alpha = 0.5f)
            else Color(0xFF334155).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Number Box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else Color(0xFF0F172A)
                    )
                    .width(42.dp)
                    .height(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = channel.channelNumber.toString(),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                )
            }

            SpacerWidth(12)

            // Logo
            AsyncImage(
                model = channel.logoUrl ?: "https://images.unsplash.com/photo-1598257006458-087169a1f08d?auto=format&fit=crop&w=100&q=80",
                contentDescription = "Channel logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F172A))
                    .border(0.5.dp, Color(0xFF475569), RoundedCornerShape(6.dp))
            )

            SpacerWidth(12)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF334155).copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = channel.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    }

                    SpacerWidth(6)

                    // Country Tag
                    Text(
                        text = channel.country,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }

            // Real-Time streaming visualizer bar
            if (isSelected) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(14.dp)
                ) {
                    Box(modifier = Modifier.size(2.dp, 10.dp).background(MaterialTheme.colorScheme.primary))
                    Box(modifier = Modifier.size(2.dp, 14.dp).background(MaterialTheme.colorScheme.primary))
                    Box(modifier = Modifier.size(2.dp, 6.dp).background(MaterialTheme.colorScheme.primary))
                }
            }
        }
    }
}

@Composable
fun SpacerHeight(dp: Int) {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(dp.dp))
}

@Composable
fun SpacerWidth(dp: Int) {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(dp.dp))
}
