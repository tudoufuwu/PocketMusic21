package com.shadowtrace.pocketmusic21.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.KeyguardManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.shadowtrace.pocketmusic21.calibration.CalibrationStore
import com.shadowtrace.pocketmusic21.calibration.CalibrationProfile
import com.shadowtrace.pocketmusic21.data.SongRepository
import com.shadowtrace.pocketmusic21.model.SongEntry
import java.lang.ref.WeakReference
import kotlin.math.abs

class MusicAccessibilityService : AccessibilityService() {
    private lateinit var windowManager: WindowManager
    private var overlayPanel: MusicOverlayPanel? = null
    @Volatile private var foregroundPackage: String? = null

    override fun onServiceConnected() {
        instanceReference = WeakReference(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val newPackage = event?.packageName?.toString() ?: return
        if (newPackage != packageName && !isTransientSystemUi(newPackage)) foregroundPackage = newPackage
    }

    override fun onInterrupt() {
        // Android may interrupt accessibility feedback for transient system UI (volume,
        // charging, rotation). A failed gesture or service destruction remains responsible
        // for stopping playback; do not turn a transient callback into a hard stop.
    }

    override fun onDestroy() {
        hideOverlay()
        PlaybackController.stop("无障碍服务已关闭")
        if (instance === this) instanceReference = WeakReference(null)
        super.onDestroy()
    }

    fun activePackageName(): String? = foregroundPackage

    fun isTargetWindowActive(targetPackage: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val focusedPackage = runCatching {
                windows.firstOrNull { it.isFocused }?.root?.packageName?.toString()
            }.getOrNull()
            when {
                focusedPackage == targetPackage -> return true
                focusedPackage == null || isTransientSystemUi(focusedPackage) -> return true
                else -> return false
            }
        }
        return activePackageName() == targetPackage
    }

    private fun isTransientSystemUi(packageName: String) = packageName == "com.android.systemui"

    fun isLandscapeAndUnlocked(): Boolean {
        val keyguard = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        return !keyguard.isKeyguardLocked && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    fun isDeviceLocked(): Boolean {
        val keyguard = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        return keyguard.isKeyguardLocked
    }

    fun dispatchKeys(keys: String, holdMs: Long, onFinished: ((Boolean) -> Unit)? = null): Boolean {
        val profile = CalibrationStore(applicationContext).load()
        val bounds = usableBounds()
        val builder = GestureDescription.Builder()
        val points = keys.map { key -> profile.points.firstOrNull { it.key == key } }
        if (points.any { it == null }) {
            onFinished?.invoke(false)
            return false
        }
        points.filterNotNull().forEach { point ->
            val x = bounds.left + point.x * bounds.width()
            val y = bounds.top + point.y * bounds.height()
            builder.addStroke(
                GestureDescription.StrokeDescription(
                    Path().apply { moveTo(x, y) },
                    0,
                    holdMs.coerceAtLeast(24L),
                ),
            )
        }
        val callback = if (onFinished != null) {
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    onFinished?.invoke(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    onFinished?.invoke(false)
                }
            }
        } else null
        val accepted = dispatchGesture(builder.build(), callback, null)
        if (!accepted) onFinished?.invoke(false)
        return accepted
    }

    fun usableBounds(): Rect {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val result = Rect(metrics.bounds)
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout(),
            )
            result.left += insets.left
            result.top += insets.top
            result.right -= insets.right
            result.bottom -= insets.bottom
            return result
        }
        @Suppress("DEPRECATION")
        return Rect(0, 0, resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
    }

    fun showOverlay() {
        val panel = overlayPanel ?: MusicOverlayPanel(this, windowManager).also { overlayPanel = it }
        panel.showExpanded()
    }

    fun hideOverlay() {
        overlayPanel?.destroy()
        overlayPanel = null
    }

    companion object {
        @Volatile private var instanceReference = WeakReference<MusicAccessibilityService>(null)
        val instance: MusicAccessibilityService? get() = instanceReference.get()
    }
}

