package ru.hse.edu.geoar.sensors

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SensorsManager(
    private val heading: HeadingProvider,
    private val steps: StepDetectorProvider,
    private val acceleration: LinearAccelerationProvider
) {

    var stepLengthMeters = 0.72
        private set

    val isMoving: Boolean
        get() = steps.timeSinceLastStep < 3_000 ||
                acceleration.raw.value > 0.8f

    private var wasMoving = false
    private var job: Job? = null

    fun start(
        scope: CoroutineScope,
        onStep: (azimuthDegrees: Double) -> Unit,
        onMovementChanged: (isMoving: Boolean) -> Unit = {}
    ) {
        heading.start()
        steps.start()
        acceleration.start()

        job = scope.launch {
            launch {
                steps.steps.collect {
                    onStep(heading.raw.value.toDouble())
                    checkMovement(onMovementChanged)
                }
            }
            launch {
                acceleration.raw.collect {
                    checkMovement(onMovementChanged)
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        heading.stop()
        steps.stop()
        acceleration.stop()
    }

    private fun checkMovement(callback: (Boolean) -> Unit) {
        val moving = isMoving
        if (moving != wasMoving) {
            wasMoving = moving
            callback(moving)
        }
    }
}