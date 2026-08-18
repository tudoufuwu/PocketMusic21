package com.shadowtrace.pocketmusic21.data

import com.shadowtrace.pocketmusic21.model.SongEvent

object SongParser {
    const val PLAYABLE_KEYS = "qwertyuasdfghjzxcvbnm"
    private val allowedKeys = (PLAYABLE_KEYS + "p").toSet()

    fun parse(text: String): List<SongEvent> {
        val events = mutableListOf<SongEvent>()
        text.removePrefix("\uFEFF").lineSequence().forEachIndexed { index, raw ->
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) return@forEachIndexed

            val parts = line.split(Regex("\\s+"))
            require(parts.size == 2) { "第 ${index + 1} 行格式错误，应为：按键 拍数" }
            val keys = parts[0].lowercase()
            val invalid = keys.filterNot { it in allowedKeys }.toSet().sorted()
            require(invalid.isEmpty()) { "第 ${index + 1} 行包含不支持的按键：${invalid.joinToString("")}" }
            require('p' !in keys || keys == "p") { "第 ${index + 1} 行休止符 p 不能组成和弦" }
            require(keys.toSet().size == keys.length) { "第 ${index + 1} 行存在重复按键：$keys" }
            val beats = parts[1].toDoubleOrNull()
                ?: throw IllegalArgumentException("第 ${index + 1} 行拍数不是数字：${parts[1]}")
            require(beats.isFinite() && beats > 0.0 && beats <= 64.0) {
                "第 ${index + 1} 行拍数必须大于 0 且不超过 64"
            }
            events += SongEvent(keys, beats)
        }
        require(events.isNotEmpty()) { "TXT 中没有可播放的音符事件" }
        return events
    }
}
