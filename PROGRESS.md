# Android手机播放器独立续跑日志

## 2026-08-20 发布前最后新增《尘外客》（当前最新）

- 鸣潮先行公约官方 `BV1G48g68Ej1` p1 中文完整音频，演唱蔡明希（不才），实测 138.261 秒。
- 自动转谱候选推荐 441 ms/拍，共 286 events；Parser 往返、21键范围、时长覆盖和跨端 240/240 门禁通过，状态 `requires_in_game_audition`。
- 已加入双端 `song_240` 并完成 beta.41 EXE、240首主播放器 APK 和独立制谱器 APK 构建；GitHub 仍未上传。

## 2026-08-20 最后一波阻塞曲完成（历史记录）

- 最后一波5首阻塞曲已找到完整、可复核来源并加入 `song_235`–`song_239`：《万神纪》《光年之外》《演员》《追梦赤子心》《世间美好与你环环相扣》；均通过 Parser、21键范围、时长覆盖，状态 `requires_in_game_audition`。
- 来源：万神纪网易云 `459831628`；光年之外B站官方MV `BV1ws411Y7wi`；演员B站完整Hi-res `BV1i14y117hX`；追梦赤子心B站完整Hi-res `BV1i14y1D74W`；世间美好网易云 `1363948882`。
- 该批结束时正式曲库为239首、曲库版本174；之后《尘外客》已作为第240首加入。

## 2026-08-20 B站经典批次（历史记录）

- 在225首基线上新增 `song_226`–`song_234`：《普通DISCO》《达拉崩吧》《勾指起誓》《权御天下》《冠世一战》《神的随波逐流》《LOSER》《撒野》《unravel》；均采用完整B站音频自动转谱，Parser、21键范围和时长覆盖通过，状态统一为 `requires_in_game_audition`。
- 《万神纪》《光年之外》《演员》《追梦赤子心》《世间美好与你环环相扣》本轮没有完整可复核来源，保留独立 blocked 报告，没有生成伪谱。
- 来源和复现资料位于 `source_scores/batch_20260820_bili_classics_a/`、`..._b/`、`..._c/`；正式库同步脚本为 `tools/Import-Bili-Classics-20260820.ps1`。
- 当前阶段的234首构建记录保留在上一条历史记录中。

## 2026-08-19 B站完整音频重跑（历史记录）

- 重新走“B站正式完整音频 → Basic Pitch/pYIN → 单旋律聚类 → 21键映射 → 时值量化 → Parser/时长门禁”流程，新增 `song_222`–`song_225`：
  - 《生僻字》：B站 `BV1yeSnBWEsZ`，213.669 秒，500 ms/拍，477 events / 310 notes / 167 rests，估算 213.750 秒。
  - 《左手指月》：B站 `BV15f4y1m7gg`，179.583 秒，650 ms/拍，380 events / 299 notes / 81 rests，估算 179.725 秒。
  - 《无羁》：B站 `BV1Mt411E7cY`，251.286 秒，750 ms/拍，410 events / 296 notes / 114 rests，估算 251.250 秒。
  - 《归去来兮》：B站 `BV12gwGerECQ`，184.877 秒，857 ms/拍，419 events / 264 notes / 155 rests，估算 184.898 秒。
- 四首均通过 SongParser、21键范围、往返和时长覆盖检查，状态统一为 `requires_in_game_audition`；未声称人工 final。来源、音频、MIDI、报告和复现脚本分别在 `source_scores/batch_20260819_retry_bili_a/`、`..._b/`、`..._c/`。
- 《踏山河》再次尝试B站来源仍返回 HTTP 412，无法取得完整媒体字节，继续 `blocked`，没有伪造谱面。
- Android/Windows 曲库同步为225首；主播放器APK和Windows EXE均已完成通用“21键弹琴自动化”品牌重建与核验，GitHub仍未上传。
- 2026-08-20 复核：《须弥》是网易《一梦江湖》（原《楚留香》手游）少林门派曲；Android `song_157` 与 Windows《须弥.txt》均已保留。跨端 225/225，缺失、独有、哈希/资源错误均为 0。

## 2026-08-19 主播放器去录制 + 独立制谱器半成品（当前交付）

