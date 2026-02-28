package ru.hse.locallense.common

/**
 * Toast holder that can be shown from anywhere.
 */
interface Toaster {

    /**
     * Show some toast message to user.
     */
    fun showToast(message: String)

}