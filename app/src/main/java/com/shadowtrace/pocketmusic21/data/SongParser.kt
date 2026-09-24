package com.shadowtrace.pocketmusic21.data

import com.shadowtrace.pocketmusic21.model.SongEvent

object SongParser {
    const val PLAYABLE_KEYS = "qwertyuasdfghjzxcvbnm"
    private val allowedKeys = (PLAYABLE_KEYS + "p").toSet()
    private val trackMarker = Regex("\\[track\\s+([a-zA-Z]+)]")
    private val allowedTracks = setOf("main", "accomp")

    fun parse(text: String): List<SongEvent> {
        val names = mutableListOf("main")
        val tracks = mutableListOf(mutableListOf<SongEvent>())
        text.removePrefix("\uFEFF").lineSequence().forEachIndexed { index, raw ->
            val line = raw.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) return@forEachIndexed

            if (line.startsWith("[")) {
                val match = trackMarker.matchEntire(line)
                    ?: throw IllegalArgumentException("第 ${index + 1} 行无法识别的分轨标记：$line")
                val name = match.groupValues[1].lowercase()
                require(name in allowedTracks) { "第 ${index + 1} 行不支持的轨道名：$name" }
                val redeclaresFirst = name == "main" && names == listOf("main") && tracks[0].isEmpty()
                require(redeclaresFirst || name !in names) { "第 ${index + 1} 行轨道重复：$name" }
                if (!redeclaresFirst) {
                    names += name
                    tracks += mutableListOf<SongEvent>()
                }
                return@forEachIndexed
            }

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
            tracks.last() += SongEvent(keys, beats)
        }
        require(tracks.all { it.isNotEmpty() }) { "每条轨都至少需要一个音符事件" }
        return if (tracks.size == 1) tracks[0] else flattenTracks(tracks)
    }

    private fun flattenTracks(tracks: List<List<SongEvent>>): List<SongEvent> {
        val starts = sortedSetOf<Double>()
        val notes = mutableMapOf<Double, MutableList<String>>()
        var totalBeats = 0.0
        tracks.forEach { events ->
            var cursor = 0.0
            events.forEach { event ->
                starts += cursor
                if (!event.isRest) notes.getOrPut(cursor) { mutableListOf() } += event.keys
                cursor += event.beats
            }
            totalBeats = maxOf(totalBeats, cursor)
        }
        starts += totalBeats
        val points = starts.toList()
        return points.zipWithNext().mapNotNull { (start, end) ->
            val beats = end - start
            if (beats <= 0.0) return@mapNotNull null
            val keys = notes[start].orEmpty().flatMap { it.asIterable() }.distinct().joinToString("")
            SongEvent(keys.ifEmpty { "p" }, beats)
        }
    }
}