- 按最新方案拆成两个可共存 APK：主播放器不再包含游戏内录制、透明触摸捕获层或 `Recording*` 生产链；正式曲库现为 225 首，悬浮收缩态显示当前选曲、播放和停止。
- 播放按钮只启动当前选中的歌曲，同一首正在播放时不重复启动；停止只停止当前播放，不清除选曲；无播放时停止按钮禁用。
- 无障碍服务不再因音量、充电、旋转、通知等瞬时系统界面回调主动停止；移除主界面生命周期中会在旋转重建时停止播放的逻辑。锁屏、服务真正销毁和用户主动停止仍会停止，这是安全边界。
- 新增独立 `scoreMaker` 模块/包名 `com.shadowtrace.scoremaker21`：横屏自有 21 键，不向游戏注入触摸；支持开始、暂停/继续、停止、撤销、删除、清空、事件预览、近同时按键合并和弦、0.5 拍量化、`p` 休止及 SAF TXT 导入/保存。
- 制谱器 TXT 与播放器解析器兼容，保存内容包含 `# 推荐节拍: N ms/拍`，事件格式为 `q 0.5`、`qa 0.5`、`p 1` 等；当前只是手工制谱半成品，待明日取得游戏音源后再做音频识别和最终精修。
- 验证：`:app:testDebugUnitTest :app:lintDebug :app:assembleDebug` 成功；`:scoreMaker:testDebugUnitTest :scoreMaker:lintDebug :scoreMaker:assembleDebug` 成功，制谱器 5 tests / 0 failures / 0 errors；两个包的 manifest/applicationId 已静态核对。
- 主播放器 APK：`artifacts/PocketMusic21-v0.1.0-225songs-no-recording-debug.apk`，10,164,008 bytes，SHA-256 `445AFA23845F58642B71B1AC484687D6AE009E2D2E023236A27F137B79B5E15E`；桌面副本 `C:\Users\rmb\Desktop\21键手机播放器-v0.1.0-225首无录制版.apk`。
- Windows EXE：`..\music_player_next\dist\JianpuPlayerNext-v1.0.0-beta.38.exe`，14,835,919 bytes，SHA-256 `8E242CD77A7CC1C5B901E323F1F47774387178E007C05AA759BA1113A338B4D6`，曲库版本 172；桌面副本 `C:\Users\rmb\Desktop\简谱播放器-beta.38-225首.exe`。
- 制谱器 APK：`artifacts/PocketMusic21-ScoreMaker-v0.1.0-half-debug.apk`，9,484,970 bytes，SHA-256 `26C72A3BC1F734E0F0BB297067C698E874049BA6A68A3284FB81699669BEF0FD`；桌面副本 `C:\Users\rmb\Desktop\21键手机制谱器-v0.1.0-半成品.apk`。
- GitHub 仍未上传；真机验收仍需在目标手机确认悬浮窗播放/停止、旋转/充电/通知连续播放，以及制谱器实际点击和 TXT 导入主播放器。

## 2026-08-19 B站经典候选池（筛选前历史快照）

- 当时已与 219 首 `library.json` 去重。以下是筛选前候选快照，不代表全部已经制作或达到 final；其中《浪人琵琶》《云与海》后续已作为 220、221 首候选入库。
- 第一优先（旋律清楚、钢琴/MIDI 资料较多、适合 21 键快速做高质量版）：《夜的钢琴曲五》《River Flows in You》《Kiss the Rain》《忧伤还是快乐》《The Truth That You Leave》《梦中的婚礼》《风居住的街道》。
- 第二优先（B站长期经典、传唱度高，需从完整原曲提取人声主旋律并试听）：《生僻字》《踏山河》《左手指月》《无羁》《归去来兮》《浪人琵琶》《云与海》《世间美好与你环环相扣》《追梦赤子心》《如果有来生》。
- 动漫/二创经典（热度高但节奏或版本较容易混淆）：《secret base ～君がくれたもの～》《unravel》《夜に駆ける》《怪物》《花之塔》《花に亡霊》《ただ君に晴れ》。
- 近期高热但不作为第一批：`Bling-Bang-Bang-Born`、`可愛くてごめん`、`大风吹`、`骁`、`万疆`、`光年之外`、`演员`、`年少有为`、`可惜没如果`。
- 制作门槛：先锁定完整版本和时长，再保留来源/版本报告；音频转谱后做 21 键映射、半拍量化、密度/时长/休止检查、播放器解析和跨端同步；没有完整来源就只写阻塞记录，不伪造曲谱。

## 2026-08-19 第一批七首并发来源筛查（阻塞，未入库）

- 已并发检查《夜的钢琴曲五》《River Flows in You》《Kiss the Rain》《忧伤还是快乐》《The Truth That You Leave》《梦中的婚礼》《风居住的街道》。
- 当前结果：3 首只有开头动机、2 首是未能证明完整时长的简化 lead 候选、2 首 YouTube 版本遇到 429/登录限制；全部保持 `blocked`，没有改动 219 首正式曲库，也没有重建 APK。
- 保留证据目录：`source_scores/batch_20260819_first_piano_core/`、`source_scores/batch_20260819_first_piano_b/`、`source_scores/batch_20260819_first_piano_c/`、`source_scores/batch_20260819_first_piano_eop/`。EOP 公开谱页/下载响应已记录，但未完成逐音符复核的页面不作为完整 TXT 来源。
- 阻塞报告明确禁止把 16 音符动机或 48.5/58.5 秒简化 lead 冒充成完整歌曲；待取得可离线复核的完整音源或公开完整谱面后继续，随后再统一编号、跨端同步、测试和打包。

