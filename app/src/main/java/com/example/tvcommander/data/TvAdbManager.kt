package com.example.tvcommander.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.tvcommander.model.ConnectionState
import com.example.tvcommander.model.RemoteKey
import com.example.tvcommander.model.TvApp
import com.example.tvcommander.model.TvMemoryStats
import dadb.AdbKeyPair
import dadb.Dadb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketException

class TvAdbManager(private val context: Context) {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var dadbInstance: Dadb? = null
    private var currentHost: String? = null
    private var currentPort: Int = 5555
    private var currentDeviceName: String = ""

    private val adbMutex = Mutex()

    private val keyPair: AdbKeyPair by lazy {
        val privKey = File(context.filesDir, "adbkey")
        val pubKey = File(context.filesDir, "adbkey.pub")
        if (!privKey.exists() || !pubKey.exists()) {
            AdbKeyPair.generate(privKey, pubKey)
        }
        AdbKeyPair.read(privKey, pubKey)
    }

    /**
     * Connect to TV ADB server
     */
    suspend fun connect(host: String, port: Int = 5555, deviceName: String = ""): Result<Unit> = withContext(Dispatchers.IO) {
        adbMutex.withLock {
            runCatching {
                _connectionState.value = ConnectionState.Connecting(host, port)
                runCatching { dadbInstance?.close() }
                dadbInstance = null

                currentHost = host
                currentPort = port
                currentDeviceName = deviceName

                val dadb = Dadb.create(host, port, keyPair)
                val testResp = dadb.shell("echo connected")
                if (testResp.exitCode != 0 && testResp.allOutput.contains("device unauthorized", ignoreCase = true)) {
                    throw IllegalStateException("Подтвердите отладку на экране телевизора!")
                }

                dadbInstance = dadb
                _connectionState.value = ConnectionState.Connected(host, port, deviceName)
            }.onFailure { err ->
                dadbInstance = null
                _connectionState.value = ConnectionState.Error(err.localizedMessage ?: "Ошибка подключения")
            }
        }
    }

    /**
     * Reconnect or ensure connection is alive (auto-healing on app resume or command fail)
     */
    suspend fun checkOrReconnect(force: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        val host = currentHost ?: return@withContext false
        val port = currentPort
        if (!force && dadbInstance != null && _connectionState.value is ConnectionState.Connected) {
            // Test if socket is genuinely alive with a 1-character echo
            try {
                val resp = dadbInstance?.shell("echo 1")
                if (resp != null && resp.exitCode == 0) return@withContext true
            } catch (_: Exception) {
                // Socket was broken in background
            }
        }

        // Reconnect silently
        connect(host, port, currentDeviceName).isSuccess
    }

    /**
     * Disconnect ADB session
     */
    fun disconnect() {
        runCatching { dadbInstance?.close() }
        dadbInstance = null
        currentHost = null
        _connectionState.value = ConnectionState.Disconnected
    }

    /**
     * Helper to safely execute ADB command with single-retry on broken pipe / stale socket
     */
    private suspend fun <T> executeWithRetry(block: (Dadb) -> T): Result<T> = withContext(Dispatchers.IO) {
        runCatching {
            var dadb = dadbInstance
            if (dadb == null) {
                val host = currentHost ?: throw IllegalStateException("Телевизор не выбран. Подключитесь в настройках.")
                val res = connect(host, currentPort, currentDeviceName)
                if (res.isFailure) throw res.exceptionOrNull() ?: IllegalStateException("Не удалось подключиться к ТВ")
                dadb = dadbInstance ?: throw IllegalStateException("Ошибка создания ADB соединения")
            }

            try {
                block(dadb)
            } catch (ioe: Exception) {
                if (ioe is SocketException || ioe is IOException || ioe.message?.contains("closed", ignoreCase = true) == true) {
                    // Socket died while in background, retry once
                    runCatching { dadbInstance?.close() }
                    dadbInstance = null
                    val host = currentHost ?: throw ioe
                    connect(host, currentPort, currentDeviceName).getOrThrow()
                    val retryDadb = dadbInstance ?: throw ioe
                    block(retryDadb)
                } else {
                    throw ioe
                }
            }
        }
    }

