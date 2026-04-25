package ru.hse.edu.locallense.glue.location

import dagger.Binds
import dagger.Module
import ru.hse.locallense.common.di.AppScope
import ru.hse.locallense.common.entities.LocationProvider

@Module
interface LocationProviderModule {

    @Binds
    @AppScope
    fun bindLocationProvider(impl: GeoArLocationProvider): LocationProvider
}