private class MusicOverlayPanel(
    private val service: MusicAccessibilityService,
    private val windowManager: WindowManager,
) {
    private enum class PlayMode(val label: String) {
        STOP("播完停止"), SEQUENCE("顺序播放"), REPEAT_ONE("单曲循环");
        fun next() = entries[(ordinal + 1) % entries.size]
    }

    private val prefs = service.getSharedPreferences("overlay_player", Context.MODE_PRIVATE)
    private val repository = SongRepository(service.applicationContext)
    private var songs = repository.allSongs()
    private val handler = Handler(Looper.getMainLooper())
    private var root: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var expanded = false
    private var statusView: TextView? = null
    private var bubbleView: TextView? = null
    private var bubbleStatusView: TextView? = null
    private var bubblePlayButton: TextView? = null
    private var bubbleStopButton: TextView? = null
    private var searchView: EditText? = null
    private var listView: ListView? = null
    private var listAdapter: SongListAdapter? = null
    private var speedView: TextView? = null
    private var modeButton: Button? = null
    private var autoButton: Button? = null
    private var favoriteButton: Button? = null
    private var selected: SongEntry = initialSong()
    private var beatMs: Int = storedBeat(selected)
    private var playMode = runCatching {
        PlayMode.valueOf(prefs.getString("play_mode", PlayMode.STOP.name)!!)
    }.getOrDefault(PlayMode.STOP)
    private var autoPlay = prefs.getBoolean("auto_play", true)
    private var completionHandled = false
    private var x = prefs.getInt("x", dp(12))
    private var y = prefs.getInt("y", dp(36))

    private val updater = object : Runnable {
        override fun run() {
            refreshStatus()
            handleCompletion()
            handler.postDelayed(this, 250L)
        }
    }

    fun showExpanded() {
        if (root == null) handler.post(updater)
        render(isExpanded = true)
    }

    fun destroy() {
        handler.removeCallbacks(updater)
        removeCurrent()
    }

    private fun render(isExpanded: Boolean) {
        expanded = isExpanded
        hideKeyboard()
        removeCurrent()
        val view = if (isExpanded) createExpandedView() else createBubbleView()
        val width = if (isExpanded) expandedWidth() else collapsedWidth()
        val flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            if (isExpanded) WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            else WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        val layout = WindowManager.LayoutParams(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = this@MusicOverlayPanel.x
            y = this@MusicOverlayPanel.y
        }
        root = view
        params = layout
        clampPosition(layout, width, dp(52))
        windowManager.addView(view, layout)
        view.post {
            if (root === view && params === layout) {
                clampPosition(layout, width, view.height.coerceAtLeast(dp(52)))
                runCatching { windowManager.updateViewLayout(view, layout) }
            }
        }
        refreshStatus()
    }

    private fun createBubbleView(): View {
        val controls = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val bubble = TextView(service).apply {
            text = bubbleText()
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = rounded(0xE66750A4.toInt(), dp(26).toFloat())
            elevation = dp(8).toFloat()
            layoutParams = LinearLayout.LayoutParams(dp(52), dp(52))
            bubbleView = this
            installDrag(this, click = { render(true) })
        }
        controls.addView(bubble)
        bubbleStatusView = text("", 11f).apply {
            gravity = Gravity.CENTER_VERTICAL
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            setPadding(dp(6), 0, dp(4), 0)
            background = rounded(0xE6222A34.toInt(), dp(8).toFloat())
            layoutParams = LinearLayout.LayoutParams(dp(120), dp(48)).apply {
                setMargins(dp(2), dp(2), 0, dp(2))
            }
        }.also(controls::addView)
        bubblePlayButton = bubbleControl("▶ 播放", 0xE61E8E5A.toInt()) {
            startAndCollapse()
        }.also(controls::addView)
        bubbleStopButton = bubbleControl("■ 停止", 0xE6B3261E.toInt()) {
            stopCurrentPlayback()
        }.also(controls::addView)
        return controls
    }

    private fun createExpandedView(): View {
        val panel = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = rounded(0xF21A2029.toInt(), dp(12).toFloat())
            elevation = dp(10).toFloat()
        }

        val header = row()
        val status = text("☰ 拖动", 12f).apply {
            maxLines = 2
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(6), dp(4), dp(4), dp(4))
            layoutParams = LinearLayout.LayoutParams(0, dp(44), 1f)
            installDrag(this, click = null)
        }
        val collapse = compactButton("收起") { render(false) }
        val calibrate = compactButton("校准") { showCalibration() }
        header.addView(status)
        header.addView(calibrate)
        header.addView(collapse)
        panel.addView(header)
        statusView = status

        val search = EditText(service).apply {
            hint = "搜索${songs.size}首歌曲"
            setHintTextColor(0xFFB7BDC7.toInt())
            setTextColor(Color.WHITE)
            setSingleLine(true)
            textSize = 14f
            background = rounded(0xFF303844.toInt(), dp(8).toFloat())
            setPadding(dp(10), 0, dp(10), 0)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(42)).apply {
                setMargins(0, dp(4), 0, dp(5))
            }
        }
        panel.addView(search)
        searchView = search

        val adapter = SongListAdapter(service)
        val list = ListView(service).apply {
            dividerHeight = 1
            setBackgroundColor(0xFF222A34.toInt())
            this.adapter = adapter
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, songListHeight())
            setOnItemClickListener { _, _, position, _ ->
                adapter.items.getOrNull(position)?.let(::selectSong)
            }
        }
        panel.addView(list)
        listAdapter = adapter
        listView = list

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                refreshList(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        val options = row()
        autoButton = compactButton("") {
            autoPlay = !autoPlay
            prefs.edit().putBoolean("auto_play", autoPlay).apply()
            refreshButtons()
        }.also(options::addView)
        modeButton = compactButton("") {
            playMode = playMode.next()
            prefs.edit().putString("play_mode", playMode.name).apply()
            refreshButtons()
        }.also(options::addView)
        favoriteButton = compactButton("") { toggleFavorite() }.also(options::addView)
        panel.addView(options)

        val speed = row()
        speed.addView(compactButton("－") { changeSpeed(-25) })
        speedView = text("", 13f).apply {
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, dp(42), 1f)
        }.also(speed::addView)
        speed.addView(compactButton("＋") { changeSpeed(25) })
        speed.addView(compactButton("推荐") { setSpeed(selected.beatMs) })
        panel.addView(speed)

        val transport = row()
        transport.addView(compactButton("▶ 播放") { startAndCollapse() })
        transport.addView(compactButton("⏯ 暂停/继续") {
            if (PlaybackController.state == PlaybackController.State.PAUSED) {
                PlaybackController.startFromOverlay(service)
                render(false)
            } else PlaybackController.pause()
        })
        transport.addView(compactButton("■ 停止") { PlaybackController.stop() })
        panel.addView(transport)

        refreshList("")
        refreshButtons()
        return panel
    }

    private fun showCalibration() {
        PlaybackController.stop("正在校准21键")
        hideKeyboard()
        removeCurrent()
        val store = CalibrationStore(service.applicationContext)
        val original = store.load()
        val grid = OverlayCalibrationView(service, original)
        val frame = FrameLayout(service).apply {
            addView(grid, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ))
            val controls = LinearLayout(service).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(dp(6), dp(4), dp(6), dp(4))
                background = rounded(0xE61A2029.toInt(), dp(10).toFloat())
                addView(compactButton("整体移动") { grid.mode = OverlayCalibrationView.Mode.MOVE })
                addView(compactButton("缩放网格") { grid.mode = OverlayCalibrationView.Mode.SCALE })
                addView(compactButton("单点修正") { grid.mode = OverlayCalibrationView.Mode.POINT })
                addView(compactButton("三行上下") { grid.mode = OverlayCalibrationView.Mode.ROW })
                addView(compactButton("七列左右") { grid.mode = OverlayCalibrationView.Mode.COLUMN })
                addView(compactButton("截图预设") { grid.reset(CalibrationProfile.wyclx20By9()) })
                addView(compactButton("取消") { render(true) })
                addView(compactButton("保存") {
                    store.save(grid.profile)
                    PlaybackController.stop("校准已保存")
                    render(false)
                })
            }
            addView(controls, FrameLayout.LayoutParams(dp(650), dp(52), Gravity.TOP or Gravity.CENTER_HORIZONTAL))
        }
        val bounds = service.usableBounds()
        val layout = WindowManager.LayoutParams(
            bounds.width(), bounds.height(),
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bounds.left
            y = bounds.top
        }
        root = frame
        params = layout
        windowManager.addView(frame, layout)
    }

    private fun selectSong(song: SongEntry) {
        selected = song
        beatMs = storedBeat(song)
        addRecent(song.id)
        prepareSelected()
        searchView?.setText(song.title)
        searchView?.setSelection(song.title.length)
        refreshButtons()
        if (autoPlay) startAndCollapse()
    }

    private fun prepareSelected(): Boolean = runCatching {
        PlaybackController.prepare(repository.parse(selected), beatMs)
        true
    }.getOrElse {
        PlaybackController.stop("曲谱解析失败：${it.message}")
        false
    }

    private fun startAndCollapse() {
        if (PlaybackController.state == PlaybackController.State.PLAYING &&
            PlaybackController.preparedTitle == selected.title
        ) {
            render(false)
            return
        }
        if (!PlaybackController.hasPreparedSong || PlaybackController.preparedTitle != selected.title) {
            if (!prepareSelected()) return
        }
        PlaybackController.startFromOverlay(service)
        // The expanded accessibility overlay necessarily consumes touches inside its bounds.
        // Always shrink after Play, including on an error; the bubble color preserves status.
        render(false)
    }

    private fun stopCurrentPlayback() {
        if (PlaybackController.state != PlaybackController.State.PLAYING &&
            PlaybackController.state != PlaybackController.State.PAUSED
        ) return
        PlaybackController.stop("已停止：${PlaybackController.preparedTitle.orEmpty()}")
        refreshStatus()
    }

    private fun changeSpeed(delta: Int) = setSpeed((beatMs + delta).coerceIn(200, 1800))

    private fun setSpeed(value: Int) {
        beatMs = value
        prefs.edit().putInt("beat_${selected.id}", beatMs).apply()
        if (PlaybackController.state == PlaybackController.State.PLAYING ||
            PlaybackController.state == PlaybackController.State.PAUSED
        ) PlaybackController.stop("速度已修改，请重新播放")
        prepareSelected()
        refreshButtons()
    }

    private fun refreshStatus() {
        val progress = if (PlaybackController.totalEvents > 0) {
            " ${PlaybackController.eventIndex}/${PlaybackController.totalEvents}"
        } else ""
        statusView?.text = "☰ 拖动 · ${PlaybackController.message}$progress"
        bubbleView?.text = bubbleText()
        bubbleStatusView?.text = collapsedStatusText()
        val isPlayingSelected = PlaybackController.state == PlaybackController.State.PLAYING &&
            PlaybackController.preparedTitle == selected.title
        bubblePlayButton?.isEnabled = !isPlayingSelected
        bubblePlayButton?.alpha = if (isPlayingSelected) 0.55f else 1f
        val canStop = PlaybackController.state == PlaybackController.State.PLAYING ||
            PlaybackController.state == PlaybackController.State.PAUSED
        bubbleStopButton?.isEnabled = canStop
        bubbleStopButton?.alpha = if (canStop) 1f else 0.55f
        bubbleView?.background = rounded(
            when (PlaybackController.state) {
                PlaybackController.State.PLAYING -> 0xE61E8E5A.toInt()
                PlaybackController.State.PAUSED -> 0xE6D18B22.toInt()
                PlaybackController.State.ERROR -> 0xE6B3261E.toInt()
                else -> 0xE66750A4.toInt()
            },
            dp(26).toFloat(),
        )
    }

    private fun bubbleText() = when (PlaybackController.state) {
        PlaybackController.State.PLAYING -> "▶"
        PlaybackController.State.PAUSED -> "Ⅱ"
        PlaybackController.State.ERROR -> "!"
        else -> "♫"
    }

    private fun collapsedStatusText(): String {
        val playbackTitle = PlaybackController.preparedTitle
        val stateLabel = when (PlaybackController.state) {
            PlaybackController.State.PLAYING -> "播放中"
            PlaybackController.State.PAUSED -> "已暂停"
            PlaybackController.State.COMPLETED -> "已播完"
            PlaybackController.State.ERROR -> "播放错误"
            else -> "已选"
        }
        return if ((PlaybackController.state == PlaybackController.State.PLAYING ||
                PlaybackController.state == PlaybackController.State.PAUSED) &&
            playbackTitle != null && playbackTitle != selected.title
        ) {
            "$stateLabel：$playbackTitle\n已选：${selected.title}"
        } else {
            "$stateLabel：\n${selected.title}"
        }
    }

    private fun handleCompletion() {
        if (PlaybackController.state != PlaybackController.State.COMPLETED) {
            completionHandled = false
            return
        }
        if (completionHandled) return
        completionHandled = true
        when (playMode) {
            PlayMode.STOP -> Unit
            PlayMode.REPEAT_ONE -> {
                prepareSelected()
                PlaybackController.startFromOverlay(service)
            }
            PlayMode.SEQUENCE -> {
                selected = songs[(songs.indexOfFirst { it.id == selected.id } + 1) % songs.size]
                beatMs = storedBeat(selected)
                addRecent(selected.id)
                prepareSelected()
                PlaybackController.startFromOverlay(service)
            }
        }
    }

    private fun refreshList(query: String) {
        songs = repository.allSongs()
        val normalized = query.trim()
        val result = if (normalized.isNotEmpty()) {
            songs.filter { it.title.contains(normalized, ignoreCase = true) }
        } else {
            val recent = recentIds().mapNotNull { id -> songs.firstOrNull { it.id == id } }
            val favorites = favoriteIds().mapNotNull { id -> songs.firstOrNull { it.id == id } }
            (recent + favorites + songs).distinctBy { it.id }
        }
        listAdapter?.items = result
        listAdapter?.notifyDataSetChanged()
    }

    private fun refreshButtons() {
        speedView?.text = "${selected.title}\n$beatMs ms/拍"
        autoButton?.text = if (autoPlay) "点歌即播✓" else "点歌即播×"
        modeButton?.text = playMode.label
        favoriteButton?.text = if (selected.id in favoriteIds()) "★ 收藏" else "☆ 收藏"
    }

    private fun toggleFavorite() {
        val ids = favoriteIds().toMutableSet()
        if (!ids.add(selected.id)) ids.remove(selected.id)
        prefs.edit().putString("favorites", ids.joinToString(",")).apply()
        refreshButtons()
        refreshList(searchView?.text?.toString().orEmpty())
    }

    private fun initialSong(): SongEntry {
        val recent = prefs.getString("recent", "")!!.split(',').firstOrNull { it.isNotBlank() }
        return songs.firstOrNull { it.id == recent } ?: songs.first()
    }

    private fun storedBeat(song: SongEntry) = prefs.getInt("beat_${song.id}", song.beatMs)

    private fun addRecent(id: String) {
        val ids = (listOf(id) + recentIds()).distinct().take(10)
        prefs.edit().putString("recent", ids.joinToString(",")).apply()
    }

    private fun recentIds() = prefs.getString("recent", "")!!.split(',').filter { it.isNotBlank() }
    private fun favoriteIds() = prefs.getString("favorites", "")!!.split(',').filter { it.isNotBlank() }.toSet()

    private fun compactButton(label: String, action: () -> Unit) = Button(service).apply {
        text = label
        textSize = 11f
        isAllCaps = false
        minWidth = 0
        minimumWidth = 0
        setPadding(dp(7), 0, dp(7), 0)
        layoutParams = LinearLayout.LayoutParams(0, dp(42), 1f).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }
        setOnClickListener { action() }
    }

    private fun bubbleControl(label: String, color: Int, action: () -> Unit) = TextView(service).apply {
        text = label
        textSize = 12f
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        background = rounded(color, dp(10).toFloat())
        elevation = dp(8).toFloat()
        layoutParams = LinearLayout.LayoutParams(dp(58), dp(48)).apply {
            setMargins(dp(2), dp(2), 0, dp(2))
        }
        setOnClickListener { action() }
    }

    private fun row() = LinearLayout(service).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun text(value: String, size: Float) = TextView(service).apply {
        text = value
        textSize = size
        setTextColor(Color.WHITE)
    }

    private fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
    }

    private fun installDrag(view: View, click: (() -> Unit)?) {
        var downX = 0f
        var downY = 0f
        var lastX = 0f
        var lastY = 0f
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX; downY = event.rawY
                    lastX = event.rawX; lastY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val layout = params ?: return@setOnTouchListener true
                    x += (event.rawX - lastX).toInt()
                    y += (event.rawY - lastY).toInt()
                    lastX = event.rawX; lastY = event.rawY
                    clampPosition(
                        layout,
                        root?.width?.takeIf { it > 0 } ?: if (expanded) expandedWidth() else collapsedWidth(),
                        root?.height?.takeIf { it > 0 } ?: dp(52),
                    )
                    root?.let { windowManager.updateViewLayout(it, layout) }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    prefs.edit().putInt("x", x).putInt("y", y).apply()
                    if (click != null && abs(event.rawX - downX) < dp(8) && abs(event.rawY - downY) < dp(8)) click()
                    true
                }
                else -> false
            }
        }
    }

    private fun clampPosition(layout: WindowManager.LayoutParams, width: Int, height: Int) {
        val bounds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds
        } else {
            @Suppress("DEPRECATION")
            Rect(0, 0, service.resources.displayMetrics.widthPixels, service.resources.displayMetrics.heightPixels)
        }
        x = x.coerceIn(0, (bounds.width() - width).coerceAtLeast(0))
        y = y.coerceIn(0, (bounds.height() - height).coerceAtLeast(0))
        layout.x = x
        layout.y = y
    }

    private fun hideKeyboard() {
        root?.windowToken?.let { token ->
            (service.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(token, 0)
        }
    }

    private fun removeCurrent() {
        root?.let { runCatching { windowManager.removeViewImmediate(it) } }
        root = null
        params = null
        statusView = null
        bubbleView = null
        bubbleStatusView = null
        bubblePlayButton = null
        bubbleStopButton = null
        searchView = null
        listView = null
        listAdapter = null
    }

    private fun dp(value: Int) = (value * service.resources.displayMetrics.density + 0.5f).toInt()

    private fun expandedWidth(): Int = minOf(dp(300), (service.usableBounds().width() * 0.38f).toInt())

    private fun collapsedWidth(): Int = dp(52 + 122 + 2 * 60)

    private fun songListHeight(): Int =
        (service.usableBounds().height() - dp(260)).coerceIn(dp(64), dp(180))

    private class SongListAdapter(private val context: Context) : BaseAdapter() {
        var items: List<SongEntry> = emptyList()
        override fun getCount() = items.size
        override fun getItem(position: Int) = items[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = (convertView as? TextView) ?: TextView(context).apply {
                setTextColor(Color.WHITE)
                textSize = 13f
                gravity = Gravity.CENTER_VERTICAL
                setPadding(14, 0, 10, 0)
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 42)
            }
            view.text = items[position].title
            view.setBackgroundColor(if (position % 2 == 0) 0xFF2A333E.toInt() else 0xFF222A34.toInt())
            return view
        }
    }
}
