package ru.hse.edu.geoar.location

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class LocationKalmanFilter(
    private val stationaryProcessNoiseM2perS: Double = 0.01,
    private val movingProcessNoiseM2perS: Double = 1.5,
    private val maxAllowedAccuracyMeters: Float = 40f,
    private val maxAllowedJumpMeters: Double = 60.0,
    private val stepUncertaintyMeters: Double = 0.3,
    private val maxTimeDeltaSeconds: Double = 30.0
) {
    private var isInitialized = false

    private var estimatedEastMeters = 0.0
    private var estimatedNorthMeters = 0.0

    private var originLatitudeDegrees = 0.0
    private var originLongitudeDegrees = 0.0

    private var varianceEastM2 = 0.0
    private var varianceNorthM2 = 0.0

    private var lastUpdateTimestampMs = 0L
    private var isMoving = false

    fun predictStep(stepLengthMeters: Double, azimuthDegrees: Double) {
        if (!isInitialized) return

        val azimuthRadians = Math.toRadians(azimuthDegrees)

        val stepEastMeters = stepLengthMeters * sin(azimuthRadians)
        val stepNorthMeters = stepLengthMeters * cos(azimuthRadians)

        estimatedEastMeters += stepEastMeters
        estimatedNorthMeters += stepNorthMeters

        val stepVarianceM2 = stepUncertaintyMeters * stepUncertaintyMeters
        varianceEastM2 += stepVarianceM2
        varianceNorthM2 += stepVarianceM2
    }

    fun process(measurement: LocationData): LocationData? {
        if (!isInitialized) return initialize(measurement)

        val isMeasurementTooInaccurate = measurement.accuracy > maxAllowedAccuracyMeters
        if (isMeasurementTooInaccurate) return null

        val measuredEastMeters = longitudeToEastMeters(measurement.longitude)
        val measuredNorthMeters = latitudeToNorthMeters(measurement.latitude)

        val isOutlier = checkForOutlier(
            measuredEastMeters = measuredEastMeters,
            measuredNorthMeters = measuredNorthMeters,
            measurementAccuracyMeters = measurement.accuracy.toDouble()
        )
        if (isOutlier) return null

        val timeDeltaSeconds = calculateTimeDelta(measurement.timestamp)
        growUncertaintyOverTime(timeDeltaSeconds)

        val measurementVarianceM2 = measurement.accuracy.toDouble().pow(2)
        applyKalmanUpdate(
            measuredEastMeters = measuredEastMeters,
            measuredNorthMeters = measuredNorthMeters,
            measurementVarianceM2 = measurementVarianceM2
        )

        return buildEstimate(measurement.timestamp)
    }

    fun setMoving(moving: Boolean) {
        isMoving = moving
    }

    fun buildEstimate(timestampMs: Long): LocationData {
        val latitudeDegrees = estimatedLatitudeDegrees()
        val longitudeDegrees = estimatedLongitudeDegrees()
        val worstVarianceM2 = maxOf(varianceEastM2, varianceNorthM2)
        val accuracyMeters = sqrt(worstVarianceM2).toFloat()

        return LocationData(
            latitude = latitudeDegrees,
            longitude = longitudeDegrees,
            accuracy = accuracyMeters,
            timestamp = timestampMs
        )
    }

    fun buildEstimateNow(): LocationData? {
        if (!isInitialized) return null
        return buildEstimate(System.currentTimeMillis())
    }

    fun reset() {
        isInitialized = false
    }

    private fun initialize(firstMeasurement: LocationData): LocationData {
        originLatitudeDegrees = firstMeasurement.latitude
        originLongitudeDegrees = firstMeasurement.longitude

        estimatedEastMeters = 0.0
        estimatedNorthMeters = 0.0

        val initialVarianceM2 = firstMeasurement.accuracy.toDouble().pow(2)
        varianceEastM2 = initialVarianceM2
        varianceNorthM2 = initialVarianceM2

        lastUpdateTimestampMs = firstMeasurement.timestamp
        isInitialized = true

        return firstMeasurement
    }

    private fun checkForOutlier(
        measuredEastMeters: Double,
        measuredNorthMeters: Double,
        measurementAccuracyMeters: Double
    ): Boolean {
        val deviationEastMeters = measuredEastMeters - estimatedEastMeters
        val deviationNorthMeters = measuredNorthMeters - estimatedNorthMeters
        val distanceMeters = sqrt(
            deviationEastMeters * deviationEastMeters +
                    deviationNorthMeters * deviationNorthMeters
        )

        val measurementVarianceM2 = measurementAccuracyMeters.pow(2)
        val worstEstimateVarianceM2 = maxOf(varianceEastM2, varianceNorthM2)
        val combinedVarianceM2 = worstEstimateVarianceM2 + measurementVarianceM2
        val outlierThresholdMeters = sqrt(combinedVarianceM2) * OUTLIER_SIGMA_MULTIPLIER

        val exceedsAbsoluteLimit = distanceMeters > maxAllowedJumpMeters
        val exceedsStatisticalLimit = distanceMeters > outlierThresholdMeters

        return exceedsAbsoluteLimit && exceedsStatisticalLimit
    }

    private fun calculateTimeDelta(currentTimestampMs: Long): Double {
        val rawDeltaMs = currentTimestampMs - lastUpdateTimestampMs
        val rawDeltaSeconds = rawDeltaMs / 1000.0
        val clampedDeltaSeconds = rawDeltaSeconds.coerceIn(0.001, maxTimeDeltaSeconds)

        lastUpdateTimestampMs = currentTimestampMs

        return clampedDeltaSeconds
    }

    private fun growUncertaintyOverTime(timeDeltaSeconds: Double) {
        val processNoiseM2perS =
            if (isMoving) movingProcessNoiseM2perS else stationaryProcessNoiseM2perS
        val processNoiseForPeriodM2 = processNoiseM2perS * timeDeltaSeconds

        varianceEastM2 += processNoiseForPeriodM2
        varianceNorthM2 += processNoiseForPeriodM2
    }

    private fun applyKalmanUpdate(
        measuredEastMeters: Double,
        measuredNorthMeters: Double,
        measurementVarianceM2: Double
    ) {
        val kalmanGainEast = varianceEastM2 / (varianceEastM2 + measurementVarianceM2)
        val kalmanGainNorth = varianceNorthM2 / (varianceNorthM2 + measurementVarianceM2)

        val innovationEastMeters = measuredEastMeters - estimatedEastMeters
        val innovationNorthMeters = measuredNorthMeters - estimatedNorthMeters

        estimatedEastMeters += kalmanGainEast * innovationEastMeters
        estimatedNorthMeters += kalmanGainNorth * innovationNorthMeters

        varianceEastM2 *= (1.0 - kalmanGainEast)
        varianceNorthM2 *= (1.0 - kalmanGainNorth)
    }

    private fun estimatedLatitudeDegrees(): Double =
        originLatitudeDegrees + estimatedNorthMeters / METERS_PER_DEGREE_LATITUDE

    private fun estimatedLongitudeDegrees(): Double =
        originLongitudeDegrees + estimatedEastMeters / metersPerDegreeLongitude(originLatitudeDegrees)

    private fun longitudeToEastMeters(longitudeDegrees: Double): Double =
        (longitudeDegrees - originLongitudeDegrees) * metersPerDegreeLongitude(originLatitudeDegrees)

    private fun latitudeToNorthMeters(latitudeDegrees: Double): Double =
        (latitudeDegrees - originLatitudeDegrees) * METERS_PER_DEGREE_LATITUDE

    companion object {
        private const val METERS_PER_DEGREE_LATITUDE = 111_320.0
        private const val OUTLIER_SIGMA_MULTIPLIER = 2.5

        private fun metersPerDegreeLongitude(latitudeDegrees: Double): Double =
            METERS_PER_DEGREE_LATITUDE * cos(Math.toRadians(latitudeDegrees))
    }
}