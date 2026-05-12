package com.jp.foodyvilla_backoffice.presentation.utils

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

@SuppressLint("ContextCastToActivity")
@Composable
fun HideSystemBars() {
    val activity = LocalContext.current as Activity
    val window = activity.window
    val controller = WindowCompat.getInsetsController(window, window.decorView)

    SideEffect {
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
