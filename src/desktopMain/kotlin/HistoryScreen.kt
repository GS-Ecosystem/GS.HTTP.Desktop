import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Cursor

@Composable
fun HistoryScreen(
    isDark: Boolean,
    isMaterialYou: Boolean,
    scanHistoryList: List<String>,
    cardBgColor: Color,
    textColorPrimary: Color,
    monochromeAccent: Color,
    onClearHistory: () -> Unit,
    strings: AppStrings
) {
    val clipboardManager = LocalClipboardManager.current
    val screenBg = if (isMaterialYou) MaterialTheme.colorScheme.background else if (isDark) Color(0xFF0A0A0A) else Color(0xFFFAFAFA)
    val effectiveCardBg = if (isMaterialYou) MaterialTheme.colorScheme.surfaceContainer else cardBgColor
    val effectiveAccent = if (isMaterialYou) MaterialTheme.colorScheme.primary else monochromeAccent
    val cardShape = if (isMaterialYou) RoundedCornerShape(20.dp) else RoundedCornerShape(14.dp)
    val buttonShape = if (isMaterialYou) CircleShape else RoundedCornerShape(10.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.history_scan,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textColorPrimary
            )

            if (scanHistoryList.isNotEmpty()) {
                OutlinedButton(
                    onClick = onClearHistory,
                    shape = buttonShape,
                    modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = strings.clear_history,
                        tint = effectiveAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.clear_history,
                        color = effectiveAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        if (scanHistoryList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = strings.history_empty,
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(scanHistoryList) { logItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(cardShape)
                            .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(logItem))
                            },
                        shape = cardShape,
                        colors = CardDefaults.cardColors(containerColor = effectiveCardBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = logItem,
                                color = textColorPrimary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = strings.btnCopy,
                                tint = effectiveAccent.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
