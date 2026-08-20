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
    [pscustomobject]@{ Id = 226; Title = '普通DISCO'; BeatMs = 535; Source = 'source_scores\batch_20260820_bili_classics_a\普通DISCO\普通DISCO.candidate.txt' },
    [pscustomobject]@{ Id = 227; Title = '达拉崩吧'; BeatMs = 511; Source = 'source_scores\batch_20260820_bili_classics_a\达拉崩吧\达拉崩吧.candidate.txt' },
    [pscustomobject]@{ Id = 228; Title = '勾指起誓'; BeatMs = 500; Source = 'source_scores\batch_20260820_bili_classics_a\勾指起誓\勾指起誓.candidate.txt' },
    [pscustomobject]@{ Id = 229; Title = '权御天下'; BeatMs = 500; Source = 'source_scores\batch_20260820_bili_classics_a\权御天下\权御天下.candidate.txt' },
    [pscustomobject]@{ Id = 230; Title = '冠世一战'; BeatMs = 541; Source = 'source_scores\batch_20260820_bili_classics_b\guanshiyizhan.candidate.txt' },
    [pscustomobject]@{ Id = 231; Title = '神的随波逐流'; BeatMs = 447; Source = 'source_scores\batch_20260820_bili_classics_b\kaminomanimani.candidate.txt' },
    [pscustomobject]@{ Id = 232; Title = 'LOSER'; BeatMs = 505; Source = 'source_scores\batch_20260820_bili_classics_b\loser.candidate.txt' },
    [pscustomobject]@{ Id = 233; Title = '撒野'; BeatMs = 517; Source = 'source_scores\batch_20260820_bili_classics_b\saye.candidate.txt' },
    [pscustomobject]@{ Id = 234; Title = 'unravel'; BeatMs = 473; Source = 'source_scores\batch_20260820_bili_classics_b\unravel.candidate.txt' }
)

$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$entries = [Collections.Generic.List[object]]::new()
@($manifest.songs) | ForEach-Object { $entries.Add($_) }
if ($entries.Count -ne 225) { throw "Unexpected baseline manifest count: $($entries.Count)" }

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
Write-Output "Imported $($batch.Count) Bilibili classics candidates; manifest count=$($entries.Count)."
