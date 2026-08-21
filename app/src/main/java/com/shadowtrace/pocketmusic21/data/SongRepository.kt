package com.shadowtrace.pocketmusic21.data

import android.content.Context
import com.shadowtrace.pocketmusic21.model.ParsedSong
import com.shadowtrace.pocketmusic21.model.SongEntry
import org.json.JSONArray
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

    fun importedSongs(): List<SongEntry> {
        val file = context.getFileStreamPath(IMPORTED_FILE)
        if (!file.isFile) return emptyList()
        return runCatching {
            val items = JSONArray(file.readText(Charsets.UTF_8))
            buildList(items.length()) {
                repeat(items.length()) { index ->
                    val item = items.getJSONObject(index)
                    add(
                        SongEntry(
                            id = item.getString("id"),
                            title = item.getString("title"),
                            assetPath = null,
                            beatMs = item.optInt("beatMs", 700),
                            importedText = item.getString("text"),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun allSongs(): List<SongEntry> = bundledSongs() + importedSongs()

    fun saveImportedSongs(songs: List<SongEntry>) {
        val items = JSONArray()
        songs.forEach { song ->
            val text = song.importedText ?: return@forEach
            items.put(
                JSONObject().apply {
                    put("id", song.id)
                    put("title", song.title)
                    put("beatMs", song.beatMs)
                    put("text", text)
                },
            )
        }
        context.openFileOutput(IMPORTED_FILE, Context.MODE_PRIVATE).bufferedWriter(Charsets.UTF_8).use {
            it.write(items.toString())
        }
    }

    private companion object {
        const val IMPORTED_FILE = "imported_songs.json"
    }
}
