package ru.hse.locallense.common_impl

import android.content.Context
import ru.hse.locallense.common.Resources

class AndroidResources(
    private val appContext: Context
) : Resources {

    override fun getString(id: Int): String {
        return appContext.getString(id)
    }

    override fun getString(id: Int, vararg placeholders: Any): String {
        return appContext.getString(id, placeholders)
    }

}