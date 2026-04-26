package ru.hse.edu.locallense.glue.placemarks.mappers

import androidx.compose.ui.graphics.Color
import ru.hse.edu.locallense.glue.common.mappers.toDomainType
import ru.hse.edu.locallense.glue.common.mappers.toTag
import ru.hse.edu.placemarks.domain.entities.Placemark
import ru.hse.edu.placemarks.entities.PlacemarkWithTags
import ru.hse.locallense.common.entities.LocationData

fun PlacemarkWithTags.toPlacemark(): Placemark {
    return Placemark(
        id = placemark.id,
        name = placemark.name,
        color = Color(placemark.color),
        tags = tags.map { it.toTag() },
        type = placemark.type.toDomainType(
            content = placemark.content,
            contentSecondary = placemark.contentSecondary,
            simple = { Placemark.Type.Simple },
            text = { Placemark.Type.Text(it) },
            photo = { Placemark.Type.Photo(it) },
            textPhoto = { text, photoPath -> Placemark.Type.TextPhoto(text, photoPath) },
        ),
        locationData = LocationData(
            latitude = placemark.latitude,
            longitude = placemark.longitude,
            altitude = placemark.altitude,
        ),
    )
}
