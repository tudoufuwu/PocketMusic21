[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$source = Get-ChildItem 'C:\Users\rmb\Desktop\cunchu' -Recurse -File -Filter '*.txt' | Where-Object Length -eq 6872 | Select-Object -First 1 -ExpandProperty FullName
$desktopSongs = Join-Path $repo 'music_player_next\builtin_songs'
$androidAssets = Join-Path $repo 'mobile_player_android\app\src\main\assets'
$asset = Join-Path $androidAssets 'songs\song_282.txt'
$manifestPath = Join-Path $androidAssets 'library.json'
$utf8 = [Text.UTF8Encoding]::new($false)
if (-not (Test-Path -LiteralPath $source)) { throw "Missing source: $source" }
$text = [IO.File]::ReadAllText($source, $utf8).TrimStart([char]0xFEFF)
if ($text -notmatch '714 ms') { throw 'Beat header mismatch' }
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$title = [string]([char]0x77e5)+([char]0x6211)
if (@($manifest.songs | Where-Object title -eq $title).Count) { throw 'title already exists' }
[IO.File]::WriteAllText((Join-Path $desktopSongs ($title + '.txt')), $text, $utf8)
[IO.File]::WriteAllText($asset, $text, $utf8)
$hash = (Get-FileHash -LiteralPath $asset -Algorithm SHA256).Hash.ToLowerInvariant()
$manifest.songs = @($manifest.songs) + [pscustomobject][ordered]@{ id='song_282'; title=$title; asset='songs/song_282.txt'; beatMs=714; sha256=$hash }
$manifest.count = @($manifest.songs).Count
[IO.File]::WriteAllText($manifestPath, (($manifest | ConvertTo-Json -Depth 8) + "`n"), $utf8)
Write-Output "Imported 知我 as song_282; manifest count=$($manifest.count)."
