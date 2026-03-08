package ru.hse.edu.placemarks.di

import ru.hse.edu.placemarks.domain.repositories.PlacemarksRepository
import kotlin.properties.Delegates.notNull

interface PlacemarksDeps {
    val placemarksRepository: PlacemarksRepository
}

interface PlacemarksDepsProvider {

    val deps: PlacemarksDeps

    companion object : PlacemarksDepsProvider by PlacemarksDepsStore
}

object PlacemarksDepsStore : PlacemarksDepsProvider {
    override var deps: PlacemarksDeps by notNull()
}