## 2026-08-19 爆种复跑与只读审查结果

- 已重新启动并发来源恢复、公开谱搜索和只读质量审查；审查结论仍是 7/7 `blocked`，可放行进入正式曲库的歌曲为 0。
- 《River Flows in You》新增公开 EOP 三页高清 PNG 证据（1=A、4/4、♩=66、Page 1/Total 3），但还没有逐音符转写，因此仍不生成完整 TXT。
- 修正 `source_scores/batch_20260819_first_piano_eop/recovery_report.json`： 《梦中的婚礼》页面显示的 159 秒改记为 `display_duration_seconds`，真实 `duration_seconds` 保持 `null`，避免下游误放行。
- 本轮没有新增正式曲目、没有改动 219 首 manifest、没有重建 APK；等完整音源/可复核谱面到手后从批次目录继续，不需要重新查找。

## 2026-08-19 下一批建议（历史计划，已执行）

- 为避开上一批的纯钢琴来源阻塞，下一批建议优先调查当前曲库没有的 7 首：`生僻字`、`踏山河`、`左手指月`、`无羁`、`归去来兮`、`浪人琵琶`、`云与海`。
- 通过来源门禁后，预定编号为 `song_220`–`song_226`；只在完整版本、时长、结构和 Parser 验证全部通过后同步 Android/Windows 并重建 APK。
- GitHub 尚未上传，开源前可以继续追加曲目；每批会同时更新 `library.json`、Windows `builtin_songs`、来源报告、`PROGRESS.md`、`HANDOFF.md` 和发布交接文件。

## 2026-08-19 下一批七首并发制作（本轮完成）

- 本轮 2 首通过来源、结构、Parser 和跨端门禁并正式入库：`song_220`《浪人琵琶（胡66）》、`song_221`《云与海（YueYue）》。两首均为完整公开音频自动转谱候选，状态保持 `requires_in_game_audition`，游戏内试听前不能称为 final。
- 《浪人琵琶（胡66）》来源为 Bilibili AV29100379，完整音频 224.607 秒，530 events / 466 notes / 64 rests，推荐 650 ms/拍，估算 224.575 秒。
- 《云与海（YueYue）》来源为网易云公开 Cover（歌曲 ID 3406250057），完整音频 238.976 秒，448 events / 447 notes / 1 rest，推荐 1000 ms/拍，估算 250 秒。
- 其余 5 首严格保持 `blocked`，未入库：《生僻字》《踏山河》《左手指月》受 YouTube 无登录反机器人验证阻挡，未取得完整可复核输入；《无羁》《归去来兮》已有完整音频和公开谱图，但谱图尚未逐音符文字化。
- 证据分别保留在 `..\source_scores\batch_20260819_next_cn_a\`、`..\source_scores\batch_20260819_next_cn_b\`、`..\source_scores\batch_20260819_next_cn_c\`。Android/Windows 曲库已同步为 221 首，跨端 221/221、Windows 26 项测试及 Android 单测/Lint/assembleDebug 均通过。
- GitHub 仍未上传；最新主播放器无录制 APK 为 `artifacts/PocketMusic21-v0.1.0-221songs-no-recording-debug.apk`。

## 2026-08-19 真机录制输入层交接修复（最终测试构建）

- 在 FIFO 修复基础上进一步撤销 `FLAG_NOT_TOUCHABLE` 异步切换：每次命中琴键后用 `removeViewImmediate` 临时摘除同一个捕获 View，下一主循环向游戏派发，24ms 后复用原 View 恢复；避免 Android 把“已完成”的注入手势仍投到透明悬浮层。
- 捕获层从全屏缩为21键校准点的包围区域；上半屏和键盘区域之外的游戏操作不再被录制层吞掉。包围区域内部的空白仍属于录制捕获范围。
- 新增 `handoffId + recordingGeneration + destroyed` 三重失效保护；暂停、保存、删除、展开、旋转、服务销毁期间的旧延迟任务不会复活悬浮层或派发幽灵音。
- 录制中展开主面板会明确暂停并移除捕获层；收起后由用户点“继续录制”，不再出现展开后捕获层永久失效。
- 手势 in-flight 时暂停/保存/删除按钮临时禁用，防止游戏已经发声但保存快照漏掉最后一个回调中的音符；旋转则在队列空闲后按新 bounds 重建捕获层。
- 最终只读复审未发现静态卡死或幽灵恢复缺陷；FIFO 2/2、RecordingSession 7/7、Android 单测/Lint/assembleDebug 全部通过。
- 最终 APK：`artifacts/PocketMusic21-v0.1.0-mvp-213songs-recording-handoff-fix-debug.apk`，9,806,370 bytes，SHA-256 `E586EBBEE8547FF515A49F87EEC0A897DF0CD862B30485E0BFABD6F3597C473A`；桌面副本 `21键手机播放器-v0.1.0-录制交接修复测试版.apk`。
- 仍需真机确认：24ms 摘除窗口、OEM 输入调度、长按/多指流没有公开系统级完成屏障，静态测试不能保证每台手机零丢触摸；验收重点为同键20次、5/10 CPS、交替键、双指和弦。GitHub 未上传。

## 2026-08-19 真机录制“计数但少发声”修复

- 根据真机反馈确认根因不在谱面：录制捕获层消费物理触摸后，`recordingDispatchInFlight` 会静默丢弃上一手势完成前的新点击；快速连点因此可能只向游戏派发少量手势。
- `FLAG_NOT_TOUCHABLE` 原先只经过两次普通主线程 `post` 就开始注入，这两次任务可能仍在同一帧；Android 会把手势回送到透明录制层并报告 `onCompleted`，造成“左上角记了、游戏没声音”。现等待两个动画帧，确保 WindowManager 的输入窗口切换完成后再注入。
- 新增 FIFO 录制派发队列：48ms 按住改为 64ms，快速触摸保留原始捕获时间、串行向游戏注入，不再因 in-flight 直接丢弃；Android 接受手势后立即恢复捕获层，缩短录制盲区。
- 修复校准缺键时 `dispatchKeys` 直接返回且不回调，导致录制 gate 永久卡死的问题；失败会正常回调并按既有策略重试一次。
- 左上角/收起按钮现在显示真实“音符数”，展开状态同时显示“音符/事件”；自动休止符不再被误看成游戏发声次数。
- 新增 FIFO 快速三连与清队列测试、休止符不计入发声音符测试；Android `testDebugUnitTest`、Lint、assembleDebug 全部通过。
- 新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-213songs-recording-queue-fix-debug.apk`，9,804,209 bytes，SHA-256 `88DDD64DF0947992D1094684128D44F04B1FC928504B54B4A9C3E06EF5054613`；桌面副本 `21键手机播放器-v0.1.0-录制排队修复测试版.apk`。
- 真机仍需验收：同一键慢点 20 次、5 CPS/10 CPS 连点、相邻键交替、双指和弦；计数应按音符增加且游戏每次有声。GitHub 未上传。

