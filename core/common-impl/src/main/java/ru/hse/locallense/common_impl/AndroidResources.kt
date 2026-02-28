package ru.hse.locallense.common_impl

import android.content.Context
import ru.hse.locallense.common.Resources

/**
 * Android realization of [Resources]
 */
class AndroidResources(
    private val appContext: Context
): Resources {

    /**
     * Get string resource from [appContext].
     */
    override fun getString(id: Int): String {
        return appContext.getString(id)
    }

    /**
     * Get string resource with [placeholders] from [appContext].
     */
    override fun getString(id: Int, vararg placeholders: Any): String {
        return appContext.getString(id, placeholders)
    }

}