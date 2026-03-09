package ru.hse.edu.placemarks.di

import dagger.Binds
import dagger.Module
import ru.hse.edu.placemarks.repositories.PlacemarksDataRepository
import ru.hse.edu.placemarks.repositories.RoomPlacemarksDataRepository
import ru.hse.locallense.common.di.AppScope

@Module(includes = [PlacemarkDataSourceModule::class])
interface PlacemarksDataRepositoryModule {

    @Binds
    @AppScope
    fun bindPlacemarksDataRepository(
        impl: RoomPlacemarksDataRepository
    ): PlacemarksDataRepository
}