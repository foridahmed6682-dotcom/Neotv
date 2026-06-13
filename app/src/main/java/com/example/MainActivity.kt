package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.IptvViewModel

class MainActivity : ComponentActivity() {
  private lateinit var iptvViewModel: IptvViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        iptvViewModel = viewModel<IptvViewModel>()
        val currentUserEmail by iptvViewModel.userEmailState.collectAsState()

        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
        ) { innerPadding ->
          MainScreen(
            viewModel = iptvViewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }

  override fun onUserLeaveHint() {
    super.onUserLeaveHint()
    if (::iptvViewModel.isInitialized) {
      val state = iptvViewModel.uiState.value
      if (state.isBackgroundPlayEnabled && state.selectedChannel != null) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
          try {
            enterPictureInPictureMode(android.app.PictureInPictureParams.Builder().build())
          } catch (e: Exception) {
            // Ignore
          }
        } else {
          @Suppress("DEPRECATION")
          try {
            enterPictureInPictureMode()
          } catch (e: Exception) {
            // Ignore
          }
        }
      }
    }
  }

  override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
    super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    if (::iptvViewModel.isInitialized) {
      iptvViewModel.setPipMode(isInPictureInPictureMode)
    }
  }
}

