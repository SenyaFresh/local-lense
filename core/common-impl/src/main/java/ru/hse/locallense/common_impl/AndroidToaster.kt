package ru.hse.locallense.common_impl

import android.content.Context
import android.widget.Toast
import ru.hse.locallense.common.Toaster

class AndroidToaster(
    private val appContext: Context
) : Toaster {

    override fun showToast(message: String) {
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
    }

}