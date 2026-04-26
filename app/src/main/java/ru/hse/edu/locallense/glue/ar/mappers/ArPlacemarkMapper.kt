package ru.hse.edu.locallense.glue.ar.mappers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.edu.locallense.glue.common.mappers.PlacemarkContentDescriptor
import ru.hse.edu.locallense.glue.common.mappers.toDomainType
import ru.hse.edu.locallense.glue.common.mappers.toTag
import ru.hse.edu.locallense.glue.common.mappers.toTagDataEntity
import ru.hse.edu.placemarks.entities.PlacemarkDataEntity
import ru.hse.edu.placemarks.entities.PlacemarkType
import ru.hse.edu.placemarks.entities.PlacemarkWithTags
import ru.hse.locallense.common.entities.LocationData

fun ArPlacemark.toPlacemarkWithTags(): PlacemarkWithTags {
    val descriptor = type.toContentDescriptor()
    val entity = PlacemarkDataEntity(
        id = id,
        name = name,
        longitude = locationData.longitude,
        latitude = locationData.latitude,
        altitude = locationData.altitude,
        isWallAnchor = isWallAnchor,
        color = color.toArgb(),
        type = descriptor.type,
        content = descriptor.content,
        contentSecondary = descriptor.contentSecondary,
    )

    return PlacemarkWithTags(
        placemark = entity,
        tags = tags.map { it.toTagDataEntity() }
    )
}

fun PlacemarkWithTags.toArPlacemark(): ArPlacemark {
    return ArPlacemark(
        id = placemark.id,
        name = placemark.name,
        color = Color(placemark.color),
        tags = tags.map { it.toTag() },
        type = placemark.type.toDomainType(
            content = placemark.content,
            contentSecondary = placemark.contentSecondary,
            simple = { ArPlacemark.Type.Simple },
            text = { ArPlacemark.Type.Text(it) },
            photo = { ArPlacemark.Type.Photo(it) },
            textPhoto = { text, photoPath -> ArPlacemark.Type.TextPhoto(text, photoPath) },
        ),
        locationData = LocationData(
            latitude = placemark.latitude,
            longitude = placemark.longitude,
            altitude = placemark.altitude,
        ),
        isWallAnchor = placemark.isWallAnchor,
    )
}

private fun ArPlacemark.Type.toContentDescriptor(): PlacemarkContentDescriptor = when (this) {
    is ArPlacemark.Type.Simple -> PlacemarkContentDescriptor(PlacemarkType.SIMPLE, null, null)
    is ArPlacemark.Type.Text -> PlacemarkContentDescriptor(PlacemarkType.TEXT, text, null)
    is ArPlacemark.Type.Photo -> PlacemarkContentDescriptor(PlacemarkType.PHOTO, filepath, null)
    is ArPlacemark.Type.TextPhoto -> PlacemarkContentDescriptor(PlacemarkType.TEXT_PHOTO, text, photoPath)
}
