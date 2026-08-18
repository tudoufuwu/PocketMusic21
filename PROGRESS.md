# Android手机播放器独立续跑日志

## 2026-08-18 增量更新

- 悬浮曲库显示完整可滚动列表，搜索结果不再截断前 8 首，点歌路径保持一致。
- 校准新增“三行上下”和“七列左右”批量拖动；行仅改 Y，列仅改 X。
- 播放不再因转屏停止；目标应用失焦或锁屏仍安全停止，充电/音量变化不触发停止。
- 新增录制会话与 TXT 导出：悬浮窗记录实际派发键事件，主界面可导出当前曲谱或录制内容；导入标题统一。
- `testDebugUnitTest assembleDebug` 通过；APK `artifacts/PocketMusic21-v0.1.1-update-debug.apk`，SHA-256 `8CB19912A419D974108C4B8AE1E3C7E386A46DE0789D1B987F6AA79F827797BB`。

## 2026-08-10 设备外独立开发完成

- 状态：`mvp_built / real_device_validation_pending`。
- M0–M3 已实现；M4 的构建、单测、Lint、签名、曲库封装和哈希已通过。
- 6 个 JVM 测试全部通过，其中全库门禁为 161/161；Android Lint 为 `No issues found.`。
- 最终 APK：`F:\codexai\01\mobile_player_android\artifacts\PocketMusic21-v0.1.0-mvp-debug.apk`。
- 桌面副本：`C:\Users\rmb\Desktop\21键手机播放器-v0.1.0-MVP.apk`。
- 大小：9,686,589 bytes；SHA-256：`34BFB14BB633ED0E0C9C4832D767DE9DD7BF0DC31BA38237AFB9C9D3AA2B6422`。
- 当前 ADB 没有连接设备，因此不得称为真机稳定版；下一步只需在用户手机上安装、授权、截图校准并做和弦/失焦/暂停回归。
- 完整可复现证据见 `RUN_LOG_20260810.md`，真机步骤见 `DEVICE_ACCEPTANCE.md`。

## 2026-08-10 MuMu 12 验收完成

- 已启动 MuMu 主实例并通过 ADB `127.0.0.1:16384` 接管 Android 12 / API 32。
- APK 安装、冷启动、161 首曲库、校准页、无障碍绑定、悬浮面板和拖动全部通过。
- 模拟器截图发现校准页左侧在 1080p 被截断；已加滚动、重建、重装并验证修复。
- 安全触摸靶验证单音坐标命中；`ad` 和弦在同一毫秒产生 pointer 1/2，确认同步多指派发。
- 暂停三秒事件数保持 4→4；停止后三秒事件数保持不变；无服务崩溃。
- 状态提升为：`emulator_validated_mvp / physical_device_and_real_game_pending`。
- 已启动真实包 `com.netease.wyclx` 并显示悬浮面板；游戏仍需下载 3306.70 MB 资源，当前尚未进入21键演奏界面。
- 游戏画面发现“外部停止后文字不刷新、标题长度导致按钮位移”，已改为250 ms实时刷新和固定状态栏宽度，重建重装后验证为“已返回播放器，播放停止”。

## 2026-08-10 M0 完成

- 已建立项目私有 Android SDK（API 35 / Build Tools 35.0.0）与 Gradle 8.9。
- Kotlin + Jetpack Compose 横屏空壳已通过 `assembleDebug`，返回码 0。
- 首个 APK 已保存到 `F:\codexai\01\mobile_player_android\artifacts\PocketMusic21-M0-0.1.0-debug.apk`。
- APK 大小 9,452,666 bytes，SHA-256 `28A4D9E1CEADB47840E82A823D039236A4ED86BE8158053D3C64770CA3F4DEAC`。
- 详细命令和失败路径见 `RUN_LOG_20260810.md`；当前进入 M1（161 首 TXT 解析与曲库）。

## 2026-08-09 22:00 规划完成

### 已验证事实

- 现有新版Windows播放器为beta.22，内置161首，曲库版本136。
- 参考APK静态分析完成，报告位于：`F:\codexai\01\apk_assessment_pocketmusic\reports\maturity_and_compatibility.md`。
- 参考APK内置246首，使用Auto.js/Rhino的JS启动壳与加密snapshot，不能直接接收现有TXT。
- 参考APK已证明悬浮窗、无障碍手势、坐标采集的产品路线可行，但其targetSdk 28、103个唯一权限和私有曲库格式不适合作为新项目底座。
- 当前开发机已有JDK 21与ADB；未发现Android SDK、sdkmanager和Gradle。

### 已锁定决策

- 新建原生Android项目，不修改或重打包参考APK。
- 直接兼容现有161首TXT，并设计可版本化JSON格式。
- 默认3×7网格；支持整体、行列、单点校准和归一化坐标。
- 离线优先、无需Root、无需登录、最小权限。

