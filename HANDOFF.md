# Android手机播放器接手说明

## 2026-08-10 最新接手点

## 2026-08-18 更新接手点

- Android/Windows 曲库已同步为 167 首，跨端门禁通过：0 缺失、0 哈希不匹配、0 节拍不匹配。
- `肘我（江湖梦二创）` 不是《恕我》，不要合并；`偶像` 与 `归零` 已从 blocked 升级为已入库候选。
- 三首候选：`肘我` 556 ms/拍、`偶像` 449 ms/拍、`归零` 534 ms/拍；都还需要游戏内试听校准，未标记 final。
- 悬浮窗支持完整滚动列表、搜索点歌、录制与 TXT 导入导出；校准支持“三行上下”和“七列左右”批量移动。
- 播放只会因目标游戏失焦、锁屏、手势失败或用户停止而停止；充电、旋转、音量变化不再作为停止条件。
- 最新 APK：`F:\\codexai\\01\\mobile_player_android\\artifacts\\PocketMusic21-v0.1.0-mvp-debug.apk`，SHA-256 `655753781AA1118FC7F509BDF993B0FE862A27D6A0A78023B727645140902D5D`，大小 9,724,344 bytes。

- 源码、161 首资产、校准、悬浮控制与无障碍调度已经完成并通过主机门禁。
- 复现命令：`F:\codexai\01\mobile_player_android\build.ps1 -Clean`。
- 最终 Debug APK：`F:\codexai\01\mobile_player_android\artifacts\PocketMusic21-v0.1.0-mvp-debug.apk`。
- SHA-256：`34BFB14BB633ED0E0C9C4832D767DE9DD7BF0DC31BA38237AFB9C9D3AA2B6422`。
- 当前唯一验收缺口是真机：ADB 无设备；不要重复搭建工程或重写解析器。
- 下一位执行者应先按 `DEVICE_ACCEPTANCE.md` 安装并记录手机型号、Android 版本、截图/录屏、系统导航方式和游戏横屏21键布局，再针对真实偏差修正。
- MuMu 12 已完成运行验收，证据见 `RUN_LOG_20260810.md` 和 `artifacts/mumu/`；不要再重复模拟器基础测试。下一步应启动模拟器内真实《一梦江湖》界面做坐标对齐，或转入物理手机验收。

## 当前目标

从零实现Android 21键手机播放器，首要适配一梦江湖，兼容`F:\codexai\01\music_player_next\builtin_songs`中的161首TXT。

## 必读顺序

1. `F:\codexai\01\mobile_player_android\PROGRESS.md`
2. `F:\codexai\01\mobile_player_android\PROJECT_PLAN.md`
3. `F:\codexai\01\mobile_player_android\DECISIONS.md`
4. `F:\codexai\01\apk_assessment_pocketmusic\reports\maturity_and_compatibility.md`

## 当前接手点

- 状态：只有规划与APK取证，尚未创建Android源码，不得声称已有APK。
- 设备工具：JDK 21、ADB可用；Android SDK、sdkmanager、Gradle未发现。
- 第一目标：配置最小SDK和Gradle Wrapper，建立项目并让`assembleDebug`成功。
- 第二目标：移植TXT解析器并让161/161曲谱通过Kotlin测试。
- 第三目标：实现3×7校准网格，再进入无障碍手势，不要反过来。

## 连续性要求

- 每完成一个里程碑立即更新`PROGRESS.md`。
- 所有APK放入`mobile_player_android/artifacts/`，记录大小和SHA256。
- 不覆盖Windows播放器、原始曲谱或参考APK分析目录。
- 真机事实缺失时先完成不依赖真机的工作，不能把整个任务停在询问阶段。

## 最新有效交接点（2026-08-18）