## 2026-08-19 曲库 213 首同步：《記憶（缘之空）》

- 曲目身份核验为《ヨスガノソラ》原声第 25 轨 `ヨスガノソラ メインテーマ-記憶-`（M5），Bruno Wen-li 作曲，原录音 3:00；它是纯音乐主主题配乐，不是 OP/ED 演唱曲，也不是 `Old Memory`。
- 完整 180.373 秒音频经 Basic Pitch、单旋律聚类、21 键自然音阶映射与半拍量化生成候选：354 个事件，其中 301 个音符事件；估算 180.263 秒，和原音频相差 0.111 秒；parser round-trip 与键位检查通过。
- Android 新增 `song_213.txt`《記憶（缘之空）》、推荐 627 ms/拍；Windows 同名 TXT 和推荐节拍同步，曲库升级为 213 首。
- 状态保持 `requires_in_game_audition`，需游戏内试听后才能标记人工 final；来源与身份报告在 `source_scores/yosuga_memory_20260819/`，原始音频/MIDI 不进入 GitHub。
- 跨端门禁 213/213、Windows 26 项测试、Android `testDebugUnitTest`/`lintDebug`/`assembleDebug`、APK 内部 213 首资源复核全部通过。
- APK：`artifacts/PocketMusic21-v0.1.0-mvp-213songs-kioku-debug.apk`，9,801,275 bytes，SHA-256 `520FAA898E507A76180246CE6FA1E80CF4A4D65D491C2E1CC264AD5B52EE5066`；桌面副本 `21键手机播放器-v0.1.0-213首-记忆测试版.apk`。
- Windows EXE：`music_player_next/dist/JianpuPlayerNext-v1.0.0-beta.38.exe`，14,824,040 bytes，SHA-256 `3CBA2EA2DAF5B538D9A7DB3DF7F6695DC9857F2D020552227E5D924D2994A478`；桌面副本 `简谱播放器-beta.38-213首-记忆测试版.exe`。
- GitHub 未上传；继续遵守用户的发布门禁。

## 2026-08-19 曲库 212 首同步

