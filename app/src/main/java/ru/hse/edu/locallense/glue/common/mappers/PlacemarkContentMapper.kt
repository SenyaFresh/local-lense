package ru.hse.edu.locallense.glue.common.mappers

import ru.hse.edu.placemarks.entities.PlacemarkType

data class PlacemarkContentDescriptor(
    val type: PlacemarkType,
    val content: String?,
    val contentSecondary: String?,
)

inline fun <T> PlacemarkType.toDomainType(
    content: String?,
    contentSecondary: String?,
    simple: () -> T,
    text: (String) -> T,
    photo: (String) -> T,
    textPhoto: (text: String, photoPath: String) -> T,
): T = when (this) {
    PlacemarkType.SIMPLE -> simple()
    PlacemarkType.TEXT -> text(content.orEmpty())
    PlacemarkType.PHOTO -> photo(content.orEmpty())
    PlacemarkType.TEXT_PHOTO -> textPhoto(content.orEmpty(), contentSecondary.orEmpty())
}
