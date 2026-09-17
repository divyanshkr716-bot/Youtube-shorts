package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ShortsRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TealAccent

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateBrowserScreen(
    initialUrl: String = "https://www.google.com",
    onCloseBrowser: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var currentUrlInput by remember { mutableStateOf(initialUrl) }
    var displayedTitle by remember { mutableStateOf("Private Incognito Browser") }
    var isDesktopMode by remember { mutableStateOf(false) }
    var defaultMobileUserAgent by remember { mutableStateOf("") }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    // Clean up completely on dispose (leaving screen, recent tabs, closing)
    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.let { wv ->
                try {
                    wv.stopLoading()
                    wv.clearHistory()
                    wv.clearCache(true)
                    wv.clearFormData()
                    wv.clearSslPreferences()
                    wv.destroy()
                } catch (_: Exception) {}
            }
            try {
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()
            } catch (_: Exception) {}
        }
    }

    // Intercept hardware back button to navigate back in webview or close
    BackHandler {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onCloseBrowser()
        }
    }

    fun navigateToUrl(target: String) {
        val trimmed = target.trim()
        if (trimmed.isEmpty()) return
        val url = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else if (trimmed.contains(".") && !trimmed.contains(" ")) {
            "https://$trimmed"
        } else {
            "https://www.google.com/search?q=" + java.net.URLEncoder.encode(trimmed, "UTF-8")
        }
        currentUrlInput = url
        webViewInstance?.loadUrl(url)
        keyboardController?.hide()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        // Top Browser Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close Browser button
            IconButton(
                onClick = onCloseBrowser,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("btn_browser_close")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close Browser", tint = ShortsRed)
            }

            // Web Navigation back/forward
            IconButton(
                onClick = { if (webViewInstance?.canGoBack() == true) webViewInstance?.goBack() },
                enabled = canGoBack,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (canGoBack) Color.White else Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = { if (webViewInstance?.canGoForward() == true) webViewInstance?.goForward() },
                enabled = canGoForward,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (canGoForward) Color.White else Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // URL Search / Address Bar
            OutlinedTextField(
                value = currentUrlInput,
                onValueChange = { currentUrlInput = it },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("browser_url_input"),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated,
                    focusedBorderColor = TealAccent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Zero-History Private",
                        tint = SuccessGreen,
                        modifier = Modifier.size(14.dp)
                    )
                },
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = TealAccent,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = { webViewInstance?.reload() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = { navigateToUrl(currentUrlInput) }
                ),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // 3-Dot Options Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("btn_browser_menu")
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Browser Options", tint = Color.White)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(DarkSurfaceElevated)
                ) {
                    // Desktop Site Toggle
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDesktopMode) Icons.Default.PhoneAndroid else Icons.Default.Computer,
                                    contentDescription = null,
                                    tint = TealAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isDesktopMode) "Desktop Site: ON" else "Desktop Site (Desktop Mode)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (isDesktopMode) TealAccent else Color.White
                                    )
                                    Text(
                                        text = if (isDesktopMode) "Tap to switch to Mobile" else "Open full PC version of any site",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = {
                            showMenu = false
                            isDesktopMode = !isDesktopMode
                            webViewInstance?.let { wv ->
                                wv.settings.userAgentString = if (isDesktopMode) DESKTOP_USER_AGENT else defaultMobileUserAgent
                                wv.settings.useWideViewPort = isDesktopMode
                                wv.settings.loadWithOverviewMode = isDesktopMode
                                wv.reload()
                            }
                        }
                    )

                    // Home button
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Google Search", fontSize = 13.sp, color = Color.White)
                            }
                        },
                        onClick = {
                            showMenu = false
                            navigateToUrl("https://www.google.com")
                        }
                    )

                    // YouTube Desktop/Mobile
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = ShortsRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("YouTube", fontSize = 13.sp, color = Color.White)
                            }
                        },
                        onClick = {
                            showMenu = false
                            navigateToUrl("https://m.youtube.com")
                        }
                    )

                    // Zero-History Info indicator
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Zero-History Incognito", fontSize = 13.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    Text("No history, cache or cookies saved anywhere", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        onClick = { showMenu = false }
                    )

                    // Close Browser
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = ShortsRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Exit & Destroy Session", fontSize = 13.sp, color = ShortsRed, fontWeight = FontWeight.Bold)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onCloseBrowser()
                        }
                    )
                }
            }
        }

        // Desktop mode banner if active
        if (isDesktopMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TealAccent.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Computer, contentDescription = null, tint = TealAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Desktop Mode Active (Computer PC User-Agent)", fontSize = 11.sp, color = TealAccent, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = "Reset",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Loading Progress Bar
        if (isLoading && loadProgress < 1f) {
            LinearProgressIndicator(
                progress = { loadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = TealAccent,
                trackColor = Color.Transparent
            )
        }

        // WebView Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply applyWebView@{
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        defaultMobileUserAgent = settings.userAgentString

                        // Configure zero-history incognito settings
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = false
                            cacheMode = WebSettings.LOAD_NO_CACHE
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            allowFileAccess = false
                            allowContentAccess = false
                            mediaPlaybackRequiresUserGesture = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        }

                        // Do not save passwords or form data
                        clearHistory()
                        clearFormData()
                        clearCache(true)

                        // Cookie session only, no persistent cookies
                        CookieManager.getInstance().apply {
                            setAcceptCookie(true)
                            setAcceptThirdPartyCookies(this@applyWebView, true)
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                url?.let {
                                    currentUrlInput = it
                                }
                                canGoBack = canGoBack()
                                canGoForward = canGoForward()
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                url?.let {
                                    currentUrlInput = it
                                }
                                displayedTitle = view?.title ?: "Browser"
                                canGoBack = canGoBack()
                                canGoForward = canGoForward()
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                // Keep all web browsing inside our WebView
                                return false
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadProgress = newProgress / 100f
                                if (newProgress == 100) {
                                    isLoading = false
                                }
                                canGoBack = canGoBack()
                                canGoForward = canGoForward()
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                super.onReceivedTitle(view, title)
                                if (!title.isNullOrBlank()) {
                                    displayedTitle = title
                                }
                            }
                        }

                        webViewInstance = this
                        loadUrl(initialUrl)
                    }
                },
                update = { wv ->
                    // Settings remain updated
                }
            )
        }
    }
}
