package com.example.tvcommander.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvcommander.data.NetworkScanner
import com.example.tvcommander.data.PreferencesManager
import com.example.tvcommander.data.TvAdbManager
import com.example.tvcommander.data.WakeOnLan
import com.example.tvcommander.model.ConnectionState
import com.example.tvcommander.model.CuratedApp
import com.example.tvcommander.model.DiscoveredDevice
import com.example.tvcommander.model.InstallStatus
import com.example.tvcommander.model.LauncherItem
import com.example.tvcommander.model.LauncherOperationStatus
import com.example.tvcommander.model.OptimizationStatus
import com.example.tvcommander.model.RemoteKey
import com.example.tvcommander.model.ScreenshotState
import com.example.tvcommander.model.TvApp
import com.example.tvcommander.model.TvDevice
import com.example.tvcommander.model.TvMemoryStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class TvViewModel(application: Application) : AndroidViewModel(application) {

    private val adbManager = TvAdbManager(application)
    private val preferences = PreferencesManager(application)

    val connectionState: StateFlow<ConnectionState> = adbManager.connectionState

    val devices: StateFlow<List<TvDevice>> = preferences.devicesFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    val activeDeviceId: StateFlow<String> = preferences.activeDeviceIdFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "xiaomi_mitv"
    )

    val autoConnect: StateFlow<Boolean> = preferences.autoConnectFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        true
    )

    val hapticEnabled: StateFlow<Boolean> = preferences.hapticEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        true
    )

    val volumeKeysEnabled: StateFlow<Boolean> = preferences.volumeKeysEnabledFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        true
    )

    val keepScreenOn: StateFlow<Boolean> = preferences.keepScreenOnFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )

    val remoteMode: StateFlow<String> = preferences.remoteModeFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "dpad"
    )

    val appTheme: StateFlow<String> = preferences.appThemeFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "cyan"
    )

    private val _installedApps = MutableStateFlow<List<TvApp>>(emptyList())
    val installedApps: StateFlow<List<TvApp>> = _installedApps.asStateFlow()

    private val _isLoadingApps = MutableStateFlow(false)
    val isLoadingApps: StateFlow<Boolean> = _isLoadingApps.asStateFlow()

    private val _installStatus = MutableStateFlow<InstallStatus>(InstallStatus.Idle)
    val installStatus: StateFlow<InstallStatus> = _installStatus.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanningNetwork = MutableStateFlow(false)
    val isScanningNetwork: StateFlow<Boolean> = _isScanningNetwork.asStateFlow()

    private val _launcherOperationStatus = MutableStateFlow<LauncherOperationStatus>(LauncherOperationStatus.Idle)
    val launcherOperationStatus: StateFlow<LauncherOperationStatus> = _launcherOperationStatus.asStateFlow()

    private val _memoryStats = MutableStateFlow(TvMemoryStats())
    val memoryStats: StateFlow<TvMemoryStats> = _memoryStats.asStateFlow()

    private val _optimizationStatus = MutableStateFlow<OptimizationStatus>(OptimizationStatus.Idle)
    val optimizationStatus: StateFlow<OptimizationStatus> = _optimizationStatus.asStateFlow()

    private val _animationScale = MutableStateFlow(1.0f)
    val animationScale: StateFlow<Float> = _animationScale.asStateFlow()

    private val _isAdGuardDnsActive = MutableStateFlow(false)
    val isAdGuardDnsActive: StateFlow<Boolean> = _isAdGuardDnsActive.asStateFlow()

    private val _sleepTimerRemainingSeconds = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingSeconds: StateFlow<Int?> = _sleepTimerRemainingSeconds.asStateFlow()
    private var sleepTimerJob: Job? = null

    private val _screenshotState = MutableStateFlow<ScreenshotState>(ScreenshotState.Idle)
    val screenshotState: StateFlow<ScreenshotState> = _screenshotState.asStateFlow()

    private val _isWakingUp = MutableStateFlow(false)
    val isWakingUp: StateFlow<Boolean> = _isWakingUp.asStateFlow()

    val curatedApps: List<CuratedApp> = listOf(
        CuratedApp(
            id = "smarttube",
            name = "SmartTube",
            packageName = "com.teamsmart.videomanager.tv",
            tag = "🔥 Топ №1 для ТВ",
            shortDesc = "YouTube для Android TV без рекламы, со встроенным SponsorBlock и фоновым звуком.",
            fullDesc = "SmartTube — лучший бесплатный клиент YouTube с открытым исходным кодом, созданный специально для пультов Android TV. Поддерживает 4K/60fps/HDR, автоматический пропуск спонсорских интеграций, работу без Google-сервисов и регулировку скорости.",
            downloadUrl = "https://github.com/yuliskov/SmartTube/releases/download/32.68s/SmartTube_stable_32.68_universal.apk",
            iconColorHex = 0xFFFF0033
        ),
        CuratedApp(
            id = "lampa",
            name = "Lampa (AIO)",
            packageName = "com.lampa",
            tag = "🎬 Онлайн-Кинотеатр",
            shortDesc = "Легендарный медиакомбайн для поиска и бесплатного просмотра фильмов и сериалов.",
            fullDesc = "Lampa — удобнейший каталог кино с рейтингами Кинопоиска/IMDb, постерами, трейлерами и онлайн-просмотром через балансеры или связку с TorrServe. Сборка All-In-One уже содержит необходимые плагины.",
            downloadUrl = "https://github.com/lxnhere/LAMPA-AIO-releases/releases/download/v596/lampa-aio.apk",
            iconColorHex = 0xFF00D2FF
        ),
        CuratedApp(
            id = "torrserve",
            name = "TorrServe Matrix",
            packageName = "ru.yourok.torrserve",
            tag = "⚡ Торренты без загрузки",
            shortDesc = "Движок для онлайн-просмотра любых торрент-фильмов сразу при клике без скачивания на диск.",
            fullDesc = "TorrServe MatriX кэширует видеопоток прямо в оперативную память телевизора, позволяя смотреть даже тяжелые 4K Blu-ray рипы онлайн без забивания встроенной памяти ТВ.",
            downloadUrl = "https://github.com/YouROK/TorrServe/releases/download/MatriX.143.Client/TorrServe_MatriX.143.Client.apk",
            iconColorHex = 0xFF9C27B0
        ),
        CuratedApp(
            id = "vlc",
            name = "VLC Player",
            packageName = "org.videolan.vlc",
            tag = "🎞️ Всеядный плеер",
            shortDesc = "Официальный плеер VLC с поддержкой любых видеоформатов, звуковых дорожек и субтитров.",
            fullDesc = "VLC for Android проигрывает любые сетевые потоки, файлы с локальных серверов SMB/DLNA, поддерживает аппаратное декодирование и воспроизведение многоканального аудио.",
            downloadUrl = "https://get.videolan.org/vlc-android/3.5.4/VLC-Android-3.5.4-armeabi-v7a.apk",
            iconColorHex = 0xFFFF8800
        )
    )

    val launchersList: List<LauncherItem> = listOf(
        LauncherItem(
            id = "flauncher",
            name = "FLauncher",
            packageNames = listOf("me.efesser.flauncher", "me.efdev.flauncher"),
            tag = "⚡ Ультра-быстрый (Open Source)",
            shortDesc = "Легковесный минималистичный лаунчер с открытым исходным кодом. Полностью без рекламы и телеметрии. Идеален для слабых ТВ.",
            fullDescription = "FLauncher — это независимый проект на Flutter, созданный как прямая альтернатива перегруженным стандартным оболочкам Android TV и Google TV. В нем нет рекламы, рекомендаций фильмов или скрытых процессов. Позволяет создавать собственные категории, менять фон, скрывать неиспользуемые системные приложения и моментально запускать медиаплееры.",
            advantages = listOf(
                "0 байт рекламы и фоновых трекеров",
                "Минимальное потребление памяти (~40-60 МБ RAM против 350+ МБ у Google TV)",
                "Гибкая сортировка приложений по своим категориям",
                "Кастомные обои (Unsplash или свои)",
                "Открытый исходный код (GPLv3)"
            ),
            disadvantages = listOf(
                "Не поддерживает системные каналы рекомендаций Android TV ('Продолжить просмотр')",
                "Нет встроенного меню переключения входов HDMI"
            ),
            ramUsage = "~50 МБ RAM (экономит до 400 МБ)",
            downloadUrl = "https://github.com/osrosal/flauncher/releases/download/v2025.07.001/flauncher-universal-release.apk",
            canDirectInstall = true
        ),
        LauncherItem(
            id = "projectivy",
            name = "Projectivy Launcher",
            packageNames = listOf("com.spocky.projectivylauncher", "com.spocky.projengmenu"),
            tag = "⭐ Выбор №1 сообщества",
            shortDesc = "Самая популярная и сбалансированная оболочка для Android TV. Аппаратное ускорение, каналы рекомендаций и переключение входов.",
            fullDescription = "Projectivy Launcher — признанный лидер среди кастомных оболочек для Android TV / Google TV. Сочетает максимальную плавность работы за счет аппаратного ускорения, премиальный дизайн и полную поддержку системных каналов рекомендаций (Watch Next). Умеет легко перехватывать нажатие кнопки Home, настраивать темы и переключать источники HDMI прямо с главного экрана.",
            advantages = listOf(
                "Аппаратное ускорение графики (плавные 60/120 fps)",
                "Поддержка каналов 'Watch Next' и рекомендаций из ваших приложений",
                "Встроенное меню переключения HDMI-входов",
                "Встроенный родительский контроль и скрытие приложений",
                "Автозапуск при старте ТВ без рута"
            ),
            disadvantages = listOf(
                "Некоторые продвинутые настройки доступны только в платной Premium-версии",
                "Потребляет чуть больше RAM, чем FLauncher (~90-120 МБ)"
            ),
            ramUsage = "~100 МБ RAM",
            downloadUrl = "https://github.com/spocky/miproja1/releases/download/4.73/ProjectivyLauncher-4.73-c98-xda-release.apk",
            canDirectInstall = true
        ),
        LauncherItem(
            id = "atv",
            name = "ATV Launcher Pro",
            packageNames = listOf("ca.dstudio.atvlauncher.pro", "ca.dstudio.atvlauncher.free"),
            tag = "🚀 Классика для слабых ТВ",
            shortDesc = "Ультралегкая плиточная оболочка, оптимизированная для ТВ с 1–1.5 ГБ ОЗУ. Поддерживает виджеты Android.",
            fullDescription = "ATV Launcher — легендарная легковесная оболочка для медиаплееров и телевизоров. Предлагает настраиваемую сетку плиток любого размера, изменение цвета и иконок приложений, а также поддержку виджетов Android (часы, погода, системный монитор). Практически не создает нагрузки на процессор ТВ.",
            advantages = listOf(
                "Работает молниеносно даже на самых слабых чипсетах",
                "Поддержка виджетов Android на главном экране",
                "Настройка количества колонок и строк плиток",
                "Скрытие любых приложений и защита папок паролем"
            ),
            disadvantages = listOf(
                "Плиточный дизайн может выглядеть старомодно",
                "Редко обновляется автором"
            ),
            ramUsage = "~45 МБ RAM",
            downloadUrl = "https://raw.githubusercontent.com/loksampel-droid/atvLauncherTV/main/ATV_Launcher_v0.1.39-pro_(30000814)_Mod.apk",
            canDirectInstall = true
        ),
        LauncherItem(
            id = "akari",
            name = "Akari Launcher",
            packageNames = listOf("konvar.akari"),
            tag = "🪶 Ультралегкий (500 КБ)",
            shortDesc = "Минималистичный быстрый лаунчер для Google TV и Android TV. Размер всего 500 КБ.",
            fullDescription = "Akari Launcher — сверхлегкая открытая оболочка нового поколения, созданная специально для максимальной отзывчивости на телевизорах со слабыми процессорами. Весит всего полмегабайта, мгновенно запускается и не держит в фоне ничего лишнего.",
            advantages = listOf(
                "Размер APK всего 500 КБ (скачивается за 1 секунду)",
                "Потребление памяти менее 30 МБ RAM",
                "Мгновенный запуск без лагов",
                "Полностью открытый исходный код"
            ),
            disadvantages = listOf(
                "Минималистичный базовый функционал без анимационных украшений"
            ),
            ramUsage = "~25-30 МБ RAM",
            downloadUrl = "https://github.com/konvar/Akari/releases/download/v0.8.2/Akari-0.8.2.apk",
            canDirectInstall = true
        ),
        LauncherItem(
            id = "cutelauncher",
            name = "CuteLauncher (tvOS)",
            packageNames = listOf("com.kirillshchetinnikov.cutelauncher"),
            tag = "🍎 В стиле Apple TV",
            shortDesc = "Современный интерфейс с крупными карточками и фокусными эффектами в стиле tvOS без рекламы.",
            fullDescription = "CuteLauncher предлагает премиальный минималистичный интерфейс телевизоров Apple TV для устройств Android TV. Крупные красивые карточки, фокусные эффекты и полное отсутствие баннеров Google.",
            advantages = listOf(
                "Стильный премиальный дизайн в духе tvOS",
                "Плавная навигация пультом",
                "Без навязчивой рекламы и трекеров"
            ),
            disadvantages = listOf(
                "Требует чуть больше RAM для плавной отрисовки (~70-90 МБ)"
            ),
            ramUsage = "~75 МБ RAM",
            downloadUrl = "https://github.com/KirillShchetinnikov/CuteLauncher-Releases/releases/download/v1.6.2/CuteLauncher-1.6.2-universal-release.apk",
            canDirectInstall = true
        )
    )

    private val _snackBarMessage = MutableSharedFlow<String>()
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            val auto = autoConnect.first()
            if (auto) {
                val currentDevices = devices.first()
                val currentId = activeDeviceId.first()
                val active = currentDevices.find { it.id == currentId } ?: currentDevices.firstOrNull()
                if (active != null) {
                    connectToDevice(active)
                }
            }
        }
    }

    /**
     * Called by MainActivity.onResume() to heal any dropped connection automatically
     */
    fun onAppResume() {
        viewModelScope.launch {
            if (autoConnect.value) {
                val currentDevices = devices.value
                val currentId = activeDeviceId.value
                val active = currentDevices.find { it.id == currentId } ?: currentDevices.firstOrNull()
                if (active != null) {
                    if (connectionState.value !is ConnectionState.Connected) {
                        adbManager.connect(active.ip, active.port, active.name)
                    } else {
                        adbManager.checkOrReconnect(force = false)
                    }
                }
            }
        }
    }

    fun connectToDevice(device: TvDevice) {
        viewModelScope.launch {
            _snackBarMessage.emit("Подключение к ${device.name} (${device.ip})...")
            adbManager.connect(device.ip, device.port, device.name).onSuccess {
                _snackBarMessage.emit("Подключено: ${device.name}")
                refreshApps()
                refreshMemoryStats()
                checkAdGuardDns()
            }.onFailure { err ->
                _snackBarMessage.emit("Ошибка: ${err.message}")
            }
        }
    }

    fun switchActiveDevice(device: TvDevice) {
        viewModelScope.launch {
            preferences.setActiveDeviceId(device.id)
            connectToDevice(device)
        }
    }

    fun disconnect() {
        adbManager.disconnect()
        _installedApps.value = emptyList()
    }

    fun sendKey(key: RemoteKey) {
        viewModelScope.launch {
            adbManager.sendKey(key).onFailure { err ->
                _snackBarMessage.emit("Ошибка: ${err.message ?: "Нет связи с ТВ"}")
            }
        }
    }

    fun sendRawKey(keyCode: Int) {
        viewModelScope.launch {
            adbManager.sendRawKeyCode(keyCode)
        }
    }

    fun sendText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            adbManager.sendText(text).onSuccess {
                _snackBarMessage.emit("Текст отправлен на ТВ")
            }.onFailure {
                _snackBarMessage.emit("Не удалось отправить текст")
            }
        }
    }

    fun sendClipboard(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            adbManager.sendClipboardText(text).onSuccess {
                _snackBarMessage.emit("Буфер вставлен на ТВ")
            }.onFailure {
                _snackBarMessage.emit("Ошибка отправки буфера")
            }
        }
    }

    fun turnOnTv() {
        viewModelScope.launch {
            if (_isWakingUp.value) return@launch
            _isWakingUp.value = true
            try {
                val currentDevices = devices.value
                val currentId = activeDeviceId.value
                val active = currentDevices.find { it.id == currentId } ?: currentDevices.firstOrNull()

                val targetMac = if (!active?.mac.isNullOrBlank()) active!!.mac else "da:a7:fc:93:e4:99"
                val targetIp = if (!active?.ip.isNullOrBlank()) active!!.ip else "192.168.68.108"
                val devName = active?.name ?: "Xiaomi Mi TV"
                val devPort = active?.port ?: 5555

                _snackBarMessage.emit("Включение $devName: отправка сигнала Wake-on-LAN...")

                // 1. Send multi-target, multi-port Wake-on-LAN bursts + Xiaomi HTTP wake
                WakeOnLan.sendWakeSignal(
                    context = getApplication(),
                    macAddress = targetMac,
                    targetIp = targetIp,
                    ports = listOf(9, 7),
                    repeatCount = 4
                )

                // 2. Allow TV Wi-Fi module to power on and obtain connection (~1.5s)
                delay(1500)

                // 3. Connect retry loop (up to 6 attempts with 800ms spacing)
                var isConnectedNow = (connectionState.value is ConnectionState.Connected)
                if (!isConnectedNow) {
                    _snackBarMessage.emit("Ожидание готовности $devName...")
                    for (attempt in 1..6) {
                        val res = adbManager.connect(targetIp, devPort, devName)
                        if (res.isSuccess) {
                            isConnectedNow = true
                            break
                        }
                        delay(800)
                    }
                }

                // 4. Send screen wake and navigate home
                if (isConnectedNow) {
                    adbManager.sendKey(RemoteKey.WAKEUP)
                    delay(200)
                    adbManager.sendKey(RemoteKey.HOME)
                    _snackBarMessage.emit("Телевизор $devName успешно включен!")
                    refreshApps()
                    refreshMemoryStats()
                    checkAdGuardDns()
                } else {
                    _snackBarMessage.emit("Сигнал включения передан на $devName")
                }
            } catch (e: Exception) {
                _snackBarMessage.emit("Ошибка включения ТВ: ${e.message}")
            } finally {
                _isWakingUp.value = false
            }
        }
    }

    fun turnOffTv() {
        viewModelScope.launch {
            adbManager.sendKey(RemoteKey.POWER).onSuccess {
                _snackBarMessage.emit("Телевизор переходит в спящий режим")
            }
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setHapticEnabled(enabled) }
    }

    fun setVolumeKeysEnabled(enabled: Boolean) {
        viewModelScope.launch { preferences.setVolumeKeysEnabled(enabled) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { preferences.setKeepScreenOn(enabled) }
    }

    fun setRemoteMode(mode: String) {
        viewModelScope.launch { preferences.setRemoteMode(mode) }
    }

    fun setAppTheme(theme: String) {
        viewModelScope.launch { preferences.setAppTheme(theme) }
    }

    fun refreshApps(includeSystem: Boolean = false) {
        viewModelScope.launch {
            if (connectionState.value !is ConnectionState.Connected) return@launch
            _isLoadingApps.value = true
            adbManager.getInstalledApps(includeSystem).onSuccess { list ->
                _installedApps.value = list
            }.onFailure { err ->
                _snackBarMessage.emit("Ошибка загрузки приложений: ${err.message}")
            }
            _isLoadingApps.value = false
        }
    }

    fun launchApp(packageName: String) {
        viewModelScope.launch {
            adbManager.launchApp(packageName).onSuccess {
                _snackBarMessage.emit("Приложение открыто")
            }.onFailure { err ->
                _snackBarMessage.emit("Не удалось запустить: ${err.message}")
            }
        }
    }

    fun forceStopApp(packageName: String) {
        viewModelScope.launch {
            adbManager.forceStopApp(packageName).onSuccess {
                _snackBarMessage.emit("Приложение остановлено")
            }
        }
    }

    fun uninstallApp(packageName: String) {
        viewModelScope.launch {
            adbManager.uninstallApp(packageName).onSuccess {
                _snackBarMessage.emit("Приложение удалено с ТВ")
                refreshApps()
            }.onFailure { err ->
                _snackBarMessage.emit("Не удалось удалить: ${err.message}")
            }
        }
    }

    fun rebootTv(mode: String = "") {
        viewModelScope.launch {
            adbManager.rebootTv(mode).onSuccess {
                _snackBarMessage.emit("Команда перезагрузки отправлена")
            }
        }
    }

    fun openUrlOnTv(url: String) {
        viewModelScope.launch {
            adbManager.openUrl(url).onSuccess {
                _snackBarMessage.emit("Ссылка открыта на ТВ")
            }.onFailure { err ->
                _snackBarMessage.emit("Ошибка открытия ссылки: ${err.message}")
            }
        }
    }

    fun installApk(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _installStatus.value = InstallStatus.Preparing(fileName)
            _installStatus.value = InstallStatus.Installing(fileName)
            val result = adbManager.installApk(uri)
            result.onSuccess { msg ->
                _installStatus.value = InstallStatus.Success(msg)
                _snackBarMessage.emit(msg)
                refreshApps()
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "Сбой установки"
                _installStatus.value = InstallStatus.Error(errorMsg)
                _snackBarMessage.emit("Ошибка: $errorMsg")
            }
        }
    }

    fun resetInstallStatus() {
        _installStatus.value = InstallStatus.Idle
    }

    fun startNetworkScan() {
        viewModelScope.launch {
            _isScanningNetwork.value = true
            _snackBarMessage.emit("Сканирование домашней сети Wi-Fi...")
            val list = NetworkScanner.scanSubnet()
            _discoveredDevices.value = list
            _isScanningNetwork.value = false
            if (list.isEmpty()) {
                _snackBarMessage.emit("Устройств с ADB не найдено (проверьте отладку по сети на ТВ)")
            } else {
                _snackBarMessage.emit("Найдено устройств: ${list.size}")
            }
        }
    }

    fun addOrUpdateDevice(device: TvDevice) {
        viewModelScope.launch {
            preferences.addOrUpdateDevice(device)
            _snackBarMessage.emit("Телевизор \"${device.name}\" сохранен")
        }
    }

    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            preferences.deleteDevice(deviceId)
            _snackBarMessage.emit("Устройство удалено")
        }
    }

    fun setAutoConnect(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAutoConnect(enabled)
        }
    }

    fun resetLauncherStatus() {
        _launcherOperationStatus.value = LauncherOperationStatus.Idle
    }

    fun downloadAndInstallLauncher(launcher: LauncherItem) {
        val downloadUrl = launcher.downloadUrl
        if (downloadUrl == null) {
            viewModelScope.launch {
                _snackBarMessage.emit("Для этого лаунчера используйте вкладку 'Установка APK'")
            }
            return
        }

        viewModelScope.launch {
            val app = getApplication<Application>()
            val cacheFile = File(app.cacheDir, "${launcher.id}_temp_${System.currentTimeMillis()}.apk")
            try {
                _launcherOperationStatus.value = LauncherOperationStatus.Working("Скачивание ${launcher.name}...")
                _snackBarMessage.emit("Скачивание ${launcher.name}...")

                downloadFileWithRedirects(downloadUrl, cacheFile)

                _launcherOperationStatus.value = LauncherOperationStatus.Working("Установка ${launcher.name} на ТВ...")
                _snackBarMessage.emit("Установка ${launcher.name} на ТВ через ADB...")

                val installResult = adbManager.installApkFile(cacheFile)
                installResult.onSuccess { msg ->
                    _launcherOperationStatus.value = LauncherOperationStatus.Success("${launcher.name} успешно установлен на ТВ!")
                    _snackBarMessage.emit("${launcher.name} установлен!")
                    refreshApps()
                }.onFailure { err ->
                    val errorMsg = err.localizedMessage ?: "Сбой установки"
                    _launcherOperationStatus.value = LauncherOperationStatus.Error(errorMsg)
                    _snackBarMessage.emit("Ошибка: $errorMsg")
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Ошибка загрузки APK"
                _launcherOperationStatus.value = LauncherOperationStatus.Error(errorMsg)
                _snackBarMessage.emit("Ошибка: $errorMsg")
            } finally {
                if (cacheFile.exists()) {
                    cacheFile.delete()
                }
            }
        }
    }

    fun launchLauncher(packageName: String) {
        viewModelScope.launch {
            adbManager.launchApp(packageName).onSuccess {
                _snackBarMessage.emit("Оболочка запущена на экране ТВ")
            }.onFailure { err ->
                _snackBarMessage.emit("Ошибка запуска: ${err.message}")
            }
        }
    }

    fun setDefaultLauncher(packageName: String) {
        viewModelScope.launch {
            adbManager.setDefaultHome(packageName).onSuccess {
                _snackBarMessage.emit("Команда запуска домашнего экрана отправлена")
            }.onFailure { err ->
                _snackBarMessage.emit("Ошибка: ${err.message}")
            }
        }
    }

    fun uninstallLauncher(packageName: String) {
        viewModelScope.launch {
            _launcherOperationStatus.value = LauncherOperationStatus.Working("Удаление лаунчера с ТВ...")
            adbManager.uninstallApp(packageName).onSuccess {
                _launcherOperationStatus.value = LauncherOperationStatus.Success("Лаунчер успешно удален с ТВ")
                _snackBarMessage.emit("Лаунчер удален с ТВ")
                refreshApps()
            }.onFailure { err ->
                _launcherOperationStatus.value = LauncherOperationStatus.Error(err.message ?: "Ошибка удаления")
                _snackBarMessage.emit("Не удалось удалить: ${err.message}")
            }
        }
    }

    fun rollbackToStockLauncher() {
        viewModelScope.launch {
            _launcherOperationStatus.value = LauncherOperationStatus.Working("Восстановление стандартного лаунчера ТВ...")
            val customPackages = launchersList.flatMap { it.packageNames }
            adbManager.rollbackToStockLauncher(customPackages).onSuccess { msg ->
                _launcherOperationStatus.value = LauncherOperationStatus.Success(msg)
                _snackBarMessage.emit(msg)
                refreshApps()
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "Сбой отката"
                _launcherOperationStatus.value = LauncherOperationStatus.Error(errorMsg)
                _snackBarMessage.emit("Ошибка: $errorMsg")
            }
        }
    }

    fun refreshMemoryStats() {
        viewModelScope.launch {
            if (connectionState.value !is ConnectionState.Connected) return@launch
            adbManager.getMemoryStats().onSuccess { stats ->
                _memoryStats.value = stats
            }
            adbManager.getAnimationScale().onSuccess { scale ->
                _animationScale.value = scale
            }
        }
    }

    fun cleanCacheOnly() {
        viewModelScope.launch {
            _optimizationStatus.value = OptimizationStatus.Running("Очистка системного кэша и временных файлов...", 0.5f)
            adbManager.cleanCache().onSuccess { msg ->
                _optimizationStatus.value = OptimizationStatus.Success(msg)
                _snackBarMessage.emit(msg)
                refreshMemoryStats()
            }.onFailure { err ->
                _optimizationStatus.value = OptimizationStatus.Error(err.message ?: "Ошибка очистки кэша")
            }
        }
    }

    fun freeRamOnly() {
        viewModelScope.launch {
            _optimizationStatus.value = OptimizationStatus.Running("Выгрузка фоновых процессов...", 0.5f)
            adbManager.freeRam().onSuccess { msg ->
                _optimizationStatus.value = OptimizationStatus.Success(msg)
                _snackBarMessage.emit(msg)
                refreshMemoryStats()
            }.onFailure { err ->
                _optimizationStatus.value = OptimizationStatus.Error(err.message ?: "Ошибка выгрузки RAM")
            }
        }
    }

    fun runFstrimOnly() {
        viewModelScope.launch {
            _optimizationStatus.value = OptimizationStatus.Running("Оптимизация накопителя ТВ (FSTRIM)...", 0.5f)
            adbManager.runFstrim().onSuccess { msg ->
                _optimizationStatus.value = OptimizationStatus.Success(msg)
                _snackBarMessage.emit(msg)
                refreshMemoryStats()
            }.onFailure { err ->
                _optimizationStatus.value = OptimizationStatus.Error(err.message ?: "Ошибка TRIM")
            }
        }
    }

    fun cleanLogsOnly() {
        viewModelScope.launch {
            _optimizationStatus.value = OptimizationStatus.Running("Очистка системных буферов логов...", 0.5f)
            adbManager.cleanLogs().onSuccess { msg ->
                _optimizationStatus.value = OptimizationStatus.Success(msg)
                _snackBarMessage.emit(msg)
            }.onFailure { err ->
                _optimizationStatus.value = OptimizationStatus.Error(err.message ?: "Ошибка очистки логов")
            }
        }
    }

    fun setAnimationScale(scale: Float) {
        viewModelScope.launch {
            adbManager.setAnimationScale(scale).onSuccess { msg ->
                _animationScale.value = scale
                _snackBarMessage.emit(msg)
            }.onFailure { err ->
                _snackBarMessage.emit("Ошибка изменения скорости: ${err.message}")
            }
        }
    }

    fun runFullOptimization() {
        viewModelScope.launch {
            val statsBefore = adbManager.getMemoryStats().getOrNull()
            val beforeUsed = statsBefore?.usedRamMb ?: 0

            _optimizationStatus.value = OptimizationStatus.Running("1/4: Очистка кэша всех приложений...", 0.25f)
            adbManager.cleanCache()
            adbManager.cleanLogs()

            _optimizationStatus.value = OptimizationStatus.Running("2/4: Завершение фоновых зомби-процессов (RAM)...", 0.50f)
            adbManager.freeRam()

            _optimizationStatus.value = OptimizationStatus.Running("3/4: Оптимизация накопителя ТВ (FSTRIM)...", 0.75f)
            adbManager.runFstrim()

            _optimizationStatus.value = OptimizationStatus.Running("4/4: Ускорение анимаций меню (0.5x)...", 0.90f)
            adbManager.setAnimationScale(0.5f)
            _animationScale.value = 0.5f

            val statsAfter = adbManager.getMemoryStats().getOrNull()
            if (statsAfter != null) {
                _memoryStats.value = statsAfter
            }
            val afterUsed = statsAfter?.usedRamMb ?: 0
            val freed = (beforeUsed - afterUsed).coerceAtLeast(0)

            val successMsg = if (freed > 0) {
                "Комплексная турбо-оптимизация завершена! Освобождено ~${freed} МБ RAM, кэш очищен, анимации ускорены в 2 раза."
            } else {
                "Комплексная турбо-оптимизация завершена! Кэш и временные логи очищены, память оптимизирована."
            }

            _optimizationStatus.value = OptimizationStatus.Success(successMsg, freed)
            _snackBarMessage.emit(successMsg)
        }
    }

    fun resetOptimizationStatus() {
        _optimizationStatus.value = OptimizationStatus.Idle
    }

    fun checkAdGuardDns() {
        viewModelScope.launch {
            if (connectionState.value !is ConnectionState.Connected) return@launch
            adbManager.isAdGuardDnsEnabled().onSuccess { enabled ->
                _isAdGuardDnsActive.value = enabled
            }
        }
    }

    fun toggleAdGuardDns() {
        viewModelScope.launch {
            val newState = !_isAdGuardDnsActive.value
            _snackBarMessage.emit(if (newState) "Включение AdGuard DNS на ТВ..." else "Отключение блокировки рекламы...")
            adbManager.setAdGuardDns(newState).onSuccess { msg ->
                _isAdGuardDnsActive.value = newState
                _snackBarMessage.emit(msg)
            }.onFailure { err ->
                _snackBarMessage.emit("Ошибка настройки DNS: ${err.message}")
            }
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            cancelSleepTimer()
            return
        }
        val totalSeconds = minutes * 60
        _sleepTimerRemainingSeconds.value = totalSeconds
        viewModelScope.launch {
            _snackBarMessage.emit("Таймер сна установлен на $minutes мин.")
        }
        sleepTimerJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _sleepTimerRemainingSeconds.value = remaining
            }
            _sleepTimerRemainingSeconds.value = null
            _snackBarMessage.emit("Таймер сна сработал: выключение телевизора")
            turnOffTv()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerRemainingSeconds.value = null
        viewModelScope.launch {
            _snackBarMessage.emit("Таймер сна отменён")
        }
    }

    fun takeScreenshot() {
        viewModelScope.launch {
            if (connectionState.value !is ConnectionState.Connected) {
                _snackBarMessage.emit("Сначала подключитесь к ТВ")
                return@launch
            }
            _screenshotState.value = ScreenshotState.Capturing
            _snackBarMessage.emit("Создание снимка экрана ТВ...")
            adbManager.takeScreenshot().onSuccess { bmp ->
                _screenshotState.value = ScreenshotState.Success(bmp, System.currentTimeMillis())
                _snackBarMessage.emit("Снимок экрана ТВ получен!")
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "Сбой скриншота"
                _screenshotState.value = ScreenshotState.Error(errorMsg)
                _snackBarMessage.emit("Ошибка снимка: $errorMsg")
            }
        }
    }

    fun dismissScreenshot() {
        _screenshotState.value = ScreenshotState.Idle
    }

    fun downloadAndInstallCuratedApp(app: CuratedApp) {
        viewModelScope.launch {
            val appCtx = getApplication<Application>()
            val cacheFile = File(appCtx.cacheDir, "${app.id}_temp_${System.currentTimeMillis()}.apk")
            try {
                _installStatus.value = InstallStatus.Preparing("${app.name}.apk")
                _snackBarMessage.emit("Скачивание ${app.name}...")

                downloadFileWithRedirects(app.downloadUrl, cacheFile)

                _installStatus.value = InstallStatus.Installing("${app.name}.apk")
                _snackBarMessage.emit("Установка ${app.name} на ТВ через Wi-Fi...")

                val installResult = adbManager.installApkFile(cacheFile)
                installResult.onSuccess { msg ->
                    _installStatus.value = InstallStatus.Success("${app.name} успешно установлен на ТВ!")
                    _snackBarMessage.emit("${app.name} установлен!")
                    refreshApps()
                }.onFailure { err ->
                    val errorMsg = err.localizedMessage ?: "Сбой установки"
                    _installStatus.value = InstallStatus.Error(errorMsg)
                    _snackBarMessage.emit("Ошибка установки ${app.name}: $errorMsg")
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Ошибка загрузки APK"
                _installStatus.value = InstallStatus.Error(errorMsg)
                _snackBarMessage.emit("Ошибка: $errorMsg")
            } finally {
                if (cacheFile.exists()) {
                    cacheFile.delete()
                }
            }
        }
    }

    fun handleSharedText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.contains("youtu")) {
                val url = if (trimmed.contains("http")) {
                    val match = Regex("https?://[^\\s]+").find(trimmed)
                    match?.value ?: trimmed
                } else {
                    trimmed
                }
                _snackBarMessage.emit("Открытие ссылки на ТВ...")
                openUrlOnTv(url)
            } else {
                sendText(trimmed)
            }
        }
    }

    private suspend fun downloadFileWithRedirects(urlString: String, destination: File) = withContext(Dispatchers.IO) {
        var currentUrl = urlString
        var redirectCount = 0
        var connection: HttpURLConnection? = null
        try {
            while (redirectCount < 8) {
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = true
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.setRequestProperty("User-Agent", "TvCommander-Android")

                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == HttpURLConnection.HTTP_SEE_OTHER ||
                    status == 307 || status == 308
                ) {
                    val newUrl = connection.getHeaderField("Location")
                        ?: throw IOException("Редирект без ссылки назначения")
                    connection.disconnect()
                    currentUrl = newUrl
                    redirectCount++
                } else if (status in 200..299) {
                    connection.inputStream.use { input ->
                        FileOutputStream(destination).use { output ->
                            input.copyTo(output)
                        }
                    }
                    return@withContext
                } else {
                    throw IOException("HTTP ошибка $status: ${connection.responseMessage}")
                }
            }
            throw IOException("Превышено количество редиректов")
        } finally {
            connection?.disconnect()
        }
    }
}
