package com.example.tvcommander.model

data class TvDevice(
    val id: String,
    val name: String,
    val ip: String,
    val port: Int = 5555,
    val mac: String = ""
)

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data class Connecting(val host: String, val port: Int) : ConnectionState
    data class Connected(val host: String, val port: Int, val deviceName: String = "") : ConnectionState
    data class Error(val message: String) : ConnectionState
}

data class TvApp(
    val packageName: String,
    val label: String,
    val isSystem: Boolean = false,
    val iconColorHex: Long = 0xFF00D2FF
)

sealed interface InstallStatus {
    data object Idle : InstallStatus
    data class Preparing(val fileName: String) : InstallStatus
    data class Installing(val fileName: String) : InstallStatus
    data class Success(val message: String) : InstallStatus
    data class Error(val error: String) : InstallStatus
}

data class DiscoveredDevice(
    val ip: String,
    val port: Int = 5555,
    val hostName: String = "",
    val suggestedName: String = ""
)

enum class RemoteKey(val keyCode: Int, val description: String) {
    POWER(26, "Power"),
    WAKEUP(224, "Wake Up"),
    SLEEP(223, "Sleep"),
    UP(19, "D-Pad Up"),
    DOWN(20, "D-Pad Down"),
    LEFT(21, "D-Pad Left"),
    RIGHT(22, "D-Pad Right"),
    CENTER(23, "D-Pad Center (OK)"),
    BACK(4, "Back"),
    HOME(3, "Home"),
    MENU(82, "Menu"),
    VOLUME_UP(24, "Volume Up"),
    VOLUME_DOWN(25, "Volume Down"),
    VOLUME_MUTE(164, "Mute"),
    CHANNEL_UP(166, "Channel Up"),
    CHANNEL_DOWN(167, "Channel Down"),
    SETTINGS(176, "Settings"),
    PLAY_PAUSE(85, "Play/Pause"),
    FAST_FORWARD(90, "Fast Forward"),
    REWIND(89, "Rewind"),
    DEL(67, "Backspace"),
    ENTER(66, "Enter"),
    SPACE(62, "Space"),
    PASTE(279, "Paste")
}

data class TvMemoryStats(
    val totalRamMb: Int = 0,
    val availRamMb: Int = 0,
    val usedRamMb: Int = 0,
    val storageTotalGb: String = "",
    val storageAvailGb: String = ""
)

sealed interface OptimizationStatus {
    data object Idle : OptimizationStatus
    data class Running(val step: String, val progress: Float = 0f) : OptimizationStatus
    data class Success(val message: String, val ramFreedMb: Int = 0) : OptimizationStatus
    data class Error(val error: String) : OptimizationStatus
}

sealed interface LauncherOperationStatus {
    data object Idle : LauncherOperationStatus
    data class Working(val message: String) : LauncherOperationStatus
    data class Success(val message: String) : LauncherOperationStatus
    data class Error(val message: String) : LauncherOperationStatus
}

data class LauncherItem(
    val id: String,
    val name: String,
    val packageNames: List<String>,
    val tag: String,
    val shortDesc: String,
    val fullDescription: String,
    val advantages: List<String>,
    val disadvantages: List<String>,
    val ramUsage: String,
    val downloadUrl: String? = null,
    val canDirectInstall: Boolean = false
)

data class CuratedApp(
    val id: String,
    val name: String,
    val packageName: String,
    val tag: String,
    val shortDesc: String,
    val fullDesc: String,
    val downloadUrl: String,
    val iconColorHex: Long = 0xFF00E5FF
)

sealed interface ScreenshotState {
    data object Idle : ScreenshotState
    data object Capturing : ScreenshotState
    data class Success(val bitmap: android.graphics.Bitmap, val timestamp: Long) : ScreenshotState
    data class Error(val message: String) : ScreenshotState
}
