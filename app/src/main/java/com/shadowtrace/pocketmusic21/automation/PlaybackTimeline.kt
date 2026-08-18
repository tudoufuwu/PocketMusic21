package com.shadowtrace.pocketmusic21.automation

import com.shadowtrace.pocketmusic21.model.SongEvent

data class TimelineStep(
    val keys: String,
    val totalMs: Long,
    val holdMs: Long,
) {
    val isRest: Boolean get() = keys == "p"
}

object PlaybackTimeline {
    fun build(events: List<SongEvent>, beatMs: Int): List<TimelineStep> {
        require(beatMs in 50..5000)
        return events.map { event ->
            val total = (event.beats * beatMs).toLong().coerceAtLeast(1L)
            TimelineStep(
                keys = event.keys,
                totalMs = total,
                holdMs = if (event.isRest) 0L else (total * 0.45).toLong().coerceIn(24L, total),
            )
        }
    }
}
