package ru.hse.edu.geoar.location

import ru.hse.edu.geoar.math.Dimens.STEP_LENGTH_METERS
import ru.hse.edu.geoar.math.Dimens.metersPerDegreeLatitude
import ru.hse.edu.geoar.math.Dimens.metersPerDegreeLongitude
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class LocationKalmanFilter(
    private val stationaryProcessNoiseByTime: Double = 0.01,
    private val movingProcessNoiseByTime: Double = 0.5,
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
    private var altitude = 0.0

    private var varianceEastMeters2 = 0.0
    private var varianceNorthMeters2 = 0.0

    private var lastUpdateTimestampMs = 0L
    private var isMoving = false

    fun predictStep(azimuthDegrees: Double) {
        if (!isInitialized) return

        val azimuthRadians = Math.toRadians(azimuthDegrees)

        val stepEastMeters = STEP_LENGTH_METERS * sin(azimuthRadians)
        val stepNorthMeters = STEP_LENGTH_METERS * cos(azimuthRadians)

        estimatedEastMeters += stepEastMeters
        estimatedNorthMeters += stepNorthMeters

        val stepVarianceMeters2 = stepUncertaintyMeters * stepUncertaintyMeters
        varianceEastMeters2 += stepVarianceMeters2
        varianceNorthMeters2 += stepVarianceMeters2
    }

    fun process(measurement: LocationData): LocationData? {
        if (!isInitialized) return initialize(measurement)
        altitude = measurement.altitude

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

        val measurementVarianceMeters2 = measurement.accuracy.toDouble().pow(2)
        applyKalmanUpdate(
            measuredEastMeters = measuredEastMeters,
            measuredNorthMeters = measuredNorthMeters,
            measurementVarianceMeters2 = measurementVarianceMeters2
        )

        return buildEstimate(measurement.timestamp)
    }

    fun setMoving(moving: Boolean) {
        isMoving = moving
    }

    fun buildEstimate(timestampMs: Long): LocationData {
        val latitudeDegrees = estimatedLatitudeDegrees()
        val longitudeDegrees = estimatedLongitudeDegrees()
        val worstVarianceMeters2 = maxOf(varianceEastMeters2, varianceNorthMeters2)
        val accuracyMeters = sqrt(worstVarianceMeters2).toFloat()

        return LocationData(
            latitude = latitudeDegrees,
            longitude = longitudeDegrees,
            altitude = altitude,
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
        altitude = firstMeasurement.altitude

        estimatedEastMeters = 0.0
        estimatedNorthMeters = 0.0

        val initialVarianceMeters2 = firstMeasurement.accuracy.toDouble().pow(2)
        varianceEastMeters2 = initialVarianceMeters2
        varianceNorthMeters2 = initialVarianceMeters2

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

        val measurementVarianceMeters2 = measurementAccuracyMeters.pow(2)
        val worstEstimateVarianceMeters2 = maxOf(varianceEastMeters2, varianceNorthMeters2)
        val combinedVarianceMeters2 = worstEstimateVarianceMeters2 + measurementVarianceMeters2
        val outlierThresholdMeters = sqrt(combinedVarianceMeters2) * OUTLIER_SIGMA_MULTIPLIER

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
        val processNoiseMeters2PerSecond =
            if (isMoving) movingProcessNoiseByTime else stationaryProcessNoiseByTime
        val processNoiseForPeriodMeters2 = processNoiseMeters2PerSecond * timeDeltaSeconds

        varianceEastMeters2 += processNoiseForPeriodMeters2
        varianceNorthMeters2 += processNoiseForPeriodMeters2
    }

    private fun applyKalmanUpdate(
        measuredEastMeters: Double,
        measuredNorthMeters: Double,
        measurementVarianceMeters2: Double
    ) {
        val kalmanGainEast = varianceEastMeters2 / (varianceEastMeters2 + measurementVarianceMeters2)
        val kalmanGainNorth = varianceNorthMeters2 / (varianceNorthMeters2 + measurementVarianceMeters2)

        val innovationEastMeters = measuredEastMeters - estimatedEastMeters
        val innovationNorthMeters = measuredNorthMeters - estimatedNorthMeters

        estimatedEastMeters += kalmanGainEast * innovationEastMeters
        estimatedNorthMeters += kalmanGainNorth * innovationNorthMeters

        varianceEastMeters2 *= (1.0 - kalmanGainEast)
        varianceNorthMeters2 *= (1.0 - kalmanGainNorth)
    }

    private fun estimatedLatitudeDegrees(): Double =
        originLatitudeDegrees + estimatedNorthMeters / metersPerDegreeLatitude()

    private fun estimatedLongitudeDegrees(): Double =
        originLongitudeDegrees + estimatedEastMeters / metersPerDegreeLongitude(originLatitudeDegrees)

    private fun longitudeToEastMeters(longitudeDegrees: Double): Double =
        (longitudeDegrees - originLongitudeDegrees) * metersPerDegreeLongitude(originLatitudeDegrees)

    private fun latitudeToNorthMeters(latitudeDegrees: Double): Double =
        (latitudeDegrees - originLatitudeDegrees) * metersPerDegreeLatitude()

    companion object {
        private const val OUTLIER_SIGMA_MULTIPLIER = 2.5
    }
}