package com.shadowtrace.pocketmusic21.calibration

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class CalibrationStore(context: Context) {
    private val preferences = context.getSharedPreferences("calibration", Context.MODE_PRIVATE)

    fun load(): CalibrationProfile = runCatching {
        val root = JSONObject(requireNotNull(preferences.getString("active", null)))
        val array = root.getJSONArray("points")
        val points = buildList(array.length()) {
            repeat(array.length()) { index ->
                val item = array.getJSONObject(index)
                add(
                    NormalizedPoint(
                        key = item.getString("key").single(),
                        row = item.getInt("row"),
                        column = item.getInt("column"),
                        x = item.getDouble("x").toFloat(),
                        y = item.getDouble("y").toFloat(),
                    ),
                )
            }
        }
        CalibrationProfile(root.getString("name"), root.getString("aspectPreset"), points)
    }.getOrElse { CalibrationProfile.wyclx20By9() }

    fun save(profile: CalibrationProfile) {
        val points = JSONArray()
        profile.points.forEach { point ->
            points.put(JSONObject().apply {
                put("key", point.key.toString())
                put("row", point.row)
                put("column", point.column)
                put("x", point.x.toDouble())
                put("y", point.y.toDouble())
            })
        }
        val root = JSONObject().apply {
            put("schema", 1)
            put("name", profile.name)
            put("aspectPreset", profile.aspectPreset)
            put("points", points)
        }
        preferences.edit().putString("active", root.toString()).apply()
    }
}