- 新增《勇气》《暖暖》《遇见》《我怀念的》四首来源可追溯的音频转写候选，Android manifest/assets 与 Windows `builtin_songs` 同步为 212 首。
- 四首均通过 `player_core.parse_song`，状态保持 `requires_in_game_audition`；未声称 final。
- 修正《光るなら》推荐节拍 375ms、《残酷天使的行动纲领》525ms；谱面哈希未改变。
- 跨端门禁 212/212、Android 单测、Lint、assembleDebug 全部通过。
- APK：`artifacts/PocketMusic21-v0.1.0-mvp-212songs-recording-fix-debug.apk`，9,800,001 bytes，SHA-256 `D9D671DA7E9F82184EBD5F3BB283EE0E076DE4597F22090A47649F9CFAF46DD6`；不上传 GitHub。

## 2026-08-19 悬浮录制体验修复

- 展开面板的歌曲列表按横屏可用高度动态缩短，并在实际测量布局后重新限位，修复底部按钮被屏幕裁掉。
- 收起面板后，音乐球旁直接显示“录制”；录制中改为“暂停/继续”“保存”“删除”，并实时显示事件数量，不需要重新展开大面板。
- 结束录制后在悬浮层询问文件名并直接保存 TXT；Android 10+ 写入 `下载/PocketMusic21/`，取消或保存失败会保留录制内容。
- 新增透明 21 键触摸录制层：手指点击已校准键位时由无障碍手势转发到游戏并写入录制；暂停时移除触摸层，继续时恢复。
- 录制事件现在保留派发事件之间的时间间隔，并按四分之一拍量化为 `p` 休止符，避免导出后节奏丢失。
- 暂停录制不会采集事件，继续时不会把暂停时长错误写成长休止；只有无障碍手势被系统接受后才写入录制。
- 音量、充电、旋转和通知不再直接覆盖游戏焦点；使用交互窗口判断、SystemUI 过滤和 1.2 秒焦点缓冲，真正切出游戏才停止。
- 录制与导出新增 7 项单测；`testDebugUnitTest lintDebug assembleDebug` 全部通过。
- 最新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-debug.apk`，9,794,743 bytes，SHA-256 `6EC7FF596EFB02EA7E6624830A6A3850F6FE1B78BA860992E75E43264EE9E77F`。
- 录制范围仍是播放器通过无障碍服务实际派发的21键事件，不采集麦克风音频，也不伪称能读取游戏内部原始触摸。

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

## 2026-08-19 鸟/霜/盗/风/年轮/寄明月批次校准

- 六首批次最终完成：`song_214`–`song_219` 分别为《盗将行》《是风动》《鸟之诗》《年轮》《霜雪千年》《寄明月》，Android/Windows 曲库升至 219 首。
- 《霜雪千年》采用 COPY 原作者账号 2015 原版 `BV1es41127Fd`，240.917 秒；855 events、418 ms/拍。《寄明月》采用 SING 女团官方正式 MV `BV1gx411g7qG`，231.723 秒；744 events、418 ms/拍。来源证据与报告保存在 `source_scores/batch_20260819_frost_moon_search/`。
- 最终 219/219 跨端门禁、Windows 26 项测试、Android `testDebugUnitTest`/`lintDebug`/`assembleDebug` 全部通过；六首都保留 `requires_in_game_audition`，不能在真机试听前称人工 final。
- 最终 APK：`artifacts/PocketMusic21-v0.1.0-mvp-219songs-recording-handoff-fix-debug.apk`，9,806,796 bytes，SHA-256 `CB6B672D866F6A27E0FAFFB2875453C233DFCB80685A5235052EF46FC06705DE`；桌面同名 219 首测试版已复制。
- 最终 Windows EXE：`music_player_next/dist_219/JianpuPlayerNext-v1.0.0-beta.38.exe`，14,829,508 bytes，SHA-256 `F1CFCDA68F456CA22AC8607A8D28F735866F7557D54A61A42E02C686400D2425`；桌面 `简谱播放器-beta.38-219首测试版.exe` 已复制。
- 后续换用可下载 B 站完整来源，新增《鸟之诗》《年轮》：`BV1gs411f73s`（368.107 秒）与 `BV1F341197ER`（274.560 秒）。两首完成 Basic Pitch、21键映射、解析回读并同步为 `song_216`/`song_217`，曲库升至 217 首。
- 《鸟之诗》869 events、487 ms/拍；《年轮》849 events、418 ms/拍；均为自动转谱候选，状态 `requires_in_game_audition`。
- 217/217 跨端门禁、Windows 26 项测试、Android 单测/Lint/assembleDebug 全部通过。
- 217 首 APK：`artifacts/PocketMusic21-v0.1.0-mvp-217songs-recording-handoff-fix-debug.apk`，9,806,654 bytes，SHA-256 `2582BDE867381C6B96B3DF27B432761A709D84502016ED7D513A417737822BED`。
- Windows 原 `dist` EXE 被运行中的进程锁定，未强制终止用户程序；新 217 首 EXE 输出到 `music_player_next/dist_217/JianpuPlayerNext-v1.0.0-beta.38.exe`，14,828,397 bytes，SHA-256 `80577B99CAE255D1EB39C3E27032D6CEA4923BBF3321FE26BDBC9A9DE4233190`。
- 《盗将行》《是风动》完整网易云公开条目音频已落盘，Basic Pitch MIDI、单旋律聚类、21键映射和 `player_core.parse_song` round-trip 均通过；候选报告保存在 `source_scores/batch_20260819_thief_wind/`，状态仍为 `requires_in_game_audition`。
- 两首已统一同步 Android `song_214`/`song_215` 与 Windows `builtin_songs/盗将行.txt`、`是风动.txt`；Android/Windows 曲库现为 215 首，跨端门禁通过（缺失、桌面独有、哈希/manifest 错误均为 0）。
- 《霜雪千年》《寄明月》的 YouTube 来源曾被 HTTP 429/登录验证阻塞，最终已改用版本明确的 B 站完整正式来源完成，未使用片段。
- Windows 26 项测试通过；Android `testDebugUnitTest`、`lintDebug`、`assembleDebug` 通过。GitHub 仍未上传。

## 2026-08-19 录制保存修复（进行中）

- 复核用户现场反馈：悬浮录制点击“存”后无可见反馈，不能按“未点到键位”处理。
- 修复保存链路：停止录制时固定待保存事件快照；保存弹窗不再依赖会话后续刷新；保存中禁用重复点击；成功/失败/空录制均以悬浮 Toast 明确反馈。
- 录制 TXT 仍由 `RecordingExporter` 写入，只有成功写入后才清理会话，失败或取消保留内容可重试。
- 已按真机反馈撤回“点击浮标反复展开/缩进”的快捷栏版本，恢复稳定的原悬浮操作；旧测试 APK `29E88...` 不再作为有效版本。
- 录制无声根因：全屏透明录制层会截获游戏触摸，合成手势也会再次落到透明层。现改为先收集约 28ms 的单音/和弦，再临时移除录制层，将手势派发到游戏；完成或取消后恢复录制层和控制按钮。
- 设备验收重点：21键逐键均应让游戏发声并增加录制事件；2/3指和弦只记一个组合事件；暂停时直接操作游戏但不录制；旋转/音量/通知后录制层仍能恢复。
- 构建门禁通过：`testDebugUnitTest`、`lintDebug`、`assembleDebug`；0 errors（仅既有国际化警告）。
- 新测试 APK：`artifacts/PocketMusic21-v0.1.0-mvp-recording-touch-fix-debug.apk`，9,797,944 bytes，SHA-256 `2BABF16B68AAE6E683E1464E5AB100438F2681B785264B9DC078548A4EB72389`；桌面副本为 `21键手机播放器-v0.1.0-录制触摸修复测试版.apk`。
- 该 APK 已完成静态构建验证，仍需真机游戏内确认“逐键发声、和弦、快速连点、暂停直通”四项；未覆盖原稳定 APK，未上传 GitHub。

## 2026-08-19 曲库198同步

- 新增 `song_196`《天空之城（君をのせて）》、`song_197`《Summer》、`song_198`《青鸟》；均来自独立 MIDI 来源并通过 parser round-trip，状态 `requires_in_game_audition`。
- Windows/Android 跨端门禁：198/198，缺失0、桌面独有0、哈希不一致0、manifest/asset错误0。
- 最新 Android APK：`artifacts/PocketMusic21-v0.1.0-mvp-198songs-recording-fix-debug.apk`，9,799,007 bytes，SHA-256 `E66125E59C21AB1FDA11F4DC70F6087E4DC81D94B6534ACFD17983C36B49A132`。
- 旧录制修复仍保留：录制层临时移开后把点击派发到游戏，再恢复录制层；GitHub 仍未上传。

## 2026-08-19 回滚快捷收缩并修复游戏无声

- 按用户实测回滚“点击浮标再展开快捷控制”的上一轮 UI：恢复上一版常驻的小型录制控制，不再一两次点击后把按钮缩回。
- 确认游戏无声根因：全屏可触摸录制层吞掉真实点击，随后注入手势仍可能命中该透明层。现在命中键位后先撤下捕获层，下一主循环把手势派发到游戏，仅在系统回调 `onCompleted` 后写入录制事件，再按正确窗口顺序恢复捕获层和按钮。
- 增加录制代次保护：暂停、保存、删除、重新录制或服务销毁发生在手势途中时，旧回调不会追加事件或复活录制层。
- 播放不再因焦点窗口变化停止；旋转、充电、音量面板和微信通知等临时界面若使手势暂时被拒绝，会等待并重试当前音符。仍会在锁屏、无障碍服务关闭、用户主动停止时停止。
- 全量门禁已通过：`testDebugUnitTest`、`lintDebug`、`assembleDebug`。
- 自动播放进一步改为等待 `dispatchGesture` 的完成回调；Android 先接受后取消的手势也会重试同一音符，并扣除已执行的按住时长，避免节奏被重复延长。
- 最终测试 APK：`artifacts/PocketMusic21-v0.1.0-mvp-rollback-recording-fix-debug.apk`，9,798,794 bytes，SHA-256 `855546574809966867CD475B3CD3F176575F728D225394A1AAF15F2592591369`；桌面副本为 `21键手机播放器-v0.1.0-回滚录制修复测试版.apk`。
- 当前没有连接的 ADB 设备，无法代替用户验证真实游戏声音；下一门禁是游戏内单音有声、事件数增加、保存 TXT 可命名，以及播放中触发旋转/充电/音量/微信通知仍继续。

## 2026-08-19 208 首录制修复测试构建

- Android/Windows 跨平台曲库门禁通过：manifest 208 首、桌面 `builtin_songs` 208 首；缺失、桌面独有、哈希/资源和 manifest 错误均为 0。
- `testDebugUnitTest`、`lintDebug`、`assembleDebug` 全部通过。
- 新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-208songs-recording-fix-debug.apk`，9,799,717 bytes，SHA-256 `439E613862908FCC5ED9B9E6C6D4AAC0490F57570F11D744C7108B4EC675A6C4`；桌面副本为 `21键手机播放器-v0.1.0-208首录制修复测试版.apk`，同大小与哈希。
- APK 内确认 `library.json` count=208 且包含 208 个 `assets/songs/song_*.txt`；未覆盖旧稳定 APK。

