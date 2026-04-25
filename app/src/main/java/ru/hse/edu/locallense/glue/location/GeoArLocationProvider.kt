package ru.hse.edu.locallense.glue.location

import ru.hse.edu.geoar.ar.ArGeoFactory
import ru.hse.locallense.common.entities.LocationData
import ru.hse.locallense.common.entities.LocationProvider
import javax.inject.Inject

class GeoArLocationProvider @Inject constructor() : LocationProvider {
    override val current: LocationData?
        get() = ArGeoFactory.locationTracker.locationState.value
}
