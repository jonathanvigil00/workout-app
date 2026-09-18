package com.jvigil.hoofmode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jvigil.hoofmode.ui.navigation.HoofNavHost
import com.jvigil.hoofmode.ui.theme.HoofModeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HoofModeRoot()
        }
    }
}

@Composable
private fun HoofModeRoot() {
    HoofModeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            HoofNavHost()
        }
    }
}
