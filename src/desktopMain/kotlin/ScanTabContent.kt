import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Cursor

@Composable
fun ScanTabContent(
    isDark: Boolean,
    isMaterialYou: Boolean,
    monochromeAccent: Color,
    monochromeSecondary: Color,
    cardBgColor: Color,
    httpMethods: List<String>,
    selectedMethodIndex: Int,
    onMethodIndexChange: (Int) -> Unit,
    urlInput: String,
    onUrlInputChange: (String) -> Unit,
    headersInput: String,
    onHeadersInputChange: (String) -> Unit,
    bodyInput: String,
    onBodyInputChange: (String) -> Unit,
    onClearFields: () -> Unit,
    resText: String,
    resTextColor: Color,
    safeText: String,
    safeTextColor: Color,
    responseTimeText: String,
    responseSizeText: String,
    isLoading: Boolean,
    lastValidUrl: String,
    strings: AppStrings,
    onOpenInspector: () -> Unit,
    onRunScan: () -> Unit,
    uriHandler: androidx.compose.ui.platform.UriHandler,
) {
    val selectedMethod = httpMethods[selectedMethodIndex]
    val showHeadersField = selectedMethod != "CONNECT"
    val showBodyField = selectedMethod in listOf("POST", "PUT", "DELETE", "PATCH")
    val hasAnyInput = urlInput.isNotEmpty() || headersInput.isNotEmpty() || bodyInput.isNotEmpty()

    val screenBg = if (isMaterialYou) MaterialTheme.colorScheme.background else if (isDark) Color(0xFF0A0A0A) else Color(0xFFFAFAFA)
    val effectiveAccent = if (isMaterialYou) MaterialTheme.colorScheme.primary else monochromeAccent
    val effectiveOnAccent = if (isMaterialYou) MaterialTheme.colorScheme.onPrimary else if (isDark) Color.Black else Color.White
    val effectiveCardBg = if (isMaterialYou) MaterialTheme.colorScheme.surfaceContainer else cardBgColor
    val fieldShape = if (isMaterialYou) RoundedCornerShape(18.dp) else RoundedCornerShape(12.dp)
    val buttonShape = if (isMaterialYou) CircleShape else RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        if (lastValidUrl.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenInspector,
                    shape = buttonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMaterialYou) MaterialTheme.colorScheme.secondaryContainer else effectiveAccent,
                        contentColor = if (isMaterialYou) MaterialTheme.colorScheme.onSecondaryContainer else effectiveOnAccent
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                ) {
                    Text(
                        text = strings.inspector_scan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 60.dp, start = 24.dp, end = 24.dp, bottom = 135.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedMethodIndex,
                containerColor = Color.Transparent,
                contentColor = effectiveAccent,
                edgePadding = 0.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedMethodIndex < tabPositions.size) {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedMethodIndex])
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(effectiveAccent)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                httpMethods.forEachIndexed { index, method ->
                    val isSelected = selectedMethodIndex == index
                    val tabTextColor = when {
                        isSelected && isMaterialYou -> effectiveAccent
                        isSelected && isDark -> Color.White
                        isSelected && !isDark -> Color.Black
                        else -> Color.Gray.copy(alpha = 0.6f)
                    }
                    Tab(
                        selected = isSelected,
                        onClick = { onMethodIndexChange(index) },
                        text = {
                            Text(
                                text = method,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = tabTextColor
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlInputChange,
                label = { Text("URL") },
                placeholder = {
                    Text(
                        text = strings.check_scan,
                        color = Color.Gray.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = fieldShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = if (isDark) Color.White else Color.Black,
                    unfocusedTextColor = if (isDark) Color.White else Color.Black,
                    focusedBorderColor = effectiveAccent,
                    unfocusedBorderColor = monochromeSecondary.copy(alpha = 0.5f),
                    focusedLabelColor = effectiveAccent
                ),
                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
            )

            if (showHeadersField) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = headersInput,
                    onValueChange = onHeadersInputChange,
                    label = { Text("Headers") },
                    placeholder = {
                        Text(
                            text = strings.headers_placeholder,
                            color = Color.Gray.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5,
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedBorderColor = effectiveAccent,
                        unfocusedBorderColor = monochromeSecondary.copy(alpha = 0.5f),
                        focusedLabelColor = effectiveAccent
                    ),
                    textStyle = TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                )
            }

            if (showBodyField) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = bodyInput,
                    onValueChange = onBodyInputChange,
                    label = { Text("Request body (json/text)") },
                    placeholder = {
                        Text(
                            text = strings.body_placeholder,
                            color = Color.Gray.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 8,
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedBorderColor = effectiveAccent,
                        unfocusedBorderColor = monochromeSecondary.copy(alpha = 0.5f),
                        focusedLabelColor = effectiveAccent
                    ),
                    textStyle = TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                )
            }

            if (hasAnyInput) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onClearFields,
                        shape = buttonShape,
                        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = strings.btnClearFields,
                            tint = effectiveAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.btnClearFields,
                            color = effectiveAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (resText.isNotEmpty()) {
                val badgeAccent = if (isMaterialYou) effectiveAccent else resTextColor
                val badgeSecondary = if (isMaterialYou) MaterialTheme.colorScheme.secondary else safeTextColor
                val badgeShape = if (isMaterialYou) CircleShape else RoundedCornerShape(8.dp)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = if (isMaterialYou) RoundedCornerShape(24.dp) else RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = effectiveCardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(badgeShape)
                                    .background(badgeAccent.copy(alpha = 0.15f))
                                    .border(1.dp, badgeAccent.copy(alpha = 0.4f), badgeShape)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = resText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeAccent
                                )
                            }

                            if (safeText.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(badgeShape)
                                        .background(badgeSecondary.copy(alpha = 0.15f))
                                        .border(1.dp, badgeSecondary.copy(alpha = 0.4f), badgeShape)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = safeText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeSecondary
                                    )
                                }
                            }

                            if (responseTimeText.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(badgeShape)
                                        .background(badgeSecondary.copy(alpha = 0.12f))
                                        .border(1.dp, badgeSecondary.copy(alpha = 0.3f), badgeShape)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = responseTimeText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = badgeSecondary
                                    )
                                }
                            }

                            if (responseSizeText.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(badgeShape)
                                        .background(badgeSecondary.copy(alpha = 0.12f))
                                        .border(1.dp, badgeSecondary.copy(alpha = 0.3f), badgeShape)
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = responseSizeText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = badgeSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(screenBg)
                .padding(bottom = 20.dp, top = 8.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (resText.isNotEmpty() && lastValidUrl.isNotEmpty()) {
                Button(
                    onClick = { uriHandler.openUri(lastValidUrl) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = effectiveCardBg,
                        contentColor = effectiveAccent
                    ),
                    shape = buttonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                ) {
                    Text(
                        text = strings.btnOpenBrowserEmoji,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Button(
                onClick = { if (!isLoading) onRunScan() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLoading) effectiveAccent.copy(alpha = 0.5f) else effectiveAccent,
                    contentColor = effectiveOnAccent
                ),
                shape = buttonShape
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = effectiveOnAccent,
                            strokeWidth = 2.dp
                        )
                    }
                    Text(
                        text = if (isLoading) strings.analysis_scan else strings.run_scan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
