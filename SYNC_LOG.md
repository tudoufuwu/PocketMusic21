# Cross-platform library sync log

## 2026-08-18 final audit

- Windows `music_player_next/builtin_songs`: 164 TXT files.
- Android `library.json` and `assets/songs/`: 164 entries/files.
- Desktop-only: 0; missing Android entries: 0; content/hash/manifest errors: 0.
- Added `不问别离`, `拜无忧`, and the audio-derived `肘我（江湖梦二创）` candidate.

## 2026-08-18 baseline audit

Command (read-only):

```powershell
powershell -ExecutionPolicy Bypass -File .\mobile_player_android\tools\Check-CrossPlatformLibrary.ps1
```

Observed state:

- Windows `music_player_next/builtin_songs`: 163 TXT files; project state says app `1.0.0-beta.24`, library `138`.
- Android `app/src/main/assets/library.json`: schema `1`, 161 entries; `assets/songs/` contains 161 files.
- Desktop-only entries: `不问别离`, `拜无忧`.
- Existing Android entries have deterministic `song_NNN.txt` asset names and SHA-256 content hashes in `library.json`; copying by filename alone is unsafe.
- Normalized UTF-8 event-content comparison passes for all 161 shared songs; line-ending differences between Windows and Android copies are ignored. Android asset raw bytes still match every manifest SHA-256.
- The check currently exits `1` for the two desktop-only songs, as intended (`$LASTEXITCODE = 1`). `-Plan` prints the reviewed manual update sequence without modifying source files.

## Sync contract

1. Canonical title is the UTF-8 TXT basename without `.txt`, normalized to Unicode NFC. Matching is case-insensitive after normalization; do not silently rename titles.
2. Canonical interchange is UTF-8 TXT. Leading `#` comment metadata is optional. Playback records are `<keys> <beats>`; `p` is the rest key, keys are from `zxcvbnmasdfghjqwertyu`, and beats must be finite and in `(0,64]`. Chords use one key token with unique characters (for example `ad 0.5`).
3. Recommended tempo may be carried in `# 推荐节拍：N ms/拍` or `# 录制基准：N ms/拍`; if absent, retain the user's per-song setting instead of guessing.
4. Desktop export and Android import must preserve event ordering, chord grouping, rests, and decimal beat values. Android's bundled manifest additionally records `id`, `title`, `asset`, `beatMs`, and `sha256`; those fields are generated metadata, not part of the portable TXT.
5. Release gate: run the check, require zero missing/desktop-only/hash/manifest errors, then run both platform parser tests and rebuild artifacts. A desktop-only song is not considered shipped to mobile until it has a manifest entry and parser evidence.

## Open items

- The two beta.24 songs still need Android asset/manifest generation by the Android owner.
- No claim is made here about overlay pagination, lifecycle resilience, recorder UI, or the three requested new songs; those require source changes and runtime validation outside this packet.

## 2026-08-18 22:41 三首候选同步

- 跨平台门禁通过：Android manifest 与 Windows 新版均为 173 首，缺失、桌面独有、哈希/资源不一致和 manifest 错误均为 0。
- 新增 `song_171`《落了白》418 ms、`song_172`《难却》418 ms、`song_173`《青衣（草帽酱原版）》441 ms；旧《青衣》`song_156` 保留。
