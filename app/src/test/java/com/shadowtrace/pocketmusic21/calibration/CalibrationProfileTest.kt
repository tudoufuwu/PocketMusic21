package com.shadowtrace.pocketmusic21.calibration

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalibrationProfileTest {
    @Test
    fun presetContainsExactly21UniqueBoundedPoints() {
        listOf("16:9", "18:9", "19.5:9", "20:9", "16:10").forEach { aspect ->
            val profile = CalibrationProfile.preset(aspect)
            assertEquals(21, profile.points.size)
            assertEquals(21, profile.points.map { it.key }.toSet().size)
            assertTrue(profile.points.all { it.x in 0f..1f && it.y in 0f..1f })
        }
    }

    @Test
    fun editsOnlyRequestedScopeAndClampsBounds() {
        val original = CalibrationProfile.preset()
        val point = original.points.first()
        val movedPoint = original.movePoint(point.key, 5f, -5f)
        assertEquals(1f, movedPoint.points.first().x)
        assertEquals(0f, movedPoint.points.first().y)
        assertEquals(original.points.drop(1), movedPoint.points.drop(1))

        val movedRow = original.moveRow(1, 0.01f, 0.02f)
        assertEquals(7, movedRow.points.zip(original.points).count { it.first != it.second })
        val movedColumn = original.moveColumn(3, 0.01f, 0.02f)
        assertEquals(3, movedColumn.points.zip(original.points).count { it.first != it.second })
    }

    @Test
    fun wyclxScreenshotPresetMatchesMeasuredTwentyOneDots() {
        val profile = CalibrationProfile.wyclx20By9()
        assertEquals("一梦江湖20:9", profile.aspectPreset)
        assertEquals(310f / 1280f, profile.points.first().x)
        assertEquals(363f / 576f, profile.points.first().y)
        assertEquals(1072f / 1280f, profile.points.last().x)
        assertEquals(508f / 576f, profile.points.last().y)
    }

    @Test
    fun directionalBatchScopesKeepRowsVerticalAndColumnsHorizontal() {
        val original = CalibrationProfile.preset()
        val row = original.moveRow(1, 0f, 0.05f)
        assertTrue(row.points.filter { it.row == 1 }.all { point ->
            point.x == original.points.first { it.key == point.key }.x
        })
        val column = original.moveColumn(3, 0.05f, 0f)
        assertTrue(column.points.filter { it.column == 3 }.all { point ->
            point.y == original.points.first { it.key == point.key }.y
        })
    }
}
