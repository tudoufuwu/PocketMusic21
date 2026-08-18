package com.shadowtrace.pocketmusic21.calibration

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class EditScope(val label: String) { POINT("单点"), ROW("整行"), COLUMN("整列"), ALL("整体") }

@Composable
fun CalibrationScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember { CalibrationStore(context.applicationContext) }
    var profile by remember { mutableStateOf(store.load()) }
    var editScope by remember { mutableStateOf(EditScope.POINT) }
    var activeKey by remember { mutableStateOf<Char?>(null) }
    val currentProfile by rememberUpdatedState(profile)

    Row(modifier = modifier.fillMaxSize().padding(12.dp)) {
        Column(
            modifier = Modifier.width(250.dp).padding(end = 12.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("3×7 校准网格", style = MaterialTheme.typography.headlineSmall)
            Text("把圆心拖到游戏的21个白点上。坐标按可用区域归一化保存。")
            EditScope.entries.forEach { scope ->
                Button(onClick = { editScope = scope }) {
                    Text(if (editScope == scope) "✓ ${scopeLabel(scope)}" else scopeLabel(scope))
                }
            }
            Text("屏幕比例预设")
            Button(onClick = {
                profile = CalibrationProfile.wyclx20By9()
                store.save(profile)
            }) { Text(if (profile.aspectPreset == "一梦江湖20:9") "✓ 一梦江湖20:9（截图）" else "一梦江湖20:9（截图）") }
            listOf("16:9", "18:9", "19.5:9", "20:9", "16:10").forEach { aspect ->
                Button(onClick = {
                    profile = CalibrationProfile.preset(aspect)
                    store.save(profile)
                }) { Text(if (profile.aspectPreset == aspect) "✓ $aspect" else aspect) }
            }
            Button(onClick = { store.save(profile) }) { Text("保存校准") }
            Text("当前：${activeKey ?: '-'} · ${scopeLabel(editScope)}")
        }
        Canvas(
            modifier = Modifier.weight(1f).fillMaxSize().background(Color(0xFF111820))
                .pointerInput(editScope) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (size.width > 0 && size.height > 0) {
                                activeKey = currentProfile.nearest(
                                    offset.x / size.width,
                                    offset.y / size.height,
                                ).key
                            }
                        },
                        onDragEnd = { store.save(currentProfile) },
                        onDragCancel = { activeKey = null },
                    ) { change, dragAmount ->
                        change.consume()
                        val key = activeKey ?: return@detectDragGestures
                        val point = currentProfile.points.first { it.key == key }
                        val dx = dragAmount.x / size.width
                        val dy = dragAmount.y / size.height
                        profile = when (editScope) {
                            EditScope.POINT -> currentProfile.movePoint(key, dx, dy)
                            EditScope.ROW -> currentProfile.moveRow(point.row, dx, dy)
                            EditScope.COLUMN -> currentProfile.moveColumn(point.column, dx, dy)
                            EditScope.ALL -> currentProfile.moveAll(dx, dy)
                        }
                    }
                },
        ) {
            profile.points.forEach { point ->
                val center = Offset(point.x * size.width, point.y * size.height)
                val selected = point.key == activeKey
                drawCircle(
                    color = if (selected) Color(0xFFFFC857) else Color(0xFF79D7FF),
                    radius = if (selected) 24f else 19f,
                    center = center,
                )
                drawCircle(Color.White, radius = 29f, center = center, style = Stroke(2f))
                drawContext.canvas.nativeCanvas.drawText(
                    point.key.uppercase(),
                    center.x,
                    center.y + 7f,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 22f
                        isFakeBoldText = true
                    },
                )
            }
        }
    }
}

private fun scopeLabel(scope: EditScope) = when (scope) {
    EditScope.POINT -> "单点"
    EditScope.ROW -> "三行上下"
    EditScope.COLUMN -> "七列左右"
    EditScope.ALL -> "整体"
}
