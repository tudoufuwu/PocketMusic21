package com.shadowtrace.scoremaker21

import kotlin.math.roundToInt

data class ScoreEvent(val keys: String, val beats: Double)

data class ImportedScore(val events: List<ScoreEvent>, val recommendedBeatMs: Int?)

object ScoreCodec {
    const val PLAYABLE_KEYS = "qwertyuasdfghjzxcvbnm"
    private val allowedKeys = (PLAYABLE_KEYS + "p").toSet()
    private val beatComment = Regex("^#\\s*(?:推荐节拍|录制基准)\\s*[:：]\\s*(\\d+)\\s*ms/拍.*$")

    fun parse(text: String): ImportedScore {
        val events = mutableListOf<ScoreEvent>()
        var recommendedBeatMs: Int? = null
        text.removePrefix("\uFEFF").lineSequence().forEachIndexed { index, raw ->
            val line = raw.trim()
            beatComment.matchEntire(line)?.let {
                recommendedBeatMs = it.groupValues[1].toInt().coerceIn(100, 5000)
                return@forEachIndexed
            }
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) return@forEachIndexed
            val parts = line.split(Regex("\\s+"))
            require(parts.size == 2) { "第 ${index + 1} 行格式错误，应为：按键 拍数" }
            val keys = parts[0].lowercase()
            val invalid = keys.filterNot { it in allowedKeys }.toSet().sorted()
            require(invalid.isEmpty()) { "第 ${index + 1} 行包含不支持的按键：${invalid.joinToString("")}" }
            require('p' !in keys || keys == "p") { "第 ${index + 1} 行休止符 p 不能组成和弦" }
            require(keys.toSet().size == keys.length) { "第 ${index + 1} 行存在重复按键：$keys" }
            val beats = parts[1].toDoubleOrNull()
                ?: throw IllegalArgumentException("第 ${index + 1} 行拍数不是数字")
            require(beats.isFinite() && beats > 0.0 && beats <= 64.0) {
                "第 ${index + 1} 行拍数必须大于 0 且不超过 64"
            }
            events += ScoreEvent(keys, beats)
        }
        require(events.isNotEmpty()) { "TXT 中没有可编辑的音符事件" }
        return ImportedScore(events, recommendedBeatMs)
    }

    fun export(events: List<ScoreEvent>, beatMs: Int): String = buildString {
        append("# 推荐节拍: ${beatMs.coerceIn(100, 5000)} ms/拍\n")
        events.forEach { append("${it.keys} ${formatBeats(it.beats)}\n") }
    }

    private fun formatBeats(beats: Double): String =
        if (beats % 1.0 == 0.0) beats.toInt().toString() else beats.toString()

    private fun quantize(beats: Double): Double = (beats / 0.5).roundToInt() * 0.5
}

class ScoreRecorder(
    beatMs: Int = 600,
    private val chordWindowMs: Long = 90L,
) {
    enum class State { STOPPED, RECORDING, PAUSED }

    private data class Hit(val key: Char, val atMs: Long)
    private data class Group(val keys: String, val atMs: Long)

    private val committed = mutableListOf<ScoreEvent>()
    private val hits = mutableListOf<Hit>()
    var beatMs: Int = beatMs.coerceIn(100, 5000)
    var state: State = State.STOPPED
        private set
    private var startedAtMs = 0L
    private var pausedAtMs: Long? = null
    private var pausedTotalMs = 0L

    fun replace(events: List<ScoreEvent>) {
        require(state == State.STOPPED) { "请先停止录制" }
        committed.clear()
        committed += events
    }

    fun start(nowMs: Long) {
        if (state != State.STOPPED) return
        hits.clear()
        startedAtMs = nowMs
        pausedAtMs = null
        pausedTotalMs = 0L
        state = State.RECORDING
    }

    fun pause(nowMs: Long) {
        if (state != State.RECORDING) return
        pausedAtMs = nowMs
        state = State.PAUSED
    }

    fun resume(nowMs: Long) {
        if (state != State.PAUSED) return
        pausedTotalMs += (nowMs - requireNotNull(pausedAtMs)).coerceAtLeast(0L)
        pausedAtMs = null
        state = State.RECORDING
    }

    fun tap(key: Char, nowMs: Long): Boolean {
        if (state != State.RECORDING || key !in ScoreCodec.PLAYABLE_KEYS) return false
        hits += Hit(key, timelineAt(nowMs))
        return true
    }

    fun stop(nowMs: Long): List<ScoreEvent> {
        if (state == State.STOPPED) return committed.toList()
        val effectiveNow = if (state == State.PAUSED) requireNotNull(pausedAtMs) else nowMs
        if (state == State.PAUSED) pausedTotalMs += (effectiveNow - requireNotNull(pausedAtMs)).coerceAtLeast(0L)
        committed += compile(hits, timelineAt(effectiveNow), includeLeadingRest = committed.isEmpty())
        hits.clear()
        pausedAtMs = null
        state = State.STOPPED
        return committed.toList()
    }

    fun snapshot(nowMs: Long): List<ScoreEvent> {
        val effectiveNow = pausedAtMs ?: nowMs
        return committed + compile(hits, timelineAt(effectiveNow), includeLeadingRest = committed.isEmpty())
    }

    fun undo(nowMs: Long): Boolean {
        if (state != State.STOPPED && hits.isNotEmpty()) {
            hits.removeAt(hits.lastIndex)
            return true
        }
        if (state == State.STOPPED && committed.isNotEmpty()) {
            committed.removeAt(committed.lastIndex)
            return true
        }
        return false
    }

    fun deleteAt(index: Int): Boolean {
        if (state != State.STOPPED || index !in committed.indices) return false
        committed.removeAt(index)
        return true
    }

    fun clear() {
        committed.clear()
        hits.clear()
        state = State.STOPPED
        pausedAtMs = null
    }

    private fun timelineAt(nowMs: Long): Long =
        (nowMs - startedAtMs - pausedTotalMs).coerceAtLeast(0L)

    private fun compile(
        source: List<Hit>,
        endAtMs: Long,
        includeLeadingRest: Boolean,
    ): List<ScoreEvent> {
        if (source.isEmpty()) return emptyList()
        val groups = mutableListOf<Group>()
        source.forEach { hit ->
            val last = groups.lastOrNull()
            if (last != null && hit.atMs - last.atMs <= chordWindowMs && hit.key !in last.keys) {
                groups[groups.lastIndex] = last.copy(keys = last.keys + hit.key)
            } else {
                groups += Group(hit.key.toString(), hit.atMs)
            }
        }
        val result = mutableListOf<ScoreEvent>()
        if (includeLeadingRest) addRest(result, groups.first().atMs)
        groups.forEachIndexed { index, group ->
            result += ScoreEvent(group.keys, QUANTUM_BEATS)
            val nextAt = groups.getOrNull(index + 1)?.atMs ?: endAtMs
            addRest(result, (nextAt - group.atMs - beatMs * QUANTUM_BEATS).roundToInt().toLong())
        }
        return result
    }

    private fun addRest(target: MutableList<ScoreEvent>, durationMs: Long) {
        var beats = quantize(durationMs.toDouble() / beatMs)
        while (beats > 64.0) {
            target += ScoreEvent("p", 64.0)
            beats -= 64.0
        }
        if (beats >= QUANTUM_BEATS) target += ScoreEvent("p", beats)
    }

    private fun quantize(beats: Double): Double =
        ((beats / QUANTUM_BEATS).roundToInt() * QUANTUM_BEATS).coerceAtLeast(0.0)

    companion object {
        const val QUANTUM_BEATS = 0.125
    }
}
