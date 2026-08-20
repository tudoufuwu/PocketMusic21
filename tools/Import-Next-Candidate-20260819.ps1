[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$desktopSongs = Join-Path $repo 'music_player_next\builtin_songs'
$androidAssets = Join-Path $repo 'mobile_player_android\app\src\main\assets'
$androidSongs = Join-Path $androidAssets 'songs'
$manifestPath = Join-Path $androidAssets 'library.json'
$utf8NoBom = [Text.UTF8Encoding]::new($false)

$batch = @(
    [pscustomobject]@{
        Id = 220
        Title = '浪人琵琶（胡66）'
        BeatMs = 650
        Source = 'source_scores\batch_20260819_next_cn_c\浪人琵琶（胡66）.candidate.txt'
    },
    [pscustomobject]@{
        Id = 221
        Title = '云与海（YueYue）'
        BeatMs = 1000
        Source = 'source_scores\batch_20260819_next_cn_c\云与海（YueYue）.candidate.txt'
    }
)

$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$entries = [Collections.Generic.List[object]]::new()
@($manifest.songs) | ForEach-Object { $entries.Add($_) }

foreach ($song in $batch) {
    $sourcePath = Join-Path $repo $song.Source
    if (-not (Test-Path -LiteralPath $sourcePath)) { throw "Missing candidate: $sourcePath" }
    $text = [IO.File]::ReadAllText($sourcePath, [Text.Encoding]::UTF8).TrimStart([char]0xFEFF)
    if ($text -notmatch "推荐节拍：$($song.BeatMs) ms/拍") { throw "Beat header mismatch: $($song.Title)" }

    $duplicate = @($entries | Where-Object { $_.title -eq $song.Title })
    if ($duplicate.Count) { throw "Title already exists: $($song.Title)" }

    $assetName = ('song_{0:D3}.txt' -f $song.Id)
    $desktopPath = Join-Path $desktopSongs ($song.Title + '.txt')
    $assetPath = Join-Path $androidSongs $assetName
    [IO.File]::WriteAllText($desktopPath, $text, $utf8NoBom)
    [IO.File]::WriteAllText($assetPath, $text, $utf8NoBom)
    $hash = (Get-FileHash -LiteralPath $assetPath -Algorithm SHA256).Hash.ToLowerInvariant()
    $entries.Add([pscustomobject]([ordered]@{
        id = ('song_{0:D3}' -f $song.Id)
        title = $song.Title
        asset = "songs/$assetName"
        beatMs = $song.BeatMs
        sha256 = $hash
    }))
}

$manifest.count = $entries.Count
$manifest.songs = @($entries)
[IO.File]::WriteAllText($manifestPath, (($manifest | ConvertTo-Json -Depth 8) + "`n"), $utf8NoBom)
Write-Output "Imported $($batch.Count) candidates; manifest count=$($entries.Count)."
