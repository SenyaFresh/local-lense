package ru.hse.edu.geoar.ar

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import ru.hse.edu.geoar.location.LocationTracker
import ru.hse.edu.geoar.sensors.HeadingProvider
import ru.hse.edu.geoar.sensors.LinearAccelerationProvider
import ru.hse.edu.geoar.sensors.SensorsManager
import ru.hse.edu.geoar.sensors.StepDetectorProvider

object ArGeoFactory {
    private var isInitialized = false

    lateinit var headingProvider: HeadingProvider
        private set

    lateinit var sensorsManager: SensorsManager
        private set

    lateinit var locationTracker: LocationTracker
        private set

    fun init(context: Context, scope: CoroutineScope) {
        if (isInitialized) return
        isInitialized = true

        headingProvider = HeadingProvider(context)
        sensorsManager = SensorsManager(
            headingProvider = headingProvider,
            stepDetectorProvider = StepDetectorProvider(context),
            linearAccelerationProvider = LinearAccelerationProvider(context),
        )
        locationTracker = LocationTracker(
            sensorsManager = sensorsManager,
            scope = scope,
            context = context
        )
        locationTracker.start()
    }


}