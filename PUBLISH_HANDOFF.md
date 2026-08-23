# GitHub 发布交接（Android）

## 当前待发布增量（2026-08-22）

- 曲库已同步至 268 首，新增 song_268《孑遗者的故乡》，推荐 500 ms/拍。
- 该曲为自动转谱候选，需游戏内试听确认；本轮尚未构建 APK，下一次构建时一并纳入。

## 当前本地曲库（2026-08-21）

- Windows/Android 当前曲库为 264 首；新增 `song_264`《夏空的歌（短原版）》，推荐 535 ms/拍，状态 `requires_in_game_audition`。
- Android APK：`artifacts/PocketMusic21-v0.1.0-264songs-no-recording-debug.apk`，10,166,777 bytes，SHA-256 `6B69587A1D1DD77F1BE7928B6B9A5D2609AD7784C85EB49857109BE790C02707`。

- Windows/Android 当前曲库为263首；新增 `song_258`–`song_263`：红豆、匆匆那年、素颜、一直很安静、传奇、千年之恋。
- 六首均为 `requires_in_game_audition` 自动候选，未完成游戏内试听，不能称为 final。
- GitHub 未上传，未创建 Release；263首包需完成本轮构建门禁后再记录附件哈希。
- Android 263首候选构建：`artifacts/PocketMusic21-v0.1.0-263songs-no-recording-debug.apk`，10,166,706 bytes，SHA-256 `50EE7EEA67EE7599852B74945F2FDCB547491D61BC8CF5E05F0DA103962CFCA5`。

## 2026-08-20 爆种 OLD-HITS04（暂不上传）

- 当前本地双端曲库为 255 首；新增 `song_255`《一生所爱》，状态 `requires_in_game_audition`。
- 《泡沫》《我们的爱》《God knows...》来源阻塞，未入库。
- 主播放器 APK：`artifacts/PocketMusic21-v0.1.0-255songs-no-recording-debug.apk`，10,166,138 bytes，SHA-256 `4560A4F1EE19B434F8BAB7D23D3E4A83BC9ACD959672F44E29A588F6BCEACE28`；桌面副本同哈希。
- 跨端检查与 Android test/lint/assembleDebug 已通过；GitHub 仍未上传。

## 2026-08-20 请求批次 8 首本地曲库更新（暂不上传）

- 当前本地双端曲库为 254 首；新增 `song_247`–`song_254` 八首候选：如愿、美丽的神话、月亮代表我的心、至少还有你、半岛铁盒、菊花台、Secret Base ～君がくれたもの～（10 years after ver.）、踏山河。
- 八首均为 `requires_in_game_audition` 自动转谱候选，发布文案必须保留该状态；GitHub 仍未上传、未创建 Release。
- 本批主播放器测试 APK：`artifacts/PocketMusic21-v0.1.0-254songs-no-recording-debug.apk`，10,166,067 bytes，SHA-256 `DA1F07246EFBB3FB1CA73806D3FC9C34363D8E7A3E3E06351B0BCDE7357F3985`；桌面副本同哈希。

## 2026-08-20 本地曲库更新（暂不上传）

- 当前本地双端曲库为 246 首；新增 `song_241`–`song_246` 六首候选（《轨迹》《江南》《枫》《修炼爱情》《可惜没如果》《Megalovania》），推荐 500 ms/拍。
- 六首均为 `requires_in_game_audition` 自动转谱候选，发布文案必须保留该状态；GitHub 仍未上传、未创建 Release。
- 开源日构建附件前先运行跨端检查，确认 254/254、缺失/独有/哈希/manifest 错误均为 0，再构建主播放器 APK、独立制谱器 APK 和 Windows EXE。

本轮本地主播放器测试 APK：`artifacts/PocketMusic21-v0.1.0-246songs-no-recording-debug.apk`（10,165,499 bytes，SHA-256 `C0FF999BDD28A9F831EC337FAB4E9D5F7FCCAD90BE15C831CB06CF34A07ABF5B`）。Windows 对应 `music_player_next/dist/JianpuPlayerNext-v1.0.0-beta.43.exe`（14,871,029 bytes，SHA-256 `7CC455CFC9BE2657B1E3BF0F462DB1426FC523109AE43C6F3695311B602C62B5`）。

## 2026-08-20 当前待发布版本（暂不上传）

