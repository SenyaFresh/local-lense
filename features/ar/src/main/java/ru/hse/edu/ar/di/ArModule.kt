package ru.hse.edu.ar.di

import dagger.Module
import dagger.Provides
import ru.hse.edu.ar.domain.repositories.ArPlacemarksRepository
import ru.hse.locallense.common.di.Feature

@Module
class ArModule {

    @Provides
    @Feature
    fun provideArRepository(deps: ArDeps): ArPlacemarksRepository = deps.arPlacemarksRepository

}