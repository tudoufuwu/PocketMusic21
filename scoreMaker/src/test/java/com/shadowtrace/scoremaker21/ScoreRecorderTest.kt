package com.shadowtrace.scoremaker21

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScoreRecorderTest {
    @Test
    fun intervalProducesQuantizedRest() {
        val recorder = ScoreRecorder(beatMs = 1000)
        recorder.start(0)
        recorder.tap('q', 0)
        recorder.tap('w', 2000)
        assertEquals(
            listOf(ScoreEvent("q", 0.125), ScoreEvent("p", 1.875), ScoreEvent("w", 0.125)),
            recorder.stop(2125),
        )
    }

    @Test
    fun nearSimultaneousDifferentKeysFormChord() {
        val recorder = ScoreRecorder(beatMs = 600)
        recorder.start(0)
        recorder.tap('q', 0)
        recorder.tap('a', 70)
        assertEquals(listOf(ScoreEvent("qa", 0.125)), recorder.stop(75))
    }

    @Test
    fun multipleKeysWithinWindowFormOneChord() {
        val recorder = ScoreRecorder(beatMs = 600)
        recorder.start(0)
        recorder.tap('q', 0)
        recorder.tap('a', 40)
        recorder.tap('z', 80)
        assertEquals(listOf(ScoreEvent("qaz", 0.125)), recorder.stop(75))
    }

    @Test
    fun repeatedKeyRemainsTwoNotes() {
        val recorder = ScoreRecorder(beatMs = 600)
        recorder.start(0)
        recorder.tap('q', 0)
        recorder.tap('q', 150)
        assertEquals(
            listOf(ScoreEvent("q", 0.125), ScoreEvent("p", 0.125), ScoreEvent("q", 0.125)),
            recorder.stop(225),
        )
    }

    @Test
    fun pausedTimeDoesNotBecomeRest() {
        val recorder = ScoreRecorder(beatMs = 1000)
        recorder.start(0)
        recorder.tap('q', 0)
        recorder.pause(500)
        recorder.resume(5500)
        recorder.tap('w', 6000)
        assertEquals(listOf(ScoreEvent("q", 0.125), ScoreEvent("p", 0.875), ScoreEvent("w", 0.125)), recorder.stop(6125))
    }

    @Test
    fun multiplePausesAndStopWhilePausedUseOnlyActiveTime() {
        val recorder = ScoreRecorder(beatMs = 1000)
        recorder.start(0)
        recorder.tap('q', 0)
        recorder.pause(500)
        recorder.resume(2500)
        recorder.pause(3000)
        recorder.resume(6000)
        recorder.tap('w', 6500)
        recorder.pause(6625)
        assertEquals(
            listOf(ScoreEvent("q", 0.125), ScoreEvent("p", 1.375), ScoreEvent("w", 0.125)),
            recorder.stop(12000),
        )
    }

    @Test
    fun appendingAfterImportDoesNotInsertLeadingRest() {
        val original = listOf(ScoreEvent("q", 1.0), ScoreEvent("p", 0.5))
        val recorder = ScoreRecorder(beatMs = 1000)
        recorder.replace(original)
        recorder.start(10_000)
        recorder.tap('w', 12_000)
        assertEquals(original + ScoreEvent("w", 0.125), recorder.stop(12_125))
    }

    @Test
    fun stopIsIdempotent() {
        val recorder = ScoreRecorder(beatMs = 600)
        recorder.start(0)
        recorder.tap('q', 0)
        val stopped = recorder.stop(300)
        assertEquals(stopped, recorder.stop(10_000))
    }

    @Test
    fun deleteUndoAndClearPreserveEditingRules() {
        val recorder = ScoreRecorder(beatMs = 600)
        recorder.replace(
            listOf(ScoreEvent("q", 0.5), ScoreEvent("p", 1.0), ScoreEvent("w", 0.5)),
        )
        assertTrue(recorder.deleteAt(1))
        assertEquals(listOf(ScoreEvent("q", 0.5), ScoreEvent("w", 0.5)), recorder.snapshot(0))
        assertTrue(recorder.undo(0))
        assertEquals(listOf(ScoreEvent("q", 0.5)), recorder.snapshot(0))

        recorder.start(100)
        recorder.tap('a', 100)
        recorder.tap('s', 140)
        assertTrue(recorder.undo(150))
        assertEquals(listOf(ScoreEvent("q", 0.5), ScoreEvent("a", 0.125)), recorder.stop(175))

        recorder.clear()
        assertEquals(emptyList(), recorder.snapshot(500))
        assertFalse(recorder.undo(500))
        assertFalse(recorder.deleteAt(0))
        assertEquals(ScoreRecorder.State.STOPPED, recorder.state)
    }

    @Test
    fun txtRoundTripPreservesEventsAndRecommendedBeat() {
        val events = listOf(
            ScoreEvent("qaz", 0.125),
            ScoreEvent("q", 0.0416667),
            ScoreEvent("q", 0.375),
            ScoreEvent("p", 1.875),
            ScoreEvent("z", 2.0),
        )
        val parsed = ScoreCodec.parse(ScoreCodec.export(events, 720))
        assertEquals(events, parsed.events)
        assertEquals(720, parsed.recommendedBeatMs)
    }

    @Test
    fun importRecognizesLibraryStyleFullWidthBeatComment() {
        val parsed = ScoreCodec.parse("# 推荐节拍：714 ms/拍（约84 BPM）\nq 0.5\n")
        assertEquals(listOf(ScoreEvent("q", 0.5)), parsed.events)
        assertEquals(714, parsed.recommendedBeatMs)
    }

    @Test
    fun freshRecordingPreservesLeadingSilenceAtEighthBeatResolution() {
        val recorder = ScoreRecorder(beatMs = 1000)
        recorder.start(0)
        recorder.tap('q', 250)
        assertEquals(
            listOf(ScoreEvent("p", 0.25), ScoreEvent("q", 0.125)),
            recorder.stop(375),
        )
    }

    @Test
    fun longRestIsSplitIntoPlayerCompatibleChunks() {
        val recorder = ScoreRecorder(beatMs = 100)
        recorder.start(0)
        recorder.tap('q', 0)
        recorder.tap('w', 13_000)
        val result = recorder.stop(13_013)
        assertTrue(result.all { it.beats in 0.000001..64.0 })
        assertEquals(
            listOf(ScoreEvent("q", 0.125), ScoreEvent("p", 64.0), ScoreEvent("p", 64.0), ScoreEvent("p", 1.875), ScoreEvent("w", 0.125)),
            result,
        )
    }
}
