package com.example.tvcommander.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvcommander.model.ConnectionState
import com.example.tvcommander.model.LauncherItem
import com.example.tvcommander.model.LauncherOperationStatus
import com.example.tvcommander.model.TvApp
import com.example.tvcommander.ui.theme.AccentAzure
import com.example.tvcommander.ui.theme.AccentCyan
import com.example.tvcommander.ui.theme.AccentTeal
import com.example.tvcommander.ui.theme.ControlButtonBackground
import com.example.tvcommander.ui.theme.ControlButtonBorder
import com.example.tvcommander.ui.theme.DarkCardSurface
import com.example.tvcommander.ui.theme.DarkSurface
import com.example.tvcommander.ui.theme.DarkSurfaceVariant
import com.example.tvcommander.ui.theme.PowerRed
import com.example.tvcommander.ui.theme.SuccessGreen
import com.example.tvcommander.ui.theme.TextMuted
import com.example.tvcommander.ui.theme.TextPrimary
import com.example.tvcommander.ui.theme.TextSecondary
import com.example.tvcommander.ui.theme.WarningAmber

@Composable
fun LaunchersScreen(
    connectionState: ConnectionState,
    launchers: List<LauncherItem>,
    installedApps: List<TvApp>,
    operationStatus: LauncherOperationStatus,
    onDownloadAndInstall: (LauncherItem) -> Unit,
    onLaunchLauncher: (String) -> Unit,
    onSetDefaultLauncher: (String) -> Unit,
    onUninstallLauncher: (String) -> Unit,
    onRollbackToStock: () -> Unit,
    onResetStatus: () -> Unit,
    onNavigateToApkTab: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isConnected = connectionState is ConnectionState.Connected

    var selectedLauncherForInfo by remember { mutableStateOf<LauncherItem?>(null) }
    var showRollbackDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ================= CONNECTION STATUS BANNER =================
        if (!isConnected) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = WarningAmber.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarningAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Предупреждение",
                        tint = WarningAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Подключитесь к телевизору вверху экрана для управления оболочками и лаунчерами.",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ================= OPERATION STATUS BANNER =================
        when (operationStatus) {
            is LauncherOperationStatus.Working -> {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentAzure.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = AccentCyan,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = operationStatus.message,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            is LauncherOperationStatus.Success -> {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessGreen.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Успех",
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = operationStatus.message,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onResetStatus() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextMuted)
                        }
                    }
                }
            }
            is LauncherOperationStatus.Error -> {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PowerRed.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PowerRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Ошибка",
                            tint = PowerRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = operationStatus.message,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onResetStatus() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextMuted)
                        }
                    }
                }
            }
            LauncherOperationStatus.Idle -> { /* Ничего не отображаем */ }
        }

        // ================= ROLLBACK CARD =================
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, PowerRed.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PowerRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Откат",
                                tint = PowerRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Откат к заводской оболочке",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Xiaomi PatchWall / TCL Home / Google TV",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Если кастомный лаунчер не понравился или возникли сбои, нажмите для мгновенного удаления сторонних оболочек и восстановления стандартного интерфейса ТВ.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showRollbackDialog = true
                    },
                    enabled = isConnected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PowerRed.copy(alpha = 0.85f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Вернуть всё как было до установки",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ================= SECTION TITLE =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Оптимизированные оболочки",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${launchers.size} доступно",
                color = AccentCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // ================= LAUNCHERS LIST =================
        launchers.forEach { launcher ->
            val matchingApp = installedApps.find { it.packageName in launcher.packageNames }
            val isInstalled = matchingApp != null

            LauncherCard(
                launcher = launcher,
                isInstalled = isInstalled,
                installedPackage = matchingApp?.packageName,
                isConnected = isConnected,
                onInfoClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedLauncherForInfo = launcher
                },
                onInstall = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDownloadAndInstall(launcher)
                },
                onLaunch = { pkg ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLaunchLauncher(pkg)
                },
                onSetDefault = { pkg ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSetDefaultLauncher(pkg)
                },
                onUninstall = { pkg ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUninstallLauncher(pkg)
                },
                onNavigateToApkTab = onNavigateToApkTab
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // ================= DETAIL INFO DIALOG =================
    selectedLauncherForInfo?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedLauncherForInfo = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = DarkSurfaceVariant,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.name,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { selectedLauncherForInfo = null },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextSecondary)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tag and RAM chip
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentCyan.copy(alpha = 0.15f),
                            modifier = Modifier.border(1.dp, AccentCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = item.tag,
                                color = AccentCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentTeal.copy(alpha = 0.15f),
                            modifier = Modifier.border(1.dp, AccentTeal.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = AccentTeal,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.ramUsage,
                                    color = AccentTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Full description
                    Text(
                        text = item.fullDescription,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    // Advantages
                    if (item.advantages.isNotEmpty()) {
                        Text(
                            text = "Преимущества:",
                            color = SuccessGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        item.advantages.forEach { adv ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = adv,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Disadvantages
                    if (item.disadvantages.isNotEmpty()) {
                        Text(
                            text = "Особенности и нюансы:",
                            color = WarningAmber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        item.disadvantages.forEach { dis ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text(
                                    text = "•",
                                    color = WarningAmber,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = dis,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedLauncherForInfo = null }) {
                    Text("Понятно", color = AccentCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // ================= ROLLBACK CONFIRMATION DIALOG =================
    if (showRollbackDialog) {
        AlertDialog(
            onDismissRequest = { showRollbackDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = DarkSurfaceVariant,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = PowerRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Откатить телевизор к заводскому виду?",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Это действие удалит установленные через приложение кастомные оболочки (FLauncher и др.), повторно активирует стандартный лаунчер Xiaomi / TCL / Android TV и откроет домашний экран телевизора.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRollbackDialog = false
                        onRollbackToStock()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PowerRed)
                ) {
                    Text("Восстановить", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRollbackDialog = false }) {
                    Text("Отмена", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun LauncherCard(
    launcher: LauncherItem,
    isInstalled: Boolean,
    installedPackage: String?,
    isConnected: Boolean,
    onInfoClick: () -> Unit,
    onInstall: () -> Unit,
    onLaunch: (String) -> Unit,
    onSetDefault: (String) -> Unit,
    onUninstall: (String) -> Unit,
    onNavigateToApkTab: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkCardSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isInstalled) AccentCyan.copy(alpha = 0.5f) else ControlButtonBorder,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name, Tag, Info button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = launcher.name,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isInstalled) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SuccessGreen.copy(alpha = 0.2f),
                                modifier = Modifier.border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = "Установлен",
                                    color = SuccessGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = launcher.tag,
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Info Button
                OutlinedButton(
                    onClick = onInfoClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ControlButtonBorder)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Описание",
                        tint = AccentCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Инфо", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = launcher.shortDesc,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            // RAM chip
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = launcher.ramUsage,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ================= ACTION BUTTONS =================
            if (isInstalled && installedPackage != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onLaunch(installedPackage) },
                        enabled = isConnected,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAzure),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Запустить", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onSetDefault(installedPackage) },
                        enabled = isConnected,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(AccentCyan.copy(alpha = 0.5f))
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "По умолч.", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { onUninstall(installedPackage) },
                        enabled = isConnected,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PowerRed),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(PowerRed.copy(alpha = 0.5f))
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(text = "Удалить", fontSize = 12.sp, color = PowerRed)
                    }
                }
            } else {
                if (launcher.canDirectInstall) {
                    Button(
                        onClick = onInstall,
                        enabled = isConnected,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentCyan,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Скачать и установить на ТВ (в 1 клик)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onNavigateToApkTab,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(ControlButtonBorder)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Установить свой APK файл",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
