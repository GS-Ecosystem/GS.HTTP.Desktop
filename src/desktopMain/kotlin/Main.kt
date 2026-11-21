import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Cursor

fun main() {
    application {
        val windowState = rememberWindowState(size = DpSize(980.dp, 720.dp))

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "GS.HTTP",
            icon = painterResource("icon.ico"),
            undecorated = false,
            transparent = false
        ) {
            LaunchedEffect(Unit) {
                try {
                    window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                    window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                    window.title = "GS.HTTP"
                } catch (_: Exception) {
                }
            }

            val prefs = remember { java.util.prefs.Preferences.userRoot().node("app_prefs") }
            var appThemeSetting by remember { mutableStateOf(prefs.get("app_theme", "system")) }
            var currentLanguage by remember { mutableStateOf(prefs.get("app_lang", "ru")) }
            var isMaterialYou by remember { mutableStateOf(prefs.getBoolean("material_you", false)) }
            val strings = remember(currentLanguage) { getStringsForLanguage(currentLanguage) }

            val isDarkTheme = when (appThemeSetting) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            val targetScheme = if (isMaterialYou) {
                if (isDarkTheme) {
                    darkColorScheme(
                        primary = Color(0xFFE6E6E6),
                        onPrimary = Color(0xFF141414),
                        secondary = Color(0xFFB3B3B3),
                        secondaryContainer = Color(0xFF2B2B2E),
                        onSecondaryContainer = Color(0xFFF2F2F2),
                        background = Color(0xFF0E0E10),
                        surface = Color(0xFF121214),
                        surfaceContainer = Color(0xFF1C1C1F)
                    )
                } else {
                    lightColorScheme(
                        primary = Color(0xFF1C1C1F),
                        onPrimary = Color.White,
                        secondary = Color(0xFF525257),
                        secondaryContainer = Color(0xFFE2E2E6),
                        onSecondaryContainer = Color(0xFF141416),
                        background = Color(0xFFF6F6F8),
                        surface = Color(0xFFF6F6F8),
                        surfaceContainer = Color(0xFFEBEBEF)
                    )
                }
            } else {
                if (isDarkTheme) darkColorScheme() else lightColorScheme()
            }

            val colorAnimSpec = spring<Color>(stiffness = Spring.StiffnessMediumLow)
            val animatedScheme = targetScheme.copy(
                primary = animateColorAsState(targetScheme.primary, colorAnimSpec).value,
                onPrimary = animateColorAsState(targetScheme.onPrimary, colorAnimSpec).value,
                secondaryContainer = animateColorAsState(targetScheme.secondaryContainer, colorAnimSpec).value,
                onSecondaryContainer = animateColorAsState(targetScheme.onSecondaryContainer, colorAnimSpec).value,
                background = animateColorAsState(targetScheme.background, colorAnimSpec).value,
                surface = animateColorAsState(targetScheme.surface, colorAnimSpec).value,
                surfaceContainer = animateColorAsState(targetScheme.surfaceContainer, colorAnimSpec).value
            )

            MaterialTheme(colorScheme = animatedScheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

                    var selectedTab by remember { mutableStateOf("scan") }
                    var isSidebarExpanded by remember { mutableStateOf(false) }
                    val sidebarWidth by animateDpAsState(
                        targetValue = if (isSidebarExpanded) 210.dp else 80.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    val targetSidebarBg = when {
                        isMaterialYou && isDarkTheme -> Color(0xFF18181B)
                        isMaterialYou && !isDarkTheme -> Color(0xFFEBEBEF)
                        isDarkTheme -> Color(0xFF141517)
                        else -> Color(0xFFE8E9ED)
                    }
                    val sidebarBg by animateColorAsState(targetSidebarBg, colorAnimSpec)

                    Row(modifier = Modifier.fillMaxSize()) {
                        NavigationRail(
                            modifier = Modifier
                                .width(sidebarWidth)
                                .fillMaxHeight(),
                            containerColor = sidebarBg,
                            header = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp, bottom = 4.dp),
                                    horizontalAlignment = if (isSidebarExpanded) Alignment.Start else Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = if (isSidebarExpanded) 16.dp else 0.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = if (isSidebarExpanded) Arrangement.SpaceBetween else Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (isDarkTheme) Color.White else Color(0xFF1C1B1F)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource("ic_gs_ht.png"),
                                                contentDescription = "GS Monitor",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        if (isSidebarExpanded) {
                                            IconButton(
                                                onClick = { isSidebarExpanded = false },
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Collapse", tint = Color.Gray)
                                            }
                                        }
                                    }

                                    if (!isSidebarExpanded) {
                                        IconButton(
                                            onClick = { isSidebarExpanded = true },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Expand", tint = Color.Gray)
                                        }
                                    }
                                }
                            }
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            RailNavItem(
                                icon = Icons.Filled.Search,
                                label = strings.nav_scanner,
                                isSelected = selectedTab == "scan",
                                isExpanded = isSidebarExpanded,
                                isDark = isDarkTheme,
                                isMaterialYou = isMaterialYou,
                                onClick = { selectedTab = "scan" }
                            )

                            RailNavItem(
                                icon = Icons.Filled.FindInPage,
                                label = strings.nav_search,
                                isSelected = selectedTab == "search",
                                isExpanded = isSidebarExpanded,
                                isDark = isDarkTheme,
                                isMaterialYou = isMaterialYou,
                                onClick = { selectedTab = "search" }
                            )

                            RailNavItem(
                                icon = Icons.Filled.Settings,
                                label = strings.home_settings,
                                isSelected = selectedTab == "settings",
                                isExpanded = isSidebarExpanded,
                                isDark = isDarkTheme,
                                isMaterialYou = isMaterialYou,
                                onClick = { selectedTab = "settings" }
                            )

                            RailNavItem(
                                icon = Icons.Filled.History,
                                label = strings.nav_history,
                                isSelected = selectedTab == "history",
                                isExpanded = isSidebarExpanded,
                                isDark = isDarkTheme,
                                isMaterialYou = isMaterialYou,
                                onClick = { selectedTab = "history" }
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            RailNavItem(
                                icon = Icons.Filled.Info,
                                label = strings.home_info,
                                isSelected = selectedTab == "info",
                                isExpanded = isSidebarExpanded,
                                isDark = isDarkTheme,
                                isMaterialYou = isMaterialYou,
                                onClick = { selectedTab = "info" }
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            MainAppScreen(
                                appThemeSetting = appThemeSetting,
                                onThemeChange = {
                                    appThemeSetting = it
                                    prefs.put("app_theme", it)
                                },
                                currentLanguage = currentLanguage,
                                onLanguageChange = {
                                    currentLanguage = it
                                    prefs.put("app_lang", it)
                                },
                                isMaterialYou = isMaterialYou,
                                onMaterialYouChange = {
                                    isMaterialYou = it
                                    prefs.putBoolean("material_you", it)
                                },
                                selectedTab = selectedTab,
                                onTabChange = { selectedTab = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.RailNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isExpanded: Boolean,
    isDark: Boolean,
    isMaterialYou: Boolean,
    onClick: () -> Unit
) {
    val activeBg = when {
        isMaterialYou -> MaterialTheme.colorScheme.secondaryContainer
        isDark -> Color.White.copy(alpha = 0.14f)
        else -> Color.Black.copy(alpha = 0.10f)
    }
    val activeTint = when {
        isMaterialYou -> MaterialTheme.colorScheme.onSecondaryContainer
        isDark -> Color.White
        else -> Color.Black
    }

    if (!isExpanded) {
        NavigationRailItem(
            selected = isSelected,
            onClick = onClick,
            modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
            icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp)
                )
            },
            alwaysShowLabel = false,
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = activeTint,
                unselectedIconColor = Color.Gray,
                indicatorColor = activeBg
            )
        )
    } else {
        val animatedBg by animateColorAsState(
            targetValue = if (isSelected) activeBg else Color.Transparent,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
        val contentColor = if (isSelected) activeTint else Color.Gray
        val itemShape = if (isMaterialYou) CircleShape else RoundedCornerShape(12.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp)
                .clip(itemShape)
                .background(animatedBg)
                .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
