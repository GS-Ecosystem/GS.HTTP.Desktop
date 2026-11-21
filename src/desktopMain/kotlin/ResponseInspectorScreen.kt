import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Cursor

@Composable
fun ResponseInspectorScreen(
    monochromeAccent: Color,
    monochromeSecondary: Color,
    textColorPrimary: Color,
    textColorSecondary: Color,
    isDark: Boolean,
    isMaterialYou: Boolean,
    strings: AppStrings,
    lastValidUrl: String,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    activeSearchTab: String,
    onActiveSearchTabChange: (String) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    responseBodyText: String,
    responseHeadersText: String,
    responseCookiesText: String,
    onBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var isCopied by remember { mutableStateOf(false) }
    var isPrettyJson by remember(responseBodyText) { mutableStateOf(false) }

    val screenBg = if (isMaterialYou) MaterialTheme.colorScheme.background else if (isDark) Color(0xFF0A0A0A) else Color(0xFFFAFAFA)
    val effectiveAccent = if (isMaterialYou) MaterialTheme.colorScheme.primary else monochromeAccent
    val effectiveOnAccent = if (isMaterialYou) MaterialTheme.colorScheme.onPrimary else if (isDark) Color.Black else Color.White
    val buttonShape = if (isMaterialYou) CircleShape else RoundedCornerShape(10.dp)
    val containerShape = if (isMaterialYou) RoundedCornerShape(24.dp) else RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = strings.back,
                        tint = textColorPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = strings.inspectorTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textColorPrimary
                    )
                    if (lastValidUrl.isNotEmpty()) {
                        Text(
                            text = lastValidUrl,
                            fontSize = 12.sp,
                            color = textColorSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (lastValidUrl.isNotEmpty()) {
                        uriHandler.openUri(lastValidUrl)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = effectiveAccent,
                    contentColor = effectiveOnAccent
                ),
                shape = buttonShape,
                modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
            ) {
                Text(
                    text = strings.btnOpenBrowserEmoji,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .width(300.dp)
                    .background(
                        if (isMaterialYou) MaterialTheme.colorScheme.surfaceContainer
                        else if (isDark) Color(0xFF1A1A22) else Color(0xFFE0E0E5),
                        if (isMaterialYou) CircleShape else RoundedCornerShape(10.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Body", "Headers", "Cookies").forEach { tab ->
                    val isSelected = activeSearchTab.equals(tab, ignoreCase = true)
                    TextButton(
                        onClick = { onActiveSearchTabChange(tab) },
                        shape = if (isMaterialYou) CircleShape else RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                when {
                                    isSelected && isMaterialYou -> MaterialTheme.colorScheme.secondaryContainer
                                    isSelected && isDark -> Color(0xFF333338)
                                    isSelected && !isDark -> Color.White
                                    else -> Color.Transparent
                                },
                                if (isMaterialYou) CircleShape else RoundedCornerShape(8.dp)
                            )
                    ) {
                        Text(
                            text = tab,
                            color = when {
                                isSelected && isMaterialYou -> MaterialTheme.colorScheme.onSecondaryContainer
                                isSelected -> textColorPrimary
                                else -> textColorSecondary
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(text = strings.searchLogPlaceholder, fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                shape = if (isMaterialYou) CircleShape else RoundedCornerShape(10.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = effectiveAccent,
                    unfocusedBorderColor = monochromeSecondary.copy(alpha = 0.5f),
                    focusedTextColor = textColorPrimary,
                    unfocusedTextColor = textColorPrimary
                )
            )
        }

        val currentTextData = when (activeSearchTab.lowercase()) {
            "body" -> if (isPrettyJson) formatJsonPretty(responseBodyText) else responseBodyText
            "headers" -> responseHeadersText
            else -> responseCookiesText
        }

        val filteredText = if (searchQuery.isEmpty()) {
            currentTextData
        } else {
            try {
                if (currentTextData.length > 500_000) {
                    strings.searchTooBig
                } else {
                    currentTextData.lines()
                        .filter { it.contains(other = searchQuery, ignoreCase = true) }
                        .joinToString(separator = "\n")
                }
            } catch (_: Exception) {
                strings.searchError
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (activeSearchTab.equals("Body", ignoreCase = true) &&
                (responseBodyText.trim().startsWith("{") || responseBodyText.trim().startsWith("["))
            ) {
                OutlinedButton(
                    onClick = { isPrettyJson = !isPrettyJson },
                    shape = buttonShape,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                ) {
                    Icon(
                        imageVector = Icons.Filled.DataObject,
                        contentDescription = strings.btnFormatJson,
                        tint = effectiveAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.btnFormatJson,
                        fontSize = 12.sp,
                        color = effectiveAccent,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    if (filteredText.isNotEmpty()) {
                        clipboardManager.setText(AnnotatedString(filteredText))
                        isCopied = true
                        scope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    }
                },
                shape = buttonShape,
                modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
            ) {
                Icon(
                    imageVector = if (isCopied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                    contentDescription = strings.btnCopy,
                    tint = effectiveAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCopied) strings.btnCopied else strings.btnCopy,
                    fontSize = 12.sp,
                    color = effectiveAccent,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    if (isMaterialYou) MaterialTheme.colorScheme.surfaceContainer
                    else if (isDark) Color(0xFF121215) else Color(0xFFEBEBEF),
                    containerShape
                )
                .border(
                    1.dp,
                    if (isMaterialYou) effectiveAccent.copy(alpha = 0.25f)
                    else if (isDark) Color(0xFF2E2E35) else Color(0xFFD0D0D8),
                    containerShape
                )
                .padding(16.dp)
        ) {
            SelectionContainer {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = if (filteredText.trim().isEmpty() && searchQuery.isNotEmpty()) {
                                strings.notFound
                            } else {
                                filteredText
                            },
                            color = textColorPrimary,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

private fun formatJsonPretty(raw: String): String {
    val trimmed = raw.trim()
    if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return raw
    val sb = StringBuilder()
    var indent = 0
    var inQuotes = false
    var escape = false

    for (ch in trimmed) {
        if (escape) {
            sb.append(ch)
            escape = false
            continue
        }
        if (ch == '\\' && inQuotes) {
            sb.append(ch)
            escape = true
            continue
        }
        if (ch == '"') {
            inQuotes = !inQuotes
            sb.append(ch)
            continue
        }
        if (!inQuotes) {
            when (ch) {
                '{', '[' -> {
                    sb.append(ch).append('\n')
                    indent += 2
                    sb.append(" ".repeat(indent))
                }
                '}', ']' -> {
                    sb.append('\n')
                    indent = (indent - 2).coerceAtLeast(0)
                    sb.append(" ".repeat(indent)).append(ch)
                }
                ',' -> {
                    sb.append(ch).append('\n').append(" ".repeat(indent))
                }
                ':' -> sb.append(": ")
                else -> if (!ch.isWhitespace()) sb.append(ch)
            }
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}
