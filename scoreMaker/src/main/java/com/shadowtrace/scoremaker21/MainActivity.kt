package com.shadowtrace.scoremaker21

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { ScoreMakerScreen() } } }
    }
}

@Composable
private fun ScoreMakerScreen() {
    val context = LocalContext.current
    val recorder = remember { ScoreRecorder() }
    val audioPlayer = remember { AudioSamplePlayer(context.applicationContext) }
    DisposableEffect(audioPlayer) { onDispose { audioPlayer.release() } }
    var events by remember { mutableStateOf(emptyList<ScoreEvent>()) }
    var selected by remember { mutableIntStateOf(-1) }
    var beatText by remember { mutableStateOf("600") }
    var status by remember { mutableStateOf("设置节拍后点击开始录制") }
    var lastKey by remember { mutableStateOf<Char?>(null) }
    var confirmClear by remember { mutableStateOf(false) }

    fun refresh() {
        events = recorder.snapshot(SystemClock.elapsedRealtime())
        if (selected !in events.indices) selected = -1
    }
    fun applyBeat(): Boolean {
        val value = beatText.toIntOrNull()
        if (value == null || value !in 100..5000) {
            status = "节拍请输入 100..5000 ms/拍"
            return false
        }
        recorder.beatMs = value
        return true
    }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain"),
    ) { uri ->
        if (uri != null) runCatching {
            require(events.isNotEmpty()) { "没有可保存的事件" }
            requireNotNull(context.contentResolver.openOutputStream(uri)).bufferedWriter(Charsets.UTF_8).use {
                it.write(ScoreCodec.export(events, recorder.beatMs))
            }
        }.onSuccess { status = "TXT 已保存" }.onFailure { status = "保存失败：${it.message}" }
    }
    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            require(recorder.state == ScoreRecorder.State.STOPPED) { "请先停止录制" }
            val text = requireNotNull(context.contentResolver.openInputStream(uri)).use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
            }
            ScoreCodec.parse(text)
        }.onSuccess { imported ->
            recorder.replace(imported.events)
            imported.recommendedBeatMs?.let { recorder.beatMs = it; beatText = it.toString() }
            selected = -1
            refresh()
            status = "已导入 ${events.size} 个事件，可编辑或继续录制"
        }.onFailure { status = "导入失败：${it.message}" }
    }

    Row(Modifier.fillMaxSize().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1.55f).fillMaxHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = beatText,
                    onValueChange = { beatText = it.filter(Char::isDigit).take(4) },
                    label = { Text("ms/拍") },
                    modifier = Modifier.width(115.dp),
                    singleLine = true,
                    enabled = recorder.state == ScoreRecorder.State.STOPPED,
                )
                when (recorder.state) {
                    ScoreRecorder.State.STOPPED -> Button(onClick = {
                        if (applyBeat()) {
                            recorder.start(SystemClock.elapsedRealtime())
                            status = "录制中；已有内容会保留并追加"
                        }
                    }) { Text("开始") }
                    ScoreRecorder.State.RECORDING -> Button(onClick = {
                        recorder.pause(SystemClock.elapsedRealtime()); status = "已暂停（暂停时间不计入）"
                    }) { Text("暂停") }
                    ScoreRecorder.State.PAUSED -> Button(onClick = {
                        recorder.resume(SystemClock.elapsedRealtime()); status = "继续录制"
                    }) { Text("继续") }
                }
                OutlinedButton(onClick = {
                    recorder.stop(SystemClock.elapsedRealtime()); refresh(); status = "已停止，共 ${events.size} 个事件"
                }, enabled = recorder.state != ScoreRecorder.State.STOPPED) { Text("停止") }
                OutlinedButton(
                    onClick = {
                        if (recorder.undo(SystemClock.elapsedRealtime())) {
                            refresh()
                            status = "已撤销；当前 ${events.size} 个事件"
                        }
                    },
                    enabled = events.isNotEmpty(),
                ) { Text("撤销") }
                OutlinedButton(onClick = {
                    if (selected >= 0 && recorder.deleteAt(selected)) { selected = -1; refresh() }
                }, enabled = selected >= 0 && recorder.state == ScoreRecorder.State.STOPPED) { Text("删除") }
                OutlinedButton(
                    onClick = { confirmClear = true },
                    enabled = recorder.state == ScoreRecorder.State.STOPPED && events.isNotEmpty(),
                ) { Text("清空") }
            }
            Spacer(Modifier.height(7.dp))
            listOf("qwertyu", "asdfghj", "zxcvbnm").forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { key ->
                        Button(
                            onClick = {
                                lastKey = key
                                val tappedAt = SystemClock.elapsedRealtime()
                                val recorded = recorder.tap(key, tappedAt)
                                audioPlayer.play(key)
                                refresh()
                                status = if (recorded) "已记录：${key.uppercase()}" else "试听：${key.uppercase()}（未在录制）"
                            },
                            modifier = Modifier.weight(1f).height(55.dp),
                        ) { Text(key.uppercase(), style = MaterialTheme.typography.titleLarge) }
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
            Text("$status　事件：${events.size}${lastKey?.let { "　最近按键：${it.uppercase()}" } ?: ""}")
        }

        Column(Modifier.weight(0.75f).fillMaxHeight()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { saveLauncher.launch("21键曲谱.txt") },
                    enabled = recorder.state == ScoreRecorder.State.STOPPED && events.isNotEmpty(),
                ) { Text("保存 TXT") }
                OutlinedButton(
                    onClick = { openLauncher.launch(arrayOf("text/plain", "text/*")) },
                    enabled = recorder.state == ScoreRecorder.State.STOPPED,
                ) { Text("导入 TXT") }
            }
            Text("事件预览（点击选择）", modifier = Modifier.padding(vertical = 6.dp))
            LazyColumn(Modifier.fillMaxSize().background(Color(0xFFF5F2FA))) {
                itemsIndexed(events) { index, event ->
                    Box(
                        Modifier.fillMaxWidth()
                            .background(if (selected == index) Color(0xFFD8CFFF) else Color.Transparent)
                            .clickable { selected = index }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) { Text("${index + 1}. ${event.keys} ${event.beats}") }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("清空全部事件？") },
            text = { Text("将删除当前 ${events.size} 个事件，此操作无法撤销。") },
            confirmButton = {
                Button(onClick = {
                    recorder.clear()
                    selected = -1
                    refresh()
                    confirmClear = false
                    status = "已清空"
                }) { Text("确认清空") }
            },
            dismissButton = {
                OutlinedButton(onClick = { confirmClear = false }) { Text("取消") }
            },
        )
    }
}
