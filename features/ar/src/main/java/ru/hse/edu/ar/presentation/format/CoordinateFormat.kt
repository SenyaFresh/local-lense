package ru.hse.edu.ar.presentation.format

import java.util.Locale
import kotlin.math.abs

fun formatCoordinates(latitude: Double, longitude: Double): String {
    val latDir = if (latitude >= 0) "C" else "Ю"
    val lonDir = if (longitude >= 0) "З" else "В"
    return String.format(
        Locale.ROOT,
        "%.4f° %s, %.4f° %s",
        abs(latitude), latDir,
        abs(longitude), lonDir,
    )
}
