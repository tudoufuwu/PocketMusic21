package com.shadowtrace.pocketmusic21

import android.net.Uri
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.shadowtrace.pocketmusic21.data.SongParser
import com.shadowtrace.pocketmusic21.data.SongRepository
import com.shadowtrace.pocketmusic21.calibration.CalibrationScreen
import com.shadowtrace.pocketmusic21.automation.MusicAccessibilityService
import com.shadowtrace.pocketmusic21.automation.PlaybackController
import com.shadowtrace.pocketmusic21.automation.AppVisibility
import com.shadowtrace.pocketmusic21.model.SongEntry
import java.util.UUID
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onStart() {
        super.onStart()
        AppVisibility.mainActivityStarted = true
    }

    override fun onStop() {
        AppVisibility.mainActivityStarted = false
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PocketMusicApp() }
    }
}

@Composable
fun PocketMusicApp() {
    val context = LocalContext.current
    // Use the device's shortest dimension so a phone rotated to landscape still
    // gets the dedicated floating-library entry point.
    val isCompactScreen = LocalConfiguration.current.smallestScreenWidthDp < 600
    val repository = remember { SongRepository(context.applicationContext) }
    val prefs = remember { context.getSharedPreferences("overlay_player", android.content.Context.MODE_PRIVATE) }
    val bundled = remember { repository.bundledSongs() }
    val imported = remember { mutableStateListOf<SongEntry>().apply { addAll(repository.importedSongs()) } }
    val queue = remember { mutableStateListOf<SongEntry>() }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<SongEntry?>(bundled.firstOrNull()) }
    var beatMs by remember { mutableFloatStateOf((selected?.beatMs ?: 700).toFloat()) }
    var speedRate by remember {
        mutableFloatStateOf(selected?.let { prefs.getFloat("speed_${it.id}", 1f) } ?: 1f)
    }
    var status by remember { mutableStateOf("曲库 ${bundled.size + imported.size} 首") }
    var showCalibration by remember { mutableStateOf(false) }

    fun readImported(uri: Uri) {
        runCatching {
            val text = context.contentResolver.openInputStream(uri)!!.bufferedReader().use { it.readText() }
            val events = SongParser.parse(text)
            val title = uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
                ?.replace('_', ' ')?.trim().orEmpty().ifBlank { "导入曲谱" }
            SongEntry(UUID.randomUUID().toString(), title, null, 700, importedText = text) to events.size
        }.onSuccess { (entry, count) ->
            imported += entry
            repository.saveImportedSongs(imported)
            selected = entry
            beatMs = entry.beatMs.toFloat()
            speedRate = prefs.getFloat("speed_${entry.id}", 1f)
            status = "已导入 ${entry.title}（$count 个事件）"
        }.onFailure { status = "导入失败：${it.message}" }
    }

    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach(::readImported)
    }
    var pendingExport by remember { mutableStateOf<String?>(null) }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null && pendingExport != null) runCatching {
            context.contentResolver.openOutputStream(uri)!!.bufferedWriter(Charsets.UTF_8).use { it.write(pendingExport!!) }
            status = "TXT 已导出"
        }.onFailure { status = "导出失败：${it.message}" }
        pendingExport = null
    }
    val allSongs = bundled + imported
    val visibleSongs = allSongs.filter { it.title.contains(query, ignoreCase = true) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(onClick = { showCalibration = false }) {
                        Text(if (!showCalibration) "✓ 曲库" else "曲库")
                    }
                    Button(onClick = { showCalibration = true }) {
                        Text(if (showCalibration) "✓ 校准网格" else "校准网格")
                    }
                }
                if (isCompactScreen && !showCalibration) {
                    Button(
                        onClick = {
                            val service = MusicAccessibilityService.instance
                            if (service == null) {
                                status = "请启用“21键悬浮演奏”无障碍服务，返回后再点一次"
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            } else {
                                service.showOverlay()
                                status = "悬浮曲库已打开；可直接搜索、选歌和播放"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    ) { Text("打开悬浮曲库") }
                }
                if (showCalibration) {
                    CalibrationScreen(modifier = Modifier.weight(1f))
                } else {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.width(330.dp).fillMaxHeight()) {
                    Text("21键曲库", style = MaterialTheme.typography.headlineSmall)
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("搜索 ${allSongs.size} 首") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { importer.launch(arrayOf("text/plain", "text/*")) }) { Text("批量导入 TXT") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            val song = selected ?: return@Button
                            val parsed = runCatching { repository.parse(song) }.getOrNull() ?: return@Button
                            pendingExport = parsed.events.joinToString("\n") { "${it.keys} ${it.beats}" } + "\n"
                            exporter.launch("${song.title}.txt")
                        }) { Text("导出当前 TXT") }
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(visibleSongs, key = { it.id }) { song ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                                    .clickable {
                                        selected = song
                                        beatMs = song.beatMs.toFloat()
                                        speedRate = prefs.getFloat("speed_${song.id}", 1f)
                                        status = "已选择 ${song.title}"
                                    },
                            ) { Text(song.title, modifier = Modifier.padding(10.dp)) }
                        }
                    }
                }
                Spacer(Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(selected?.title ?: "请选择曲谱", style = MaterialTheme.typography.headlineMedium)
                    selected?.let { song ->
                        val parsed = remember(song) { runCatching { repository.parse(song) } }
                        Text(parsed.fold(
                            onSuccess = { "${it.events.size} 个事件 · ${"%.1f".format(it.totalBeats)} 拍" },
                            onFailure = { "解析失败：${it.message}" },
                        ))
                        val effectiveBeatMs = (beatMs / speedRate).roundToInt().coerceAtLeast(1)
                        Text("一拍 $effectiveBeatMs ms · ${"%.2f".format(speedRate)}x")
                        Slider(
                            value = beatMs,
                            onValueChange = {
                                beatMs = it
                                prefs.edit().putInt("beat_${song.id}", it.roundToInt()).apply()
                            },
                            valueRange = 200f..1800f,
                            steps = 31,
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 3f, 4f).forEach { preset ->
                                Button(onClick = {
                                    speedRate = preset
                                    prefs.edit().putFloat("speed_${song.id}", preset).apply()
                                }) { Text("${"%.2f".format(preset)}x") }
                            }
                        }
                        Slider(
                            value = speedRate,
                            onValueChange = {
                                speedRate = (it * 100f).roundToInt() / 100f
                                prefs.edit().putFloat("speed_${song.id}", speedRate).apply()
                            },
                            valueRange = 0.25f..4f,
                            steps = 374,
                        )
                        if (speedRate > 2f) {
                            Text("实验档：超过2x可能丢键或连键", color = MaterialTheme.colorScheme.error)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { beatMs = song.beatMs.toFloat() }) { Text("推荐速度") }
                            Button(onClick = { queue += song; status = "已加入队列（${queue.size}）" }) { Text("加入队列") }
                        }
                        Button(onClick = {
                            val service = MusicAccessibilityService.instance
                            if (service == null) {
                                status = "请启用“21键悬浮演奏”无障碍服务，返回后再点一次"
                                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            } else {
                                service.showOverlay()
                                status = "悬浮曲库已打开；可直接搜索、选歌和播放"
                            }
                        }) { Text("打开悬浮曲库") }
                    }
                    Text("队列 ${queue.size} 首")
                    queue.take(5).forEachIndexed { index, item -> Text("${index + 1}. ${item.title}") }
                    Spacer(Modifier.weight(1f))
                    Text(status)
                    Text("下一步：校准21键后启用悬浮演奏", style = MaterialTheme.typography.bodySmall)
                }
                    }
                }
            }
        }
    }
}
