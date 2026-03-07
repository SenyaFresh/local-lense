package ru.hse.edu.geoar.math

import kotlin.math.pow
import kotlin.math.roundToInt

fun Float.round(decimals: Int): Float {
    val multiplier = 10.0.pow(decimals.toDouble()).toFloat()
    return (this * multiplier).roundToInt() / multiplier
}

fun Double.round(decimals: Int): Double {
    val multiplier = 10.0.pow(decimals.toDouble())
    return (this * multiplier).roundToInt() / multiplier
}