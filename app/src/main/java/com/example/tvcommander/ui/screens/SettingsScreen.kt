package com.example.tvcommander.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvcommander.model.ConnectionState
import com.example.tvcommander.model.DiscoveredDevice
import com.example.tvcommander.model.TvDevice
import com.example.tvcommander.ui.theme.AccentAzure
import com.example.tvcommander.ui.theme.AccentCyan
import com.example.tvcommander.ui.theme.ControlButtonBackground
import com.example.tvcommander.ui.theme.ControlButtonBorder
import com.example.tvcommander.ui.theme.DarkCardSurface
import com.example.tvcommander.ui.theme.PowerRed
import com.example.tvcommander.ui.theme.SuccessGreen
import com.example.tvcommander.ui.theme.TextMuted
import com.example.tvcommander.ui.theme.TextPrimary
import com.example.tvcommander.ui.theme.TextSecondary
import com.example.tvcommander.ui.theme.WarningAmber

@Composable
fun SettingsScreen(
    connectionState: ConnectionState,
    devices: List<TvDevice>,
    activeDeviceId: String,
    autoConnect: Boolean,
    discoveredDevices: List<DiscoveredDevice>,
    isScanningNetwork: Boolean,
    hapticEnabled: Boolean = true,
    volumeKeysEnabled: Boolean = true,
    keepScreenOn: Boolean = false,
    appTheme: String = "cyan",
    onSelectDevice: (TvDevice) -> Unit,
    onAddOrUpdateDevice: (TvDevice) -> Unit,
    onDeleteDevice: (String) -> Unit,
    onSetAutoConnect: (Boolean) -> Unit,
    onSetHapticEnabled: (Boolean) -> Unit = {},
    onSetVolumeKeysEnabled: (Boolean) -> Unit = {},
    onSetKeepScreenOn: (Boolean) -> Unit = {},
    onSetAppTheme: (String) -> Unit = {},
    onScanNetwork: () -> Unit,
    onRebootTv: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var editingDevice by remember { mutableStateOf<TvDevice?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var inputUrl by remember { mutableStateOf("") }
    val isConnected = connectionState is ConnectionState.Connected

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ================= HEADER & CONNECTION STATUS =================
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isConnected) AccentCyan.copy(alpha = 0.5f) else ControlButtonBorder,
                    RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            when (connectionState) {
                                is ConnectionState.Connected -> SuccessGreen.copy(alpha = 0.2f)
                                is ConnectionState.Connecting -> AccentCyan.copy(alpha = 0.2f)
                                is ConnectionState.Error -> PowerRed.copy(alpha = 0.2f)
                                is ConnectionState.Disconnected -> ControlButtonBackground
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Default.CastConnected else Icons.Default.Tv,
                        contentDescription = null,
                        tint = when (connectionState) {
                            is ConnectionState.Connected -> SuccessGreen
                            is ConnectionState.Connecting -> AccentCyan
                            is ConnectionState.Error -> PowerRed
                            is ConnectionState.Disconnected -> TextSecondary
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (connectionState) {
                            is ConnectionState.Connected -> "Подключено к ТВ"
                            is ConnectionState.Connecting -> "Подключение..."
                            is ConnectionState.Error -> "Ошибка связи"
                            is ConnectionState.Disconnected -> "Не подключен"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )

                    Text(
                        text = when (connectionState) {
                            is ConnectionState.Connected -> "${(connectionState as ConnectionState.Connected).deviceName} (${(connectionState as ConnectionState.Connected).host}:${(connectionState as ConnectionState.Connected).port})"
                            is ConnectionState.Connecting -> "Связь с ${(connectionState as ConnectionState.Connecting).host}:${(connectionState as ConnectionState.Connecting).port}"
                            is ConnectionState.Error -> (connectionState as ConnectionState.Error).message
                            is ConnectionState.Disconnected -> "Выберите устройство ниже"
                        },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }
            }
        }

        // ================= COLOR THEMES SELECTOR =================
        Text(
            text = "Тема оформления",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val themes = listOf(
                Triple("cyan", "Cyber Cyan", Color(0xFF00E5FF)),
                Triple("amethyst", "Amethyst", Color(0xFFC084FC)),
                Triple("emerald", "Emerald", Color(0xFF10B981)),
                Triple("crimson", "Crimson", Color(0xFFFF4B4B))
            )

            themes.forEach { (id, name, color) ->
                val isSelected = appTheme == id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) color.copy(alpha = 0.2f) else DarkCardSurface,
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.5.dp,
                            if (isSelected) color else ControlButtonBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSetAppTheme(id)
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = name,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) color else TextSecondary
                        )
                    }
                }
            }
        }

        // ================= CONTROL PREFERENCES =================
        Text(
            text = "Управление и эргономика",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, ControlButtonBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Haptic feedback switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Тактильная вибрация", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Виброотклик при нажатии кнопок пульта", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = {
                            if (it) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSetHapticEnabled(it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = AccentCyan, uncheckedTrackColor = ControlButtonBackground)
                    )
                }

                // Hardware volume switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Кнопки громкости смартфона", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Физические клавиши телефона меняют звук ТВ", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = volumeKeysEnabled,
                        onCheckedChange = { onSetVolumeKeysEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = AccentCyan, uncheckedTrackColor = ControlButtonBackground)
                    )
                }

                // Keep screen on switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Не гасить экран смартфона", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Держать экран активным при открытом приложении", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = keepScreenOn,
                        onCheckedChange = { onSetKeepScreenOn(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = AccentCyan, uncheckedTrackColor = ControlButtonBackground)
                    )
                }

                // Auto connect switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Автоподключение", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Автоматически связываться с активным ТВ при старте", color = TextSecondary, fontSize = 12.sp)
                    }
                    Switch(
                        checked = autoConnect,
                        onCheckedChange = { onSetAutoConnect(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = AccentCyan, uncheckedTrackColor = ControlButtonBackground)
                    )
                }
            }
        }

        // ================= TV DEVICES SECTION =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Мои Телевизоры",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Auto scan network button
                IconButton(
                    onClick = {
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onScanNetwork()
                    },
                    enabled = !isScanningNetwork,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentAzure.copy(alpha = 0.2f))
                ) {
                    if (isScanningNetwork) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AccentCyan, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Поиск ТВ в сети", tint = AccentCyan, modifier = Modifier.size(18.dp))
                    }
                }

                // Add device button
                IconButton(
                    onClick = {
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        editingDevice = null
                        showAddDialog = true
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentCyan.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить ТВ", tint = AccentCyan, modifier = Modifier.size(18.dp))
                }
            }
        }

        // List of configured devices
        devices.forEach { device ->
            val isActive = device.id == activeDeviceId
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isActive) DarkCardSurface else ControlButtonBackground.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isActive) AccentCyan else ControlButtonBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectDevice(device)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isActive,
                        onClick = {
                            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectDevice(device)
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentCyan, unselectedColor = TextMuted)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = device.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${device.ip}:${device.port}" + if (device.mac.isNotBlank()) " • WoL: ${device.mac}" else "",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    // Edit button
                    IconButton(
                        onClick = {
                            editingDevice = device
                            showAddDialog = true
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }

                    // Delete button (if more than 1 device)
                    if (devices.size > 1) {
                        IconButton(
                            onClick = { onDeleteDevice(device.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = PowerRed.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Network Scan Discovered devices
        AnimatedVisibility(visible = discoveredDevices.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkCardSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AccentAzure, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Найдено устройств в сети Wi-Fi:",
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    discoveredDevices.forEach { disc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ControlButtonBackground)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(disc.suggestedName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("${disc.ip}:${disc.port}", color = TextSecondary, fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    val newDev = TvDevice(
                                        id = "tv_${System.currentTimeMillis()}",
                                        name = disc.suggestedName,
                                        ip = disc.ip,
                                        port = disc.port
                                    )
                                    onAddOrUpdateDevice(newDev)
                                    onSelectDevice(newDev)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentAzure),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Добавить", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // ================= ADB TOOLS (Reboot & URL) =================
        Text(
            text = "Быстрые команды ADB",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showUrlDialog = true },
                enabled = isConnected,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ControlButtonBackground),
                modifier = Modifier.weight(1f).height(44.dp)
            ) {
                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentCyan)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Открыть ссылку", fontSize = 12.sp, color = TextPrimary)
            }

            Button(
                onClick = onRebootTv,
                enabled = isConnected,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PowerRed.copy(alpha = 0.2f)),
                modifier = Modifier.weight(1f).height(44.dp)
            ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = PowerRed)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Перезагрузка", fontSize = 12.sp, color = PowerRed)
            }
        }

        // ================= XIAOMI TV WOL INSTRUCTIONS =================
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Как настроить Xiaomi Mi TV для включения по Wi-Fi",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "1. На телевизоре откройте: «Настройки» -> «Настройки устройства» -> «Питание» -> включите «Быстрый старт» (Fast Boot).\n" +
                            "2. В «Сеть и Интернет» включите «Пробуждение по сети» (WoWLAN).\n" +
                            "3. Выключайте ТВ коротким нажатием кнопки на пульте (режим ожидания), а не долгим зажатием.\n" +
                            "4. Если подключить ТВ интернет-кабелем (Ethernet LAN), Wake-on-LAN срабатывает в 100% случаев из любого режима сна.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }

    // Add / Edit TV Dialog
    if (showAddDialog) {
        var devName by remember { mutableStateOf(editingDevice?.name ?: "Мой Телевизор") }
        var devIp by remember { mutableStateOf(editingDevice?.ip ?: "192.168.68.108") }
        var devPort by remember { mutableStateOf((editingDevice?.port ?: 5555).toString()) }
        var devMac by remember { mutableStateOf(editingDevice?.mac ?: "") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(if (editingDevice == null) "Добавить Телевизор" else "Редактировать ТВ", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = devName,
                        onValueChange = { devName = it },
                        label = { Text("Название (например: Xiaomi Mi TV)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentCyan, focusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = devIp,
                        onValueChange = { devIp = it },
                        label = { Text("IP-адрес (например: 192.168.68.108)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentCyan, focusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = devPort,
                        onValueChange = { devPort = it },
                        label = { Text("Порт ADB (по умолч. 5555)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentCyan, focusedTextColor = TextPrimary)
                    )
                    OutlinedTextField(
                        value = devMac,
                        onValueChange = { devMac = it },
                        label = { Text("MAC-адрес для WoL (необязательно)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentCyan, focusedTextColor = TextPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val portInt = devPort.toIntOrNull() ?: 5555
                        val device = TvDevice(
                            id = editingDevice?.id ?: "tv_${System.currentTimeMillis()}",
                            name = devName.ifBlank { "Телевизор" },
                            ip = devIp.trim(),
                            port = portInt,
                            mac = devMac.trim()
                        )
                        onAddOrUpdateDevice(device)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
                ) {
                    Text("Сохранить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Отмена", color = TextSecondary)
                }
            }
        )
    }

    // Open URL Dialog
    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            title = { Text("Открыть ссылку на экране ТВ", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    label = { Text("URL (YouTube, видео или сайт)") },
                    singleLine = true,
                    placeholder = { Text("https://youtu.be/...") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentCyan, focusedTextColor = TextPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputUrl.isNotBlank()) {
                            onOpenUrl(inputUrl.trim())
                            showUrlDialog = false
                            inputUrl = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black)
                ) {
                    Text("Открыть", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUrlDialog = false }) {
                    Text("Отмена", color = TextSecondary)
                }
            }
        )
    }
}
