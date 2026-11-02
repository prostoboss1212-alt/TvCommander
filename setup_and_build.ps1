$ErrorActionPreference = "Stop"
$sdkDir = "$HOME\android-sdk"
$zipFile = "$sdkDir\cmdline-tools.zip"

Write-Host "=== Шаг 1: Распаковка инструментов разработчика ==="
if (-not (Test-Path "$sdkDir\cmdline-tools\latest\bin\sdkmanager.bat")) {
    Expand-Archive -Path $zipFile -DestinationPath "$sdkDir\temp" -Force
    New-Item -ItemType Directory -Path "$sdkDir\cmdline-tools" -Force | Out-Null
    if (Test-Path "$sdkDir\cmdline-tools\latest") {
        Remove-Item -Path "$sdkDir\cmdline-tools\latest" -Recurse -Force
    }
    Move-Item -Path "$sdkDir\temp\cmdline-tools" -Destination "$sdkDir\cmdline-tools\latest" -Force
    Remove-Item -Path "$sdkDir\temp" -Recurse -Force
    Write-Host "Инструменты распакованы."
}

$env:ANDROID_HOME = $sdkDir
$env:ANDROID_SDK_ROOT = $sdkDir

Write-Host "=== Шаг 2: Принятие лицензий Google SDK ==="
$sdkmanager = "$sdkDir\cmdline-tools\latest\bin\sdkmanager.bat"
$yes = @("y", "y", "y", "y", "y", "y", "y", "y", "y", "y")
$yes | & $sdkmanager --licenses | Out-Null

Write-Host "=== Шаг 3: Установка компонентов Android 14 (API 34) ==="
$yes | & $sdkmanager "platforms;android-34" "build-tools;34.0.0"

Write-Host "=== Шаг 4: Настройка local.properties ==="
$escapedSdk = $sdkDir.Replace('\', '\\').Replace(':', '\:')
"sdk.dir=$escapedSdk" | Set-Content -Path "$HOME\TvCommander\local.properties" -Encoding UTF8

Write-Host "=== Шаг 5: Компиляция APK через Gradle ==="
Set-Location "$HOME\TvCommander"
& .\gradlew.bat assembleDebug --no-daemon

Write-Host "=== Шаг 6: Финализация ==="
$sourceApk = "$HOME\TvCommander\app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $sourceApk) {
    Copy-Item -Path $sourceApk -Destination "$HOME\TvCommander.apk" -Force
    Copy-Item -Path $sourceApk -Destination "$HOME\Desktop\TvCommander.apk" -Force -ErrorAction SilentlyContinue
    Write-Host "ГОТОВО! Файл сохранен в: $HOME\TvCommander.apk и на Рабочий стол!"
} else {
    Write-Host "Проверка скомпилированных файлов..."
}