- 发布时应提供两个可独立安装的 Android 附件，用户可以只下载其中一个：
  - 主播放器：`artifacts/PocketMusic21-v0.1.0-240songs-no-recording-debug.apk`，10,165,073 bytes，SHA-256 `1D7CCBFF7ED8FC7F428B31D5FE69845DC68443CB4BA57C5234C9510F6FF8937C`，applicationId `com.shadowtrace.pocketmusic21`。
  - 独立制谱器半成品：`artifacts/PocketMusic21-ScoreMaker-v0.1.0-240songs-debug.apk`，9,484,970 bytes，SHA-256 `26C72A3BC1F734E0F0BB297067C698E874049BA6A68A3284FB81699669BEF0FD`，applicationId `com.shadowtrace.scoremaker21`。
- 主播放器本版已移除游戏内录制入口、透明触摸捕获层和 `Recording*` 生产链；收缩悬浮窗保留当前选曲、播放和停止。
- 制谱器使用自有横屏 21 键记录点击并导出播放器通用 TXT，不向游戏注入触摸。发布说明必须把它作为单独附件，并明确标注“手工制谱半成品，待音源识别和真机精修”。
- 当前 Android/Windows 正式曲库为 240 首；最新 `song_240`《尘外客》中文版本候选状态为 `requires_in_game_audition`，游戏内试听前不能称为 final。
- 《须弥》确认为网易《一梦江湖》（原《楚留香》手游）少林门派曲，已作为 Android `song_157` 与 Windows《须弥.txt》保留；推荐节拍 511 ms/拍。
- 跨端检查：240/240，缺失0、独有0、哈希/资源错误0；主播放器与制谱器构建、lint、单元测试均通过。
- GitHub 当前仍未上传；开源日再执行下方流程，不要提前创建 Release、推送或提交。

## 标准文件边界

应提交：Kotlin/Gradle 源码、`app/src/main/assets/library.json`、`app/src/main/assets/songs/*.txt`、README、LICENSE、`docs/`、`PROGRESS.md`、`HANDOFF.md`、`PUBLISH_HANDOFF.md`。

不得提交：`.gradle/`、`.android-sdk/`、`.tools/`、`local.properties`、`app/build/`、日志缓存、原始录音、CSV/MIDI/NPZ 和任何凭据。

APK 和 `app/build/` 默认不进入 Git 历史；两个 APK 在开源日作为 GitHub Release 的独立附件上传。

## 开源日执行步骤

先在本目录核对登录、远端和发布附件，不在当前回合执行：

```powershell
gh auth status
git remote -v
gh repo view tudoufuwu/PocketMusic21
```

上传前运行门禁：

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\tools\Check-CrossPlatformLibrary.ps1
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug --no-daemon
.\gradlew.bat :scoreMaker:testDebugUnitTest :scoreMaker:lintDebug :scoreMaker:assembleDebug --no-daemon
```

若公开仓库尚不存在，再创建、提交并推送；若已存在，跳过 `gh repo create`：

```powershell
gh repo create tudoufuwu/PocketMusic21 --public --source . --remote origin
git add --all
git commit -m "release: PocketMusic21 240-song MVP"
git push -u origin main
gh release create v0.1.0-mvp artifacts/PocketMusic21-v0.1.0-240songs-no-recording-debug.apk artifacts/PocketMusic21-ScoreMaker-v0.1.0-240songs-debug.apk --title "PocketMusic21 v0.1.0 MVP · 240 songs" --notes-file PUBLISH_HANDOFF.md
```

发布前再次确认 Release 页面包含两个不同 applicationId 的附件，并在说明中区分“主播放器无录制”和“独立制谱器半成品”。
# Android 发布交接

## 当前待发布版本（2026-08-22）

- 曲库 267 首，包含 `song_264`–`song_267`；新增候选仍需游戏内试听确认。
- 悬浮曲库入口兼容横屏手机；悬浮面板将基础节拍与倍速分开，倍速支持直接输入 `0.25`–`4.00`。
- Debug APK：`app/build/outputs/apk/debug/app-debug.apk`。
- APK SHA-256：`C94255ECE23B76FFF6042A091E97480900E36E83A0FEC5CD3CEFB5951CC943D5`。
- GitHub/Gitee Release 和自动更新源尚未发布。
# 当前待发布版本（2026-08-23）

- Android 曲库为 270 首，`song_269` 和 `song_270` 已同步；跨端检查 270/270 通过。
- Debug APK：`app/build/outputs/apk/debug/app-debug.apk`，10,167,203 bytes，SHA-256 `27997EC2BA63D5981FA918F32E369859E702EF2CC0A9BCC00A4CC6CF4377432F`。
- 构建命令：`.\gradlew.bat :app:assembleDebug --no-daemon`。
