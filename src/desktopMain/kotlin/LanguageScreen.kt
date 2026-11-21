import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Cursor

@Composable
fun LanguageScreen(
    isDark: Boolean,
    isMaterialYou: Boolean = false,
    cardBgColor: Color,
    currentLanguage: String,
    textColorPrimary: Color,
    monochromeAccent: Color,
    strings: AppStrings,
    onLanguageSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val screenBg = if (isMaterialYou) MaterialTheme.colorScheme.background else if (isDark) Color(0xFF0A0A0A) else Color(0xFFFAFAFA)
    val effectiveCardBg = if (isMaterialYou) MaterialTheme.colorScheme.surfaceContainer else cardBgColor
    val effectiveAccent = if (isMaterialYou) MaterialTheme.colorScheme.primary else monochromeAccent
    val cardShape = if (isMaterialYou) RoundedCornerShape(24.dp) else RoundedCornerShape(16.dp)
    val rowShape = if (isMaterialYou) CircleShape else RoundedCornerShape(12.dp)

    val languages = listOf("ru" to "Русский", "en" to "English")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
            Text(
                text = strings.langTitle,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColorPrimary
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = effectiveCardBg)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                languages.forEach { (code, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(rowShape)
                            .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
                            .clickable { onLanguageSelected(code) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        RadioButton(
                            selected = currentLanguage == code,
                            onClick = { onLanguageSelected(code) },
                            colors = RadioButtonDefaults.colors(selectedColor = effectiveAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label, color = textColorPrimary, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
