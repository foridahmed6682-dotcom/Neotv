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
        viewModelScope.launch {
            try {
                // Instantly pre-load default lists to give users immediate playback access
                repository.seedDefaultDataIfEmpty()
                
                // Let's set the first channel as selected by default once channels load
                channels.firstOrNull { it.isNotEmpty() }?.firstOrNull()?.let { firstChannel ->
                    _selectedChannel.value = firstChannel
                }
            } catch (e: Exception) {
                Log.e("IptvViewModel", "Initialization error: ${e.message}")
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
                        val (isLinkActive, latencyMs) = repository.verifyChannelLinkWithLatency(channel.url)
                        if (!isLinkActive) {
                            Log.d("IptvViewModel", "Channel [${channel.name}] is down. Offlining stream in Room.")
                            repository.updateChannelValidation(channel.url, false, 99999L)
                            // If currently playing channel goes offline, deselect it or warn user
                            if (_selectedChannel.value?.url == channel.url) {
                                // Keep it loaded but mark as offline, or keep play attempt
                            }
                        } else {
                            Log.d("IptvViewModel", "Channel [${channel.name}] active. Speed measured: ${latencyMs}ms.")
                            repository.updateChannelValidation(channel.url, true, latencyMs)
                        }
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
