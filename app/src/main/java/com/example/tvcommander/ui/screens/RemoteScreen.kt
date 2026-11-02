package com.example.tvcommander.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvcommander.model.ConnectionState
import com.example.tvcommander.model.RemoteKey
import com.example.tvcommander.model.ScreenshotState
import com.example.tvcommander.ui.theme.AccentAzure
import com.example.tvcommander.ui.theme.AccentCyan
import com.example.tvcommander.ui.theme.ControlButtonBackground
import com.example.tvcommander.ui.theme.ControlButtonBorder
import com.example.tvcommander.ui.theme.ControlButtonPressed
import com.example.tvcommander.ui.theme.DarkCardSurface
import com.example.tvcommander.ui.theme.DarkSurface
import com.example.tvcommander.ui.theme.PowerRed
import com.example.tvcommander.ui.theme.PowerRedGlow
import com.example.tvcommander.ui.theme.TextMuted
import com.example.tvcommander.ui.theme.TextPrimary
import com.example.tvcommander.ui.theme.TextSecondary
import com.example.tvcommander.ui.theme.WarningAmber
import kotlin.math.abs

@Composable
fun RemoteScreen(
    connectionState: ConnectionState,
    sleepTimerRemainingSeconds: Int? = null,
    screenshotState: ScreenshotState = ScreenshotState.Idle,
    isWakingUp: Boolean = false,
    activeDeviceName: String = "Xiaomi Mi TV",
    remoteMode: String = "dpad",
    hapticEnabled: Boolean = true,
    onChangeRemoteMode: (String) -> Unit = {},
    onSendKey: (RemoteKey) -> Unit,
    onSendText: (String) -> Unit = {},
    onSendClipboard: (String) -> Unit = {},
    onLaunchApp: (String) -> Unit = {},
    onTurnOn: () -> Unit,
    onTurnOff: () -> Unit,
    onSetSleepTimer: (Int) -> Unit = {},
    onCancelSleepTimer: () -> Unit = {},
    onTakeScreenshot: () -> Unit = {},
    onDismissScreenshot: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    val isConnected = connectionState is ConnectionState.Connected
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showWolHelpDialog by remember { mutableStateOf(false) }
    var showKeyboardBar by remember { mutableStateOf(false) }
    var quickInputText by remember { mutableStateOf("") }

    fun triggerKey(key: RemoteKey) {
        if (hapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        onSendKey(key)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ================= TOP BAR (Power, Status Pill & Settings) =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Power Button
            TactileIconButton(
                icon = Icons.Default.PowerSettingsNew,
                label = "Питание",
                tint = PowerRed,
                background = PowerRedGlow,
                border = PowerRed.copy(alpha = 0.5f),
                size = 52.dp,
                onClick = {
                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isConnected) onTurnOff() else onTurnOn()
                }
            )

            // Centered Remote Header / Status Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkCardSurface,
                modifier = Modifier.border(1.dp, ControlButtonBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) AccentCyan else TextMuted)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isConnected) "Пульт активен" else "Ожидание ТВ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isConnected) AccentCyan else TextSecondary
                    )
                }
            }

            // Keyboard / Text input toggle
            TactileIconButton(
                icon = Icons.Default.Keyboard,
                label = "Ввод текста",
                tint = if (isConnected) (if (showKeyboardBar) AccentCyan else TextPrimary) else TextMuted,
                size = 52.dp,
                enabled = isConnected,
                onClick = {
                    showKeyboardBar = !showKeyboardBar
                }
            )
        }

        // ================= QUICK TEXT INPUT BAR (Collapsible) =================
        AnimatedVisibility(visible = isConnected && showKeyboardBar) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkCardSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quickInputText,
                        onValueChange = { quickInputText = it },
                        placeholder = { Text("Ввести текст на ТВ...", fontSize = 12.sp, color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = ControlButtonBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    IconButton(
                        onClick = {
                            if (quickInputText.isNotBlank()) {
                                onSendText(quickInputText)
                                quickInputText = ""
                            }
                        },
                        enabled = quickInputText.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (quickInputText.isNotBlank()) AccentCyan else ControlButtonBackground)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Отправить",
                            tint = if (quickInputText.isNotBlank()) DarkSurface else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrBlank()) {
                                onSendClipboard(clip)
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(ControlButtonBackground)
                    ) {
                        Icon(
                            Icons.Default.ContentPaste,
                            contentDescription = "Вставить буфер",
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ================= WAKE-ON-LAN HERO CARD (When TV is off/sleeping) =================
        if (!isConnected) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkCardSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isWakingUp) AccentCyan.copy(alpha = 0.5f) else PowerRed.copy(alpha = 0.4f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (isWakingUp) AccentCyan else PowerRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (isWakingUp) "Включение телевизора..." else "Телевизор выключен или спит",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = if (isWakingUp)
                            "Отправлен Magic Packet + MiTV API. Ожидаем ответ сети $activeDeviceName..."
                        else
                            "Нажмите кнопку, чтобы отправить сигнал Wake-on-LAN по Wi-Fi на $activeDeviceName",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = {
                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTurnOn()
                        },
                        enabled = !isWakingUp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PowerRed,
                            disabledContainerColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isWakingUp) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = AccentCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Будим ТВ по сети...",
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Включить $activeDeviceName",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }

                    // WoL Help Dialog Button
                    TextButton(
                        onClick = { showWolHelpDialog = true },
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Не включается? Инструкция для Xiaomi TV",
                            fontSize = 11.sp,
                            color = WarningAmber
                        )
                    }
                }
            }
        }

        // ================= CONTROLLER MODE SWITCHER (D-PAD vs TRACKPAD) =================
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = ControlButtonBackground,
            modifier = Modifier.border(1.dp, ControlButtonBorder, RoundedCornerShape(14.dp))
        ) {
            Row(modifier = Modifier.padding(3.dp)) {
                val isDpad = remoteMode != "trackpad"

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (isDpad) DarkCardSurface else Color.Transparent)
                        .clickable { onChangeRemoteMode("dpad") }
                        .padding(horizontal = 20.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Кнопки (D-Pad)",
                        color = if (isDpad) AccentCyan else TextSecondary,
                        fontWeight = if (isDpad) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (!isDpad) DarkCardSurface else Color.Transparent)
                        .clickable { onChangeRemoteMode("trackpad") }
                        .padding(horizontal = 20.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Сенсорный тачпад",
                        color = if (!isDpad) AccentCyan else TextSecondary,
                        fontWeight = if (!isDpad) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // ================= MAIN CONTROLLER AREA =================
        if (remoteMode == "trackpad") {
            TactileTrackpad(
                enabled = isConnected,
                hapticEnabled = hapticEnabled,
                onSwipeUp = { triggerKey(RemoteKey.UP) },
                onSwipeDown = { triggerKey(RemoteKey.DOWN) },
                onSwipeLeft = { triggerKey(RemoteKey.LEFT) },
                onSwipeRight = { triggerKey(RemoteKey.RIGHT) },
                onTap = { triggerKey(RemoteKey.CENTER) }
            )
        } else {
            TactileDPad(
                enabled = isConnected,
                hapticEnabled = hapticEnabled,
                onUp = { triggerKey(RemoteKey.UP) },
                onDown = { triggerKey(RemoteKey.DOWN) },
                onLeft = { triggerKey(RemoteKey.LEFT) },
                onRight = { triggerKey(RemoteKey.RIGHT) },
                onCenter = { triggerKey(RemoteKey.CENTER) }
            )
        }

        // ================= NAVIGATION ROW (Back, Home, Menu) =================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TactileIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                label = "Назад",
                enabled = isConnected,
                size = 54.dp,
                onClick = { triggerKey(RemoteKey.BACK) }
            )

            TactileIconButton(
                icon = Icons.Default.Home,
                label = "Домой",
                tint = AccentCyan,
                enabled = isConnected,
                size = 62.dp,
                onClick = { triggerKey(RemoteKey.HOME) }
            )

            TactileIconButton(
                icon = Icons.Default.Menu,
                label = "Меню",
                enabled = isConnected,
                size = 54.dp,
                onClick = { triggerKey(RemoteKey.MENU) }
            )
        }

        // ================= QUICK APP LAUNCH BAR (Direct from Remote) =================
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DarkCardSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ControlButtonBorder, RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickAppIcon(
                    icon = Icons.Default.PlayArrow,
                    label = "SmartTube",
                    color = Color(0xFFFF0033),
                    enabled = isConnected,
                    onClick = { onLaunchApp("com.teamsmart.videomanager.tv") }
                )
                QuickAppIcon(
                    icon = Icons.Default.Movie,
                    label = "Кинопоиск",
                    color = Color(0xFFFF6600),
                    enabled = isConnected,
                    onClick = { onLaunchApp("ru.kinopoisk.tv") }
                )
                QuickAppIcon(
                    icon = Icons.Default.LiveTv,
                    label = "Lampa",
                    color = AccentCyan,
                    enabled = isConnected,
                    onClick = { onLaunchApp("com.lampa") }
                )
                QuickAppIcon(
                    icon = Icons.Default.Settings,
                    label = "Настройки",
                    color = TextPrimary,
                    enabled = isConnected,
                    onClick = { triggerKey(RemoteKey.SETTINGS) }
                )
            }
        }

        // ================= VOLUME & MEDIA CONTROL =================
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkCardSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ControlButtonBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Volume Controls (Pill Rocker)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(ControlButtonBackground)
                        .border(1.dp, ControlButtonBorder, RoundedCornerShape(16.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { triggerKey(RemoteKey.VOLUME_DOWN) },
                        enabled = isConnected,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeDown, contentDescription = "Громкость -", tint = TextPrimary)
                    }

                    IconButton(
                        onClick = { triggerKey(RemoteKey.VOLUME_MUTE) },
                        enabled = isConnected,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeMute, contentDescription = "Без звука", tint = WarningAmber)
                    }

                    IconButton(
                        onClick = { triggerKey(RemoteKey.VOLUME_UP) },
                        enabled = isConnected,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Громкость +", tint = TextPrimary)
                    }
                }

                // Media Playback Row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TactileIconButton(
                        icon = Icons.Default.FastRewind,
                        label = "Назад",
                        enabled = isConnected,
                        size = 42.dp,
                        onClick = { triggerKey(RemoteKey.REWIND) }
                    )

                    TactileIconButton(
                        icon = Icons.Default.PlayArrow,
                        label = "Play / Pause",
                        tint = AccentCyan,
                        enabled = isConnected,
                        size = 46.dp,
                        onClick = { triggerKey(RemoteKey.PLAY_PAUSE) }
                    )

                    TactileIconButton(
                        icon = Icons.Default.FastForward,
                        label = "Вперед",
                        enabled = isConnected,
                        size = 42.dp,
                        onClick = { triggerKey(RemoteKey.FAST_FORWARD) }
                    )
                }
            }
        }

        // ================= QUICK TOOLS (Sleep Timer & Screenshot) =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val hasTimer = sleepTimerRemainingSeconds != null && sleepTimerRemainingSeconds > 0
            val timerText = if (hasTimer) {
                val mins = (sleepTimerRemainingSeconds ?: 0) / 60 + 1
                "Сон: ~$mins мин"
            } else {
                "Таймер сна"
            }

            OutlinedButton(
                onClick = {
                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showSleepTimerDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (hasTimer) AccentCyan.copy(alpha = 0.15f) else DarkCardSurface,
                    contentColor = if (hasTimer) AccentCyan else TextPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (hasTimer) AccentCyan else ControlButtonBorder)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Таймер",
                    tint = if (hasTimer) AccentCyan else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = timerText,
                    fontSize = 12.sp,
                    fontWeight = if (hasTimer) FontWeight.Bold else FontWeight.Medium
                )
            }

            OutlinedButton(
                onClick = {
                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTakeScreenshot()
                },
                enabled = isConnected && screenshotState !is ScreenshotState.Capturing,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DarkCardSurface,
                    contentColor = TextPrimary
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ControlButtonBorder)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                if (screenshotState is ScreenshotState.Capturing) {
                    CircularProgressIndicator(
                        color = AccentCyan,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Скриншот",
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (screenshotState is ScreenshotState.Capturing) "Снимок..." else "Скриншот ТВ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // ================= WOL XIAOMI HELP DIALOG =================
    if (showWolHelpDialog) {
        AlertDialog(
            onDismissRequest = { showWolHelpDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = DarkCardSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Включение Xiaomi Mi TV", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Почему ТВ не включается по Wi-Fi?",
                        color = AccentCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "В прошивках Xiaomi при полном выключении модуль Wi-Fi физически обесточивается для экономии энергии. Чтобы ТВ просыпался со смартфона:",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ControlButtonBackground,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("1. На ТВ откройте: Настройки -> Настройки устройства -> Питание -> включите «Быстрый старт» (Fast Boot) или режим ожидания.", fontSize = 12.sp, color = TextPrimary)
                            Text("2. В Сеть и Интернет -> включите «Пробуждение по сети» (WoWLAN).", fontSize = 12.sp, color = TextPrimary)
                            Text("3. Нажимайте кнопку питания на пульте быстро (режим сна), а не долгим зажатием (полное выключение).", fontSize = 12.sp, color = TextPrimary)
                            Text("4. Самый безотказный вариант: подключить ТВ интернет-кабелем (LAN) — проводная сетевая карта не засыпает никогда.", fontSize = 12.sp, color = AccentCyan)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showWolHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Понятно", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // ================= SLEEP TIMER DIALOG =================
    if (showSleepTimerDialog) {
        val hasTimer = sleepTimerRemainingSeconds != null && sleepTimerRemainingSeconds > 0
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = DarkCardSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Таймер сна ТВ", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (hasTimer) {
                            val mins = (sleepTimerRemainingSeconds ?: 0) / 60 + 1
                            "Телевизор автоматически выключится через ~$mins мин."
                        } else {
                            "Выберите время, через которое телевизор перейдет в спящий режим:"
                        },
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    val times = listOf(15, 30, 45, 60, 90)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        times.forEach { min ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ControlButtonBackground,
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, ControlButtonBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        showSleepTimerDialog = false
                                        onSetSleepTimer(min)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$min м",
                                        color = AccentCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (hasTimer) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                showSleepTimerDialog = false
                                onCancelSleepTimer()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PowerRed.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Отменить активный таймер", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text("Закрыть", color = TextSecondary)
                }
            }
        )
    }

    // ================= SCREENSHOT PREVIEW DIALOG =================
    if (screenshotState is ScreenshotState.Success) {
        AlertDialog(
            onDismissRequest = { onDismissScreenshot() },
            shape = RoundedCornerShape(20.dp),
            containerColor = DarkCardSurface,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Снимок экрана ТВ",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onDismissScreenshot() }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextSecondary)
                    }
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            bitmap = screenshotState.bitmap.asImageBitmap(),
                            contentDescription = "Скриншот экрана ТВ",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Снимок получен через ADB со 100% качеством",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onDismissScreenshot() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Готово", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Modern Touchpad with gestures (Swipe up/down/left/right & Tap to OK)
 */
@Composable
fun TactileTrackpad(
    enabled: Boolean,
    hapticEnabled: Boolean,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onTap: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }
    val dragThreshold = 55f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .shadow(12.dp, RoundedCornerShape(26.dp), spotColor = AccentCyan.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(26.dp))
            .background(DarkCardSurface)
            .border(2.dp, ControlButtonBorder, RoundedCornerShape(26.dp))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onTap = {
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTap()
                    }
                )
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y

                        if (abs(totalDragX) > dragThreshold || abs(totalDragY) > dragThreshold) {
                            if (abs(totalDragX) > abs(totalDragY)) {
                                if (totalDragX > 0) {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSwipeRight()
                                } else {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSwipeLeft()
                                }
                            } else {
                                if (totalDragY > 0) {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSwipeDown()
                                } else {
                                    if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSwipeUp()
                                }
                            }
                            totalDragX = 0f
                            totalDragY = 0f
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = if (enabled) AccentCyan.copy(alpha = 0.8f) else TextMuted,
                modifier = Modifier.size(38.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (enabled) "Свайпайте в любую сторону" else "Пульт не подключен",
                color = if (enabled) TextPrimary else TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (enabled) "Тапните в центр для выбора (OK)" else "Включите телевизор выше",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Tactile D-Pad Controller with center OK button
 */
@Composable
fun TactileDPad(
    enabled: Boolean,
    hapticEnabled: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onCenter: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    fun vibrate() {
        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Box(
        modifier = Modifier
            .size(230.dp)
            .shadow(12.dp, CircleShape, spotColor = AccentCyan.copy(alpha = 0.15f))
            .clip(CircleShape)
            .background(DarkCardSurface)
            .border(2.dp, ControlButtonBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // UP
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(width = 80.dp, height = 66.dp)
                .clickable(enabled = enabled) {
                    vibrate()
                    onUp()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Вверх",
                tint = if (enabled) TextPrimary else TextMuted,
                modifier = Modifier.size(34.dp)
            )
        }

        // DOWN
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(width = 80.dp, height = 66.dp)
                .clickable(enabled = enabled) {
                    vibrate()
                    onDown()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Вниз",
                tint = if (enabled) TextPrimary else TextMuted,
                modifier = Modifier.size(34.dp)
            )
        }

        // LEFT
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(width = 66.dp, height = 80.dp)
                .clickable(enabled = enabled) {
                    vibrate()
                    onLeft()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Влево",
                tint = if (enabled) TextPrimary else TextMuted,
                modifier = Modifier.size(34.dp)
            )
        }

        // RIGHT
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 66.dp, height = 80.dp)
                .clickable(enabled = enabled) {
                    vibrate()
                    onRight()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Вправо",
                tint = if (enabled) TextPrimary else TextMuted,
                modifier = Modifier.size(34.dp)
            )
        }

        // CENTER OK BUTTON
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        Box(
            modifier = Modifier
                .size(82.dp)
                .clip(CircleShape)
                .background(
                    if (isPressed) Brush.linearGradient(listOf(AccentAzure, AccentAzure))
                    else Brush.radialGradient(listOf(ControlButtonPressed, ControlButtonBackground))
                )
                .border(
                    width = 2.dp,
                    color = if (isPressed) AccentCyan else AccentCyan.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled
                ) {
                    vibrate()
                    onCenter()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "OK",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = if (enabled) AccentCyan else TextMuted
            )
        }
    }
}

@Composable
fun QuickAppIcon(
    icon: ImageVector,
    label: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (enabled) color.copy(alpha = 0.2f) else ControlButtonBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = if (enabled) color else TextMuted, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(text = label, fontSize = 10.sp, color = if (enabled) TextSecondary else TextMuted, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun TactileIconButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    tint: Color = TextPrimary,
    background: Color = ControlButtonBackground,
    border: Color = ControlButtonBorder,
    size: androidx.compose.ui.unit.Dp = 50.dp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        shape = CircleShape,
        color = if (!enabled) ControlButtonBackground.copy(alpha = 0.3f)
        else if (isPressed) ControlButtonPressed
        else background,
        modifier = Modifier
            .size(size)
            .border(
                1.dp,
                if (!enabled) ControlButtonBorder.copy(alpha = 0.3f)
                else if (isPressed) AccentCyan
                else border,
                CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) tint else TextMuted,
                modifier = Modifier.size(size * 0.48f)
            )
        }
    }
}