    /**
     * Send remote control key event (Instant execution)
     */
    suspend fun sendKey(key: RemoteKey): Result<Unit> = executeWithRetry { dadb ->
        dadb.shell("input keyevent ${key.keyCode}")
        Unit
    }

    /**
     * Send raw key code (Instant execution)
     */
    suspend fun sendRawKeyCode(keyCode: Int): Result<Unit> = executeWithRetry { dadb ->
        dadb.shell("input keyevent $keyCode")
        Unit
    }

    /**
     * Type text into an active input field on the TV.
     * Supports Russian (Cyrillic), spaces, and special characters!
     */
    suspend fun sendText(text: String): Result<Unit> = executeWithRetry { dadb ->
        if (text.isBlank()) return@executeWithRetry

        // Check if string contains Cyrillic or non-ASCII characters
        val hasNonAscii = text.any { it.code > 127 }

        if (hasNonAscii) {
            // For Cyrillic text: inject via clipboard and paste
            val escaped = text.replace("'", "'\\''")
            // Try standard Android 10+ cmd clipboard
            val clipResp = dadb.shell("cmd clipboard set text '$escaped'")
            if (clipResp.exitCode == 0) {
                dadb.shell("input keyevent 279") // KEYCODE_PASTE
            } else {
                // Fallback for older Android TV versions: service call clipboard
                dadb.shell("service call clipboard 2 s16 \"$escaped\"")
                dadb.shell("input keyevent 279")
            }
        } else {
            // For ASCII: format spaces with %s and escape quotes
            val formatted = text.replace(" ", "%s").replace("'", "\\'")
            dadb.shell("input text '$formatted'")
        }
        Unit
    }

    /**
     * Send text from smartphone clipboard directly to TV
     */
    suspend fun sendClipboardText(clipboardText: String): Result<Unit> = executeWithRetry { dadb ->
        val escaped = clipboardText.replace("'", "'\\''")
        dadb.shell("cmd clipboard set text '$escaped'")
        dadb.shell("input keyevent 279")
        Unit
    }

    /**
     * Open a web link or YouTube URL on the TV
     */
    suspend fun openUrl(url: String): Result<Unit> = executeWithRetry { dadb ->
        val escaped = url.replace("'", "'\\''")
        dadb.shell("am start -a android.intent.action.VIEW -d '$escaped'")
        Unit
    }

    /**
     * Get list of installed apps on the TV with human-readable names and colors
     */
    suspend fun getInstalledApps(includeSystem: Boolean = false): Result<List<TvApp>> = executeWithRetry { dadb ->
        val flag = if (includeSystem) "" else "-3"
        val output = dadb.shell("pm list packages $flag").output

        output.lineSequence()
            .filter { it.startsWith("package:") }
            .map { line ->
                val pkg = line.removePrefix("package:").trim()
                val friendly = getFriendlyAppName(pkg)
                val colorHex = getColorForPackage(pkg)
                TvApp(
                    packageName = pkg,
                    label = friendly,
                    isSystem = includeSystem,
                    iconColorHex = colorHex
                )
            }
            .sortedWith(compareBy({ it.label.startsWith("com.") }, { it.label.lowercase() }))
            .toList()
    }

    /**
     * Launch an app on the TV
     */
    suspend fun launchApp(packageName: String): Result<Unit> = executeWithRetry { dadb ->
        val resp = dadb.shell("monkey -p $packageName -c android.intent.category.LAUNCHER 1")
        if (resp.exitCode != 0 && resp.allOutput.contains("No activities found", ignoreCase = true)) {
            dadb.shell("monkey -p $packageName -c android.intent.category.LEANBACK_LAUNCHER 1")
        }
        Unit
    }

    /**
     * Force stop an application on TV
     */
    suspend fun forceStopApp(packageName: String): Result<Unit> = executeWithRetry { dadb ->
        dadb.shell("am force-stop $packageName")
        Unit
    }

