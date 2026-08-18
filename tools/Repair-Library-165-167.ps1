$ErrorActionPreference = 'Stop'
$android = Split-Path -Parent $PSScriptRoot
$desktop = Join-Path (Split-Path -Parent $android) 'music_player_next\builtin_songs'
$manifestPath = Join-Path $android 'app\src\main\assets\library.json'
$manifest = Get-Content $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$manifest.songs = @($manifest.songs | Where-Object { $_.id })
$sources = @(Get-ChildItem -LiteralPath $desktop -Filter '*.txt' -File | Sort-Object LastWriteTime -Descending | Select-Object -First 3)
if ($sources.Count -ne 3) { throw "Expected 3 new desktop songs, found $($sources.Count)" }
$number = 165
$beats = @(534, 449, 500)
$beatIndex = 0
foreach ($sourceItem in $sources) {
    $source = $sourceItem.FullName
    $itemId = "song_{0:D3}" -f $number
    $assetName = "$itemId.txt"
    $assetPath = Join-Path $android "app\src\main\assets\songs\$assetName"
    Copy-Item -LiteralPath $source -Destination $assetPath -Force
    $hash = (Get-FileHash $assetPath -Algorithm SHA256).Hash.ToLowerInvariant()
    $manifest.songs += [pscustomobject]@{
        id = $itemId
        title = [IO.Path]::GetFileNameWithoutExtension($sourceItem.Name)
        asset = "songs/$assetName"
        beatMs = $beats[$beatIndex]
        sha256 = $hash
    }
    $number++
    $beatIndex++
}
Remove-Item -LiteralPath (Join-Path $android 'app\src\main\assets\songs\.txt') -Force -ErrorAction SilentlyContinue
$manifest.count = @($manifest.songs).Count
$manifest | ConvertTo-Json -Depth 6 | Set-Content $manifestPath -Encoding UTF8
Write-Output "count=$($manifest.count)"