### 当前状态

- 阶段：`planned`
- active milestone：`M0 Android开发环境与可编译空壳`
- 已完成比例：规划与取证100%；Android实现0%。
- 预计首个可安装MVP：开发开始后12–18小时；真机稳定版通常还需1–3天校准。

### 明日第一条命令前检查

1. 读取本文件、`PROJECT_PLAN.md`、`DECISIONS.md`与`HANDOFF.md`。
2. 检查Android SDK/Gradle是否已经存在，避免重复安装。
3. 先完成空壳`assembleDebug`，再写功能；不得同时大范围铺开。
4. 每个里程碑完成后立即追加构建命令、返回码、APK路径和SHA256。

### 未完成项

- Android SDK与Gradle环境。
- Android工程源码。
- TXT Kotlin解析器。
- 3×7校准网格。
- AccessibilityService调度器。
- 第一份测试APK。

### 下一步

执行M0，产出能安装启动的最小Debug APK；随后进入M1全库解析。
## 2026-08-09 22:50 暂停说明

- 用户决定不使用 OpenClaw 24 小时续跑，改为明天由 Codex 继续执行。
- 未安装 OpenClaw，未启动任何后台进程，也未开始 Android 实现。
- 当前断点不变：M0（Android 构建环境与可编译空壳）；下次必须先读取本目录四份日志，再从环境盘点开始。


## M5 — overlay-first player and in-game calibration (complete; real game alignment pending)

- Direct overlay search/selection/autoplay, three play modes, recent, favorites, per-song speed, progress, and 52dp bubble are implemented.
- Transparent in-game calibration provides whole-grid move, X/Y scaling, per-point correction, cancel, and normalized save.
- MuMu screenshots and runtime gesture evidence are under `artifacts/mumu/`; build/lint/tests passed.
- Final real-game alignment is pending the remaining game resource download and entry into the 21-key performance screen.


## 2026-08-18 更新完成

- 曲库已同步为 167 首，`肘我（江湖梦二创）`、`偶像`、`归零`、`不败的英雄（铠甲勇士刑天）` 均已入库。
- `肘我` 独立于《恕我》，不再按别名合并。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，SHA-256 `655753781AA1118FC7F509BDF993B0FE862A27D6A0A78023B727645140902D5D`。
- 爆种并发歌曲单元已回收：`肘我` 精修为 550 事件并重新同步 Android；`归零` 标记多版本歧义；`偶像` 标记公开素材访问受限。
- 最终重建 APK SHA-256 `04BFA01D518FA2C53638C2B9C6025882C97CD2A3F875A38E6168659FE4758CF2`，大小 9,735,680 bytes。

- Android 曲库已与 `music_player_next/builtin_songs` 跨端门禁通过，0 缺失、0 哈希不匹配、0 节拍不匹配。
- 新增《肘我（江湖梦二创）》《偶像》《归零》音频转写候选；来源与 Basic Pitch 原始事件分别保存在对应 `source_audio/*_GAME/`。
- 悬浮完整列表/搜索、录制 TXT、三行上下和七列左右校准、播放抗充电/旋转/音量中断已实现。
- `build.ps1`：测试、Lint、assemble 全部通过；APK SHA-256 `655753781AA1118FC7F509BDF993B0FE862A27D6A0A78023B727645140902D5D`。

## 当前有效交接状态（2026-08-18 21:35）

- 曲库已更新为 168 首，新增《念张师DJ版》并同步到 Android `song_168.txt`。
- Android `testDebugUnitTest`、Lint、assembleDebug 均通过；APK 内确认包含 `assets/songs/song_168.txt`，TXT 资源总数 168。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，大小 9,724,415 bytes，SHA-256 `1EC944ECEDB5F83CF4525C211E3CC466562366502F0301BA388AEF6B3C2091A2`。
- 目标视频：B 站 `BV1moVB6RE62`《念张师DJ版完整版》；候选自动转谱，等待游戏内试听。

## 当前有效交接状态（2026-08-18 21:50）

- 曲库已更新为 169 首，新增《幻昼DJ版》并同步到 Android `song_169.txt`。
- Android testDebugUnitTest、Lint、assembleDebug 均通过；APK 内确认包含 `assets/songs/song_169.txt`，TXT 资源总数 169。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，大小 9,724,486 bytes，SHA-256 `C3C80576C4834A86700CCCEF180C12839C88C2DE8D764FF9070AEC16679BC3D1`。
- 目标视频：B 站 `BV1NtoXBPE9W`《幻昼dj》降调版；候选自动转谱，等待游戏内试听。

## 当前有效交接状态（2026-08-18 22:10）