    /**
     * Uninstall an application from TV
     */
    suspend fun uninstallApp(packageName: String): Result<Unit> = executeWithRetry { dadb ->
        val resp = dadb.shell("pm uninstall $packageName")
        if (resp.exitCode != 0 && !resp.allOutput.contains("Success", ignoreCase = true)) {
            throw IllegalStateException(resp.allOutput.ifBlank { "Ошибка удаления приложения" })
        }
        Unit
    }

    /**
     * Reboot TV
     */
    suspend fun rebootTv(mode: String = ""): Result<Unit> = executeWithRetry { dadb ->
        val cmd = if (mode.isBlank()) "reboot" else "reboot $mode"
        dadb.shell(cmd)
        Unit
    }

    /**
     * Install an APK file to TV
     */
    suspend fun installApk(uri: Uri): Result<String> = executeWithRetry { dadb ->
        val tempFile = File(context.cacheDir, "install_temp_${System.currentTimeMillis()}.apk")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalArgumentException("Не удалось прочитать выбранный APK-файл")

            dadb.install(tempFile)
            "Приложение успешно установлено на телевизор!"
        } finally {
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    /**
     * Directly install a local File APK to TV
     */
    suspend fun installApkFile(file: File): Result<String> = executeWithRetry { dadb ->
        if (!file.exists() || file.length() == 0L) {
            throw IllegalArgumentException("APK-файл пуст или поврежден")
        }
        dadb.install(file)
        "Лаунчер успешно установлен на телевизор!"
    }

    /**
     * Enable a package on the TV
     */
    suspend fun enablePackage(packageName: String): Result<Unit> = executeWithRetry { dadb ->
        dadb.shell("pm enable $packageName")
        Unit
    }

    /**
     * Set default home activity / launcher on TV
     */
    suspend fun setDefaultHome(packageName: String): Result<Unit> = executeWithRetry { dadb ->
        val launch = dadb.shell("monkey -p $packageName -c android.intent.category.HOME 1")
        if (launch.exitCode != 0) {
            dadb.shell("monkey -p $packageName -c android.intent.category.LEANBACK_LAUNCHER 1")
        }
        Unit
    }

    /**
     * Full Rollback to factory TV stock launcher:
     * 1. Uninstalls any custom launcher package (FLauncher, etc.)
     * 2. Re-enables all standard TV OEM launchers (Google TV, Xiaomi PatchWall, TCL)
     * 3. Triggers Home keyevent 3 to bring user back to native TV home screen
     */
    suspend fun rollbackToStockLauncher(customPackages: List<String>): Result<String> = executeWithRetry { dadb ->
        // 1. Uninstall custom packages
        for (pkg in customPackages) {
            try {
                dadb.shell("pm uninstall $pkg")
            } catch (_: Exception) { }
        }

        // 2. Re-enable standard OEM launchers
        val stockLaunchers = listOf(
            "com.google.android.tvlauncher",
            "com.google.android.leanbacklauncher",
            "com.google.android.apps.tv.launcherx",
            "com.xiaomi.mitv.tvhome",
            "com.mitv.tvhome",
            "com.tcl.tv",
            "com.tcl.homelauncher",
            "com.tcl.home",
            "com.droidlogic.tv.launcher"
        )
        for (stock in stockLaunchers) {
            try {
                dadb.shell("pm enable $stock")
            } catch (_: Exception) { }
        }

        // 3. Trigger Home key on TV
        dadb.shell("input keyevent 3")

        "Телевизор успешно возвращен к исходному состоянию!"
    }

    /**
     * Get TV RAM and Internal Storage statistics
     */
    suspend fun getMemoryStats(): Result<TvMemoryStats> = executeWithRetry { dadb ->
        var totalRamMb = 0
        var availRamMb = 0
        var storageTotalGb = ""
        var storageAvailGb = ""

        try {
            val meminfo = dadb.shell("cat /proc/meminfo").allOutput
            for (line in meminfo.lines()) {
                val lower = line.lowercase()
                if (lower.startsWith("memtotal:")) {
                    val kb = line.filter { it.isDigit() }.toLongOrNull() ?: 0L
                    totalRamMb = (kb / 1024).toInt()
                } else if (lower.startsWith("memavailable:")) {
                    val kb = line.filter { it.isDigit() }.toLongOrNull() ?: 0L
                    availRamMb = (kb / 1024).toInt()
                } else if (lower.startsWith("memfree:") && availRamMb == 0) {
                    val kb = line.filter { it.isDigit() }.toLongOrNull() ?: 0L
                    availRamMb = (kb / 1024).toInt()
                }
            }
        } catch (_: Exception) { }

        try {
            val dfOutput = dadb.shell("df -h /data").allOutput
            val lines = dfOutput.lines().filter { it.isNotBlank() }
            if (lines.size > 1) {
                val parts = lines[1].trim().split(Regex("\\s+"))
                if (parts.size >= 4) {
                    storageTotalGb = parts[1]
                    storageAvailGb = parts[3]
                }
            }
        } catch (_: Exception) { }

        val usedRamMb = (totalRamMb - availRamMb).coerceAtLeast(0)
        TvMemoryStats(
            totalRamMb = totalRamMb,
            availRamMb = availRamMb,
            usedRamMb = usedRamMb,
            storageTotalGb = storageTotalGb,
            storageAvailGb = storageAvailGb
        )
    }

    /**
     * Clear application caches across all packages
     */
    suspend fun cleanCache(): Result<String> = executeWithRetry { dadb ->
        dadb.shell("pm trim-caches 999999999999")
        dadb.shell("rm -rf /data/local/tmp/*")
        "Кэш всех приложений и временные файлы успешно очищены!"
    }

    /**
     * Terminate background processes and reclaim RAM
     */
    suspend fun freeRam(): Result<String> = executeWithRetry { dadb ->
        dadb.shell("am kill-all")
        "Фоновые процессы завершены, оперативная память освобождена!"
    }

    /**
     * Hardware flash memory TRIM (accelerates NAND flash read/write)
     */
    suspend fun runFstrim(): Result<String> = executeWithRetry { dadb ->
        dadb.shell("sm fstrim")
        "FSTRIM накопителя успешно выполнен (память оптимизирована)!"
    }

    /**
     * Clear system log buffers (logcat) which take up RAM
     */
    suspend fun cleanLogs(): Result<String> = executeWithRetry { dadb ->
        dadb.shell("logcat -c")
        "Системные буферы логов успешно очищены!"
    }

    /**
     * Set system animation scale (0.0x for instant, 0.5x for turbo snappy, 1.0x for normal)
     */
    suspend fun setAnimationScale(scale: Float): Result<String> = executeWithRetry { dadb ->
        val str = scale.toString()
        dadb.shell("settings put global window_animation_scale $str")
        dadb.shell("settings put global transition_animation_scale $str")
        dadb.shell("settings put global animator_duration_scale $str")
        val label = when (scale) {
            0.0f -> "Мгновенно (0.0x - анимации отключены)"
            0.5f -> "Турбо (0.5x - ускорение в 2 раза)"
            else -> "Стандарт (1.0x)"
        }
        "Скорость меню установлена: $label"
    }

    /**
     * Read current animation scale
     */
    suspend fun getAnimationScale(): Result<Float> = executeWithRetry { dadb ->
        val out = dadb.shell("settings get global window_animation_scale").allOutput.trim()
        out.toFloatOrNull() ?: 1.0f
    }

    /**
     * Enable or disable AdGuard Private DNS on TV for system-wide ad-blocking
     */
    suspend fun setAdGuardDns(enabled: Boolean): Result<String> = executeWithRetry { dadb ->
        if (enabled) {
            dadb.shell("settings put global private_dns_mode hostname")
            dadb.shell("settings put global private_dns_specifier dns.adguard-dns.com")
            "Блокировка рекламы включена (AdGuard DNS активен)!"
        } else {
            dadb.shell("settings put global private_dns_mode off")
            dadb.shell("settings put global private_dns_specifier \"\"")
            "Блокировка рекламы отключена (стандартный DNS)."
        }
    }

    /**
     * Check if AdGuard Private DNS is currently enabled on TV
     */
    suspend fun isAdGuardDnsEnabled(): Result<Boolean> = executeWithRetry { dadb ->
        val mode = dadb.shell("settings get global private_dns_mode").allOutput.trim()
        val spec = dadb.shell("settings get global private_dns_specifier").allOutput.trim()
        mode.equals("hostname", ignoreCase = true) && spec.contains("adguard", ignoreCase = true)
    }

    /**
     * Capture TV screen via screencap and decode to Bitmap
     */
    suspend fun takeScreenshot(): Result<Bitmap> = executeWithRetry { dadb ->
        val b64Resp = dadb.shell("screencap -p | base64")
        if (b64Resp.exitCode != 0 || b64Resp.allOutput.isBlank()) {
            throw IllegalStateException("Не удалось сделать снимок экрана ТВ")
        }
        val cleanB64 = b64Resp.allOutput.replace("\r", "").replace("\n", "").trim()
        val bytes = Base64.decode(cleanB64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IllegalStateException("Ошибка декодирования изображения")
        bitmap
    }

    /**
     * Friendly names dictionary for popular Android TV apps
     */
    private fun getFriendlyAppName(pkg: String): String {
        return when (pkg) {
            "com.google.android.youtube.tv" -> "YouTube"
            "com.teamsmart.videomanager.tv" -> "SmartTube"
            "ru.kinopoisk" -> "Кинопоиск"
            "com.lampa" -> "Lampa"
            "com.github.torrserve", "ru.yourok.torrserve" -> "TorrServe"
            "org.videolan.vlc" -> "VLC Media Player"
            "com.mxtech.videoplayer.ad", "com.mxtech.videoplayer.pro" -> "MX Player"
            "com.lonelycatgames.Xplore" -> "X-plore File Manager"
            "com.estrongs.android.pop" -> "ES Проводник"
            "ru.ivi.client" -> "IVI"
            "ru.okko.tv" -> "Okko"
            "ru.kion.kion" -> "KION"
            "ru.start.android" -> "START"
            "ru.wink.tv" -> "Wink"
            "tv.megafon.vplay" -> "МегаФон ТВ"
            "com.netflix.ninja" -> "Netflix"
            "com.apple.atve.androidtv.appletv" -> "Apple TV"
            "com.spotify.tv.android" -> "Spotify"
            "com.google.android.katniss" -> "Google Ассистент"
            "com.android.vending" -> "Google Play Маркет"
            "com.google.android.tvlauncher" -> "Главный экран (Лаунчер)"
            "com.android.tv.settings" -> "Настройки ТВ"
            "com.xiaomi.mitv.mediaexplorer" -> "Медиаплеер Xiaomi"
            "com.mitv.tvhome" -> "Mi TV Домашний экран"
            "com.droidlogic.tv.settings" -> "Системные настройки"
            else -> {
                val lastPart = pkg.substringAfterLast('.')
                if (lastPart.length in 2..20) {
                    lastPart.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                } else {
                    pkg
                }
            }
        }
    }

    private fun getColorForPackage(pkg: String): Long {
        return when {
            pkg.contains("youtube") || pkg.contains("smarttube") -> 0xFFFF0033
            pkg.contains("kinopoisk") -> 0xFFFF6600
            pkg.contains("vlc") -> 0xFFFF8800
            pkg.contains("spotify") -> 0xFF1DB954
            pkg.contains("lampa") -> 0xFF00D2FF
            pkg.contains("torrserve") -> 0xFF9C27B0
            pkg.contains("netflix") -> 0xFFE50914
            pkg.contains("google") -> 0xFF4285F4
            else -> {
                val colors = longArrayOf(
                    0xFF00D2FF, 0xFF00E676, 0xFF7C4DFF, 0xFFFF5252,
                    0xFFFFAB00, 0xFF448AFF, 0xFFE040FB, 0xFF69F0AE
                )
                colors[Math.abs(pkg.hashCode()) % colors.size]
            }
        }
    }
}
