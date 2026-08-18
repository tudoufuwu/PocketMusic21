[CmdletBinding()]
param(
    [string]$DesktopSongs,
    [string]$AndroidAssets,
    [switch]$Plan
)

$ErrorActionPreference = 'Stop'
$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
if ([string]::IsNullOrWhiteSpace($DesktopSongs)) { $DesktopSongs = Join-Path $scriptRoot '..\..\music_player_next\builtin_songs' }
if ([string]::IsNullOrWhiteSpace($AndroidAssets)) { $AndroidAssets = Join-Path $scriptRoot '..\app\src\main\assets' }
$utf8 = [Text.UTF8Encoding]::new($false)
function Get-NormalizedTextHash([string]$path) {
    $text = [IO.File]::ReadAllText($path, $utf8).TrimStart([char]0xFEFF)
    $text = $text.Replace("`r`n", "`n").Replace("`r", "`n")
    $bytes = $utf8.GetBytes($text)
    return ([Security.Cryptography.SHA256]::Create().ComputeHash($bytes) | ForEach-Object { $_.ToString('x2') }) -join ''
}
function Get-RecommendedBeatMs([string]$path) {
    $firstLine = [IO.File]::ReadLines($path, $utf8) | Select-Object -First 1
    # Keep the script ASCII-only so Windows PowerShell 5.1 can parse the
    # UTF-8-without-BOM source. Song headers put the beat value before "ms".
    if ($firstLine -match '(\d+)\s*ms') { return [int]$Matches[1] }
    return $null
}
$manifestPath = Join-Path $AndroidAssets 'library.json'
if (-not (Test-Path -LiteralPath $manifestPath)) { throw "Missing Android manifest: $manifestPath" }
if (-not (Test-Path -LiteralPath $DesktopSongs)) { throw "Missing desktop songs: $DesktopSongs" }

$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$entries = @($manifest.songs)
$desktop = @{}
Get-ChildItem -LiteralPath $DesktopSongs -Filter '*.txt' -File | ForEach-Object {
    $desktop[$_.BaseName.Normalize([Text.NormalizationForm]::FormC).ToUpperInvariant()] = $_
}

$missing = [System.Collections.Generic.List[string]]::new()
$hashMismatch = [System.Collections.Generic.List[string]]::new()
$invalid = [System.Collections.Generic.List[string]]::new()
$seen = @{}
foreach ($entry in $entries) {
    $key = ([string]$entry.title).Normalize([Text.NormalizationForm]::FormC).ToUpperInvariant()
    if ($seen.ContainsKey($key)) { $invalid.Add("duplicate title: $($entry.title)") } else { $seen[$key] = $true }
    $source = $desktop[$key]
    if ($null -eq $source) { $missing.Add([string]$entry.title); continue }
    $assetPath = Join-Path $AndroidAssets ([string]$entry.asset)
    if (-not (Test-Path -LiteralPath $assetPath)) { $invalid.Add("missing asset: $($entry.asset)"); continue }
    $sourceHash = Get-NormalizedTextHash $source.FullName
    $assetHash = Get-NormalizedTextHash $assetPath
    $rawAssetHash = (Get-FileHash -LiteralPath $assetPath -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($sourceHash -ne $assetHash) {
        $hashMismatch.Add("$($entry.title) normalized source=$sourceHash asset=$assetHash")
    } elseif ($rawAssetHash -ne ([string]$entry.sha256).ToLowerInvariant()) {
        $invalid.Add("manifest hash mismatch: $($entry.title) asset=$rawAssetHash manifest=$($entry.sha256)")
    }
    $sourceBeatMs = Get-RecommendedBeatMs $source.FullName
    if ($null -ne $sourceBeatMs -and [int]$entry.beatMs -ne $sourceBeatMs) {
        $invalid.Add("manifest beatMs mismatch: $($entry.title) source=$sourceBeatMs manifest=$($entry.beatMs)")
    }
}

$desktopOnly = @(Get-ChildItem -LiteralPath $DesktopSongs -Filter '*.txt' -File | Where-Object {
    $key = $_.BaseName.Normalize([Text.NormalizationForm]::FormC).ToUpperInvariant()
    -not $seen.ContainsKey($key)
})
Write-Output "Android manifest: $($entries.Count) songs (schema $($manifest.schema))"
Write-Output "Desktop builtin_songs: $((Get-ChildItem -LiteralPath $DesktopSongs -Filter '*.txt' -File).Count) songs"
Write-Output "Missing on desktop for Android entries: $($missing.Count)"
Write-Output "Desktop-only songs: $($desktopOnly.Count)"
Write-Output "Hash/asset mismatches: $($hashMismatch.Count)"
Write-Output "Manifest/asset errors: $($invalid.Count)"
if ($missing.Count) { Write-Output ('  missing: ' + ($missing -join ', ')) }
if ($desktopOnly.Count) { Write-Output ('  desktop-only: ' + (($desktopOnly | ForEach-Object BaseName) -join ', ')) }
if ($hashMismatch.Count) { $hashMismatch | ForEach-Object { Write-Output "  $_" } }
if ($invalid.Count) { $invalid | ForEach-Object { Write-Output "  $_" } }
if ($Plan) {
    Write-Output 'PLAN (manual, review before editing):'
    Write-Output '  1. Copy each desktop-only TXT into Android assets/songs/song_NNN.txt using a new manifest entry.'
    Write-Output '  2. Preserve UTF-8 text and exact event lines; set beat_ms from the TXT header.'
    Write-Output '  3. Rebuild library.json, run Android parser tests, then rebuild APK.'
}
if ($missing.Count -or $desktopOnly.Count -or $hashMismatch.Count -or $invalid.Count) { exit 1 }
exit 0