## 2026-08-19 212 首当前校准结果

- 当前共享工作区实际曲库为 212 首：Android `library.json`、Android TXT 资源、Windows `builtin_songs` 均为 212；跨平台门禁结果为缺失 0、桌面独有 0、哈希/资源不匹配 0、manifest/asset 错误 0。
- 当前构建门禁已通过：`testDebugUnitTest`、`lintDebug`、`assembleDebug`。
- 当前 APK：`artifacts/PocketMusic21-v0.1.0-mvp-212songs-recording-fix-debug.apk`，9,800,001 bytes，SHA-256 `D9D671DA7E9F82184EBD5F3BB283EE0E076DE4597F22090A47649F9CFAF46DD6`；桌面副本：`21键手机播放器-v0.1.0-212songs-recording-fix-debug.apk`。
- `PUBLISH_HANDOFF.md` 已同步到 212 首和当前 APK；GitHub 仍未上传。真机游戏内声音/旋转/通知验收仍不能由无设备的静态构建代替。

## 2026-08-19 全曲库节拍与结构审计

- 修复《嗵嗵》手机端旧谱/错误节拍：`song_043.txt` 已替换为原始 547 事件候选，推荐节拍 `700 → 493 ms`；Android/Windows 谱面哈希重新一致。
- 依据原始制作报告修复 8 首错误默认节拍：Daisy Crown（日文版）`757`、发如雪 `1083`、自无垠处归航之星 `874`、不老梦 `800`、牵丝戏 `800`、童话镇 `857`、群青 `441`、锦鲤抄 `800`；电脑端推荐节拍表同步。
- 全 212 首完整性门禁：缺失、重复 ID/标题/asset、manifest 哈希错误、非法键、非法/非正时值、空谱均为 0；Android/Windows 规范化内容差异为 0。
- 仍需回源重转的高置信结构异常候选：`song_205`《平凡之路》、`song_203`《童话》、`song_202`《小幸运》、`song_170`《新宝岛》、`song_206`《追光者》、`song_168`《念张师DJ版》、`song_201`《超级马力欧地面主题》、`song_007`《Running For Your Life》、`song_186`《东风破》。这些问题不是改速度能解决，本轮不伪造修复。
- 验证通过：Android/Windows 跨端门禁 212/212、Android `testDebugUnitTest`/`lintDebug`/`assembleDebug`、Windows 26 tests + 235 subtests。
- 新测试 APK：`artifacts/PocketMusic21-v0.1.0-mvp-212songs-tempo-audit-debug.apk`，9,800,001 bytes，SHA-256 `ECF4DD6A93E4FBE29EA5E2BAE455E1B07DEC759604039BFC49E85E18B58E5A63`；桌面副本为 `21键手机播放器-v0.1.0-212首节拍修复测试版.apk`。GitHub 未上传。

