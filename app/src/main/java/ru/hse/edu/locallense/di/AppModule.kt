package ru.hse.edu.locallense.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

/**
 * Dagger module that provides dependencies for the app.
 */
@Module(
    includes = [

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