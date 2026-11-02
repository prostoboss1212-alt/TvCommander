param(
    [string]$TagName = "v2.0.0",
    [string]$ReleaseName = "TV Commander v2.0 (Dual-Mode Remote & Turbo Optimization)"
)

$tokenFile = Join-Path $PSScriptRoot ".github_token"
$token = if (Test-Path $tokenFile) { (Get-Content $tokenFile -Raw).Trim() } else { $env:GITHUB_TOKEN }
$owner = "prostoboss1212-alt"
$repo = "TvCommander"
$apkPath = "C:\Users\Ильясис Бутенкасис\Desktop\TvCommander.apk"

Write-Host "=== TV Commander: Синхронизация с GitHub ===" -ForegroundColor Cyan

# 1. Git push
git push -u origin master

# 2. Upload APK to GitHub Release
if ($token -and (Test-Path $apkPath)) {
    Write-Host "Обновление релиза на GitHub..." -ForegroundColor Cyan
    $headers = @{
        "Authorization" = "Bearer $token"
        "Accept" = "application/vnd.github+json"
        "X-GitHub-Api-Version" = "2022-11-28"
        "User-Agent" = "TvCommander-Release-Agent"
    }

    $release = $null
    try {
        $release = Invoke-RestMethod -Uri "https://api.github.com/repos/$owner/$repo/releases/tags/$TagName" -Headers $headers -ErrorAction Stop
    } catch {
        $body = @{
            tag_name = $TagName
            name = $ReleaseName
            draft = $false
            prerelease = $false
        } | ConvertTo-Json
        $release = Invoke-RestMethod -Uri "https://api.github.com/repos/$owner/$repo/releases" -Method Post -Headers $headers -Body $body -ContentType "application/json"
    }

    if ($release) {
        $existing = $release.assets | Where-Object { $_.name -eq "TvCommander.apk" }
        if ($existing) {
            Invoke-RestMethod -Uri "https://api.github.com/repos/$owner/$repo/releases/assets/$($existing.id)" -Method Delete -Headers $headers
        }

        $uploadUrl = $release.upload_url -replace '\{.*\}', "?name=TvCommander.apk"
        $apkBytes = [System.IO.File]::ReadAllBytes($apkPath)
        $uploadHeaders = @{
            "Authorization" = "Bearer $token"
            "Accept" = "application/vnd.github+json"
            "X-GitHub-Api-Version" = "2022-11-28"
            "Content-Type" = "application/vnd.android.package-archive"
            "User-Agent" = "TvCommander-Release-Agent"
        }
        $asset = Invoke-RestMethod -Uri $uploadUrl -Method Post -Headers $uploadHeaders -Body $apkBytes
        Write-Host "Релиз успешно обновлен: $($asset.browser_download_url)" -ForegroundColor Green
    }
}
