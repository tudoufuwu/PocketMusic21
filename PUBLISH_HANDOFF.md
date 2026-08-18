# GitHub 发布交接（Android）

## 当前发布状态

- 项目：`PocketMusic21`（Android 端）
- 曲库：195 首；`song_177`–`song_195` 为本轮新增周杰伦候选
- APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，9,745,182 bytes，SHA-256 `7A378C2252C8BD10E5545140DC0F4F048E09063D3B1864AC41FB91D0E73BE834`
- APK 和 `app/build/` 默认不进 Git 历史；发布时作为 GitHub Release 附件上传

## 标准文件边界

应提交：Kotlin/Gradle 源码、`app/src/main/assets/library.json`、`app/src/main/assets/songs/*.txt`、README、LICENSE、`docs/`、`PROGRESS.md`、`HANDOFF.md`、`PUBLISH_HANDOFF.md`。

不得提交：`.gradle/`、`.android-sdk/`、`.tools/`、`local.properties`、`app/build/`、日志缓存、原始录音、CSV/MIDI/NPZ 和任何凭据。

## 下一回合上传步骤

在本目录初始化 Git 后，创建公开仓库 `tudoufuwu/PocketMusic21`，提交源码和 TXT，推送 `main`；用 `gh release create` 上传 APK：

```powershell
gh repo create tudoufuwu/PocketMusic21 --public --source . --remote origin
git add --all; git commit -m "release: PocketMusic21 195-song MVP"
git push -u origin main
gh release create v0.1.0-mvp artifacts/PocketMusic21-v0.1.0-mvp-debug.apk --title "PocketMusic21 v0.1.0 MVP" --notes-file PUBLISH_HANDOFF.md
```

若仓库已存在，跳过 `gh repo create`，先检查 `git remote -v` 和 `gh repo view`。上传前运行：

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\Check-CrossPlatformLibrary.ps1
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

录制和导入功能已包含在源码：悬浮窗录制实际 21 键事件，主界面导出/批量导入 TXT；下一回合不要删除这些入口。
