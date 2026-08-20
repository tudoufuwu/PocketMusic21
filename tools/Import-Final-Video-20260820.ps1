[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$desktopSongs = Join-Path $repo 'music_player_next\builtin_songs'
$androidAssets = Join-Path $repo 'mobile_player_android\app\src\main\assets'
$androidSongs = Join-Path $androidAssets 'songs'
$manifestPath = Join-Path $androidAssets 'library.json'
$sourcePath = Join-Path $repo 'source_scores\batch_20260820_final_video_b\尘外客_p1.candidate.txt'
$utf8NoBom = [Text.UTF8Encoding]::new($false)

$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$entries = [Collections.Generic.List[object]]::new()
@($manifest.songs) | ForEach-Object { $entries.Add($_) }
if ($entries.Count -ne 239) { throw "Unexpected baseline manifest count: $($entries.Count)" }
if (@($entries | Where-Object { $_.title -eq '尘外客' }).Count) { throw 'Title already exists: 尘外客' }

$text = [IO.File]::ReadAllText($sourcePath, [Text.Encoding]::UTF8).TrimStart([char]0xFEFF)
if ($text -notmatch '推荐节拍：441 ms/拍') { throw 'Beat header mismatch: 尘外客' }
if ($text -notmatch '候选需游戏内试听复核') { throw 'Missing audition disclaimer: 尘外客' }

$desktopPath = Join-Path $desktopSongs '尘外客.txt'
$assetName = 'song_240.txt'
$androidPath = Join-Path $androidSongs $assetName
[IO.File]::WriteAllText($desktopPath, $text, $utf8NoBom)
[IO.File]::WriteAllText($androidPath, $text, $utf8NoBom)
$hash = (Get-FileHash -LiteralPath $androidPath -Algorithm SHA256).Hash.ToLowerInvariant()
$entries.Add([pscustomobject]([ordered]@{
    id = 'song_240'
    title = '尘外客'
    asset = "songs/$assetName"
    beatMs = 441
    sha256 = $hash
}))
$manifest.count = $entries.Count
$manifest.songs = @($entries)
[IO.File]::WriteAllText($manifestPath, (($manifest | ConvertTo-Json -Depth 8) + "`n"), $utf8NoBom)
Write-Output "Imported 尘外客 as song_240; manifest count=$($entries.Count)."
