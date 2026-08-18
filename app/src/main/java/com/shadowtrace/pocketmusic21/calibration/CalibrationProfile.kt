package com.shadowtrace.pocketmusic21.calibration

import kotlin.math.abs

data class NormalizedPoint(
    val key: Char,
    val row: Int,
    val column: Int,
    val x: Float,
    val y: Float,
)

data class CalibrationProfile(
    val name: String,
    val aspectPreset: String,
    val points: List<NormalizedPoint>,
) {
    init {
        require(points.size == 21)
        require(points.map { it.key }.toSet().size == 21)
        require(points.all { it.x in 0f..1f && it.y in 0f..1f })
    }

    fun movePoint(key: Char, dx: Float, dy: Float) = transform { point ->
        if (point.key == key) point.shift(dx, dy) else point
    }

    fun moveRow(row: Int, dx: Float, dy: Float) = transform { point ->
        if (point.row == row) point.shift(dx, dy) else point
    }

    fun moveColumn(column: Int, dx: Float, dy: Float) = transform { point ->
        if (point.column == column) point.shift(dx, dy) else point
    }

    fun moveAll(dx: Float, dy: Float) = transform { it.shift(dx, dy) }

    fun scaleAll(scaleX: Float, scaleY: Float): CalibrationProfile {
        val centerX = points.map { it.x }.average().toFloat()
        val centerY = points.map { it.y }.average().toFloat()
        return transform { point ->
            point.copy(
                x = (centerX + (point.x - centerX) * scaleX).coerceIn(0f, 1f),
                y = (centerY + (point.y - centerY) * scaleY).coerceIn(0f, 1f),
            )
        }
    }

    fun nearest(normalizedX: Float, normalizedY: Float): NormalizedPoint =
        points.minBy { abs(it.x - normalizedX) + abs(it.y - normalizedY) }

    private fun transform(block: (NormalizedPoint) -> NormalizedPoint) =
        copy(points = points.map(block))

    private fun NormalizedPoint.shift(dx: Float, dy: Float) =
        copy(x = (x + dx).coerceIn(0f, 1f), y = (y + dy).coerceIn(0f, 1f))

    companion object {
        const val KEY_ROWS = "qwertyu|asdfghj|zxcvbnm"

        /** Measured from the user's 1280×576 一梦江湖 20:9 performance screenshot. */
        fun wyclx20By9(): CalibrationProfile {
            val xs = listOf(310f, 437f, 564f, 691f, 818f, 945f, 1072f).map { it / 1280f }
            val ys = listOf(363f, 436f, 508f).map { it / 576f }
            val points = KEY_ROWS.split('|').flatMapIndexed { row, keys ->
                keys.mapIndexed { column, key ->
                    NormalizedPoint(key, row, column, xs[column], ys[row])
                }
            }
            return CalibrationProfile("一梦江湖截图校准", "一梦江湖20:9", points)
        }

        fun preset(aspect: String = "16:9"): CalibrationProfile {
            val rows = KEY_ROWS.split('|')
            val horizontalMargin = when (aspect) {
                "20:9" -> 0.16f
                "19.5:9" -> 0.15f
                "18:9" -> 0.13f
                "16:10" -> 0.10f
                else -> 0.11f
            }
            val points = rows.flatMapIndexed { rowIndex, keys ->
                keys.mapIndexed { columnIndex, key ->
                    NormalizedPoint(
                        key = key,
                        row = rowIndex,
                        column = columnIndex,
                        x = horizontalMargin + columnIndex * (1f - horizontalMargin * 2f) / 6f,
                        y = 0.25f + rowIndex * 0.25f,
                    )
                }
            }
            return CalibrationProfile("默认校准", aspect, points)
        }
    }
}
