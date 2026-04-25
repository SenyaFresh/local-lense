package ru.hse.edu.geoar.location

import ru.hse.locallense.common.entities.LocationData

private const val MAX_PERSISTED_AGE_MS = 5L * 60L * 1000L

internal data class PreLossSnapshot(
    val timestampMs: Long,
    val location: LocationData,
    val heading: Float,
)

internal class PreLossSnapshotStore(private val persistence: AnchorPersistence?) {

    private var snapshot: PreLossSnapshot? = null

    fun take(): PreLossSnapshot? {
        val taken = snapshot
        snapshot = null
        return taken
    }

    fun captureFromCurrent(location: LocationData?, heading: Float?) {
        if (location == null || heading == null) return
        val snap = PreLossSnapshot(
            timestampMs = System.currentTimeMillis(),
            location = location,
            heading = heading,
        )
        snapshot = snap
        persistence?.save(snap.toAnchorSnapshot())
    }

    fun persistNow(location: LocationData?, heading: Float?) {
        if (location == null || heading == null) return
        persistence?.save(
            AnchorPersistence.Snapshot(
                timestampMs = System.currentTimeMillis(),
                location = location,
                heading = heading,
            )
        )
    }

    fun restoreFromDisk() {
        if (snapshot != null) return
        val persisted = persistence?.load(MAX_PERSISTED_AGE_MS) ?: return
        snapshot = PreLossSnapshot(
            timestampMs = persisted.timestampMs,
            location = persisted.location,
            heading = persisted.heading,
        )
    }

    fun clear() {
        snapshot = null
    }
}

private fun PreLossSnapshot.toAnchorSnapshot() =
    AnchorPersistence.Snapshot(
        timestampMs = timestampMs,
        location = location,
        heading = heading,
    )
