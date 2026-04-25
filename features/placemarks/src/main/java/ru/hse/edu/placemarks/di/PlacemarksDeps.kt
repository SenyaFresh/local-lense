package ru.hse.edu.placemarks.di

import ru.hse.edu.placemarks.domain.repositories.PlacemarksRepository
import ru.hse.locallense.common.entities.LocationProvider
import kotlin.properties.Delegates.notNull

interface PlacemarksDeps {
    val placemarksRepository: PlacemarksRepository
    val locationProvider: LocationProvider
}

interface PlacemarksDepsProvider {

    val deps: PlacemarksDeps

    companion object : PlacemarksDepsProvider by PlacemarksDepsStore
}

object PlacemarksDepsStore : PlacemarksDepsProvider {
    override var deps: PlacemarksDeps by notNull()
}