package ru.hse.edu.geoar.sensors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SensorsManager(
    private val headingProvider: HeadingProvider,
    private val stepDetectorProvider: StepDetectorProvider,
    private val linearAccelerationProvider: LinearAccelerationProvider
) {

    val isMoving: Boolean
        get() = stepDetectorProvider.timeSinceLastStep < 3_000 ||
                linearAccelerationProvider.rawValue.value > 0.8f

    private var wasMoving = false
    private var sensingJob: Job? = null

    fun start(
        scope: CoroutineScope,
        onStep: (azimuthDegrees: Double) -> Unit,
        onMovementChanged: (isMoving: Boolean) -> Unit = {}
    ) {
        headingProvider.start()
        stepDetectorProvider.start()
        linearAccelerationProvider.start()

        sensingJob = scope.launch {
            launch {
                stepDetectorProvider.steps.collect {
                    onStep(headingProvider.rawValue.value.toDouble())
                    checkMovement(onMovementChanged)
                }
            }
            launch {
                linearAccelerationProvider.rawValue.collect {
                    checkMovement(onMovementChanged)
                }
            }
        }
    }

    fun stop() {
        sensingJob?.cancel()
        sensingJob = null
        headingProvider.stop()
        stepDetectorProvider.stop()
        linearAccelerationProvider.stop()
    }

    private fun checkMovement(callback: (isMoving: Boolean) -> Unit) {
        val isCurrentlyMoving = isMoving
        if (isCurrentlyMoving != wasMoving) {
            wasMoving = isCurrentlyMoving
            callback(isCurrentlyMoving)
        }
    }
}