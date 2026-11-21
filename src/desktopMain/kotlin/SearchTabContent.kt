import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Cursor

@Composable
fun SearchTabContent(
    isDark: Boolean,
    isMaterialYou: Boolean = false,
    monochromeAccent: Color,
    monochromeSecondary: Color,
    textColorPrimary: Color,
    cardBgColor: Color,
    searchQueryInput: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    searchResultsList: List<String>,
    isSearchLoading: Boolean,
    onRunSearch: () -> Unit,
    strings: AppStrings,
    onSelectSite: (String) -> Unit
) {
    var selectedZoneIndex by remember { mutableStateOf(0) }
    val zones = listOf("ALL", "COM", "ORG", "NET", "RU", "IO", "ME", "CO", "CC", "APP", "DEV")

    val screenBg = if (isMaterialYou) MaterialTheme.colorScheme.background else if (isDark) Color(0xFF0A0A0A) else Color(0xFFFAFAFA)
    val effectiveAccent = if (isMaterialYou) MaterialTheme.colorScheme.primary else monochromeAccent
    val effectiveOnAccent = if (isMaterialYou) MaterialTheme.colorScheme.onPrimary else if (isDark) Color.Black else Color.White
    val effectiveCardBg = if (isMaterialYou) MaterialTheme.colorScheme.surfaceContainer else cardBgColor
    val fieldShape = if (isMaterialYou) RoundedCornerShape(18.dp) else RoundedCornerShape(12.dp)
    val buttonShape = if (isMaterialYou) CircleShape else RoundedCornerShape(12.dp)
    val hasAnySearchData = searchQueryInput.isNotEmpty() || searchResultsList.isNotEmpty()

    val filteredResults = remember(searchResultsList, selectedZoneIndex) {
        if (selectedZoneIndex == 0) {
            searchResultsList
        } else {
            val targetZone = "." + zones[selectedZoneIndex].lowercase()
            searchResultsList.filter { site -> site.endsWith(targetZone) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = strings.searches_matches,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColorPrimary
            )

            OutlinedTextField(
                value = searchQueryInput,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = strings.example_keyword,
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
                    unfocusedBorderColor = monochromeSecondary.copy(alpha = 0.5f)
                ),
                textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
            )

            if (hasAnySearchData) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            selectedZoneIndex = 0
                            onClearSearch()
                        },
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
            if (searchResultsList.isNotEmpty() && !searchResultsList.contains(strings.notFound)) {
                ScrollableTabRow(
                    selectedTabIndex = selectedZoneIndex,
                    containerColor = Color.Transparent,
                    contentColor = effectiveAccent,
                    edgePadding = 0.dp
                ) {
                    zones.forEachIndexed { index, zone ->
                        Tab(
                            selected = selectedZoneIndex == index,
                            onClick = { selectedZoneIndex = index },
                            text = { Text(text = zone, fontSize = 12.sp) }
                        )
                    }
                }
            }

            if (filteredResults.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = if (isMaterialYou) RoundedCornerShape(24.dp) else RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = effectiveCardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${strings.sites_found} (${filteredResults.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val searchScrollState = rememberScrollState()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                                .background(
                                    if (isDark) Color(0xFF141414) else Color(0xFFEFEFEF),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(searchScrollState)
                                    .padding(end = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filteredResults.forEach { site ->
                                    Text(
                                        text = site,
                                        fontSize = 14.sp,
                                        color = textColorPrimary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                                            .pointerInput(Unit) {
                                                detectTapGestures(onTap = {
                                                    if (site != strings.notFound) {
                                                        onSelectSite(site)
                                                    }
                                                })
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { if (!isSearchLoading) onRunSearch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSearchLoading) Color.Gray else effectiveAccent,
                    contentColor = effectiveOnAccent
                ),
                shape = buttonShape
            ) {
                Text(
                    text = if (isSearchLoading) strings.searchLoading else strings.findMatches,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
