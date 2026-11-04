package com.yayadile.ricecheck

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.yayadile.ricecheck.di.LocalAppContainer
import com.yayadile.ricecheck.ui.navigation.RiceCheckRoot
import com.yayadile.ricecheck.ui.theme.RiceCheckTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is always light, so force dark (black) system bar icons regardless of the
        // device dark-mode setting: battery/clock indicators and the navigation buttons.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )

        val container = (application as RiceCheckApplication).container

        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                RiceCheckTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        RiceCheckRoot()
                    }
                }
            }
        }
    }
}
