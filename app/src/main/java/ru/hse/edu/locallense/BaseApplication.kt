package ru.hse.edu.locallense

import android.app.Application
import ru.hse.locallense.common.Core
import ru.hse.locallense.common_impl.DefaultCoreProvider

/**
 * Base application class for setting up application-wide dependencies and configurations.
 */
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Core.init(DefaultCoreProvider(this))
    }
}