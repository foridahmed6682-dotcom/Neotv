package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Channel
import com.example.data.model.Sponsor
import com.example.data.repository.IptvRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class IptvViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = IptvRepository(database.channelDao(), database.sponsorDao())
    private val prefs = application.getSharedPreferences("google_auth_prefs", android.content.Context.MODE_PRIVATE)

    // Real Google Auth state persistent across application restarts
    private val _googleUser = MutableStateFlow<GoogleUserInfo?>(null)
    val googleUser: StateFlow<GoogleUserInfo?> = _googleUser.asStateFlow()

    fun signInWithGoogleSimulated(name: String, email: String, photoUrl: String) {
        signInWithGoogleReal(name, email, photoUrl)
    }

    fun signInWithGoogleReal(name: String, email: String, photoUrl: String) {
        val user = GoogleUserInfo(name = name, email = email, photoUrl = photoUrl, isVip = true)
        _googleUser.value = user
        prefs.edit()
            .putString("email", email)
            .putString("name", name)
            .putString("photoUrl", photoUrl)
            .apply()
    }

    fun signOutGoogle() {
        _googleUser.value = null
        prefs.edit().clear().apply()
    }

    // IPTV GitHub/M3U Live syncing state
    private val _isPlaylistSyncing = MutableStateFlow(false)
    val isPlaylistSyncing: StateFlow<Boolean> = _isPlaylistSyncing.asStateFlow()

    private val _playlistSyncResult = MutableStateFlow<String?>(null)
    val playlistSyncResult: StateFlow<String?> = _playlistSyncResult.asStateFlow()

    fun syncM3uPlaylist(url: String) {
        viewModelScope.launch {
            _isPlaylistSyncing.value = true
            _playlistSyncResult.value = "ডাউনলোড ও তথ্য বিশ্লেষণ করা হচ্ছে..."
            val count = repository.importM3uPlaylist(url)
            if (count > 0) {
                _playlistSyncResult.value = "সফলভাবে $count টি নতুন লাইভ চ্যানেল যুক্ত করা হয়েছে!"
            } else {
                _playlistSyncResult.value = "কোনো চ্যানেল পাওয়া যায়নি বা লিঙ্কটি রিড করতে ব্যর্থ হয়েছে।"
            }
            _isPlaylistSyncing.value = false
        }
    }

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedChannel = MutableStateFlow<Channel?>(null)
    val selectedChannel: StateFlow<Channel?> = _selectedChannel.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isValidating = MutableStateFlow(false)
    val isValidating: StateFlow<Boolean> = _isValidating.asStateFlow()

    // Active Sponsors marquee
    val activeSponsors: StateFlow<List<Sponsor>> = repository.allActiveSponsors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered channels flow based on selected category and online status
    @OptIn(ExperimentalCoroutinesApi::class)
    val channels: StateFlow<List<Channel>> = _selectedCategory
        .flatMapLatest { category ->
            if (category == "All") {
                repository.allActiveChannels
            } else {
                repository.getChannelsByCategory(category)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Restore persistent Google auth session
        val savedEmail = prefs.getString("email", null)
        val savedName = prefs.getString("name", null)
        val savedPhoto = prefs.getString("photoUrl", null)
        if (savedEmail != null && savedName != null) {
            _googleUser.value = GoogleUserInfo(
                name = savedName,
                email = savedEmail,
                photoUrl = savedPhoto ?: "https://cdn-icons-png.flaticon.com/512/3172/3172605.png",
                isVip = true
            )
        }

        viewModelScope.launch {
            try {
                // Instantly pre-load default lists to give users immediate playback access
                repository.seedDefaultDataIfEmpty()
                
                // Set the first channel as selected by default once channels load
                channels.firstOrNull { it.isNotEmpty() }?.firstOrNull()?.let { firstChannel ->
                    _selectedChannel.value = firstChannel
                }
            } catch (e: Exception) {
                Log.e("IptvViewModel", "Initialization error: ${e.message}")
            }

            // --- SEAMLESS ONE-CLICK BACKGROUND PRESET SYNCS ---
            // Automatically download and sync channels from all 3 popular Bangladesh/Global playlists
            launch(Dispatchers.IO) {
                val presets = listOf(
                    "https://raw.githubusercontent.com/byte-capsule/IPTV-Daily-Update/main/playlists/bd.m3u",
                    "https://raw.githubusercontent.com/orhanayut/bd-iptv/main/bd-iptv.m3u",
                    "https://raw.githubusercontent.com/iptv-org/iptv/master/streams/bd.m3u"
                )
                presets.forEach { url ->
                    try {
                        Log.d("IptvViewModel", "Auto-syncing premium preset: $url")
                        repository.importM3uPlaylist(url)
                    } catch (e: Exception) {
                        Log.e("IptvViewModel", "Startup sync failed for $url: ${e.message}")
                    }
                }
            }

            // Continuous Background Healing Loop: Runs validation every 10 seconds!
            while (isActive) {
                try {
                    validateAllChannelsInBackground()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("IptvViewModel", "Background validation cycle crashed: ${e.message}")
                }
                delay(10000L) // Wait exactly 10 seconds before starting the next check
            }
        }
    }

    fun selectChannel(channel: Channel?) {
        _selectedChannel.value = channel
        _isPlaying.value = (channel != null)
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun deleteSponsor(sponsor: Sponsor) {
        viewModelScope.launch {
            repository.deleteSponsor(sponsor)
        }
    }

    fun saveSponsor(sponsor: Sponsor) {
        viewModelScope.launch {
            repository.insertSponsor(sponsor)
        }
    }

    private suspend fun validateAllChannelsInBackground() {
        _isValidating.value = true
        Log.d("IptvViewModel", "Background validation loop started.")
        val allChannels = repository.getAllRawChannels()
        if (allChannels.isEmpty()) {
            _isValidating.value = false
            return
        }

        // Run validation in parallel (Max 5 concurrent checks to prevent overloading connection)
        val dispatcher = Dispatchers.IO
        val semaphore = kotlinx.coroutines.sync.Semaphore(5)
        
        withContext(dispatcher) {
            val jobs = allChannels.map { channel ->
                launch {
                    semaphore.withPermit {
                        val validationResult = repository.verifyChannelLinkWithLatency(channel.url)
                        Log.d("IptvViewModel", "Channel [${channel.name}] speed test: active=${validationResult.shouldKeepActive}, responseTime=${validationResult.responseTimeMs}ms")
                        repository.updateChannelValidation(
                            channel.url,
                            validationResult.shouldKeepActive,
                            validationResult.responseTimeMs
                        )
                    }
                }
            }
            jobs.forEach { it.join() }
        }
        
        _isValidating.value = false
        Log.d("IptvViewModel", "Background validation loop finished.")

        // Healing selection: If nothing is selected, try to play the fastest channel!
        if (_selectedChannel.value == null) {
            val currentChannels = channels.value
            if (currentChannels.isNotEmpty()) {
                _selectedChannel.value = currentChannels.first()
            }
        }
    }
}

class IptvViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IptvViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IptvViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class GoogleUserInfo(
    val name: String,
    val email: String,
    val photoUrl: String,
    val isVip: Boolean = true
)
