package com.shadowtrace.pocketmusic21.data

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SongParserTest {
    @Test
    fun parsesNotesChordsRestsCommentsAndBom() {
        val events = SongParser.parse("\uFEFF# title\nq 1\nas 0.5\np 2\n// end")
        assertEquals(3, events.size)
        assertEquals("as", events[1].keys)
        assertTrue(events[2].isRest)
    }

    @Test
    fun rejectsMalformedInput() {
        assertFailsWith<IllegalArgumentException> { SongParser.parse("ap 1") }
        assertFailsWith<IllegalArgumentException> { SongParser.parse("aa 1") }
        assertFailsWith<IllegalArgumentException> { SongParser.parse("k 1") }
        assertFailsWith<IllegalArgumentException> { SongParser.parse("q 0") }
        assertFailsWith<IllegalArgumentException> { SongParser.parse("q NaN") }
    }

    @Test
    fun parsesAll282BundledSongs() {
        val songDir = File("src/main/assets/songs")
        val files = songDir.listFiles { file -> file.extension == "txt" }?.sortedBy { it.name }.orEmpty()
        assertEquals(282, files.size)
        files.forEach { file ->
            val events = SongParser.parse(file.readText(Charsets.UTF_8))
            assertTrue(events.isNotEmpty(), file.name)
        }
    }
}
