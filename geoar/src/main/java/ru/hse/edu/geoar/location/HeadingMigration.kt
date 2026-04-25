package ru.hse.edu.geoar.location

import ru.hse.locallense.common.entities.LocationData

private const val MIGRATION_DURATION_MS = 1500L

internal data class MigratedFix(val location: LocationData, val heading: Float)

internal class HeadingMigration {

    private var startedAtMs: Long = 0L
    private var latOffset: Double = 0.0
    private var lonOffset: Double = 0.0
    private var altOffset: Double = 0.0
    private var headingOffset: Float = 0f

    fun schedule(
        snapshot: PreLossSnapshot,
        anchorLocation: LocationData,
        anchorHeading: Float,
    ) {
        latOffset = snapshot.location.latitude - anchorLocation.latitude
        lonOffset = snapshot.location.longitude - anchorLocation.longitude
        altOffset = snapshot.location.altitude - anchorLocation.altitude
        headingOffset = signedAngleDelta(snapshot.heading, anchorHeading)
        startedAtMs = System.currentTimeMillis()
    }

    fun cancel() {
        startedAtMs = 0L
        latOffset = 0.0
        lonOffset = 0.0
        altOffset = 0.0
        headingOffset = 0f
    }

    fun apply(rawLocation: LocationData, rawHeading: Float): MigratedFix {
        val factor = factor()
        return MigratedFix(
            location = LocationData(
                latitude = rawLocation.latitude + latOffset * factor,
                longitude = rawLocation.longitude + lonOffset * factor,
                altitude = rawLocation.altitude + altOffset * factor,
            ),
            heading = ((rawHeading + headingOffset * factor.toFloat()) + 360f).mod(360f),
        )
    }

    private fun factor(): Double {
        if (startedAtMs == 0L) return 0.0
        val elapsed = System.currentTimeMillis() - startedAtMs
        if (elapsed >= MIGRATION_DURATION_MS) {
            cancel()
            return 0.0
        }
        val linear = 1.0 - elapsed.toDouble() / MIGRATION_DURATION_MS
        return linear * linear
    }
}
