# TV Commander 📺⚡

[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B-green?style=flat-square&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-blue?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Download APK](https://img.shields.io/badge/Download-TvCommander.apk-brightgreen?style=for-the-badge&logo=android)](../../releases/latest/download/TvCommander.apk)

**TV Commander** — мощное мобильное приложение на Android (смартфон) для удаленного беспроводного управления телевизорами Android TV / Google TV (Xiaomi Mi TV, TCL и др.) по домашней сети Wi-Fi через прямой ADB-протокол без необходимости ставить серверные приложения на телевизор.

---

## 📥 Скачать приложение
Актуальный готовый APK-файл всегда доступен в разделе [Releases](../../releases/latest):
👉 **[Скачать TvCommander.apk](../../releases/latest/download/TvCommander.apk)**

---

## ✨ Ключевые возможности

### 🎮 1. Мгновенный тактильный пульт (Dual Mode)
- **Два режима на выбор:**
  - **Кнопки (D-Pad):** классический тактильный диск с виброоткликом и центральной кнопкой OK.
  - **Сенсорный тачпад (Trackpad):** управление ТВ плавными свайпами пальца в любую сторону и тапом для выбора.
- **Быстрый запуск программ прямо с пульта:** 1-тап запуск SmartTube, Кинопоиск, Lampa и настроек ТВ.
- **Быстрая клавиатура:** ввод длинных текстов, поисковых запросов и вставка из буфера обмена смартфона прямо в поиск на ТВ.
- **Аппаратная качелька громкости:** физические кнопки громкости смартфона меняют громкость телевизора.
- **Таймер сна (Sleep Timer):** автовыключение ТВ через 15, 30, 45, 60, 90 минут.
- **Скриншот экрана ТВ:** мгновенный снимок текущего кадра ТВ в 100% качестве по ADB.

### ⚡ 2. Включение телевизора по Wi-Fi (Wake-on-LAN + Xiaomi MiTV API)
- Пробуждение спящего ТВ в один клик прямо со смартфона.
- Мультиадресная рассылка пакетов Magic Packet (broadcast `255.255.255.255`, subnet `X.X.X.255` и прямой unicast) на порты 9 и 7.
- Параллельный вызов нативного сервисного API Xiaomi Mi Remote на порту `6095/8345`.
- Автоматический цикл повторных попыток подключения ADB с ожиданием старта Wi-Fi чипа ТВ.

### 🚀 3. Глубокая оптимизация ТВ без смены лаунчера («Очистка»)
- **Мониторинг ресурсов в реальном времени:** точные показатели свободной RAM (`/proc/meminfo`) и хранилища (`df /data`).
- **Турбо-ускорение в 1 клик:**
  - Глубокая очистка кэша всех приложений (`pm trim-caches`).
  - Выгрузка спящих фоновых зомби-процессов (`am kill-all`).
  - Аппаратный TRIM флеш-памяти (`sm fstrim`).
  - Ускорение системных анимаций интерфейса ТВ (0.0x / 0.5x / 1.0x).
- **Блокировка рекламы на ТВ (AdGuard Private DNS):** системная фильтрация баннеров без установки тяжелых приложений.

### 📺 4. Каталог быстрых лаунчеров (Установка в 1 клик)
- **FLauncher** (ультралегкий, Open Source, без рекламы).
- **Projectivy Launcher** (официальный релиз 4.73 от Spocky с аппаратным ускорением).
- **ATV Launcher Pro** (плиточный интерфейс для слабых ТВ с 1 ГБ RAM).
- **Akari Launcher** (всего 500 КБ, потребляет <30 МБ памяти).
- **CuteLauncher** (премиальный дизайн в стиле Apple tvOS).
- **Откат к заводской оболочке** в 1 клик (Xiaomi PatchWall / Google TV).

### 🍿 5. Топ-приложения для ТВ в 1 клик
- **SmartTube:** лучший YouTube для Android TV без рекламы со встроенным SponsorBlock.
- **Lampa AIO:** онлайн-кинотеатр со встроенными плагинами и каталогами.
- **TorrServe Matrix:** онлайн-просмотр торрент-фильмов без предварительного скачивания.
- **VLC:** всеядный медиаплеер.

---

## 🛠️ Стек технологий
- **Язык:** Kotlin 2.0.0 (JVM 17)
- **UI:** Jetpack Compose (Material 3), Dark OLED & Cyberpunk Glassmorphism
- **Сетевой ADB-клиент:** `dev.mobile:dadb:1.2.6` (чистый сокетный протокол, без внешних бинарников)
- **Автоматизация релизов:** GitHub Actions CI/CD (автоматическая сборка и публикация APK)

---

## 📦 Сборка из исходников
```bash
git clone https://github.com/<USERNAME>/TvCommander.git
cd TvCommander
./gradlew assembleDebug
```
Готовый файл: `app/build/outputs/apk/debug/app-debug.apk`
