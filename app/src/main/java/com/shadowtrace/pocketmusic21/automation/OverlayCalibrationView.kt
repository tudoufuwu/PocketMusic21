package com.shadowtrace.pocketmusic21.automation

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import com.shadowtrace.pocketmusic21.calibration.CalibrationProfile
import kotlin.math.abs

/** Transparent in-game calibration surface: move the grid, resize it, then fine-tune points. */
class OverlayCalibrationView(context: Context, initial: CalibrationProfile) : View(context) {
    enum class Mode { MOVE, SCALE, POINT, ROW, COLUMN }

    var profile: CalibrationProfile = initial
        private set
    var mode: Mode = Mode.SCALE
        set(value) { field = value; invalidate() }

    fun reset(value: CalibrationProfile) {
        profile = value
        activeKey = null
        invalidate()
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(235, 80, 210, 255); style = Paint.Style.STROKE; strokeWidth = 3f
    }
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(225, 255, 200, 60) }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK; textAlign = Paint.Align.CENTER; textSize = 22f; isFakeBoldText = true
    }
    private val shadePaint = Paint().apply { color = Color.argb(38, 0, 0, 0) }
    private var startX = 0f
    private var startY = 0f
    private var startProfile = initial
    private var activeKey: Char? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), shadePaint)
        val box = bounds(profile)
        canvas.drawRect(box, linePaint)
        if (mode == Mode.SCALE) {
            listOf(box.left to box.top, box.right to box.top, box.left to box.bottom, box.right to box.bottom)
                .forEach { (x, y) -> canvas.drawRect(x - 15, y - 15, x + 15, y + 15, pointPaint) }
        }
        profile.points.forEach { point ->
            val x = point.x * width
            val y = point.y * height
            canvas.drawCircle(x, y, if (point.key == activeKey) 23f else 18f, pointPaint)
            canvas.drawText(point.key.uppercase(), x, y + 7f, textPaint)
        }
        val hint = when (mode) {
            Mode.MOVE -> "拖动任意位置：整体移动"
            Mode.SCALE -> "拖动四角方块：横向/纵向缩放"
            Mode.POINT -> "拖动圆点：逐键精修"
            Mode.ROW -> "拖动任意一行圆点：三行整体上下移动"
            Mode.COLUMN -> "拖动任意一列圆点：七列整体左右移动"
        }
        linePaint.style = Paint.Style.FILL
        linePaint.textSize = 26f
        canvas.drawText(hint, 24f, height - 24f, linePaint)
        linePaint.style = Paint.Style.STROKE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                startProfile = profile
                activeKey = if (mode == Mode.POINT || mode == Mode.ROW || mode == Mode.COLUMN) nearestKey(event.x, event.y) else null
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (width == 0 || height == 0) return true
                when (mode) {
                    Mode.MOVE -> profile = startProfile.moveAll(
                        (event.x - startX) / width,
                        (event.y - startY) / height,
                    )
                    Mode.POINT -> activeKey?.let { key ->
                        profile = startProfile.movePoint(
                            key,
                            (event.x - startX) / width,
                            (event.y - startY) / height,
                        )
                    }
                    Mode.ROW -> activeKey?.let { key ->
                        val point = startProfile.points.first { it.key == key }
                        profile = startProfile.moveRow(point.row, 0f, (event.y - startY) / height)
                    }
                    Mode.COLUMN -> activeKey?.let { key ->
                        val point = startProfile.points.first { it.key == key }
                        profile = startProfile.moveColumn(point.column, (event.x - startX) / width, 0f)
                    }
                    Mode.SCALE -> {
                        val box = bounds(startProfile)
                        val cx = box.centerX()
                        val cy = box.centerY()
                        val initialHalfX = abs(startX - cx).coerceAtLeast(24f)
                        val initialHalfY = abs(startY - cy).coerceAtLeast(24f)
                        val scaleX = (abs(event.x - cx) / initialHalfX).coerceIn(0.35f, 2.5f)
                        val scaleY = (abs(event.y - cy) / initialHalfY).coerceIn(0.35f, 2.5f)
                        profile = startProfile.scaleAll(scaleX, scaleY)
                    }
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activeKey = null
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun bounds(value: CalibrationProfile): RectF {
        val xs = value.points.map { it.x * width }
        val ys = value.points.map { it.y * height }
        return RectF(xs.min(), ys.min(), xs.max(), ys.max())
    }

    private fun nearestKey(x: Float, y: Float): Char = profile.points.minBy { point ->
        abs(point.x * width - x) + abs(point.y * height - y)
    }.key
}
