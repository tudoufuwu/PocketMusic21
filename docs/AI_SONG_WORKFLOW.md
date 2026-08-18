# Android 曲库同步与通用录制说明

Android 曲库的真值来自 Windows 端 `music_player_next/builtin_songs/`，不要手工维护两套不同的旋律数据。

## 同步门禁

1. 按标题复制 TXT 到 `app/src/main/assets/songs/song_NNN.txt`。
2. 在 `library.json` 写入唯一 ID、标题、`beatMs` 和 SHA-256。
3. 运行 `tools\Check-CrossPlatformLibrary.ps1`，必须得到缺失、桌面独有、哈希和 manifest 错误均为 0。
4. 运行 `build.ps1`，确认单测、Lint、assembleDebug 全部通过。

## 玩家通用功能

- 悬浮窗可录制实际派发的 21 键事件，并从主界面导出 TXT。
- 主界面支持批量导入 TXT；导入时保留 `# 推荐节拍`，解析失败应提示行号而不是静默丢弃。
- 导出的 TXT 可回传 Windows 播放器或其他兼容客户端；导入前由玩家确认歌曲来源和使用权。
- 录制/导入属于本机数据功能，不代表项目拥有歌曲原作、录音或改编授权。

## AI 交接约束

自动转谱只能标记 `requires_in_game_audition`；来源不可核验时只写阻塞报告。每次发布同时更新 `PROGRESS.md`、`HANDOFF.md` 和曲库数量，确保下一位维护者可以从日志继续。