- 历史段落中的“尚未创建 APK”已过期；Android 工程和 Debug APK 已完成。
- 当前曲库为 167 首，已包含 `肘我（江湖梦二创）`、`偶像`、`归零`。
- 最新 APK：`F:\\codexai\\01\\mobile_player_android\\artifacts\\PocketMusic21-v0.1.0-mvp-debug.apk`。
- APK SHA256：`655753781AA1118FC7F509BDF993B0FE862A27D6A0A78023B727645140902D5D`。
- 与 Windows `music_player_next/builtin_songs` 的跨平台检查通过：0 missing、0 desktop-only、0 hash mismatch、0 manifest/asset error。
- `肘我（江湖梦二创）` 独立于 `恕我`；三首都还是候选曲，等待游戏内试听定稿。

## 2026-08-18 新增《念张师DJ版》

- 目标版本锁定 B 站 `BV1moVB6RE62`《念张师DJ版完整版》，实际音轨 95.2 秒；不要混用 6:49 的其他 DJ 版本。
- 当前 Android 曲库已更新为 168 首，新增 asset：`assets/songs/song_168.txt`，APK 内确认存在 168 个 TXT 资源。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，大小 9,724,415 bytes，SHA256 `1EC944ECEDB5F83CF4525C211E3CC466562366502F0301BA388AEF6B3C2091A2`。
- `念张师DJ版` 为自动转谱候选：68 events、31 rests、216 beats、441 ms/拍；尚未游戏内试听定稿。

## 2026-08-18 新增《幻昼DJ版》

- 目标版本为 B 站 `BV1NtoXBPE9W`《幻昼dj》降调版，实际音轨 118.77 秒，不是 4:11 Shirfine 原版。
- 当前 Android 曲库为 169 首，新增 asset：`assets/songs/song_169.txt`；APK 内确认 TXT 资源总数 169。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，大小 9,724,486 bytes，SHA256 `C3C80576C4834A86700CCCEF180C12839C88C2DE8D764FF9070AEC16679BC3D1`。
- 《幻昼DJ版》为自动转谱候选：232 events、115 rests、237.75 beats、500 ms/拍；等待游戏内试听定稿。

## 2026-08-18 新增《新宝岛》

- 目标版本为 B 站 `BV1xE411v7WN` 的サカナクション（鱼韵）原版《新宝島》，实际音轨约 313 秒。
- 当前 Android 曲库为 170 首，新增 asset：`assets/songs/song_170.txt`；APK 内确认 TXT 资源总数 170。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，大小 9,724,557 bytes，SHA256 `0C7150EECD755E8B50E35B27E150C2ADD0CFA25745B580515DAD52F533368F2A`。
- 《新宝岛》为原版音频自动转谱候选：136 events、71 rests、841.5 beats、372 ms/拍；等待游戏内试听并重点检查稀疏段落。

## 2026-08-18 三首候选同步

- Android 曲库现为 173 首，新增 `song_171`《落了白》、`song_172`《难却》、`song_173`《青衣（草帽酱原版）》。
- 原 `song_156`《青衣》未改动；不要把 `song_173` 改回同名，否则跨端唯一标题门禁会失败。
- 最新 APK SHA-256：`6F8110A308C70B1187415CCF4D4BBAD457A9DD72F348CFEBD6ED2760DE45FC93`；已确认包含 173 个 TXT。

## 2026-08-18 最新 175 首交接

- Android/Windows 曲库已同步为 175 首；新增 `song_174`《咏春》与 `song_175`《朋友的酒》。
- 跨平台门禁、Android 单测、Lint、assembleDebug 全部通过。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，9,724,911 bytes，SHA-256 `0253261CA380D1C8372E7D10DD22F5E5D7F1EC2D72E3D5E9B285239096357CE2`。
- 桌面副本 `C:\Users\rmb\Desktop\21键手机播放器-v0.1.0-MVP.apk` 已覆盖为同一构建。
- 两首均需游戏内试听校准，不能标记为最终准确谱。

## 2026-08-18 《二泉映月》交接

