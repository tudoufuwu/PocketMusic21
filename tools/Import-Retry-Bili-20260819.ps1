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
    [pscustomobject]@{ Id = 222; Title = '生僻字'; BeatMs = 500; Source = 'source_scores\batch_20260819_retry_bili_a\生僻字_候选.txt' },
    [pscustomobject]@{ Id = 223; Title = '左手指月'; BeatMs = 650; Source = 'source_scores\batch_20260819_retry_bili_b\左手指月.candidate.txt' },
    [pscustomobject]@{ Id = 224; Title = '无羁'; BeatMs = 750; Source = 'source_scores\batch_20260819_retry_bili_b\无羁.candidate.txt' },
    [pscustomobject]@{ Id = 225; Title = '归去来兮'; BeatMs = 857; Source = 'source_scores\batch_20260819_retry_bili_c\song_224_归去来兮_候选.txt' }
)

$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$entries = [Collections.Generic.List[object]]::new()
@($manifest.songs) | ForEach-Object { $entries.Add($_) }

foreach ($song in $batch) {
    $sourcePath = Join-Path $repo $song.Source
    if (-not (Test-Path -LiteralPath $sourcePath)) { throw "Missing candidate: $sourcePath" }
    $text = [IO.File]::ReadAllText($sourcePath, [Text.Encoding]::UTF8).TrimStart([char]0xFEFF)
    if ($text -notmatch "推荐节拍：$($song.BeatMs) ms/拍") { throw "Beat header mismatch: $($song.Title)" }
    if ($text -notmatch "候选需游戏内试听复核|自动转谱候选") { throw "Missing audition disclaimer: $($song.Title)" }
    if (@($entries | Where-Object { $_.title -eq $song.Title }).Count) { throw "Title already exists: $($song.Title)" }

    $assetName = ('song_{0:D3}.txt' -f $song.Id)
    [IO.File]::WriteAllText((Join-Path $desktopSongs ($song.Title + '.txt')), $text, $utf8NoBom)
    [IO.File]::WriteAllText((Join-Path $androidSongs $assetName), $text, $utf8NoBom)
    $hash = (Get-FileHash -LiteralPath (Join-Path $androidSongs $assetName) -Algorithm SHA256).Hash.ToLowerInvariant()
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
Write-Output "Imported $($batch.Count) retry candidates; manifest count=$($entries.Count)."
