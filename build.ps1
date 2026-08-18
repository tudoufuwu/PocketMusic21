[CmdletBinding()]
param([switch]$Clean)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$PrivateSdk = Join-Path $Root '.android-sdk'

if (Test-Path -LiteralPath $PrivateSdk) {
    $env:ANDROID_HOME = $PrivateSdk
    $env:ANDROID_SDK_ROOT = $PrivateSdk
} elseif (-not $env:ANDROID_HOME -and -not $env:ANDROID_SDK_ROOT) {
    throw '未找到 Android SDK。请安装 API 35 与 Build Tools 35.0.0，或设置 ANDROID_HOME。'
}

Push-Location $Root
try {
    if ($Clean) { & .\gradlew.bat --no-daemon clean }
    & .\gradlew.bat --no-daemon testDebugUnitTest lintDebug assembleDebug
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    $Apk = Join-Path $Root 'app\build\outputs\apk\debug\app-debug.apk'
    $Hash = (Get-FileHash -LiteralPath $Apk -Algorithm SHA256).Hash
    Write-Output "APK=$Apk"
    Write-Output "SIZE=$((Get-Item -LiteralPath $Apk).Length)"
    Write-Output "SHA256=$Hash"
} finally {
    Pop-Location
}