- Android/Windows 曲库已同步为 176 首，新增 `song_176`《二泉映月》，推荐节拍 488 ms/拍。
- 来源：Wikimedia Commons `File:二泉映月.ogg`，张沛坚演奏，CC BY-SA 4.0；报告与 MIDI/CSV 均已保留。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，9,737,940 bytes，SHA-256 `3F57BD96DB25C7DDC75660A3FC290CE76F9DEB2B8F20A0BB13ACAD734F1113D1`。
- 桌面副本 `C:\Users\rmb\Desktop\21键手机播放器-v0.1.0-MVP.apk` 已覆盖为同一构建。
- 该曲为音频转写候选，需游戏内试听校准，不能标记为最终准确谱。

## 周杰伦批次交接（待构建）

- 桌面端已新增 19 首候选，目标 Android 曲库为 195 首；《轨迹》《半岛铁盒》《菊花台》因可靠音源阻塞，不进入 manifest。
- Android 同步必须从 `music_player_next/builtin_songs/` 复制 `song_177`–`song_195`，然后运行跨平台检查、单测、Lint、assembleDebug。

## 周杰伦批次已完成交接

- Android/Windows 曲库已同步为 195 首，新增 `song_177`–`song_195`，19 首均保留来源报告并标记需游戏内试听。
- 《轨迹》《半岛铁盒》《菊花台》只有阻塞报告，没有伪造 TXT。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，9,745,182 bytes，SHA-256 `7A378C2252C8BD10E5545140DC0F4F048E09063D3B1864AC41FB91D0E73BE834`。
- `C:\Users\rmb\Desktop\21键手机播放器-v0.1.0-MVP.apk` 已覆盖为同一构建。
- Windows beta.33 已完成打包：`F:\codexai\01\music_player_next\dist\JianpuPlayerNext-v1.0.0-beta.33.exe`，SHA-256 `28D0E64F4599D483A2A7252781911776DEEFB0E077B2FF8C0502459848F3C0F7`。
- 三首只完成候选生成、入库和构建，后续需游戏内试听才可标记 final。
## 2026-08-09 22:50 暂停说明

- 用户决定不使用 OpenClaw 24 小时续跑，改为明天由 Codex 继续执行。
- 未安装 OpenClaw，未启动任何后台进程，也未开始 Android 实现。
- 当前断点不变：M0（Android 构建环境与可编译空壳）；下次必须先读取本目录四份日志，再从环境盘点开始。


## 2026-08-10 10:54 新断点

- 悬浮曲库 v2、点歌即播、三种播放模式、最近/收藏/每曲速度已落盘并通过 MuMu 运行验证。
- 游戏内透明网格校准已落盘：整体移动、横纵缩放、单点修正、保存/取消。
- 最新 APK 与桌面副本 SHA-256：`34BFB14BB633ED0E0C9C4832D767DE9DD7BF0DC31BA38237AFB9C9D3AA2B6422`。
- 《一梦江湖》仍在实例 0 下载资源；进入琴键页后直接用悬浮面板“校准”完成最终对点。


## 2026-08-10 手机截图断点

- 已从用户 1280×576 真机截图测得 21 点并加入“一梦江湖20:9（截图）”预设。
- 预设中心 X=`310,437,564,691,818,945,1072`，Y=`363,436,508`（相对 1280×576）。
- 最新桌面 APK SHA-256：`34BFB14BB633ED0E0C9C4832D767DE9DD7BF0DC31BA38237AFB9C9D3AA2B6422`。


## 2026-08-10 真机遮挡修复断点

- 展开悬浮窗已缩窄、缩短；“☰ 拖动”整块状态区可拖。
- 点击播放后始终收成 52dp 音乐球，避免 Accessibility Overlay 挡住左侧琴键。
- 最新桌面 APK SHA-256：`34BFB14BB633ED0E0C9C4832D767DE9DD7BF0DC31BA38237AFB9C9D3AA2B6422`。
