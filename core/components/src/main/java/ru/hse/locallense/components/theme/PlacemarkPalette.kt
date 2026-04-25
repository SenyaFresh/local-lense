package ru.hse.locallense.components.theme

import androidx.compose.ui.graphics.Color
import ru.hse.locallense.common.entities.Tag

object PlacemarkPalette {

    val Default: Color = Color(0xFF7C4DFF)

    val Presets: List<Color> = listOf(
        Color(0xFFEF5350),
        Color(0xFFFF9800),
        Color(0xFFFFEB3B),
        Color(0xFF4CAF50),
        Color(0xFF2196F3),
        Color(0xFF7C4DFF),
    )

    val TagColors: List<Color> = listOf(
        Color(0xFF1565C0),
        Color(0xFF2E7D32),
        Color(0xFFE65100),
        Color(0xFF6A1B9A),
        Color(0xFF00838F),
        Color(0xFFC62828),
        Color(0xFFF9A825),
        Color(0xFF37474F),
    )

    fun tagColor(tagName: String): Color =
        TagColors[(tagName.hashCode() and 0x7FFFFFFF) % TagColors.size]

    fun tagColor(tag: Tag): Color = tagColor(tag.name)
}

object RgbChannel {
    val Red: Color = Color(0xFFF44336)
    val Green: Color = Color(0xFF4CAF50)
    val Blue: Color = Color(0xFF2196F3)
}
