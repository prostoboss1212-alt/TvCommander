param(
    [string]$RepoUrl = ""
)

Write-Host "=== TV Commander: Синхронизация с GitHub ===" -ForegroundColor Cyan

if ($RepoUrl -ne "") {
    git remote remove origin 2>$null
    git remote add origin $RepoUrl
    Write-Host "Remote origin установлен: $RepoUrl" -ForegroundColor Green
}

$hasRemote = git remote get-url origin 2>$null
if (-not $hasRemote) {
    Write-Host "Remote репозиторий еще не привязан." -ForegroundColor Yellow
    Write-Host "Создайте пустой репозиторий на https://github.com/new (назовите TvCommander)"
    Write-Host "Затем запустите: .\sync_github.ps1 https://github.com/ВАШ_ЛОГИН/TvCommander.git"
    exit 1
}

Write-Host "Отправка кода на GitHub..." -ForegroundColor Cyan
git add .
git commit -m "update: sync release $(Get-Date -Format 'yyyy-MM-dd HH:mm')" 2>$null
git push -u origin master

Write-Host "`nГотово! GitHub Actions автоматически соберет APK и прикрепит его к релизу." -ForegroundColor Green
$cleanUrl = $hasRemote -replace '\.git$', ''
Write-Host "Ссылка для скачивания: $cleanUrl/releases/latest" -ForegroundColor Cyan
