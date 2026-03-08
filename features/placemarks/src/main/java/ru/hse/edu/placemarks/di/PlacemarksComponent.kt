package ru.hse.edu.placemarks.di

import dagger.BindsInstance
import dagger.Component
import ru.hse.locallense.common.di.Feature

@Feature
@Component(modules = [PlacemarksModule::class])
internal interface PlacemarksComponent {

    fun inject(it: PlacemarksDiContainer)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun deps(deps: PlacemarksDeps): Builder

        fun build(): PlacemarksComponent
    }
}