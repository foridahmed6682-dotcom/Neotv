package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainScreen
import com.example.ui.theme.SlateIPTVTheme
import com.example.ui.viewmodel.IptvViewModel
import com.example.ui.viewmodel.IptvViewModelFactory

class MainActivity : ComponentActivity() {
    
    private val viewModel: IptvViewModel by viewModels {
        IptvViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Flow layout edge-to-edge behind translucent system elements
        enableEdgeToEdge()
        
        setContent {
            SlateIPTVTheme {
                MainScreen(
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding() // Safeguard notch bounds
                )
            }
        }
    }
}
