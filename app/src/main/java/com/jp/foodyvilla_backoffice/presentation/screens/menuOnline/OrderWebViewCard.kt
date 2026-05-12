package com.jp.foodyvilla_backoffice.presentation.screens.menuOnline

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

private val Gold = Color(0xFFFFD700)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun OrderOnlineScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val isAppInDarkTheme = isSystemInDarkTheme()

    // --- State Management ---
    var isRefreshing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    // We remember the WebView instance to maintain state during recompositions
    val webView = remember { WebView(context) }

    // Handle Back Navigation
    BackHandler {
        if (webView.canGoBack()) webView.goBack() else onBackClick()
    }

    // Connectivity Helper
    fun isOnline(): Boolean {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val capabilities = cm?.getNetworkCapabilities(cm.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Foody Villa Online",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "order.foodyvilla_backoffice.in",
                            style = MaterialTheme.typography.labelSmall.copy(color = colors.primary)
                        )
                    }
                },

                actions = {
                    Surface(
                        color = Gold,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            "LIVE",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold, color = Color.Black
                            )
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background)
        ) {
            if (hasError) {
                NoInternetScreen {
                    if (isOnline()) {
                        hasError = false
                        isLoading = true
                        webView.reload()
                    }
                }
            } else {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        if (isOnline()) {
                            isRefreshing = true
                            webView.reload()
                        } else {
                            hasError = true
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            webView.apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )

                                // Standard WebView Configuration
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                }

                                // Initial Dark Mode Setup
                                applyDarkMode(this, isAppInDarkTheme)

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, fav: Bitmap?) {
                                        isLoading = true
                                    }
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isLoading = false
                                        isRefreshing = false
                                    }
                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        // Only show error screen if the main page fails
                                        if (request?.isForMainFrame == true) {
                                            hasError = true
                                            isLoading = false
                                            isRefreshing = false
                                        }
                                    }
                                }

                                if (isOnline()) loadUrl("https://order.foodyvilla.in")
                                else hasError = true

                                // This allows PullToRefresh to trigger only when WebView is at top
                                setOnTouchListener { v, event ->
                                    v.parent.requestDisallowInterceptTouchEvent(v.scrollY > 0)
                                    false
                                }
                            }
                        },
                        update = { view ->
                            // Update Dark Mode dynamically if theme changes while screen is visible
                            applyDarkMode(view, isAppInDarkTheme)
                        }
                    )

                    if (isLoading && !isRefreshing) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                            color = colors.primary,
                            trackColor = Color.Transparent
                        )
                    }
                }
            }
        }
    }
}

/**
 * Handles the logic for Force Dark mode via the AndroidX Webkit library
 */
private fun applyDarkMode(webView: WebView, isDark: Boolean) {
    if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
        WebSettingsCompat.setForceDark(
            webView.settings,
            if (isDark) WebSettingsCompat.FORCE_DARK_ON else WebSettingsCompat.FORCE_DARK_OFF
        )
    }
    // Prevent white flicker by setting background color
    webView.setBackgroundColor((if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE))
}

@Composable
private fun NoInternetScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.WifiOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(24.dp))
        Text("No Connection", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Please check your internet settings to view the menu and place orders.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth(0.6f)) {
            Text("Retry")
        }
    }
}