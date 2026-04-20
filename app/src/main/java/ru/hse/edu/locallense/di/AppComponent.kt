package ru.hse.edu.locallense.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.hse.edu.ar.di.ArDeps
import ru.hse.edu.ar.domain.repositories.ArPlacemarksRepository
import ru.hse.edu.locallense.CoreModule
import ru.hse.edu.locallense.MainActivity
import ru.hse.edu.placemarks.di.PlacemarksDeps
import ru.hse.edu.placemarks.domain.repositories.PlacemarksRepository
import ru.hse.locallense.common.CoreProvider
import ru.hse.locallense.common.di.AppScope

@AppScope
@Component(
    modules = [
        AppModule::class,
        CoreModule::class,
    ]
)
interface AppComponent : PlacemarksDeps, ArDeps {

    override val placemarksRepository: PlacemarksRepository

    override val arPlacemarksRepository: ArPlacemarksRepository

    fun inject(mainActivity: MainActivity)

    val coreProvider: CoreProvider

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent
    }
}