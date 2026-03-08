package ru.hse.edu.locallense.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import ru.hse.edu.locallense.CoreModule
import ru.hse.edu.locallense.MainActivity
import ru.hse.locallense.common.CoreProvider
import ru.hse.locallense.common.di.AppScope

@AppScope
@Component(
    modules = [
        AppModule::class,
        CoreModule::class,
    ]
)
interface AppComponent {

    fun inject(mainActivity: MainActivity)

    val coreProvider: CoreProvider

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: Context) : Builder

        fun build() : AppComponent
    }
}