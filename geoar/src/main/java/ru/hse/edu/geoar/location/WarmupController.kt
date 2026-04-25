package ru.hse.edu.geoar.location

private const val MIN_WARMUP_DURATION_MS = 1500L
private const val MAX_WARMUP_DURATION_MS = 6000L
private const val MAX_ACCEPTABLE_ACCURACY_METERS = 8.0

internal class WarmupController(private val locationTracker: LocationTracker) {

    private var startedAtMs: Long = 0L

    var isCommitted: Boolean = false
        private set

    fun reset() {
        startedAtMs = 0L
        isCommitted = false
    }

    fun shouldCommit(): Boolean {
        if (isCommitted) return false
        if (startedAtMs == 0L) {
            startedAtMs = System.currentTimeMillis()
            return false
        }
        val elapsed = System.currentTimeMillis() - startedAtMs
        if (elapsed < MIN_WARMUP_DURATION_MS) return false

        val accuracy = locationTracker.currentAccuracyMeters()
        val gpsConverged = accuracy != null && accuracy <= MAX_ACCEPTABLE_ACCURACY_METERS
        return gpsConverged || elapsed >= MAX_WARMUP_DURATION_MS
    }

    fun markCommitted() {
        isCommitted = true
    }
}
