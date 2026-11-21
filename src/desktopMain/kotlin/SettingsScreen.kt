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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    isDark: Boolean,
    isMaterialYou: Boolean,
    onMaterialYouChange: (Boolean) -> Unit,
    cardBgColor: Color,
    textColorPrimary: Color,
    monochromeAccent: Color,
    monochromeSecondary: Color,
    followRedirectsSetting: Boolean,
    onFollowRedirectsChange: (Boolean) -> Unit,
    requestTimeoutSetting: Int,
    onRequestTimeoutChange: (Int) -> Unit,
    verifySslSetting: Boolean,
    onVerifySslChange: (Boolean) -> Unit,
    ignoreSslErrorsSetting: Boolean,
    onIgnoreSslChange: (Boolean) -> Unit,
    customUserAgentSetting: String,
    defaultUserAgent: String,
    onUserAgentChange: (String) -> Unit,
    onOpenThemeScreen: () -> Unit,
    onOpenLanguageScreen: () -> Unit,
    onClearData: () -> Unit,
    strings: AppStrings
) {
    val screenBg = if (isMaterialYou) MaterialTheme.colorScheme.background else if (isDark) Color(0xFF0A0A0A) else Color(0xFFFAFAFA)
    val effectiveCardBg = if (isMaterialYou) MaterialTheme.colorScheme.surfaceContainer else cardBgColor
    val effectiveAccent = if (isMaterialYou) MaterialTheme.colorScheme.primary else monochromeAccent
    val effectiveOnAccent = if (isMaterialYou) MaterialTheme.colorScheme.onPrimary else if (isDark) Color.Black else Color.White
    val cardShape = if (isMaterialYou) RoundedCornerShape(24.dp) else RoundedCornerShape(16.dp)
    val buttonShape = if (isMaterialYou) CircleShape else RoundedCornerShape(10.dp)
    val fieldShape = if (isMaterialYou) RoundedCornerShape(18.dp) else RoundedCornerShape(10.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = strings.application_settings,
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
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.network_and_connectivity,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMaterialYou) effectiveAccent else monochromeSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = strings.auto_redirect, color = textColorPrimary, fontSize = 15.sp)
                        Text(text = strings.common_redirect, color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = followRedirectsSetting,
                        onCheckedChange = onFollowRedirectsChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = effectiveOnAccent,
                            checkedTrackColor = effectiveAccent
                        )
                    )
                }

                Text(
                    text = "${strings.requesttimeout} ${requestTimeoutSetting}с",
                    color = textColorPrimary,
                    fontSize = 15.sp
                )
                Slider(
                    value = requestTimeoutSetting.toFloat(),
                    onValueChange = { onRequestTimeoutChange(it.toInt()) },
                    valueRange = 2f..30f,
                    steps = 28,
                    colors = SliderDefaults.colors(
                        thumbColor = effectiveAccent,
                        activeTrackColor = effectiveAccent
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = strings.user_agent, color = textColorPrimary, fontSize = 15.sp)
                    TextButton(
                        onClick = { onUserAgentChange(defaultUserAgent) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = strings.default, fontSize = 12.sp, color = effectiveAccent)
                    }
                }
                OutlinedTextField(
                    value = customUserAgentSetting,
                    onValueChange = onUserAgentChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = fieldShape,
                    textStyle = TextStyle(fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = effectiveAccent,
                        unfocusedBorderColor = monochromeSecondary.copy(alpha = 0.5f),
                        focusedTextColor = textColorPrimary,
                        unfocusedTextColor = textColorPrimary
                    )
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = effectiveCardBg)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.securityandssl,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMaterialYou) effectiveAccent else monochromeSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = strings.checkssl, color = textColorPrimary, fontSize = 15.sp)
                        Text(text = strings.strictcertificateverification, color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = verifySslSetting,
                        onCheckedChange = onVerifySslChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = effectiveOnAccent,
                            checkedTrackColor = effectiveAccent
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = strings.ignoresslerrors, color = textColorPrimary, fontSize = 15.sp)
                        Text(text = strings.self_signed_certificates, color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = ignoreSslErrorsSetting,
                        onCheckedChange = onIgnoreSslChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = effectiveOnAccent,
                            checkedTrackColor = effectiveAccent
                        )
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = effectiveCardBg)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = strings.registration,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isMaterialYou) effectiveAccent else monochromeSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = strings.material_you_title, color = textColorPrimary, fontSize = 15.sp)
                        Text(text = strings.material_you_desc, color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isMaterialYou,
                        onCheckedChange = onMaterialYouChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = effectiveOnAccent,
                            checkedTrackColor = effectiveAccent
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onOpenThemeScreen,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = effectiveAccent,
                            contentColor = effectiveOnAccent
                        )
                    ) {
                        Text(
                            text = strings.change_the_design_theme,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onOpenLanguageScreen,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = effectiveAccent,
                            contentColor = effectiveOnAccent
                        )
                    ) {
                        Text(
                            text = strings.langTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onClearData,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMaterialYou) MaterialTheme.colorScheme.secondaryContainer
                        else if (isDark) Color(0xFF2C2C2C) else Color(0xFFD6D6D6),
                        contentColor = if (isMaterialYou) MaterialTheme.colorScheme.onSecondaryContainer else textColorPrimary
                    ),
                    shape = buttonShape
                ) {
                    Text(
                        text = strings.clear_history_and_input,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
