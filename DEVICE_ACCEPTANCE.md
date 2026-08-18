# 真机验收清单

## 准备

1. 记录手机型号、Android 版本、屏幕分辨率、系统导航方式及是否隐藏导航条。
2. 安装 `artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`。
3. 保存一张游戏横屏 21 键原始截图；截图不要裁剪。

## ADB 基础验收

```powershell
$adb='F:\codexai\01\mobile_player_android\.android-sdk\platform-tools\adb.exe'
& $adb devices -l
& $adb install -r 'F:\codexai\01\mobile_player_android\artifacts\PocketMusic21-v0.1.0-mvp-debug.apk'
& $adb shell am start -W -n com.shadowtrace.pocketmusic21/.MainActivity
& $adb shell pidof com.shadowtrace.pocketmusic21
```

记录每条命令的返回码和有界输出。

## 功能回归

- 曲库显示 161 首；搜索中文、日文和英文各一首。
- 任取短、中、长曲各一首，确认解析统计可见。
- 批量导入 2 个有效 TXT；导入一个非法键 TXT，确认有错误提示且应用不崩溃。
- 选择正确比例预设，将 21 点拖到游戏白点；退出重进确认配置保留。
- 分别验证单点、整行、整列和整体拖动。
- 启用“21键悬浮演奏”，确认系统描述与应用用途一致。
- 在游戏前台从悬浮面板开始；验证单音、和弦、休止、暂停、继续、停止。
- 播放中切换应用、返回播放器、锁屏、转为竖屏，确认不再派发新手势。
- 验证悬浮面板可拖动、收起，且不会遮住关键游戏控件。

## 通过门槛

- 无崩溃/ANR；停止后没有残留点击。
- 21 点全部命中；和弦效果按设备实际限制记录。
- 保存必要的截图、录屏、`adb logcat` 有界片段和最终校准配置。
- 未通过时保留复现步骤，不把 Debug MVP 标记为稳定版。
