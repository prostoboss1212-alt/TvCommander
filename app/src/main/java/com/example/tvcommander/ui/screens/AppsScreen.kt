package com.example.tvcommander.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvcommander.model.ConnectionState
import com.example.tvcommander.model.CuratedApp
import com.example.tvcommander.model.TvApp
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
fun AppsScreen(
    connectionState: ConnectionState,
    apps: List<TvApp>,
    isLoading: Boolean,
    curatedApps: List<CuratedApp> = emptyList(),
    onRefreshApps: (Boolean) -> Unit,
    onLaunchApp: (String) -> Unit,
    onForceStopApp: (String) -> Unit,
    onUninstallApp: (String) -> Unit,
    onInstallCuratedApp: (CuratedApp) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    var includeSystem by remember { mutableStateOf(false) }
    var appToDelete by remember { mutableStateOf<TvApp?>(null) }
    var selectedCuratedAppForInfo by remember { mutableStateOf<CuratedApp?>(null) }

    val isConnected = connectionState is ConnectionState.Connected

    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) {
            apps
        } else {
            apps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search & Refresh Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Поиск приложений...", color = TextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Поиск", tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Очистить", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentCyan,
                    unfocusedBorderColor = ControlButtonBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRefreshApps(includeSystem)
                },
                enabled = isConnected && !isLoading,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCardSurface)
                    .border(1.dp, ControlButtonBorder, RoundedCornerShape(14.dp))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AccentCyan, strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить",
                        tint = if (isConnected) AccentCyan else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // System apps toggle chip & count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = includeSystem,
                onClick = {
                    includeSystem = !includeSystem
                    onRefreshApps(includeSystem)
                },
                label = { Text("Системные приложения", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                    selectedLabelColor = AccentCyan,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = includeSystem,
                    borderColor = if (includeSystem) AccentCyan else ControlButtonBorder
                )
            )

            Text(
                text = "${filteredApps.size} прил.",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Curated Top TV Apps Row (1-click install)
        if (searchQuery.isBlank() && curatedApps.isNotEmpty()) {
            Text(
                text = "⭐ Рекомендуемые приложения для ТВ (1 клик)",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(curatedApps, key = { it.id }) { item ->
                    val isInstalled = apps.any { it.packageName == item.packageName }
                    CuratedAppCard(
                        app = item,
                        isInstalled = isInstalled,
                        isConnected = isConnected,
                        onInstall = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onInstallCuratedApp(item)
                        },
                        onLaunch = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLaunchApp(item.packageName)
                        },
                        onInfo = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedCuratedAppForInfo = item
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Apps List
        if (!isConnected) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Подключитесь к телевизору для просмотра приложений", color = TextSecondary, fontSize = 14.sp)
            }
        } else if (filteredApps.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Tv, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Ничего не найдено" else "Список приложений пуст",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppItemCard(
                        app = app,
                        onLaunch = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLaunchApp(app.packageName)
                        },
                        onStop = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onForceStopApp(app.packageName)
                        },
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            appToDelete = app
                        }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (appToDelete != null) {
        val app = appToDelete!!
        AlertDialog(
            onDismissRequest = { appToDelete = null },
            title = { Text("Удалить приложение?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Вы действительно хотите удалить «${app.label}» (${app.packageName}) с экрана телевизора?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUninstallApp(app.packageName)
                        appToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PowerRed)
                ) {
                    Text("Удалить", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { appToDelete = null }) {
                    Text("Отмена", color = TextSecondary)
                }
            },
            containerColor = DarkCardSurface
        )
    }

    // Curated App Info Dialog
    selectedCuratedAppForInfo?.let { curated ->
        AlertDialog(
            onDismissRequest = { selectedCuratedAppForInfo = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = DarkSurfaceVariant,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = curated.name, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { selectedCuratedAppForInfo = null }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = TextSecondary)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(curated.iconColorHex).copy(alpha = 0.2f),
                        modifier = Modifier.border(1.dp, Color(curated.iconColorHex).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = curated.tag,
                            color = Color(curated.iconColorHex),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(text = curated.fullDesc, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCuratedAppForInfo = null }) {
                    Text("Понятно", color = AccentCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun CuratedAppCard(
    app: CuratedApp,
    isInstalled: Boolean,
    isConnected: Boolean,
    onInstall: () -> Unit,
    onLaunch: () -> Unit,
    onInfo: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkCardSurface,
        modifier = Modifier
            .width(220.dp)
            .border(
                1.dp,
                if (isInstalled) AccentCyan.copy(alpha = 0.5f) else ControlButtonBorder,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Colored Avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(app.iconColorHex).copy(alpha = 0.2f))
                        .border(1.dp, Color(app.iconColorHex), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.name.take(2).uppercase(),
                        color = Color(app.iconColorHex),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                IconButton(onClick = onInfo, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "Инфо", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = app.name,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = app.tag,
                color = Color(app.iconColorHex),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.shortDesc,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isInstalled) {
                Button(
                    onClick = onLaunch,
                    enabled = isConnected,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen.copy(alpha = 0.85f)),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Запустить", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onInstall,
                    enabled = isConnected,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Установить", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AppItemCard(
    app: TvApp,
    onLaunch: () -> Unit,
    onStop: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkCardSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ControlButtonBorder, RoundedCornerShape(16.dp))
            .clickable { onLaunch() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Avatar Icon (First letters of name)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(app.iconColorHex).copy(alpha = 0.2f))
                    .border(1.dp, Color(app.iconColorHex), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.label.take(2).uppercase(),
                    color = Color(app.iconColorHex),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // App Label & Package Name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = app.packageName,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action Buttons (Play, Force Stop, Delete)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onLaunch,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentCyan.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Запустить",
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WarningAmber.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Остановить",
                        tint = WarningAmber,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (!app.isSystem) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PowerRed.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Удалить",
                            tint = PowerRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
