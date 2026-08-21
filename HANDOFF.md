# Android手机播放器接手说明

## 2026-08-20 曲库 246 首本地候选基线

- 主播放器曲库已由 240 首同步至 246 首：`song_241`–`song_243` 为《轨迹》《江南》《枫》，`song_244`–`song_246` 为《修炼爱情》《可惜没如果》《Megalovania》，推荐速度均为 500 ms/拍。
- 六首均为完整 B 站音频自动转谱候选，Parser/时长门禁通过，但仍须游戏内试听；不要在发布说明中称为 final。
- Windows 真值位于 `music_player_next/builtin_songs/`，Android 对应 `app/src/main/assets/songs/` 与 `library.json`；跨端门禁应以 243/243 为目标。

## 2026-08-20 v0.2.0 有声独立制谱器

- 独立应用 `com.shadowtrace.scoremaker21` 已完成 v0.2.0 有声制谱：横屏 21 键每次点击都会立即播放本地游戏采样，无论当前是否录制；播放器生命周期结束时安全释放音频资源。
- 录制支持按点击时间保留音符间隔、90ms 窗口内合并和弦、0.125 拍量化与 `p` 休止；长休止会拆分为播放器可接受的块。完整工作流为开始、暂停、继续、停止、撤销、选中删除、确认清空、TXT 导入和 TXT 保存。
- 保存仅在停止且存在事件时启用，导入仅允许停止状态；界面持续显示事件数和最近按键。TXT 推荐节拍兼容半角/全角冒号。
- 最终门禁：13 项测试、Lint、assembleDebug 全部成功。TXT 导入接受播放器协议的任意 `0 < 拍数 ≤ 64`，并识别推荐节拍/录制基准的半角与全角冒号。最新版 `artifacts/PocketMusic21-ScoreMaker-v0.2.0-sound-debug.apk`，11,875,218 bytes，SHA-256 `081883CFC01AC80F73A04FFC3393CFF60CA37C177467669D993A66BF82F5C4BB`；桌面同名副本一致。
- 旧制谱器 APK 已移动至 `C:\Users\rmb\Desktop\旧版APK归档`，可恢复。`发布全部GitHub.ps1 -DryRun` 已成功：Windows 26 项测试、Android/Windows 240/240 跨端检查均通过；GitHub **未上传**，未创建 Release。

## 2026-08-19 最新接手点（主播放器无录制 + 独立制谱器）

- 当前交付不是“播放器内录制”：主播放器 `com.shadowtrace.pocketmusic21` 已删除游戏内录制入口、透明触摸捕获层和全部 `Recording*` 生产类；保留 240 首曲库、选曲/搜索/自动播放。
- 收缩悬浮窗现在固定显示音乐球、状态/当前选曲、`▶ 播放`、`■ 停止`。播放只针对当前选曲且同曲播放中不重复启动；停止不清除选曲；无活动播放时停止禁用。
- `MusicAccessibilityService.onInterrupt()` 不再把瞬时系统界面当成停止条件；主界面旋转重建也不再主动停止播放。锁屏、无障碍服务真正销毁和用户主动停止仍是停止条件。
- 独立制谱器 `com.shadowtrace.scoremaker21` 位于 `scoreMaker/`，无无障碍权限/悬浮窗权限。自有横屏 21 键支持开始、暂停/继续、停止、撤销、删除、清空、和弦合并、0.5 拍量化、`p` 休止、TXT 导入/保存；可生成播放器通用 TXT。
- 主播放器 APK：`artifacts/PocketMusic21-v0.1.0-240songs-no-recording-debug.apk`（10,165,073 bytes，`1D7CCBFF7ED8FC7F428B31D5FE69845DC68443CB4BA57C5234C9510F6FF8937C`）。
- Windows EXE：`JianpuPlayerNext-v1.0.0-beta.41.exe`（14,850,457 bytes，`D15F10A339D60BAA98804912E4A8594BE3DA6814D90264DB5C127E3EC3A54C13`）。
- 制谱器 APK：`artifacts/PocketMusic21-ScoreMaker-v0.1.0-240songs-debug.apk`（9,484,970 bytes，`26C72A3BC1F734E0F0BB297067C698E874049BA6A68A3284FB81699669BEF0FD`）。
- 构建门禁已通过；下一步只做真机验收及明日音源到手后的识别/精修。GitHub 暂不上传。
- 本轮新增 `song_226`–`song_239` 十四首经典候选，均为完整公开音频自动转谱候选、状态 `requires_in_game_audition`，不能标记 final。
- 《须弥》确认为网易《一梦江湖》（原《楚留香》手游）少林门派曲，已作为 `song_157` 保留在双端正式曲库，推荐节拍 511 ms/拍；不得再次误删。
- Android/Windows 曲库已同步为 240 首；`song_240` 为《尘外客》中文候选；跨端检查 240/240，缺失、独有、哈希/资源错误均为 0。GitHub 暂未上传。
- 第一批七首已完成并发来源筛查但全部暂时 `blocked`，证据在 `source_scores/batch_20260819_first_piano_*`；不要把批次目录中的动机/简化 lead TXT 直接导入正式曲库。取得完整音源后从这些目录续跑即可。

## 2026-08-19 最新接手点（录制输入层交接修复）

- 当前最终方案：快速点击 FIFO；捕获层临时摘除后注入游戏、24ms 复用恢复；捕获范围缩为21键区域；暂停/保存/展开/旋转/销毁均有代次与 handoff token 保护。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-213songs-recording-handoff-fix-debug.apk`，SHA-256 `E586EBBEE8547FF515A49F87EEC0A897DF0CD862B30485E0BFABD6F3597C473A`，9,806,370 bytes。
- Android 单测/Lint/assembleDebug 及最终只读并发审查通过；没有静态阻断项。下一门禁是真机同键20次、5/10 CPS、交替键和双指和弦。未上传 GitHub。

## 2026-08-19 最新接手点（录制排队修复）

- 已修复真机“录制计数但游戏少发声”：快速点击 FIFO 排队、窗口输入切换等待两个动画帧、缺键失败必回调、真实音符数与休止事件分开显示。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-213songs-recording-queue-fix-debug.apk`，SHA-256 `88DDD64DF0947992D1094684128D44F04B1FC928504B54B4A9C3E06EF5054613`，9,804,209 bytes。
- Android 单测/Lint/assembleDebug 已通过；下一步只做真机同键慢点、5/10 CPS、交替键、双指和弦验收。未上传 GitHub。

## 2026-08-19 最新接手点（213 首）

- Android/Windows 曲库已同步为 213 首；第 213 首为《記憶（缘之空）》、627 ms/拍，自动转写候选，需游戏内试听。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-213songs-kioku-debug.apk`，SHA-256 `520FAA898E507A76180246CE6FA1E80CF4A4D65D491C2E1CC264AD5B52EE5066`，9,801,275 bytes。
- 跨端 213/213、Android 单测/Lint/assembleDebug、Windows 26 项测试和 EXE 构建通过；GitHub 未上传。
- 录制低延迟与《肘我》标题修复仍包含在本构建中；真机验收缺口仍是游戏内录制手感和《記憶》听感。

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
- `肘我（江湖梦二创）` 独立于 `恕我`；三首都还是候选曲，等待游戏内试听定稿。当前运行时展示名称简化为 `肘我`，括号后缀只保留在来源留痕中。

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
