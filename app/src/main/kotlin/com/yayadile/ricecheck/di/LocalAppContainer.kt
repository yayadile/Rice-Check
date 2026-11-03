package com.yayadile.ricecheck.di

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Manual DI bridge: the Application owns the AppContainer, Compose reads it from here.
 */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer belum disediakan")
}
