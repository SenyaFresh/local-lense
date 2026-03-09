package ru.hse.edu.ar.di

import dagger.BindsInstance
import dagger.Component
import ru.hse.locallense.common.di.Feature

@Feature
@Component(modules = [ArModule::class])
internal interface ArComponent {

    fun inject(it: ArDiContainer)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun deps(deps: ArDeps): Builder

        fun build(): ArComponent
    }
}