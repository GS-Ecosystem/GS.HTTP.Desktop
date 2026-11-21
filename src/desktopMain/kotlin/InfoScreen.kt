import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Cursor

@Composable
fun InfoScreen(
    isDark: Boolean,
    isMaterialYou: Boolean = false,
    version: String,
    cardBgColor: Color,
    textColorPrimary: Color,
    monochromeAccent: Color,
    uriHandler: androidx.compose.ui.platform.UriHandler,
    strings: AppStrings
) {
    val screenBg = if (isMaterialYou) MaterialTheme.colorScheme.background else if (isDark) Color(0xFF0A0A0A) else Color(0xFFFAFAFA)
    val effectiveCardBg = if (isMaterialYou) MaterialTheme.colorScheme.surfaceContainer else cardBgColor
    val effectiveAccent = if (isMaterialYou) MaterialTheme.colorScheme.primary else monochromeAccent
    val cardShape = if (isMaterialYou) RoundedCornerShape(24.dp) else RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = strings.app_info,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = textColorPrimary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = effectiveCardBg)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.White else Color(0xFF1C1B1F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource("ic_gs_ht.png"),
                            contentDescription = "GS HTTP",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column {
                        Text(
                            text = "GS HTTP",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = textColorPrimary
                        )
                        Text(
                            text = "${strings.version_info}: $version",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                HorizontalDivider(color = textColorPrimary.copy(alpha = 0.1f))

                Text(text = strings.dev_info, fontSize = 15.sp, color = textColorPrimary)
                Text(text = strings.download_info, fontSize = 15.sp, color = textColorPrimary)

                HorizontalDivider(color = textColorPrimary.copy(alpha = 0.1f))

                Text(
                    text = strings.source_code_info,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                TextButton(
                    onClick = { uriHandler.openUri("https://github.com/GS-Ecosystem/GS.HTTP.Desktop") },
                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource("github.png"),
                            contentDescription = strings.github_info_info,
                            tint = textColorPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = strings.github_info,
                            fontSize = 14.sp,
                            color = effectiveAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = strings.document_info,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { uriHandler.openUri("https://gs-ht.ru") },
                        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "${strings.home_site} ↗",
                            fontSize = 14.sp,
                            color = effectiveAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    TextButton(
                        onClick = { uriHandler.openUri("https://gs-ht.ru/PRIVACY_GS.Monitor_.html") },
                        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR))),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = strings.privacy_policy_info,
                            fontSize = 14.sp,
                            color = effectiveAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
