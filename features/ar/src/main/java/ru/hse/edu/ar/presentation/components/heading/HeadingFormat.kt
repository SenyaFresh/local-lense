package ru.hse.edu.ar.presentation.components.heading

import kotlin.math.roundToInt

private val CardinalLabels = arrayOf("С", "СВ", "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ")

internal fun normalizeHeading(heading: Float): Float {
    val normalized = heading % 360f
    return if (normalized < 0f) normalized + 360f else normalized
}

internal fun formatHeading(heading: Float): String =
    "${formatHeadingDegrees(heading)} · ${headingDirectionLabel(heading)}"

internal fun formatHeadingDegrees(heading: Float): String {
    val degrees = normalizeHeading(heading).roundToInt() % 360
    return "$degrees°"
}

internal fun headingDirectionLabel(heading: Float): String {
    val normalized = normalizeHeading(heading)
    val index = ((normalized + 22.5f) / 45f).toInt() % CardinalLabels.size
    return CardinalLabels[index]
}
