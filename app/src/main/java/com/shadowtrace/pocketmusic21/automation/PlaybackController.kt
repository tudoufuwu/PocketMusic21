package com.shadowtrace.pocketmusic21.automation

import com.shadowtrace.pocketmusic21.model.ParsedSong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object PlaybackController {
    enum class State { EMPTY, READY, PLAYING, PAUSED, STOPPED, COMPLETED, ERROR }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var prepared: ParsedSong? = null
    private var beatMs: Int = 700
    private var job: Job? = null

    @Volatile var state: State = State.EMPTY
        private set
    @Volatile var message: String = "尚未选择曲谱"
        private set
    @Volatile var eventIndex: Int = 0
        private set

    val preparedTitle: String? get() = prepared?.entry?.title
    val totalEvents: Int get() = prepared?.events?.size ?: 0
    val currentBeatMs: Int get() = beatMs
    val hasPreparedSong: Boolean get() = prepared != null

    fun prepare(song: ParsedSong, beatMs: Int) {
        stop("已装入新曲谱")
        prepared = song.copy(entry = song.entry.copy(beatMs = beatMs))
        this.beatMs = beatMs
        state = State.READY
        eventIndex = 0
        message = "已选择：${song.entry.title}"
    }

    fun startFromOverlay(service: MusicAccessibilityService): Boolean {
        if (state == State.PAUSED) {
            state = State.PLAYING
            message = "继续播放：${preparedTitle.orEmpty()}"
            return true
        }
        if (job?.isActive == true) return true
        val song = prepared ?: run {
            state = State.ERROR
            message = "请先选择曲谱"
            return false
        }
        if (AppVisibility.mainActivityStarted) {
            state = State.READY
            message = "歌曲已就绪；切回游戏后从音乐球播放"
            return false
        }
        val activePackage = service.activePackageName()
        if (activePackage.isNullOrBlank() || activePackage == service.packageName) {
            state = State.ERROR
            message = "请切回游戏后再播放"
            return false
        }
        val timeline = PlaybackTimeline.build(song.events, beatMs)
        state = State.PLAYING
        message = "正在播放：${song.entry.title}"
        job = scope.launch {
            runCatching {
                for (index in eventIndex until timeline.size) {
                    while (state == State.PAUSED && isActive) delay(50)
                    if (state != State.PLAYING || !isActive) break
                    if (service.isDeviceLocked()) {
                        stop("锁屏，已安全停止")
                        break
                    }
                    val step = timeline[index]
                    if (!step.isRest) {
                        while (state == State.PLAYING && isActive &&
                            !dispatchAndAwait(service, step.keys, step.holdMs)
                        ) {
                            // Volume/charging/notification/rotation windows can temporarily make
                            // Android reject gesture injection. Wait and retry this note instead of
                            // turning a transient system event into a stopped song.
                            message = "系统界面暂时占用，等待继续：${song.entry.title}"
                            delay(GESTURE_RETRY_MS)
                            if (service.isDeviceLocked()) {
                                stop("锁屏，已安全停止")
                                break
                            }
                        }
                        while (state == State.PAUSED && isActive) delay(GESTURE_RETRY_MS)
                        if (state != State.PLAYING || !isActive) break
                        message = "正在播放：${song.entry.title}"
                    }
                    delay(if (step.isRest) step.totalMs else (step.totalMs - step.holdMs).coerceAtLeast(0L))
                    eventIndex = index + 1
                }
                if (eventIndex >= timeline.size && state == State.PLAYING) {
                    state = State.COMPLETED
                    message = "播放完成：${song.entry.title}"
                }
            }.onFailure {
                if (it !is CancellationException) {
                    state = State.ERROR
                    message = "播放错误：${it.message}"
                }
            }
        }
        return true
    }

    fun pause() {
        if (state == State.PLAYING) {
            state = State.PAUSED
            message = "已暂停：${preparedTitle.orEmpty()}"
        }
    }

    private suspend fun dispatchAndAwait(
        service: MusicAccessibilityService,
        keys: String,
        holdMs: Long,
    ): Boolean {
        val result = CompletableDeferred<Boolean>()
        service.dispatchKeys(keys, holdMs) { completed -> result.complete(completed) }
        return result.await()
    }

    fun stop(reason: String = "已停止") {
        job?.cancel()
        job = null
        eventIndex = 0
        if (prepared == null) state = State.EMPTY else state = State.STOPPED
        message = reason
    }

    private const val GESTURE_RETRY_MS = 50L
}
