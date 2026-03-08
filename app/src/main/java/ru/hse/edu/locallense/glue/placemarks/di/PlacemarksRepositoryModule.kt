package ru.hse.edu.locallense.glue.placemarks.di

import dagger.Binds
import dagger.Module
import ru.hse.edu.locallense.glue.placemarks.repositories.AdapterPlacemarksRepository
import ru.hse.edu.placemarks.domain.repositories.PlacemarksRepository

@Module
interface PlacemarksRepositoryModule {

    @Binds
    fun bindPlacemarksRepository(
        adapter: AdapterPlacemarksRepository
    ): PlacemarksRepository

}