package ru.hse.edu.locallense

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import ru.hse.edu.ar.di.ArDepsStore
import ru.hse.edu.locallense.di.AppComponent
import ru.hse.edu.locallense.di.DaggerAppComponent
import ru.hse.edu.placemarks.di.PlacemarksDepsStore
import ru.hse.locallense.common.Core

class BaseApplication : Application() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .context(this)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        Core.init(appComponent.coreProvider)

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)

        PlacemarksDepsStore.deps = appComponent
        ArDepsStore.deps = appComponent
    }
}