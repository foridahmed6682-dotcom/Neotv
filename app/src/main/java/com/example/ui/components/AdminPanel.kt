package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Sponsor
import com.example.ui.viewmodel.GoogleUserInfo

@Composable
fun AdminPanel(
    sponsors: List<Sponsor>,
    onSaveSponsor: (Sponsor) -> Unit,
    onDeleteSponsor: (Sponsor) -> Unit,
    onClose: () -> Unit,
    googleUser: GoogleUserInfo?,
    onGoogleSignIn: (String, String, String) -> Unit,
    onGoogleSignOut: () -> Unit,
    isPlaylistSyncing: Boolean = false,
    playlistSyncResult: String? = null,
    onSyncPlaylist: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var newSponsorText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Google Login, 1 = Sponsor Notice, 2 = IPTV Sync
    var showAccountSelector by remember { mutableStateOf(false) }

    // Educational Guides Expansion State
    var step1Expanded by remember { mutableStateOf(true) }
    var step2Expanded by remember { mutableStateOf(false) }
    var step3Expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙️ settings & console",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "বন্ধ করুন",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onClose() }
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs (Now expanded to 3 items)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔒 Google Auth",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) Color.Black else Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📢 নোটিশ বোর্ড",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) Color.Black else Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selectedTab == 2) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { selectedTab = 2 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔗 গিটহাব সিঙ্ক",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 2) Color.Black else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Content Switcher
            if (selectedTab == 0) {
                // GOOGLE AUTHENTICATION VIEW + GUIDE
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Profile/Interactive Simulator Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (googleUser != null) {
                                // SIGNED IN VIEW
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black)
                                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = googleUser.photoUrl,
                                            contentDescription = "User profile logo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = googleUser.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Verified Profile",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = googleUser.email,
                                            fontSize = 12.sp,
                                            color = Color.LightGray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "👑 Premium VIP Customer",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { onGoogleSignOut() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("লগ আউট (Sign Out Account)", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                // SIGNED OUT / INTERACTIVE TRIGGER
                                Text(
                                    text = "গুগল সাইন-ইন ইন্টারঅ্যাক্টিভ ডেমো",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Google Account কানেক্ট করার অভিজ্ঞতা ও UI কেমন হবে তা নিচে ক্লিক করে এখনই ডেমো দেখুন।",
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Centered Styled Google Button
                                PremiumGoogleSignInButton(
                                    onClick = { showAccountSelector = true }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // EDUCATIONAL GUIDE HEADER
                    Text(
                        text = "🛠️ Google Sign-In ইন্টিগ্রেশন প্রসেস গাইড:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "আপনার এন্ড্রয়েড অ্যাপে গুগল সাইন-ইন আসল API যুক্ত করার জন্য নিচের ৩টি ধাপে কাজ করতে হবে:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Step 1 Accordion
                    StepAccordion(
                        stepNumber = "ধাপ ১",
                        title = "Google Cloud এবং Firebase কনসোল সেটআপ",
                        isExpanded = step1Expanded,
                        onToggle = { step1Expanded = !step1Expanded }
                    ) {
                        Text(
                            text = "১. প্রথমে Google Cloud Console (console.cloud.google.com) এ গিয়ে একটি নতুন প্রজেক্ট তৈরি করুন।\n" +
                                    "২. OAuth Consent Screen ট্যাব এ গিয়ে Internal বা External সিলেক্ট করে প্রয়োজনীয় তথ্য পূরণ করুন।\n" +
                                    "৩. Credentials এ গিয়ে 'Create Credentials' -> 'OAuth client ID' তে ক্লিক করুন।\n" +
                                    "৪. Application Type সিলেক্ট করুন 'Android'.\n" +
                                    "৫. আপনার অ্যাপ্লিকেশনের Package Name (com.aistudio.etc) এবং আপনার কম্পিউটারের SHA-1 fingerprint কী যুক্ত করুন। (SHA-1 কী পাওয়ার জন্য অ্যান্ড্রয়েড স্টুডিওর Gradle ট্যাব থেকে signingReport স্ক্রিপ্ট রান করুন)।\n" +
                                    "৬. এরপর Firebase Console এ গিয়ে আপনার এই অ্যান্ড্রয়েড প্রজেক্টটি যুক্ত করে Google authentication সচল করুন এবং 'google-services.json' ফাইলটি ডাউনলোড করে app ডিরেক্টরির ভেতর প্লেস করুন।",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 2 Accordion
                    StepAccordion(
                        stepNumber = "ধাপ ২",
                        title = "প্রয়োজনীয় Dependencies যুক্ত করা",
                        isExpanded = step2Expanded,
                        onToggle = { step2Expanded = !step2Expanded }
                    ) {
                        Text(
                            text = "আপনার app লেভেলের build.gradle.kts এর dependencies ব্লকে আধুনিক Google Credential Manager লাইব্রেরিটি যুক্ত করুন:\n\n" +
                                    "dependencies {\n" +
                                      "    // Google Credential Manager\n" +
                                    "    implementation(\"androidx.credentials:credentials:1.2.2\")\n" +
                                    "    implementation(\"androidx.credentials:credentials-play-services-auth:1.2.2\")\n" +
                                    "    implementation(\"com.google.android.libraries.identity.googleid:googleid:1.1.1\")\n" +
                                    "}",
                            fontSize = 11.sp,
                            color = Color(0xFFA5B4FC),
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Step 3 Accordion
                    StepAccordion(
                        stepNumber = "ধাপ ৩",
                        title = "Kotlin কোড ইমপ্লিমেন্টেশন (Sign-In Logic)",
                        isExpanded = step3Expanded,
                        onToggle = { step3Expanded = !step3Expanded }
                    ) {
                        Text(
                            text = "Credential Manager দিয়ে ইউজারকে সাইন-ইন করানোর স্ট্যান্ডার্ড কোড স্ট্রাকচার:\n\n" +
                                    "import androidx.credentials.CredentialManager\n" +
                                    "import androidx.credentials.GetCredentialRequest\n" +
                                    "import com.google.android.libraries.identity.googleid.GetGoogleIdOption\n" +
                                    "\n" +
                                    "suspend fun performGoogleSignIn(context: Context) {\n" +
                                    "    val credentialManager = CredentialManager.create(context)\n" +
                                    "    \n" +
                                    "    val googleIdOption = GetGoogleIdOption.Builder()\n" +
                                    "        .setFilterByAuthorizedAccounts(false)\n" +
                                    "        .setServerClientId(\"YOUR_WEB_CLIENT_ID_FROM_GOOGLE_CONSOLE\")\n" +
                                    "        .setAutoSelectEnabled(true)\n" +
                                    "        .build()\n" +
                                    "        \n" +
                                    "    val request = GetCredentialRequest.Builder()\n" +
                                    "        .addCredentialOption(googleIdOption)\n" +
                                    "        .build()\n" +
                                    "        \n" +
                                    "    try {\n" +
                                    "        val result = credentialManager.getCredential(\n" +
                                    "            context = context,\n" +
                                    "            request = request\n" +
                                    "        )\n" +
                                    "        // handleCredentialSuccess(result.credential)\n" +
                                    "    } catch (e: Exception) {\n" +
                                      "        // handleCredentialFailure(e)\n" +
                                    "    }\n" +
                                    "}",
                            fontSize = 11.sp,
                            color = Color(0xFFC084FC),
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(8.dp)
                        )
                    }
                }
            } else if (selectedTab == 1) {
                // SPONSOR ADS MANAGEMENT VIEW
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    Text(
                        text = "Manage Sponsor Notice Copy:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Text input
                    OutlinedTextField(
                        value = newSponsorText,
                        onValueChange = { newSponsorText = it },
                        label = { Text("নতুন স্ক্রোলিং নোটিশ বার্তা লিখুন...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (newSponsorText.isNotBlank()) {
                                onSaveSponsor(Sponsor(name = "SPONSOR", description = newSponsorText, isActive = true))
                                newSponsorText = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Publish Sponsor Ticker", fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Active Ads list (Tap to delete / মুছে ফেলতে ক্লিক করুন):",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (sponsors.isEmpty()) {
                            item {
                                Text(
                                    text = "No custom sponsors. Showing default notice.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        } else {
                            items(sponsors) { sponsor ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onDeleteSponsor(sponsor) },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF334155))
                                ) {
                                    Text(
                                        text = sponsor.description,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // IPTV GITHUB PLAYLIST SYNC TAB
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    var customUrl by remember { mutableStateOf("https://raw.githubusercontent.com/byte-capsule/IPTV-Daily-Update/main/playlists/bd.m3u") }

                    Text(
                        text = "🔗 IPTV M3U / GitHub প্লেলিস্ট সিঙ্ক করুন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "যেকোনো সচল IPTV লিংক বা GitHub m3u প্লেলিস্ট লিংক নিচে বসিয়ে 'সিঙ্ক করুন' বাটনে ক্লিক করলেই সব চ্যানেল ইনস্ট্যান্টলি লোড হয়ে যাবে।",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // URL Input field
                    OutlinedTextField(
                        value = customUrl,
                        onValueChange = { customUrl = it },
                        label = { Text("IPTV M3U Playlist URL (GitHub বা raw লিংক)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isPlaylistSyncing) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = playlistSyncResult ?: "ভিডিও প্লেলিস্ট ডাউনলোড হচ্ছে, দয়া করে অপেক্ষা করুন...",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (customUrl.isNotBlank()) {
                                    onSyncPlaylist(customUrl)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("সিঙ্ক করুন (Sync Playlist)", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    playlistSyncResult?.let { result ->
                        if (!isPlaylistSyncing) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Status",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = result,
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "🚀 জনপ্রিয় গিটহাব প্লেলিস্ট প্রিসেটসমূহ (এক ক্লিকে সিঙ্ক):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset Button 1
                    PresetSyncButton(
                        title = "🇧🇩 BD IPTV Daily Update (Byte-Capsule)",
                        description = "বাংলাদেশী ক্যাটাগরির অন্যতম জনপ্রিয় ও নিয়মিত আপডেটেড প্লেলিস্ট",
                        onClick = {
                            customUrl = "https://raw.githubusercontent.com/byte-capsule/IPTV-Daily-Update/main/playlists/bd.m3u"
                            onSyncPlaylist("https://raw.githubusercontent.com/byte-capsule/IPTV-Daily-Update/main/playlists/bd.m3u")
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset Button 2
                    PresetSyncButton(
                        title = "🇧🇩 Bangladesh IPTV List (OrhanAyut)",
                        description = "সকল দেশী চ্যানেলের নির্ভরযোগ্য সরাসরি সম্প্রচার সংযোগের বড় সংগ্রহ",
                        onClick = {
                            customUrl = "https://raw.githubusercontent.com/orhanayut/bd-iptv/main/bd-iptv.m3u"
                            onSyncPlaylist("https://raw.githubusercontent.com/orhanayut/bd-iptv/main/bd-iptv.m3u")
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset Button 3
                    PresetSyncButton(
                        title = "🌍 IPTV-org Bangladesh streams",
                        description = "গ্লোবাল আইপিটিভি অর্গানাইজেশন কর্তৃক ভেরিফাইড বাংলাদেশ চ্যানেল সংযোগ",
                        onClick = {
                            customUrl = "https://raw.githubusercontent.com/iptv-org/iptv/master/streams/bd.m3u"
                            onSyncPlaylist("https://raw.githubusercontent.com/iptv-org/iptv/master/streams/bd.m3u")
                        }
                    )
                }
            }
        }
    }

    // Google Sign-In Selector Sheet/Dialog Simulation
    if (showAccountSelector) {
        Dialog(onDismissRequest = { showAccountSelector = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Google",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )
                    Text(
                        text = "Choose an account to continue to Slate IPTV\n(এন্টারপ্রাইজ সাইন-ইন সেশন)",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Divider(color = Color(0xFF334155))

                    // Account Option 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onGoogleSignIn(
                                    "Forid Ahmed",
                                    "foridahmed6682@gmail.com",
                                    "https://images.unsplash.com/photo-1544005313-94ddf0286df2?fit=crop&w=120&h=120"
                                )
                                showAccountSelector = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("FA", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Forid Ahmed", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("foridahmed6682@gmail.com", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Divider(color = Color(0xFF334155))

                    // Account Option 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onGoogleSignIn(
                                    "Tester Pro",
                                    "guest@example.com",
                                    "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?fit=crop&w=120&h=120"
                                )
                                showAccountSelector = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF64748B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Guest User", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("guest@example.com", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { showAccountSelector = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun PresetSyncButton(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PremiumGoogleSignInButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Row(modifier = Modifier.size(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEA4335)), // Red
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Sign In with Google",
                color = Color(0xFF1E293B),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun StepAccordion(
    stepNumber: String,
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.clickable { onToggle() }.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stepNumber,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    content()
                }
            }
        }
    }
}