- 曲库已更新为 170 首，新增《新宝岛》并同步到 Android `song_170.txt`。
- Android testDebugUnitTest、Lint、assembleDebug 均通过；APK 内确认包含 `assets/songs/song_170.txt`，TXT 资源总数 170。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，大小 9,724,557 bytes，SHA-256 `0C7150EECD755E8B50E35B27E150C2ADD0CFA25745B580515DAD52F533368F2A`。
- 目标视频：B 站 `BV1xE411v7WN` サカナクション《新宝島》原版；自动转谱候选，等待游戏内试听。

## 当前有效交接状态（2026-08-18 22:41）

- 曲库已更新为 173 首：`song_171.txt`《落了白》、`song_172.txt`《难却》、`song_173.txt`《青衣（草帽酱原版）》。
- 原有 `song_156.txt`《青衣》保留；新候选使用版本后缀，避免覆盖和重复标题。
- Android 单测、Lint、assembleDebug 及跨平台 173/173 检查全部通过；缺失、桌面独有、哈希/资源和 manifest 错误均为 0。
- APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，9,724,770 bytes，SHA-256 `6F8110A308C70B1187415CCF4D4BBAD457A9DD72F348CFEBD6ED2760DE45FC93`；内含 173 个 TXT。
- 三首均为 `candidate_done_requires_listening`，尚非 final。

## 当前有效交接状态（2026-08-18，最新）

- 曲库已更新为 175 首：`song_174.txt`《咏春》、`song_175.txt`《朋友的酒》。
- 《咏春》采用七朵组合 B 站 `BV1dyFNzoEYi` 候选，推荐 409 ms/拍；《朋友的酒》采用李晓杰 DJ 小鱼儿版 `BV1h1421m7QY` 候选，推荐 465 ms/拍。
- Android 单测、Lint、assembleDebug 及跨平台 175/175 检查全部通过；缺失、桌面独有、哈希/资源和 manifest 错误均为 0。
- APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，9,724,911 bytes，SHA-256 `0253261CA380D1C8372E7D10DD22F5E5D7F1EC2D72E3D5E9B285239096357CE2`；桌面副本同哈希。
- 两首均为音频转写候选，状态为 `requires_in_game_audition`，尚非 final。

## 当前有效交接状态（2026-08-18，二泉映月）

- 曲库已更新为 176 首，新增 `song_176.txt`《二泉映月》；推荐 488 ms/拍。
- 音源为 Wikimedia Commons 张沛坚演奏录音（CC BY-SA 4.0），转写报告记录 403 个映射音符、547 个播放事件、550 拍。
- Android 单测、Lint、assembleDebug 及跨平台 176/176 检查全部通过；缺失、桌面独有、哈希/资源和 manifest 错误均为 0。
- APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，9,737,940 bytes，SHA-256 `3F57BD96DB25C7DDC75660A3FC290CE76F9DEB2B8F20A0BB13ACAD734F1113D1`；桌面副本同哈希。
- 《二泉映月》为音频转写候选，状态为 `requires_in_game_audition`，尚非 final。

## 周杰伦批次待同步（2026-08-18）

- 桌面端已并行完成 19 首周杰伦候选，当前桌面曲库为 195 首；3 首（《轨迹》《半岛铁盒》《菊花台》）仅保留阻塞报告，未生成伪谱。
- Android manifest 尚待本轮构建门禁确认；同步来源固定为桌面端 `builtin_songs/*.txt`，新增曲目将使用 `song_177`–`song_195`。

## 周杰伦批次完成（2026-08-18）

- 19 首候选已同步到 Android `song_177`–`song_195`，曲库共 195 首；《轨迹》《半岛铁盒》《菊花台》保持阻塞报告，不进入曲库。
- 跨平台检查 195/195 通过；Android 单测、Lint、assembleDebug 通过；Windows 单测 26/26 通过。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，9,745,182 bytes，SHA-256 `7A378C2252C8BD10E5545140DC0F4F048E09063D3B1864AC41FB91D0E73BE834`；桌面副本同哈希。
- 19 首均为音频转写候选，需游戏内试听校准，尚非 final。
- Windows beta.33 已打包：`F:\codexai\01\music_player_next\dist\JianpuPlayerNext-v1.0.0-beta.33.exe`，14,807,404 bytes，SHA-256 `28D0E64F4599D483A2A7252781911776DEEFB0E077B2FF8C0502459848F3C0F7`。

## 当前有效交接状态（2026-08-18）

- 上述 APK 为当前有效产物；较早的 `04BFA01D...` 哈希记录属于历史构建，不是最新 APK。
- 当前 APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，大小 9,724,344 bytes，SHA-256 `655753781AA1118FC7F509BDF993B0FE862A27D6A0A78023B727645140902D5D`。
- 跨平台检查已通过：167/167 首，缺失 0，桌面独有 0，哈希不一致 0，manifest/asset 错误 0。
- 尚未完成的是三首候选的游戏内试听定稿，不是 APK 同步；`肘我（江湖梦二创）` 仍独立于 `恕我`。
