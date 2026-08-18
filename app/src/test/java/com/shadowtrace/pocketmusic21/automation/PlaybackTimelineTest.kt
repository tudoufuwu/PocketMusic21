package com.shadowtrace.pocketmusic21.automation

import com.shadowtrace.pocketmusic21.model.SongEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlaybackTimelineTest {
    @Test
    fun preservesTimingRestAndChord() {
        val steps = PlaybackTimeline.build(
            listOf(SongEvent("q", 1.0), SongEvent("as", 0.5), SongEvent("p", 2.0)),
            beatMs = 600,
        )
        assertEquals(listOf(600L, 300L, 1200L), steps.map { it.totalMs })
        assertEquals("as", steps[1].keys)
        assertTrue(steps[2].isRest)
        assertEquals(0L, steps[2].holdMs)
    }
}
