package com.shadowtrace.pocketmusic21.automation

import com.shadowtrace.pocketmusic21.model.SongEvent

/** In-memory recorder for notes dispatched by the player; callers can export the snapshot as TXT. */
object RecordingSession {
    @Volatile var active: Boolean = false
        private set
    private val events = mutableListOf<SongEvent>()

    @Synchronized fun start() { events.clear(); active = true }
    @Synchronized fun stop(): List<SongEvent> { active = false; return events.toList() }
    @Synchronized fun append(keys: String, beatMs: Int, holdMs: Long) {
        if (active && keys.isNotBlank()) events += SongEvent(keys, (holdMs.toDouble() / beatMs).coerceAtLeast(0.01))
    }
    @Synchronized fun snapshot(): List<SongEvent> = events.toList()
}
