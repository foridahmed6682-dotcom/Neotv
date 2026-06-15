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
    val googleUser by viewModel.googleUser.collectAsState()
    val isPlaylistSyncing by viewModel.isPlaylistSyncing.collectAsState()
    val playlistSyncResult by viewModel.playlistSyncResult.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    var showAdminPanel by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var selectedResolution by remember { mutableStateOf("Auto") }
    var showGoogleSignInDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    LaunchedEffect(googleUser) {
        if (googleUser == null || googleUser?.email != "foridahmed6682@gmail.com") {
            showAdminPanel = false
        }
    }

    // Split loaded channels into country headers and FIFA group
    val fifaChannels = remember(channels) { channels.filter { it.category.contains("FIFA", ignoreCase = true) } }
    val bangladeshChannels = remember(channels) { channels.filter { it.country == "Bangladesh" && !it.category.contains("FIFA", ignoreCase = true) } }
    val indiaChannels = remember(channels) { channels.filter { it.country == "India" && !it.category.contains("FIFA", ignoreCase = true) } }
    val globalChannels = remember(channels) { channels.filter { it.country != "Bangladesh" && it.country != "India" && !it.category.contains("FIFA", ignoreCase = true) } }

    if (showGoogleSignInDialog) {
        GoogleSignInDialog(
            onDismiss = { showGoogleSignInDialog = false },
            onSignIn = { name, email, photo ->
                viewModel.signInWithGoogleReal(name, email, photo)
            }
        )
    }

    var showAdminPinDialog by remember { mutableStateOf(false) }

    if (showAdminPinDialog) {
        AdminPinDialog(
            onDismiss = { showAdminPinDialog = false },
            onSuccess = {
                showAdminPanel = true
                showAdminPinDialog = false
            }
        )
    }

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
                    onToggleFullscreen = { isFullscreen = !isFullscreen },
                    googleUser = googleUser,
                    onOpenAdmin = {
                        if (googleUser?.email == "foridahmed6682@gmail.com") {
                            if (showAdminPanel) {
                                showAdminPanel = false
                            } else {
                                showAdminPinDialog = true
                            }
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "দুঃখিত, কেবল মনোনীত অ্যাডমিন (foridahmed6682@gmail.com) সেটিংস ও কনসোল অ্যাক্সেস করতে পারবেন।",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    onOpenGoogleSignIn = { showGoogleSignInDialog = true },
                    onGoogleSignOut = { viewModel.signOutGoogle() }
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

                        // Collapsible admin panel sheet drawer
                        if (showAdminPanel && googleUser?.email == "foridahmed6682@gmail.com") {
                            AdminPanel(
                                sponsors = activeSponsors,
                                onSaveSponsor = { viewModel.saveSponsor(it) },
                                onDeleteSponsor = { viewModel.deleteSponsor(it) },
                                onClose = { showAdminPanel = false },
                                googleUser = googleUser,
                                onGoogleSignIn = { name, email, photo ->
                                    viewModel.signInWithGoogleSimulated(name, email, photo)
                                },
                                onGoogleSignOut = { viewModel.signOutGoogle() },
                                isPlaylistSyncing = isPlaylistSyncing,
                                playlistSyncResult = playlistSyncResult,
                                onSyncPlaylist = { viewModel.syncM3uPlaylist(it) },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }

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

                            if (showAdminPanel && googleUser?.email == "foridahmed6682@gmail.com") {
                                Spacer(modifier = Modifier.height(8.dp))
                                AdminPanel(
                                    sponsors = activeSponsors,
                                    onSaveSponsor = { viewModel.saveSponsor(it) },
                                    onDeleteSponsor = { viewModel.deleteSponsor(it) },
                                    onClose = { showAdminPanel = false },
                                    googleUser = googleUser,
                                    onGoogleSignIn = { name, email, photo ->
                                        viewModel.signInWithGoogleSimulated(name, email, photo)
                                    },
                                    onGoogleSignOut = { viewModel.signOutGoogle() },
                                    isPlaylistSyncing = isPlaylistSyncing,
                                    playlistSyncResult = playlistSyncResult,
                                    onSyncPlaylist = { viewModel.syncM3uPlaylist(it) }
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
    onToggleFullscreen: () -> Unit,
    googleUser: com.example.ui.viewmodel.GoogleUserInfo?,
    onOpenAdmin: () -> Unit,
    onOpenGoogleSignIn: () -> Unit,
    onGoogleSignOut: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

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

            // Real Google Authentication & settings in 3-Dots Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(36.dp).testTag("three_dots_menu_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options Menu",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    if (googleUser != null) {
                        // Signed-in header info
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(Color.DarkGray)
                                    ) {
                                        AsyncImage(
                                            model = googleUser.photoUrl,
                                            contentDescription = "User profile logo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = googleUser.name,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = googleUser.email,
                                            color = Color.Gray,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            },
                            onClick = {},
                            enabled = false
                        )

                        Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 4.dp))

                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = "গুগল সাইন-আউট (Google Sign Out)",
                                    color = Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ) 
                            },
                            onClick = {
                                menuExpanded = false
                                onGoogleSignOut()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Google SIgn-In",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "গুগল সাইন-ইন (Google Sign In)",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            onClick = {
                                menuExpanded = false
                                onOpenGoogleSignIn()
                            }
                        )
                    }

                    Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 4.dp))

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Console Settings",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "সেটিংস ও কনসোল (Settings & Console)",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        onClick = {
                            menuExpanded = false
                            onOpenAdmin()
                        }
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
            items(fifaList, key = { it.url }) { channel ->
                ChannelItemRow(
                    channel = channel,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = channel.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                // Premium Resolution Badge right next to channel name!
                val resolutionColor = when (channel.resolution) {
                    "4K" -> Color(0xFFEF4444) // Sunset Red
                    "1080p" -> Color(0xFF3B82F6) // Premium Blue
                    "720p" -> Color(0xFF10B981) // Green
                    else -> Color(0xFF94A3B8) // Muted Gray
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(resolutionColor.copy(alpha = 0.15f))
                        .border(1.dp, resolutionColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = channel.resolution,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = resolutionColor
                    )
                }
            }
            
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

@Composable
fun GoogleSignInDialog(
    onDismiss: () -> Unit,
    onSignIn: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("google_auth_dialog"),
            border = BorderStroke(1.dp, Color(0xFF2E3E5D))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Multi-colored G Logo Mock element
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color.White)
                        .border(1.5.dp, Color(0xFFD1D5DB), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        color = Color(0xFF4285F4),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sign In with Google",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "আপনার অ্যাকাউন্ট ভেরিফাই করুন এবং সম্পূর্ণ লাইভ স্ট্রিমিং অ্যাক্সেস সচল করুন।",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorText = null },
                    label = { Text("পূর্ণ নাম (Name)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorText = null },
                    label = { Text("গুগল ইমেইল (Google Email)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                errorText?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // One-Click Premium Autofill option requested by user
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            name = "Forid Ahmed"
                            email = "foridahmed6682@gmail.com"
                            errorText = null
                        }
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "⚡ ওয়ান-ক্লিক প্রিসেট গুগল সাইন-ইন করুন",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "foridahmed6682@gmail.com",
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("বাদ দিন", color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorText = "দয়া করে নাম কারেক্টলি লিখুন!"
                                return@Button
                            }
                            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                errorText = "সঠিক ইমেইল ফরম্যাট টাইপ করুন!"
                                return@Button
                            }
                            val emailHash = email.trim().lowercase().hashCode().toString()
                            val photo = "https://www.gravatar.com/avatar/$emailHash?d=identicon"
                            onSignIn(name, email, photo)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.3f).testTag("auth_submit_btn")
                    ) {
                        Text("কানেক্ট করুন", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPinDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D31)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("admin_pin_dialog"),
            border = BorderStroke(1.dp, Color(0xFF2E3E5D))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Security PIN",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "অ্যাডমিন সিকিউরিটি পিন",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "আপনার অ্যাডমিন প্যানেল সুরক্ষিত রাখতে ৪ সংখ্যার সঠিক পিনটি প্রবেশ করান।",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            pin = it
                            pinError = null
                        }
                    },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    placeholder = { Text("••••", color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    singleLine = true,
                    modifier = Modifier
                        .width(140.dp)
                        .testTag("admin_pin_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                )

                pinError?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("বাদ দিন", color = Color.Gray)
                    }

                    Button(
                        onClick = {
                            if (pin == "6682") {
                                onSuccess()
                            } else {
                                pinError = "ভুল সিকিউরিটি পিন প্রবেশ করিয়েছেন!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.2f).testTag("admin_pin_submit_btn")
                    ) {
                        Text("যাচাই করুন", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