## 2026-08-19 九首异常谱面重制（进行中）

- 用户确认《一梦惊鸿》《弱水三千》《我，江湖！》《长天雪满》试听正常，保持现状。
- 爆种模式分三组重制：A《平凡之路》《童话》《小幸运》；B《新宝岛》《追光者》《念张师DJ版》；C《超级马力欧地面主题》《Running For Your Life》《东风破》。
- 并发产物只写入 `source_scores/audit_redo_20260819/group_*`，不直接覆盖正式曲库；必须通过来源、完整时长、密度、解析和旧版对比门禁后，由主线统一同步 Android/Windows。
- 已通过门禁并同步 8 首新候选：平凡之路 584 音符、童话 380、小幸运 531、新宝岛 837、追光者 428、念张师DJ版 242、超级马力欧地面主题 462、Running For Your Life 428；Android/Windows 212/212 内容门禁通过。
- 《东风破》第一轮仍阻塞：本地 WAV 仅 44.547 秒，不能用残段冒充完整歌曲；正在继续查找完整、可追溯来源。

## 2026-08-19 九首异常谱面重制完成

- 《东风破》已找到完整 B 站来源 `BV1iL411j7BU`（av464977718），本地工作音频 310.869 秒；完整音频经 Basic Pitch 转写、单旋律聚类和 21 键映射，生成 777 个音符事件，覆盖至 99.3%，估算时长 310.828 秒。
- 九首均已同步 Android/Windows：平凡之路、童话、小幸运、新宝岛、追光者、念张师DJ版、超级马力欧地面主题、Running For Your Life、东风破；四首用户确认正常的《一梦惊鸿》《弱水三千》《我，江湖！》《长天雪满》未改动。
- 最终跨端门禁：Android 212 首、Windows 212 首；缺失、桌面独有、哈希/资源、manifest 错误均为 0。Android `testDebugUnitTest`、`lintDebug`、`assembleDebug` 通过；Windows 26 tests + 235 subtests 通过。
- 最终 APK：`artifacts/PocketMusic21-v0.1.0-mvp-212songs-redo9-debug.apk`，9,800,001 bytes，SHA-256 `A04F72D355CA801756D4A1C37FA69C4911D314911A82C9EF634B8FE7F40B125A`；桌面副本：`21键手机播放器-v0.1.0-212首重制9曲测试版.apk`。
- Windows EXE：`music_player_next/dist/JianpuPlayerNext-v1.0.0-beta.38.exe`，14,822,471 bytes，SHA-256 `8DF7092EA0B55F85FB252DB24C26A04ECFEB6FAF395EC7FF254537248003A769`；桌面副本：`简谱播放器-beta.38-212首重制9曲.exe`。
- 九首仍按音频/MIDI候选管理，需用户在游戏内逐首试听后才能标记人工 final；GitHub 未上传。

