package ru.hse.edu.locallense

import android.app.Application
import ru.hse.edu.locallense.di.AppComponent
import ru.hse.edu.locallense.di.DaggerAppComponent
import ru.hse.edu.placemarks.di.PlacemarksDepsStore
import ru.hse.locallense.common.Core

/**
 * Base application class for setting up application-wide dependencies and configurations.
 */
class BaseApplication : Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .context(this)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        Core.init(appComponent.coreProvider)

        PlacemarksDepsStore.deps = appComponent

    }
}