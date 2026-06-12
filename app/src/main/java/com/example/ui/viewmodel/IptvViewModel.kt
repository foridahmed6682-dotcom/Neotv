package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Channel
import com.example.data.model.Sponsor
import com.example.data.repository.IptvRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IptvUiState(
    val selectedChannel: Channel? = null,
    val selectedCategory: String = "All",
    val playlistUrl: String = "https://iptv-org.github.io/iptv/countries/bd.m3u", // default Bangladesh to highlight Bangla instantly
    val isLoadingChannels: Boolean = false,
    val isSyncingSponsors: Boolean = false,
    val message: String? = null,
    val isErrorMessage: Boolean = false,
    // TV digits
    val digitSwitchCode: String = "",
    val digitSwitchChannelName: String? = null,
    // Admin state
    val isAdminLoggedIn: Boolean = false,
    val adminPasswordChallengeActive: Boolean = false,
    val editingSponsor: Sponsor? = null, // if non-null, we are editing this sponsor
    val listSponsorsForAdmin: List<Sponsor> = emptyList(),
    val adminEmails: List<String> = emptyList(),
    val isSyncingAdmins: Boolean = false
)

class IptvViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = IptvRepository(
        context = application,
        channelDao = database.channelDao(),
        sponsorDao = database.sponsorDao()
    )

    private val _uiState = MutableStateFlow(IptvUiState())
    val uiState: StateFlow<IptvUiState> = _uiState.asStateFlow()

    // Active sponsors matching list
    val activeSponsors: StateFlow<List<Sponsor>> = repository.activeSponsorsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All channels in DB (sorted Bangladesh -> India -> Global)
    val allChannels: StateFlow<List<Channel>> = repository.allChannelsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered channels based on selectedCategory
    val filteredChannels: StateFlow<List<Channel>> = combine(
        allChannels,
        _uiState
    ) { channels, state ->
        if (state.selectedCategory == "All") {
            channels
        } else {
            channels.filter { it.category.equals(state.selectedCategory, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin sponsors list
    init {
        viewModelScope.launch {
            // Seed sponsors and sync immediately
            repository.seedDefaultSponsorsIfEmpty()
            repository.syncSponsors()
            fetchAdmins()

            // Ensure the primary email is always in the list for first-time firebase setups
            val primaryEmail = "foridahmed6682@gmail.com"
            repository.addAdminToFirestore(primaryEmail)

            // Observe all sponsors for admin list
            repository.allSponsorsFlow.collect { sponsors ->
                _uiState.update { it.copy(listSponsorsForAdmin = sponsors) }
            }
        }

        // Try load default playlist on launch
        loadPlaylist(_uiState.value.playlistUrl)
    }

    private fun fetchAdmins() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingAdmins = true) }
            repository.fetchAdminsFromFirestore().onSuccess { emails ->
                _uiState.update { it.copy(adminEmails = emails, isSyncingAdmins = false) }
            }.onFailure {
                _uiState.update { it.copy(isSyncingAdmins = false) }
            }
        }
    }

    fun addAdmin(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingAdmins = true) }
            repository.addAdminToFirestore(email).onSuccess {
                fetchAdmins()
                _uiState.update { it.copy(message = "Admin $email added successfully") }
            }.onFailure { e ->
                _uiState.update { it.copy(message = "Error adding admin: ${e.message}", isErrorMessage = true, isSyncingAdmins = false) }
            }
        }
    }

    fun removeAdmin(email: String) {
        if (email == "foridahmed6682@gmail.com") return // Prevent removing primary admin

        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingAdmins = true) }
            repository.removeAdminFromFirestore(email).onSuccess {
                fetchAdmins()
                _uiState.update { it.copy(message = "Admin $email removed") }
            }.onFailure { e ->
                _uiState.update { it.copy(message = "Error removing admin: ${e.message}", isErrorMessage = true, isSyncingAdmins = false) }
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        // Select first channel in the filtered list if category changes
        viewModelScope.launch {
            // Small delay to let combining complete
            delay(50)
            val currentFiltered = filteredChannels.value
            if (currentFiltered.isNotEmpty()) {
                val currentSelected = _uiState.value.selectedChannel
                // If current selected is NOT in new filter, default to first item
                if (currentSelected == null || !currentFiltered.contains(currentSelected)) {
                    selectChannel(currentFiltered[0])
                }
            }
        }
    }

    fun selectChannel(channel: Channel) {
        _uiState.update { it.copy(selectedChannel = channel) }
    }

    fun updatePlaylistUrl(url: String) {
        _uiState.update { it.copy(playlistUrl = url) }
    }

    fun loadPlaylist(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingChannels = true, message = "Fetching channels...", isErrorMessage = false) }
            val result = repository.loadPlaylist(url)
            result.onSuccess { count ->
                _uiState.update {
                    it.copy(
                        isLoadingChannels = false,
                        playlistUrl = url,
                        message = "Successfully cached $count Live IPTV Channels!",
                        isErrorMessage = false
                    )
                }
                // Automatically play the first stream (default Bangladesh sorting is top!)
                val channels = allChannels.value
                if (channels.isNotEmpty()) {
                    selectChannel(channels[0])
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoadingChannels = false,
                        message = "Failed to load: ${exception.message}",
                        isErrorMessage = true
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    // --- TV Remote Hotkeys Digit Accumulator ---
    private var digitChangeJob: Job? = null

    fun onNumericKeyPress(digit: Int) {
        digitChangeJob?.cancel()
        val currentDigits = _uiState.value.digitSwitchCode + digit
        // Find matching channel by number
        val list = allChannels.value
        val channelNumberInt = currentDigits.toIntOrNull() ?: 1
        val matchChannel = list.find { it.channelNumber == channelNumberInt }

        _uiState.update {
            it.copy(
                digitSwitchCode = currentDigits,
                digitSwitchChannelName = matchChannel?.name ?: "Searching channels..."
            )
        }

        digitChangeJob = viewModelScope.launch {
            // Wait 1.5 seconds for final digit confirmation
            delay(1500)
            if (matchChannel != null) {
                selectChannel(matchChannel)
                _uiState.update {
                    it.copy(
                        message = "Switched to ${matchChannel.name} (Ch ${matchChannel.channelNumber})",
                        isErrorMessage = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        message = "Channel #$currentDigits not found in current playlist.",
                        isErrorMessage = true
                    )
                }
            }
            // Clear prompt
            _uiState.update {
                it.copy(
                    digitSwitchCode = "",
                    digitSwitchChannelName = null
                )
            }
        }
    }

    // --- Admin Operations ---

    fun challengeAdminPassword(password: String) {
        // secure challenge for developer: foridahmed6682@gmail.com
        if (password == "admin6682" || password == "faridahmed" || password == "foridahmed") {
            _uiState.update {
                it.copy(
                    isAdminLoggedIn = true,
                    adminPasswordChallengeActive = false,
                    message = "Admin control panel successfully unlocked."
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    message = "Access Denied. Invalid Authorization Code.",
                    isErrorMessage = true
                )
            }
        }
    }

    fun logoutAdmin() {
        _uiState.update { it.copy(isAdminLoggedIn = false) }
    }

    fun toggleAdminChallenge(active: Boolean) {
        _uiState.update { it.copy(adminPasswordChallengeActive = active) }
    }

    fun setEditingSponsor(sponsor: Sponsor?) {
        _uiState.update { it.copy(editingSponsor = sponsor) }
    }

    fun saveSponsor(sponsor: Sponsor) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingSponsors = true) }
            val result = repository.addSponsor(sponsor)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        editingSponsor = null,
                        message = "Sponsor advertisement configuration saved.",
                        isSyncingSponsors = false
                    )
                }
                repository.syncSponsors()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        message = "Saved locally. Cloud sync failed: ${e.message}",
                        isErrorMessage = false, // Not a fatal error
                        isSyncingSponsors = false,
                        editingSponsor = null
                    )
                }
            }
        }
    }

    fun deleteSponsor(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingSponsors = true) }
            val result = repository.deleteSponsor(id)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        message = "Sponsor advertisement successfully deleted.",
                        isSyncingSponsors = false
                    )
                }
                repository.syncSponsors()
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        message = "Deleted locally. Cloud removal failed: ${e.message}",
                        isErrorMessage = false,
                        isSyncingSponsors = false
                    )
                }
            }
        }
    }
}
