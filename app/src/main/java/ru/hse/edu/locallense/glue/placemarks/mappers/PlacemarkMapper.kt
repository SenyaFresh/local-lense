package ru.hse.edu.locallense.glue.placemarks.mappers

import androidx.compose.ui.graphics.Color
import ru.hse.edu.locallense.glue.ar.mappers.toTag
import ru.hse.edu.placemarks.domain.entities.Placemark
import ru.hse.edu.placemarks.entities.PlacemarkType
import ru.hse.edu.placemarks.entities.PlacemarkWithTags
import ru.hse.locallense.common.entities.LocationData

fun PlacemarkWithTags.toPlacemark(): Placemark {
    val arType = when (placemark.type) {
        PlacemarkType.SIMPLE -> Placemark.Type.Simple
        PlacemarkType.TEXT -> Placemark.Type.Text(placemark.content ?: "")
        PlacemarkType.PHOTO -> Placemark.Type.Photo(placemark.content ?: "")
        PlacemarkType.AUDIO -> Placemark.Type.Audio(placemark.content ?: "")
        PlacemarkType.TEXT_PHOTO -> Placemark.Type.TextPhoto(
            text = placemark.content.orEmpty(),
            photoPath = placemark.contentSecondary.orEmpty(),
        )
    }

    return Placemark(
        id = placemark.id,
        name = placemark.name,
        color = Color(placemark.color),
        tags = tags.map { it.toTag() },
        type = arType,
        locationData = LocationData(
            latitude = placemark.latitude,
            longitude = placemark.longitude,
            altitude = placemark.altitude,
        ),
    )
}