# GitHub 发布交接（Android）

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
