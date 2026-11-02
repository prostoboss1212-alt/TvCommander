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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvcommander.model.ConnectionState
import com.example.tvcommander.model.OptimizationStatus
import com.example.tvcommander.model.TvMemoryStats
import com.example.tvcommander.ui.theme.AccentAzure
import com.example.tvcommander.ui.theme.AccentCyan
import com.example.tvcommander.ui.theme.AccentTeal
import com.example.tvcommander.ui.theme.ControlButtonBackground
import com.example.tvcommander.ui.theme.ControlButtonBorder
import com.example.tvcommander.ui.theme.DarkCardSurface
import com.example.tvcommander.ui.theme.DarkSurfaceVariant
import com.example.tvcommander.ui.theme.PowerRed
import com.example.tvcommander.ui.theme.SuccessGreen
import com.example.tvcommander.ui.theme.TextMuted
import com.example.tvcommander.ui.theme.TextPrimary
import com.example.tvcommander.ui.theme.TextSecondary
import com.example.tvcommander.ui.theme.WarningAmber

@Composable
fun OptimizationScreen(
    connectionState: ConnectionState,
    memoryStats: TvMemoryStats,
    optimizationStatus: OptimizationStatus,
    currentAnimationScale: Float,
    onRunFullOptimization: () -> Unit,
    onCleanCache: () -> Unit,
    onFreeRam: () -> Unit,
    onRunFstrim: () -> Unit,
    onCleanLogs: () -> Unit,
    onSetAnimationScale: (Float) -> Unit,
    onRefreshStats: () -> Unit,
    onResetStatus: () -> Unit,
    isAdGuardDnsActive: Boolean = false,
    onToggleAdGuardDns: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val isConnected = connectionState is ConnectionState.Connected
    val isRunning = optimizationStatus is OptimizationStatus.Running

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ================= CONNECTION WARNING BANNER =================
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
                        text = "Подключитесь к телевизору вверху экрана для запуска очистки и оптимизации.",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ================= STATUS REPORT BANNER =================
        when (optimizationStatus) {
            is OptimizationStatus.Running -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AccentAzure.copy(alpha = 0.18f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AccentCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = AccentCyan,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = optimizationStatus.step,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (optimizationStatus.progress > 0f) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { optimizationStatus.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AccentCyan,
                                trackColor = ControlButtonBackground
                            )
                        }
                    }
                }
            }
            is OptimizationStatus.Success -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SuccessGreen.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = optimizationStatus.message,
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
            is OptimizationStatus.Error -> {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PowerRed.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PowerRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = PowerRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = optimizationStatus.error,
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
            OptimizationStatus.Idle -> { /* Скрыто */ }
        }

        // ================= SYSTEM METRICS LIVE DASHBOARD =================
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ControlButtonBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Память телевизора в реальном времени",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRefreshStats()
                        },
                        enabled = isConnected,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить",
                            tint = if (isConnected) AccentCyan else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // RAM Bar
                val totalRam = memoryStats.totalRamMb
                val availRam = memoryStats.availRamMb
                val usedRam = memoryStats.usedRamMb
                val ramPercent = if (totalRam > 0) (usedRam.toFloat() / totalRam.toFloat()).coerceIn(0f, 1f) else 0f

                val ramColor = when {
                    ramPercent > 0.85f -> PowerRed
                    ramPercent > 0.70f -> WarningAmber
                    else -> SuccessGreen
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ОЗУ (RAM):",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (totalRam > 0) "$usedRam МБ из $totalRam МБ (${(ramPercent * 100).toInt()}%)" else "Нажмите для замера",
                        color = ramColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { if (totalRam > 0) ramPercent else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ramColor,
                    trackColor = ControlButtonBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Storage and Available Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (memoryStats.storageAvailGb.isNotBlank()) "Свободно на диске: ${memoryStats.storageAvailGb}" else "Свободно ОЗУ: $availRam МБ",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (memoryStats.storageTotalGb.isNotBlank()) {
                        Text(
                            text = "Всего: ${memoryStats.storageTotalGb}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // ================= HERO: 1-CLICK TURBO BOOST =================
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = DarkSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    2.dp,
                    Brush.horizontalGradient(listOf(AccentCyan, AccentAzure, AccentTeal)),
                    RoundedCornerShape(18.dp)
                )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(AccentCyan.copy(alpha = 0.3f), AccentAzure.copy(alpha = 0.4f)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "Турбо",
                            tint = AccentCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Комплексная Турбо-Очистка",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Максимальный разгон без смены оболочки",
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Выполняет полный цикл очистки: сбрасывает кэш всех приложений (YouTube, Кинопоиск, плееры), выгружает фоновые зомби-процессы, чистит системные логи, оптимизирует флеш-накопитель (TRIM) и ускоряет анимации меню в 2 раза.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRunFullOptimization()
                    },
                    enabled = isConnected && !isRunning,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCyan,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isRunning) "Оптимизация..." else "Запустить Турбо-Очистку в 1 клик",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // ================= ADGUARD DNS AD-BLOCK CARD =================
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (isAdGuardDnsActive) SuccessGreen.copy(alpha = 0.6f) else ControlButtonBorder,
                    RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isAdGuardDnsActive) SuccessGreen.copy(alpha = 0.2f)
                                    else ControlButtonBackground
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isAdGuardDnsActive) SuccessGreen else AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Блокировка рекламы на ТВ",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAdGuardDnsActive) "AdGuard DNS активен (защищено)" else "Стандартный DNS телевизора",
                                color = if (isAdGuardDnsActive) SuccessGreen else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (isAdGuardDnsActive) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SuccessGreen.copy(alpha = 0.2f),
                            modifier = Modifier.border(1.dp, SuccessGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = "Активно",
                                color = SuccessGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Блокирует рекламные баннеры, трекеры и спам-запросы на уровне всей системы Android TV через AdGuard Private DNS.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleAdGuardDns()
                    },
                    enabled = isConnected && !isRunning,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAdGuardDnsActive) PowerRed.copy(alpha = 0.85f) else AccentCyan,
                        contentColor = if (isAdGuardDnsActive) Color.White else Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth().height(40.dp)
                ) {
                    Text(
                        text = if (isAdGuardDnsActive) "Отключить AdGuard DNS" else "Включить блокировку рекламы в 1 клик",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ================= INDIVIDUAL OPTIMIZATIONS TITLE =================
        Text(
            text = "Выборочная настройка и очистка",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        // 1. Очистка кэша
        OptimizationActionCard(
            icon = Icons.Default.CleaningServices,
            iconTint = AccentCyan,
            title = "Очистка кэша всех приложений",
            description = "Безопасно удаляет гигабайты временных эскизов и кэша из YouTube, стримингов и плееров. Пароли, аккаунты и настройки сохраняются.",
            buttonText = "Очистить кэш",
            buttonColor = AccentCyan,
            enabled = isConnected && !isRunning,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCleanCache()
            }
        )

        // 2. Освобождение RAM
        OptimizationActionCard(
            icon = Icons.Default.Memory,
            iconTint = SuccessGreen,
            title = "Освободить оперативную память (RAM)",
            description = "Принудительно останавливает уснувшие фоновые службы и процессы (am kill-all), возвращая ТВ максимальную отзывчивость.",
            buttonText = "Освободить RAM",
            buttonColor = SuccessGreen,
            enabled = isConnected && !isRunning,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onFreeRam()
            }
        )

        // 3. Скорость системных анимаций
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkCardSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ControlButtonBorder, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = AccentAzure,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Скорость анимаций интерфейса ТВ",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ускорение меню в 2 раза устраняет эффект «задумчивости» при переходе между приложениями.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimationSpeedChip(
                        label = "0.0x Мгновенно",
                        isSelected = currentAnimationScale == 0.0f,
                        enabled = isConnected && !isRunning,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSetAnimationScale(0.0f)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AnimationSpeedChip(
                        label = "0.5x Турбо",
                        isSelected = currentAnimationScale == 0.5f,
                        enabled = isConnected && !isRunning,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSetAnimationScale(0.5f)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AnimationSpeedChip(
                        label = "1.0x Стандарт",
                        isSelected = currentAnimationScale == 1.0f,
                        enabled = isConnected && !isRunning,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSetAnimationScale(1.0f)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. TRIM флеш-памяти
        OptimizationActionCard(
            icon = Icons.Default.Storage,
            iconTint = AccentTeal,
            title = "FSTRIM флеш-накопителя",
            description = "Аппаратная оптимизация блоков памяти накопителя телевизора. Устраняет подтормаживания и ускоряет запись.",
            buttonText = "Запустить TRIM",
            buttonColor = AccentTeal,
            enabled = isConnected && !isRunning,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onRunFstrim()
            }
        )

        // 5. Очистка системных логов
        OptimizationActionCard(
            icon = Icons.Default.DeleteSweep,
            iconTint = WarningAmber,
            title = "Очистка системных логов и мусора",
            description = "Очищает кольцевые буферы logcat и временные файлы каталога /data/local/tmp.",
            buttonText = "Очистить логи",
            buttonColor = WarningAmber,
            enabled = isConnected && !isRunning,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCleanLogs()
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun OptimizationActionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    buttonText: String,
    buttonColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkCardSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ControlButtonBorder, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = buttonColor),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(buttonColor.copy(alpha = 0.6f))
                ),
                modifier = Modifier.fillMaxWidth().height(40.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) buttonColor else TextMuted
                )
            }
        }
    }
}

@Composable
private fun AnimationSpeedChip(
    label: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) AccentCyan.copy(alpha = 0.2f) else ControlButtonBackground,
        modifier = modifier
            .border(
                1.dp,
                if (isSelected) AccentCyan else ControlButtonBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) AccentCyan else if (enabled) TextPrimary else TextMuted,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
