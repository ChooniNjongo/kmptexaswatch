package com.jetbrains.spacetutorial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jetbrains.spacetutorial.navigation.AppNavigation
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TexasWatchTheme {
                AppNavigation()
            }
        }
    }
}
