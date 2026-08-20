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
    [pscustomobject]@{ Id = 235; Title = '万神纪'; BeatMs = 625; Source = 'source_scores\batch_20260820_final_retry_a\万神纪\万神纪.candidate.txt' },
    [pscustomobject]@{ Id = 236; Title = '光年之外'; BeatMs = 667; Source = 'source_scores\batch_20260820_final_retry_a\光年之外\光年之外.candidate.txt' },
    [pscustomobject]@{ Id = 237; Title = '演员'; BeatMs = 800; Source = 'source_scores\batch_20260820_final_retry_b\演员.candidate.txt' },
    [pscustomobject]@{ Id = 238; Title = '追梦赤子心'; BeatMs = 433; Source = 'source_scores\batch_20260820_final_retry_b\追梦赤子心.candidate.txt' },
    [pscustomobject]@{ Id = 239; Title = '世间美好与你环环相扣'; BeatMs = 500; Source = 'source_scores\batch_20260820_final_retry_c\世间美好与你环环相扣.candidate.txt' }
)

$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$entries = [Collections.Generic.List[object]]::new()
@($manifest.songs) | ForEach-Object { $entries.Add($_) }
if ($entries.Count -ne 234) { throw "Unexpected baseline manifest count: $($entries.Count)" }

foreach ($song in $batch) {
    $sourcePath = Join-Path $repo $song.Source
    if (-not (Test-Path -LiteralPath $sourcePath)) { throw "Missing candidate: $sourcePath" }
    $text = [IO.File]::ReadAllText($sourcePath, [Text.Encoding]::UTF8).TrimStart([char]0xFEFF)
    if ($text -notmatch "推荐节拍：$($song.BeatMs) ms/拍") { throw "Beat header mismatch: $($song.Title)" }
    if ($text -notmatch '候选需游戏内试听复核') { throw "Missing audition disclaimer: $($song.Title)" }
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
Write-Output "Imported $($batch.Count) final-retry candidates; manifest count=$($entries.Count)."
