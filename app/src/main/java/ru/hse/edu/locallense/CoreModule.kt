package ru.hse.edu.locallense

import android.content.Context
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers
import ru.hse.locallense.common.CoreProvider
import ru.hse.locallense.common.di.AppScope
import ru.hse.locallense.common.flow.DefaultLazyFlowLoaderFactory
import ru.hse.locallense.common.flow.LazyFlowLoaderFactory
import ru.hse.locallense.common_impl.DefaultCoreProvider

@Module
class CoreModule {

    @Provides
    fun provideCoreProvider(
        context: Context
    ): CoreProvider {
        return DefaultCoreProvider(context)
    }

    @Provides
    @AppScope
    fun provideLazyFlowLoaderFactory(): LazyFlowLoaderFactory {
        return DefaultLazyFlowLoaderFactory(Dispatchers.IO)
    }

}