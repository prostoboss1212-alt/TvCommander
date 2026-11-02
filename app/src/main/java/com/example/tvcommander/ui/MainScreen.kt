package com.example.tvcommander.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.tvcommander.ui.screens.AppsScreen
import com.example.tvcommander.ui.screens.InstallApkScreen
import com.example.tvcommander.ui.screens.LaunchersScreen
import com.example.tvcommander.ui.screens.OptimizationScreen
import com.example.tvcommander.ui.screens.RemoteScreen
import com.example.tvcommander.ui.screens.SettingsScreen
import com.example.tvcommander.ui.theme.AccentAzure
import com.example.tvcommander.ui.theme.AccentCyan
import com.example.tvcommander.ui.theme.ControlButtonBackground
import com.example.tvcommander.ui.theme.ControlButtonBorder
import com.example.tvcommander.ui.theme.DarkBackground
import com.example.tvcommander.ui.theme.DarkCardSurface
import com.example.tvcommander.ui.theme.DarkSurface
import com.example.tvcommander.ui.theme.PowerRed
import com.example.tvcommander.ui.theme.SuccessGreen
import com.example.tvcommander.ui.theme.TextMuted
import com.example.tvcommander.ui.theme.TextPrimary
import com.example.tvcommander.ui.theme.TextSecondary
import com.example.tvcommander.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TvViewModel) {
    val haptic = LocalHapticFeedback.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    val connectionState by viewModel.connectionState.collectAsState()
    val devices by viewModel.devices.collectAsState()
    val activeDeviceId by viewModel.activeDeviceId.collectAsState()
    val autoConnect by viewModel.autoConnect.collectAsState()

    val installedApps by viewModel.installedApps.collectAsState()
    val isLoadingApps by viewModel.isLoadingApps.collectAsState()
    val installStatus by viewModel.installStatus.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val isScanningNetwork by viewModel.isScanningNetwork.collectAsState()
    val launcherOperationStatus by viewModel.launcherOperationStatus.collectAsState()
    val launchersList = viewModel.launchersList
    val memoryStats by viewModel.memoryStats.collectAsState()
    val optimizationStatus by viewModel.optimizationStatus.collectAsState()
    val animationScale by viewModel.animationScale.collectAsState()
    val isAdGuardDnsActive by viewModel.isAdGuardDnsActive.collectAsState()
    val sleepTimerRemainingSeconds by viewModel.sleepTimerRemainingSeconds.collectAsState()
    val screenshotState by viewModel.screenshotState.collectAsState()
    val isWakingUp by viewModel.isWakingUp.collectAsState()
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()
    val volumeKeysEnabled by viewModel.volumeKeysEnabled.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val remoteMode by viewModel.remoteMode.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val curatedApps = viewModel.curatedApps

    var showDeviceDropdown by remember { mutableStateOf(false) }

    val activeDevice = devices.find { it.id == activeDeviceId } ?: devices.firstOrNull()

    // Listen for snackbar messages
    LaunchedEffect(Unit) {
        viewModel.snackBarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title
                        Text(
                            text = "TV Commander",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )

                        // TV Device Switcher Dropdown
                        Box {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = DarkCardSurface,
                                modifier = Modifier
                                    .border(1.dp, ControlButtonBorder, RoundedCornerShape(20.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showDeviceDropdown = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status dot
                                    val statusDotColor = when (connectionState) {
                                        is ConnectionState.Connected -> SuccessGreen
                                        is ConnectionState.Connecting -> WarningAmber
                                        is ConnectionState.Error -> PowerRed
                                        is ConnectionState.Disconnected -> TextMuted
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(statusDotColor)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = activeDevice?.name ?: "Выбрать ТВ",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )

                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Выбор ТВ",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Device Selection Menu
                            DropdownMenu(
                                expanded = showDeviceDropdown,
                                onDismissRequest = { showDeviceDropdown = false },
                                modifier = Modifier.background(DarkCardSurface).border(1.dp, ControlButtonBorder, RoundedCornerShape(8.dp))
                            ) {
                                devices.forEach { dev ->
                                    val isSelected = dev.id == activeDeviceId
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isSelected) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                }
                                                Column {
                                                    Text(dev.name, color = if (isSelected) AccentCyan else TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                                    Text(dev.ip, color = TextMuted, fontSize = 11.sp)
                                                }
                                            }
                                        },
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.switchActiveDevice(dev)
                                            showDeviceDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedTab = 0
                    },
                    icon = { Icon(Icons.Default.SettingsRemote, contentDescription = "Пульт") },
                    label = { Text("Пульт", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedTab = 1
                        if (memoryStats.totalRamMb == 0) {
                            viewModel.refreshMemoryStats()
                        }
                    },
                    icon = { Icon(Icons.Default.RocketLaunch, contentDescription = "Очистка") },
                    label = { Text("Очистка", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedTab = 2
                        if (installedApps.isEmpty()) {
                            viewModel.refreshApps()
                        }
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Лаунчеры") },
                    label = { Text("Лаунчеры", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedTab = 3
                        if (installedApps.isEmpty()) {
                            viewModel.refreshApps()
                        }
                    },
                    icon = { Icon(Icons.Default.Apps, contentDescription = "Программы") },
                    label = { Text("Программы", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedTab = 4
                    },
                    icon = { Icon(Icons.Default.InstallMobile, contentDescription = "APK") },
                    label = { Text("APK", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedTab = 5
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Настройки") },
                    label = { Text("Опции", fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        indicatorColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            when (selectedTab) {
                0 -> RemoteScreen(
                    connectionState = connectionState,
                    sleepTimerRemainingSeconds = sleepTimerRemainingSeconds,
                    screenshotState = screenshotState,
                    isWakingUp = isWakingUp,
                    activeDeviceName = activeDevice?.name ?: "Xiaomi Mi TV",
                    remoteMode = remoteMode,
                    hapticEnabled = hapticEnabled,
                    onChangeRemoteMode = { viewModel.setRemoteMode(it) },
                    onSendKey = { viewModel.sendKey(it) },
                    onSendText = { viewModel.sendText(it) },
                    onSendClipboard = { viewModel.sendClipboard(it) },
                    onLaunchApp = { viewModel.launchApp(it) },
                    onTurnOn = { viewModel.turnOnTv() },
                    onTurnOff = { viewModel.turnOffTv() },
                    onSetSleepTimer = { viewModel.setSleepTimer(it) },
                    onCancelSleepTimer = { viewModel.cancelSleepTimer() },
                    onTakeScreenshot = { viewModel.takeScreenshot() },
                    onDismissScreenshot = { viewModel.dismissScreenshot() }
                )
                1 -> OptimizationScreen(
                    connectionState = connectionState,
                    memoryStats = memoryStats,
                    optimizationStatus = optimizationStatus,
                    currentAnimationScale = animationScale,
                    isAdGuardDnsActive = isAdGuardDnsActive,
                    onRunFullOptimization = { viewModel.runFullOptimization() },
                    onCleanCache = { viewModel.cleanCacheOnly() },
                    onFreeRam = { viewModel.freeRamOnly() },
                    onRunFstrim = { viewModel.runFstrimOnly() },
                    onCleanLogs = { viewModel.cleanLogsOnly() },
                    onSetAnimationScale = { viewModel.setAnimationScale(it) },
                    onRefreshStats = { viewModel.refreshMemoryStats() },
                    onResetStatus = { viewModel.resetOptimizationStatus() },
                    onToggleAdGuardDns = { viewModel.toggleAdGuardDns() }
                )
                2 -> LaunchersScreen(
                    connectionState = connectionState,
                    launchers = launchersList,
                    installedApps = installedApps,
                    operationStatus = launcherOperationStatus,
                    onDownloadAndInstall = { viewModel.downloadAndInstallLauncher(it) },
                    onLaunchLauncher = { viewModel.launchLauncher(it) },
                    onSetDefaultLauncher = { viewModel.setDefaultLauncher(it) },
                    onUninstallLauncher = { viewModel.uninstallLauncher(it) },
                    onRollbackToStock = { viewModel.rollbackToStockLauncher() },
                    onResetStatus = { viewModel.resetLauncherStatus() },
                    onNavigateToApkTab = { selectedTab = 4 }
                )
                3 -> AppsScreen(
                    connectionState = connectionState,
                    apps = installedApps,
                    isLoading = isLoadingApps,
                    curatedApps = curatedApps,
                    onRefreshApps = { viewModel.refreshApps(it) },
                    onLaunchApp = { viewModel.launchApp(it) },
                    onForceStopApp = { viewModel.forceStopApp(it) },
                    onUninstallApp = { viewModel.uninstallApp(it) },
                    onInstallCuratedApp = { viewModel.downloadAndInstallCuratedApp(it) }
                )
                4 -> InstallApkScreen(
                    connectionState = connectionState,
                    installStatus = installStatus,
                    onInstallApk = { uri, fileName -> viewModel.installApk(uri, fileName) },
                    onResetStatus = { viewModel.resetInstallStatus() }
                )
                5 -> SettingsScreen(
                    connectionState = connectionState,
                    devices = devices,
                    activeDeviceId = activeDeviceId,
                    autoConnect = autoConnect,
                    discoveredDevices = discoveredDevices,
                    isScanningNetwork = isScanningNetwork,
                    hapticEnabled = hapticEnabled,
                    volumeKeysEnabled = volumeKeysEnabled,
                    keepScreenOn = keepScreenOn,
                    appTheme = appTheme,
                    onSelectDevice = { dev -> viewModel.switchActiveDevice(dev) },
                    onAddOrUpdateDevice = { dev -> viewModel.addOrUpdateDevice(dev) },
                    onDeleteDevice = { devId -> viewModel.deleteDevice(devId) },
                    onSetAutoConnect = { auto -> viewModel.setAutoConnect(auto) },
                    onSetHapticEnabled = { viewModel.setHapticEnabled(it) },
                    onSetVolumeKeysEnabled = { viewModel.setVolumeKeysEnabled(it) },
                    onSetKeepScreenOn = { viewModel.setKeepScreenOn(it) },
                    onSetAppTheme = { viewModel.setAppTheme(it) },
                    onScanNetwork = { viewModel.startNetworkScan() },
                    onRebootTv = { viewModel.rebootTv() },
                    onOpenUrl = { url -> viewModel.openUrlOnTv(url) }
                )
            }
        }
    }
}
