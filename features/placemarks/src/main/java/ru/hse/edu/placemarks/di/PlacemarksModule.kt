package ru.hse.edu.placemarks.di

import dagger.Module
import dagger.Provides
import ru.hse.edu.placemarks.domain.repositories.PlacemarksRepository
import ru.hse.locallense.common.di.Feature
import ru.hse.locallense.common.entities.LocationProvider

@Module
class PlacemarksModule {

    @Provides
    @Feature
    fun providePlacemarksRepository(deps: PlacemarksDeps): PlacemarksRepository =
        deps.placemarksRepository

    @Provides
    @Feature
    fun provideLocationProvider(deps: PlacemarksDeps): LocationProvider =
        deps.locationProvider
}