package ru.hse.edu.locallense.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import ru.hse.edu.locallense.glue.ar.di.ArPlacemarksRepositoryModule
import ru.hse.edu.locallense.glue.location.LocationProviderModule
import ru.hse.edu.locallense.glue.placemarks.di.PlacemarksRepositoryModule
import ru.hse.edu.placemarks.di.PlacemarksDataRepositoryModule

/**
 * Dagger module that provides dependencies for the app.
 */
@Module(
    includes = [
        PlacemarksDataRepositoryModule::class,
        PlacemarksRepositoryModule::class,
        ArPlacemarksRepositoryModule::class,
        LocationProviderModule::class,
    ]
)
class AppModule {

    /**
     * Provides the application context.
     *
     * @param context The application context.
     * @return The application context.
     **/
    @Provides
    fun provideApplication(context: Context): Application {
        return context.applicationContext as Application
    }

}