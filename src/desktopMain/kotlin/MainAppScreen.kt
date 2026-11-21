import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URI
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

@Composable
fun MainAppScreen(
    appThemeSetting: String,
    onThemeChange: (String) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    isMaterialYou: Boolean,
    onMaterialYouChange: (Boolean) -> Unit,
    selectedTab: String,
    onTabChange: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val prefs = remember { java.util.prefs.Preferences.userRoot().node("app_prefs") }
    val defaultUserAgent = "GS.HTTP/1.0"
    val version = "1.0.4"

    val strings = remember(currentLanguage) {
        getStringsForLanguage(currentLanguage)
    }

    var isThemeScreenOpen by remember { mutableStateOf(false) }
    var isLanguageScreenOpen by remember { mutableStateOf(false) }
    var isResponseInspectorOpen by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        isThemeScreenOpen = false
        isLanguageScreenOpen = false
        isResponseInspectorOpen = false
    }

    var activeSearchTab by remember { mutableStateOf("Body") }
    var searchQuery by remember { mutableStateOf("") }

    var followRedirectsSetting by remember { mutableStateOf(prefs.getBoolean("follow_redirects", true)) }
    var requestTimeoutSetting by remember { mutableStateOf(prefs.getInt("request_timeout", 10)) }
    var ignoreSslErrorsSetting by remember { mutableStateOf(prefs.getBoolean("ignore_ssl", false)) }
    var verifySslSetting by remember { mutableStateOf(prefs.getBoolean("verify_ssl", true)) }
    var customUserAgentSetting by remember { mutableStateOf(prefs.get("user_agent", defaultUserAgent)) }

    var urlInput by remember { mutableStateOf("") }
    var headersInput by remember { mutableStateOf("") }
    var bodyInput by remember { mutableStateOf("") }

    var resText by remember { mutableStateOf("") }
    var resTextColor by remember { mutableStateOf(Color.White) }
    var safeText by remember { mutableStateOf("") }
    var safeTextColor by remember { mutableStateOf(Color.White) }
    var responseTimeText by remember { mutableStateOf("") }
    var responseSizeText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var responseBodyText by remember { mutableStateOf("") }
    var responseHeadersText by remember { mutableStateOf("") }
    var responseCookiesText by remember { mutableStateOf("") }
    var lastValidUrl by remember { mutableStateOf("") }

    val scanHistoryList = remember {
        val savedHistory = prefs.get("scan_history_items", "")
        val list = mutableStateListOf<String>()
        if (savedHistory.isNotEmpty()) {
            list.addAll(savedHistory.split("\n"))
        }
        list
    }

    val saveHistoryToPrefs: () -> Unit = {
        prefs.put("scan_history_items", scanHistoryList.joinToString("\n"))
    }

    val httpMethods = listOf("GET", "POST", "PUT", "HEAD", "DELETE", "PATCH", "OPTIONS", "TRACE", "CONNECT")
    var selectedMethodIndex by remember { mutableStateOf(0) }
    val selectedMethod = httpMethods[selectedMethodIndex]

    val isDark = when (appThemeSetting) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val textColorPrimary = if (isDark) Color.White else Color(0xFF1C1B1F)
    val textColorSecondary = if (isDark) Color.Gray else Color(0xFF5E5E62)
    val cardBgColor = if (isDark) Color(0xFF1A1A1A) else Color(0xFFEEEEEE)
    val monochromeAccent = if (isDark) Color.White else Color.Black
    val monochromeSecondary = if (isDark) Color(0xFF888888) else Color(0xFF666666)

    var searchQueryInput by remember { mutableStateOf("") }
    val searchResultsList = remember { mutableStateListOf<String>() }
    var isSearchLoading by remember { mutableStateOf(false) }

    val runSearch: () -> Unit = {
        val query = searchQueryInput.trim().lowercase().replace(" ", "")
        if (query.isNotEmpty()) {
            isSearchLoading = true
            searchResultsList.clear()

            scope.launch(Dispatchers.IO) {
                val topExtensions = listOf("com", "org", "net", "ru", "io", "me", "co", "cc", "app", "dev")
                val activeDomains = mutableListOf<String>()
                val candidateUrls = topExtensions.map { ext -> "$query.$ext" }

                candidateUrls.map { domain ->
                    launch {
                        try {
                            val address = InetAddress.getByName(domain)
                            val socket = Socket()
                            socket.connect(InetSocketAddress(address, 80), 1200)
                            socket.close()

                            synchronized(activeDomains) {
                                activeDomains.add(domain)
                            }
                        } catch (_: Exception) {
                        }
                    }
                }.joinAll()

                withContext(Dispatchers.Main) {
                    if (activeDomains.isEmpty()) {
                        searchResultsList.add(strings.notFound)
                    } else {
                        val sorted = activeDomains.sortedBy { ext ->
                            when {
                                ext.endsWith(".com") -> 0
                                ext.endsWith(".ru") -> 1
                                else -> 2
                            }
                        }
                        searchResultsList.addAll(sorted)
                    }

                    val searchLogItem = "[ПОИСК] Ключевое слово: '$query' -> Найдено доменов: ${activeDomains.size}"
                    scanHistoryList.add(0, searchLogItem)
                    saveHistoryToPrefs()
                    isSearchLoading = false
                }
            }
        }
    }
    val runScan: () -> Unit = {
        val url = urlInput.trim()
        val hasSpaces = url.contains(" ")
        val isInvalidProtocol = (url.startsWith("https:/") && !url.startsWith("https://")) ||
                (url.startsWith("http:/") && !url.startsWith("http://"))

        if (url.isEmpty() || hasSpaces || isInvalidProtocol) {
            resText = strings.statusError
            resTextColor = monochromeAccent
            safeText = strings.statusInvalid
            safeTextColor = monochromeSecondary
            responseTimeText = ""
            responseSizeText = ""
            isLoading = false
        } else {
            isLoading = true
            resText = ""
            safeText = ""
            responseTimeText = ""
            responseSizeText = ""
            responseBodyText = ""
            responseHeadersText = ""
            responseCookiesText = ""
            lastValidUrl = ""

            scope.launch(Dispatchers.IO) {
                val fullUrl = if (url.startsWith("http")) url else "https://$url"
                val startTimeNs = System.nanoTime()

                if (selectedMethod == "CONNECT") {
                    try {
                        val uri = URI(fullUrl)
                        val host = uri.host ?: throw IllegalArgumentException("Invalid host")
                        val port = if (uri.port != -1) uri.port else if (uri.scheme == "http") 80 else 443

                        Socket().use { socket ->
                            socket.connect(InetSocketAddress(host, port), requestTimeoutSetting * 1000)
                        }
                        val elapsedMs = (System.nanoTime() - startTimeNs) / 1_000_000

                        resText = "CONNECT 200"
                        resTextColor = monochromeAccent
                        safeText = "${strings.statusConnected} ($host:$port)"
                        safeTextColor = monochromeSecondary
                        responseTimeText = "$elapsedMs ms"
                        responseSizeText = "0 B"
                        lastValidUrl = fullUrl
                        responseBodyText = "TCP connection established successfully to $host:$port in $elapsedMs ms."
                        responseHeadersText = "Host: $host\nPort: $port\nProtocol: TCP"
                        responseCookiesText = strings.cookiesEmpty

                        scanHistoryList.add(0, "[CONNECT] $host:$port -> 200 OK ($elapsedMs ms)")
                        saveHistoryToPrefs()
                    } catch (_: IllegalArgumentException) {
                        resText = strings.statusError
                        resTextColor = monochromeAccent
                        safeText = strings.statusInvalid
                        safeTextColor = monochromeSecondary
                    } catch (_: IOException) {
                        resText = strings.statusError
                        resTextColor = monochromeAccent
                        safeText = strings.statusNoServer
                        safeTextColor = monochromeSecondary
                    } finally {
                        isLoading = false
                    }
                } else {
                    try {
                        val clientBuilder = OkHttpClient.Builder()
                            .connectTimeout(requestTimeoutSetting.toLong(), TimeUnit.SECONDS)
                            .readTimeout(requestTimeoutSetting.toLong(), TimeUnit.SECONDS)
                            .followRedirects(followRedirectsSetting)

                        if (ignoreSslErrorsSetting && !verifySslSetting) {
                            try {
                                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                                    override fun checkClientTrusted(
                                        chain: Array<java.security.cert.X509Certificate>,
                                        authType: String
                                    ) {}
                                    override fun checkServerTrusted(
                                        chain: Array<java.security.cert.X509Certificate>,
                                        authType: String
                                    ) {}
                                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
                                })
                                val sslContext = SSLContext.getInstance("TLS")
                                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                                clientBuilder.sslSocketFactory(
                                    sslContext.socketFactory,
                                    trustAllCerts[0] as X509TrustManager
                                )
                                clientBuilder.hostnameVerifier { _, _ -> true }
                            } catch (_: Exception) {
                            }
                        }

                        val client = clientBuilder.build()
                        val requestBuilder = Request.Builder()
                            .url(fullUrl)
                            .header("User-Agent", customUserAgentSetting)

                        if (headersInput.isNotBlank()) {
                            headersInput.lines().forEach { line ->
                                val colonIndex = line.indexOf(':')
                                if (colonIndex > 0) {
                                    val key = line.substring(0, colonIndex).trim()
                                    val value = line.substring(colonIndex + 1).trim()
                                    if (key.isNotEmpty()) {
                                        requestBuilder.header(key, value)
                                    }
                                }
                            }
                        }

                        val trimmedBody = bodyInput.trim()
                        val isJson = trimmedBody.startsWith("{") || trimmedBody.startsWith("[")
                        val mediaType = if (isJson) {
                            "application/json; charset=utf-8".toMediaType()
                        } else {
                            "text/plain; charset=utf-8".toMediaType()
                        }
                        val requestBody = bodyInput.toByteArray(Charsets.UTF_8).toRequestBody(mediaType)

                        when (selectedMethod) {
                            "GET" -> requestBuilder.get()
                            "POST" -> requestBuilder.post(requestBody)
                            "PUT" -> requestBuilder.put(requestBody)
                            "HEAD" -> requestBuilder.head()
                            "DELETE" -> requestBuilder.delete(requestBody)
                            "PATCH" -> requestBuilder.patch(requestBody)
                            "OPTIONS" -> requestBuilder.method("OPTIONS", null)
                            "TRACE" -> requestBuilder.method("TRACE", null)
                        }

                        client.newCall(requestBuilder.build()).execute().use { response ->
                            val code = response.code
                            val bodyBytes = response.body?.bytes() ?: ByteArray(0)
                            val elapsedMs = (System.nanoTime() - startTimeNs) / 1_000_000

                            resText = "HTTP $code"
                            resTextColor = monochromeAccent
                            val isHttps = response.request.url.isHttps
                            safeText = if (isHttps) strings.statusSsl else strings.statusHttp
                            safeTextColor = monochromeSecondary
                            responseTimeText = "$elapsedMs ms"
                            responseSizeText = formatBytesSize(bodyBytes.size.toLong())

                            lastValidUrl = fullUrl
                            responseBodyText = bodyBytes.toString(Charsets.UTF_8)
                            responseHeadersText = response.headers.joinToString("\n") { "${it.first}: ${it.second}" }
                            val cookies = response.headers("Set-Cookie")
                            responseCookiesText =
                                if (cookies.isNotEmpty()) cookies.joinToString(separator = "\n") else strings.cookiesEmpty

                            scanHistoryList.add(
                                0,
                                "[$selectedMethod] $fullUrl -> HTTP $code ($elapsedMs ms)"
                            )
                            saveHistoryToPrefs()
                        }
                    } catch (_: IllegalArgumentException) {
                        resText = strings.statusError
                        resTextColor = monochromeAccent
                        safeText = strings.statusInvalid
                        safeTextColor = monochromeSecondary
                    } catch (_: IOException) {
                        resText = strings.statusError
                        resTextColor = monochromeAccent
                        safeText = strings.statusNoServer
                        safeTextColor = monochromeSecondary
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    }
    val activeScreenKey = when {
        isResponseInspectorOpen -> "inspector"
        isThemeScreenOpen -> "theme"
        isLanguageScreenOpen -> "language"
        else -> selectedTab
    }

    AnimatedContent(
        targetState = activeScreenKey,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) togetherWith
                    fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            "inspector" -> {
                ResponseInspectorScreen(
                    monochromeAccent = monochromeAccent,
                    monochromeSecondary = monochromeSecondary,
                    textColorPrimary = textColorPrimary,
                    textColorSecondary = textColorSecondary,
                    isDark = isDark,
                    isMaterialYou = isMaterialYou,
                    strings = strings,
                    lastValidUrl = lastValidUrl,
                    uriHandler = uriHandler,
                    activeSearchTab = activeSearchTab,
                    onActiveSearchTabChange = { activeSearchTab = it },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    responseBodyText = responseBodyText,
                    responseHeadersText = responseHeadersText,
                    responseCookiesText = responseCookiesText,
                    onBack = { isResponseInspectorOpen = false }
                )
            }

            "theme" -> {
                ThemeSelectionScreen(
                    isDark = isDark,
                    isMaterialYou = isMaterialYou,
                    cardBgColor = cardBgColor,
                    appThemeSetting = appThemeSetting,
                    textColorPrimary = textColorPrimary,
                    monochromeAccent = monochromeAccent,
                    strings = strings,
                    onThemeChange = onThemeChange,
                    onBack = { isThemeScreenOpen = false }
                )
            }

            "language" -> {
                LanguageScreen(
                    isDark = isDark,
                    isMaterialYou = isMaterialYou,
                    cardBgColor = cardBgColor,
                    currentLanguage = currentLanguage,
                    textColorPrimary = textColorPrimary,
                    monochromeAccent = monochromeAccent,
                    strings = strings,
                    onLanguageSelected = onLanguageChange,
                    onBack = { isLanguageScreenOpen = false }
                )
            }

            "scan" -> {
                ScanTabContent(
                    isDark = isDark,
                    isMaterialYou = isMaterialYou,
                    monochromeAccent = monochromeAccent,
                    monochromeSecondary = monochromeSecondary,
                    cardBgColor = cardBgColor,
                    httpMethods = httpMethods,
                    selectedMethodIndex = selectedMethodIndex,
                    onMethodIndexChange = { selectedMethodIndex = it },
                    urlInput = urlInput,
                    onUrlInputChange = { urlInput = it },
                    headersInput = headersInput,
                    onHeadersInputChange = { headersInput = it },
                    bodyInput = bodyInput,
                    onBodyInputChange = { bodyInput = it },
                    onClearFields = {
                        urlInput = ""
                        headersInput = ""
                        bodyInput = ""
                        resText = ""
                        safeText = ""
                        responseTimeText = ""
                        responseSizeText = ""
                        lastValidUrl = ""
                    },
                    resText = resText,
                    resTextColor = resTextColor,
                    safeText = safeText,
                    safeTextColor = safeTextColor,
                    responseTimeText = responseTimeText,
                    responseSizeText = responseSizeText,
                    isLoading = isLoading,
                    lastValidUrl = lastValidUrl,
                    onOpenInspector = { isResponseInspectorOpen = true },
                    onRunScan = runScan,
                    strings = strings,
                    uriHandler = uriHandler,
                )
            }

            "search" -> {
                SearchTabContent(
                    isDark = isDark,
                    isMaterialYou = isMaterialYou,
                    monochromeAccent = monochromeAccent,
                    monochromeSecondary = monochromeSecondary,
                    textColorPrimary = textColorPrimary,
                    cardBgColor = cardBgColor,
                    searchQueryInput = searchQueryInput,
                    onSearchQueryChange = { searchQueryInput = it },
                    onClearSearch = {
                        searchQueryInput = ""
                        searchResultsList.clear()
                    },
                    searchResultsList = searchResultsList,
                    isSearchLoading = isSearchLoading,
                    onRunSearch = runSearch,
                    onSelectSite = { site ->
                        urlInput = site
                        onTabChange("scan")
                    },
                    strings = strings
                )
            }

            "settings" -> {
                SettingsScreen(
                    isDark = isDark,
                    isMaterialYou = isMaterialYou,
                    onMaterialYouChange = onMaterialYouChange,
                    cardBgColor = cardBgColor,
                    textColorPrimary = textColorPrimary,
                    monochromeAccent = monochromeAccent,
                    monochromeSecondary = monochromeSecondary,
                    followRedirectsSetting = followRedirectsSetting,
                    onFollowRedirectsChange = { followRedirectsSetting = it; prefs.putBoolean("follow_redirects", it) },
                    requestTimeoutSetting = requestTimeoutSetting,
                    onRequestTimeoutChange = { requestTimeoutSetting = it; prefs.putInt("request_timeout", it) },
                    verifySslSetting = verifySslSetting,
                    onVerifySslChange = {
                        verifySslSetting = it
                        prefs.putBoolean("verify_ssl", it)
                        if (it) {
                            ignoreSslErrorsSetting = false
                            prefs.putBoolean("ignore_ssl", false)
                        }
                    },
                    ignoreSslErrorsSetting = ignoreSslErrorsSetting,
                    onIgnoreSslChange = {
                        ignoreSslErrorsSetting = it
                        prefs.putBoolean("ignore_ssl", it)
                        if (it) {
                            verifySslSetting = false
                            prefs.putBoolean("verify_ssl", false)
                        }
                    },
                    customUserAgentSetting = customUserAgentSetting,
                    defaultUserAgent = defaultUserAgent,
                    onUserAgentChange = {
                        customUserAgentSetting = it
                        prefs.put("user_agent", it)
                    },
                    onOpenThemeScreen = { isThemeScreenOpen = true },
                    onOpenLanguageScreen = { isLanguageScreenOpen = true },
                    onClearData = {
                        scanHistoryList.clear()
                        saveHistoryToPrefs()
                        urlInput = ""
                        headersInput = ""
                        bodyInput = ""
                        searchQueryInput = ""
                        searchResultsList.clear()
                        resText = ""
                        safeText = ""
                        responseTimeText = ""
                        responseSizeText = ""
                        responseBodyText = ""
                        responseHeadersText = ""
                        responseCookiesText = ""
                        lastValidUrl = ""
                    },
                    strings = strings
                )
            }

            "history" -> {
                HistoryScreen(
                    isDark = isDark,
                    isMaterialYou = isMaterialYou,
                    scanHistoryList = scanHistoryList,
                    cardBgColor = cardBgColor,
                    textColorPrimary = textColorPrimary,
                    monochromeAccent = monochromeAccent,
                    onClearHistory = {
                        scanHistoryList.clear()
                        saveHistoryToPrefs()
                    },
                    strings = strings
                )
            }

            "info" -> {
                InfoScreen(
                    isDark = isDark,
                    isMaterialYou = isMaterialYou,
                    version = version,
                    cardBgColor = cardBgColor,
                    textColorPrimary = textColorPrimary,
                    monochromeAccent = monochromeAccent,
                    uriHandler = uriHandler,
                    strings = strings
                )
            }
        }
    }
}


private fun formatBytesSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
    }
}
