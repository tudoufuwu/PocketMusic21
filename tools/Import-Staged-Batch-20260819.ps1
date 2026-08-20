[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$desktopSongs = Join-Path $root 'music_player_next\builtin_songs'
$androidAssets = Join-Path $root 'mobile_player_android\app\src\main\assets'
$androidSongs = Join-Path $androidAssets 'songs'
$manifestPath = Join-Path $androidAssets 'library.json'
$utf8NoBom = [Text.UTF8Encoding]::new($false)

$batch = @(
    [pscustomobject]@{ Id = 209; Title = '勇气'; BeatMs = 938; Source = 'music_player_next\staging\batch_20260819_sources\勇气\勇气.candidate.txt' },
    [pscustomobject]@{ Id = 210; Title = '暖暖'; BeatMs = 833; Source = 'music_player_next\staging\batch_20260819_sources\暖暖\暖暖.candidate.txt' },
    [pscustomobject]@{ Id = 211; Title = '遇见'; BeatMs = 714; Source = 'music_player_next\staging\batch_20260819_sources\遇见\遇见.candidate.txt' },
    [pscustomobject]@{ Id = 212; Title = '我怀念的'; BeatMs = 896; Source = 'music_player_next\staging\batch_20260819_sources\我怀念的\我怀念的.candidate.txt' }
)

$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$entries = [Collections.Generic.List[object]]::new()
@($manifest.songs) | ForEach-Object { $entries.Add($_) }

foreach ($repair in @(@{ Title = '光るなら'; BeatMs = 375 }, @{ Title = '残酷天使的行动纲领'; BeatMs = 525 })) {
    $entry = $entries | Where-Object { $_.title -eq $repair.Title } | Select-Object -Single
    if ($null -eq $entry) { throw "Missing existing manifest entry: $($repair.Title)" }
    $entry.beatMs = $repair.BeatMs
}

foreach ($song in $batch) {
    $sourcePath = Join-Path $root $song.Source
    if (-not (Test-Path -LiteralPath $sourcePath)) { throw "Missing staged candidate: $sourcePath" }
    $text = [IO.File]::ReadAllText($sourcePath, [Text.Encoding]::UTF8).TrimStart([char]0xFEFF)
    if ($text -notmatch "推荐节拍：$($song.BeatMs) ms") { throw "Beat header mismatch: $($song.Title)" }

    $existing = @($entries | Where-Object { $_.title -eq $song.Title })
    if ($existing.Count -gt 1) { throw "Duplicate manifest title: $($song.Title)" }

    $assetName = ('song_{0:D3}.txt' -f $song.Id)
    $desktopPath = Join-Path $desktopSongs ($song.Title + '.txt')
    $assetPath = Join-Path $androidSongs $assetName
    [IO.File]::WriteAllText($desktopPath, $text, $utf8NoBom)
    [IO.File]::WriteAllText($assetPath, $text, $utf8NoBom)
    $hash = (Get-FileHash -LiteralPath $assetPath -Algorithm SHA256).Hash.ToLowerInvariant()

    if ($existing.Count -eq 0) {
        $entries.Add([pscustomobject]([ordered]@{
            id = ('song_{0:D3}' -f $song.Id)
            title = $song.Title
            asset = "songs/$assetName"
            beatMs = $song.BeatMs
            sha256 = $hash
        }))
    } else {
        $existing[0].id = ('song_{0:D3}' -f $song.Id)
        $existing[0].asset = "songs/$assetName"
        $existing[0].beatMs = $song.BeatMs
        $existing[0].sha256 = $hash
    }
}

$manifest.count = $entries.Count
$manifest.songs = @($entries)
$json = $manifest | ConvertTo-Json -Depth 8
[IO.File]::WriteAllText($manifestPath, $json + "`n", $utf8NoBom)
Write-Output "Imported staged candidates; manifest count=$($entries.Count)."
