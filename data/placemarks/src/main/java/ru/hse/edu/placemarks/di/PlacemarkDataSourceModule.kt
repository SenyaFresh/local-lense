package ru.hse.edu.placemarks.di

import dagger.Binds
import dagger.Module
import ru.hse.edu.placemarks.sources.PlacemarksDataSource
import ru.hse.edu.placemarks.sources.RoomPlacemarksDataSource
import ru.hse.locallense.common.di.AppScope

@Module
interface PlacemarkDataSourceModule {

    @Binds
    @AppScope
    fun bindPlacemarksDataSource(
        impl: RoomPlacemarksDataSource
    ): PlacemarksDataSource

}