## 2026-08-19 录制低延迟与《肘我》标题修复完成

- 录制单击卡顿根因：每次触摸都移除捕获层、派发手势、重建整套悬浮控件再恢复捕获层，90ms 手势期间形成明显盲区。现保留浮标与捕获层实例，派发期间仅把捕获层原地切成 `NOT_TOUCHABLE`，回调后恢复；仅在厂商 WindowManager 拒绝原地更新时才安全重建。
- 单音/和弦聚合等待 `28ms → 16ms`，手势按住 `90ms → 48ms`；手势被系统临时取消时，在捕获层仍移除的状态下快速重试一次，只在 `onCompleted` 后记入谱面。
- 《肘我（江湖梦二创）》运行时显示统一为《肘我》；Android `song_164`、Windows 文件名和推荐节拍键已同步，仍与《恕我》保持独立。
- 修复 Android 曲库单测仍硬编码 195 首的问题，更新为当前 212 首；跨端检查 212/212、Windows 26 tests + 235 subtests、Android 单测/Lint/assembleDebug 全部通过。
- 新 APK：`artifacts/PocketMusic21-v0.1.0-mvp-212songs-recording-low-latency-title-fix-debug.apk`，9,801,204 bytes，SHA-256 `79DFDF849E973DABE97DB7AA1D18D8D96E3E5BF7BD26545C194C245056A60BE3`；桌面副本：`21键手机播放器-v0.1.0-录制低延迟-肘我标题修复测试版.apk`。
- Windows EXE：`music_player_next/dist/JianpuPlayerNext-v1.0.0-beta.38.exe`，14,820,525 bytes，SHA-256 `AA98307ADC0038AE5584B4C5AE84CF2CAE5882B7BC83815EB5CBB491FE5CEF86`；桌面副本：`简谱播放器-beta.38-录制低延迟-肘我标题修复.exe`。
- 《群青》复核：完整来源 148.587 秒，441ms/拍正确；第一颗可靠旋律音在 11.443 秒，当前 `p 26` 对应 11.466 秒，属于原曲伴奏前奏而非节拍错误，本轮不重跑覆盖。
- 真机仍需验证：单点 20 次、5/10 CPS 连点、双指和弦、录制保存 TXT 后重新导入播放。GitHub 未上传。
