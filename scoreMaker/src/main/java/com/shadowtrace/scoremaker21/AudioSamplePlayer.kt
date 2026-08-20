package com.shadowtrace.scoremaker21

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.util.Collections

class AudioSamplePlayer(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .build()

    private val soundIds = HashMap<Char, Int>()
    private val loadedIds = Collections.synchronizedSet(mutableSetOf<Int>())

    @Volatile
    private var released = false

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && !released) loadedIds.add(sampleId)
        }
        val resources = mapOf(
            'q' to R.raw.key_q, 'w' to R.raw.key_w, 'e' to R.raw.key_e,
            'r' to R.raw.key_r, 't' to R.raw.key_t, 'y' to R.raw.key_y,
            'u' to R.raw.key_u, 'a' to R.raw.key_a, 's' to R.raw.key_s,
            'd' to R.raw.key_d, 'f' to R.raw.key_f, 'g' to R.raw.key_g,
            'h' to R.raw.key_h, 'j' to R.raw.key_j, 'z' to R.raw.key_z,
            'x' to R.raw.key_x, 'c' to R.raw.key_c, 'v' to R.raw.key_v,
            'b' to R.raw.key_b, 'n' to R.raw.key_n, 'm' to R.raw.key_m,
        )
        resources.forEach { (key, resourceId) ->
            try {
                val soundId = soundPool.load(context.applicationContext, resourceId, 1)
                if (soundId != 0) soundIds[key] = soundId
            } catch (_: RuntimeException) {
                // A missing/invalid optional sample must not crash ScoreMaker startup.
            }
        }
    }

    @Synchronized
    fun play(key: Char) {
        if (released) return
        val soundId = soundIds[key.lowercaseChar()] ?: return
        if (!loadedIds.contains(soundId)) return
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    @Synchronized
    fun release() {
        if (released) return
        released = true
        loadedIds.clear()
        soundIds.clear()
        soundPool.release()
    }
}
