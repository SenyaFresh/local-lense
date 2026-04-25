package ru.hse.edu.geoar.location

import android.content.Context
import androidx.core.content.edit
import ru.hse.locallense.common.entities.LocationData

class AnchorPersistence(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class Snapshot(
        val timestampMs: Long,
        val location: LocationData,
        val heading: Float,
    )

    fun save(snapshot: Snapshot) {
        prefs.edit {
            putLong(
                KEY_TIMESTAMP_MS,
                snapshot.timestampMs
            )
                .putLong(
                    KEY_LATITUDE_BITS,
                    java.lang.Double.doubleToRawLongBits(snapshot.location.latitude)
                )
                .putLong(
                    KEY_LONGITUDE_BITS,
                    java.lang.Double.doubleToRawLongBits(snapshot.location.longitude)
                )
                .putLong(
                    KEY_ALTITUDE_BITS,
                    java.lang.Double.doubleToRawLongBits(snapshot.location.altitude)
                )
                .putFloat(KEY_HEADING, snapshot.heading)
        }
    }

    fun load(maxAgeMs: Long): Snapshot? {
        val ts = prefs.getLong(KEY_TIMESTAMP_MS, 0L)
        if (ts == 0L) return null
        if (System.currentTimeMillis() - ts > maxAgeMs) return null
        if (!prefs.contains(KEY_LATITUDE_BITS) ||
            !prefs.contains(KEY_LONGITUDE_BITS) ||
            !prefs.contains(KEY_ALTITUDE_BITS) ||
            !prefs.contains(KEY_HEADING)
        ) return null
        val latitude = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_LATITUDE_BITS, 0L))
        val longitude = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_LONGITUDE_BITS, 0L))
        val altitude = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_ALTITUDE_BITS, 0L))
        val heading = prefs.getFloat(KEY_HEADING, Float.NaN)
        if (heading.isNaN()) return null
        return Snapshot(
            timestampMs = ts,
            location = LocationData(latitude, longitude, altitude),
            heading = heading,
        )
    }

    fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "geoar_anchor_snapshot"
        private const val KEY_TIMESTAMP_MS = "ts"
        private const val KEY_LATITUDE_BITS = "lat_bits"
        private const val KEY_LONGITUDE_BITS = "lon_bits"
        private const val KEY_ALTITUDE_BITS = "alt_bits"
        private const val KEY_HEADING = "heading"
    }
}
