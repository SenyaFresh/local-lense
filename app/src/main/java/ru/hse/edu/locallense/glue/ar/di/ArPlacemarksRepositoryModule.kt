package ru.hse.edu.locallense.glue.ar.di

import dagger.Binds
import dagger.Module
import ru.hse.edu.ar.domain.repositories.ArPlacemarksRepository
import ru.hse.edu.locallense.glue.ar.repositories.AdapterArPlacemarksRepository

@Module
interface ArPlacemarksRepositoryModule {

    @Binds
    fun bindArPlacemarksRepository(
        adapter: AdapterArPlacemarksRepository
    ): ArPlacemarksRepository

}