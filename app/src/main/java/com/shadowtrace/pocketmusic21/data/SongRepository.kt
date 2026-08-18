package com.shadowtrace.pocketmusic21.data

import android.content.Context
import com.shadowtrace.pocketmusic21.model.ParsedSong
import com.shadowtrace.pocketmusic21.model.SongEntry
import org.json.JSONObject

class SongRepository(private val context: Context) {
    fun bundledSongs(): List<SongEntry> {
        val json = context.assets.open("library.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = JSONObject(json)
        val songs = root.getJSONArray("songs")
        return buildList(songs.length()) {
            repeat(songs.length()) { index ->
                val item = songs.getJSONObject(index)
                add(
                    SongEntry(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        assetPath = item.getString("asset"),
                        beatMs = item.getInt("beatMs"),
                        sha256 = item.optString("sha256").ifBlank { null },
                    ),
                )
            }
        }
    }

    fun parse(entry: SongEntry): ParsedSong {
        val text = entry.importedText ?: context.assets.open(requireNotNull(entry.assetPath))
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        return ParsedSong(entry, SongParser.parse(text))
    }
}
