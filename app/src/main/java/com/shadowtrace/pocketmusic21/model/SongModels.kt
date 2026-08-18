package com.shadowtrace.pocketmusic21.model

data class SongEvent(val keys: String, val beats: Double) {
    val isRest: Boolean get() = keys == "p"
}

data class SongEntry(
    val id: String,
    val title: String,
    val assetPath: String?,
    val beatMs: Int,
    val sha256: String? = null,
    val importedText: String? = null,
)

data class ParsedSong(
    val entry: SongEntry,
    val events: List<SongEvent>,
) {
    val totalBeats: Double get() = events.sumOf { it.beats }
    val durationMs: Long get() = (totalBeats * entry.beatMs).toLong()
}
