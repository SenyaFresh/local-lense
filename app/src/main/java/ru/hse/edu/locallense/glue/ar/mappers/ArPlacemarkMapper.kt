package ru.hse.edu.locallense.glue.ar.mappers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.edu.placemarks.entities.PlacemarkDataEntity
import ru.hse.edu.placemarks.entities.PlacemarkType
import ru.hse.edu.placemarks.entities.PlacemarkWithTags
import ru.hse.locallense.common.entities.LocationData

fun ArPlacemark.toPlacemarkWithTags(): PlacemarkWithTags {
    val arType = type

    val entityType = when (arType) {
        is ArPlacemark.Type.Simple -> PlacemarkType.SIMPLE
        is ArPlacemark.Type.Text -> PlacemarkType.TEXT
        is ArPlacemark.Type.Photo -> PlacemarkType.PHOTO
        is ArPlacemark.Type.Audio -> PlacemarkType.AUDIO
        is ArPlacemark.Type.TextPhoto -> PlacemarkType.TEXT_PHOTO
    }

    val entityContent = when (arType) {
        is ArPlacemark.Type.Text -> arType.text
        is ArPlacemark.Type.Photo -> arType.filepath
        is ArPlacemark.Type.Audio -> arType.filepath
        is ArPlacemark.Type.TextPhoto -> arType.text
        is ArPlacemark.Type.Simple -> null
    }

    val entityContentSecondary = when (arType) {
        is ArPlacemark.Type.TextPhoto -> arType.photoPath
        else -> null
    }

    val entity = PlacemarkDataEntity(
        id = id,
        name = name,
        longitude = locationData.longitude,
        latitude = locationData.latitude,
        altitude = locationData.altitude,
        isWallAnchor = isWallAnchor,
        color = color.toArgb(),
        type = entityType,
        content = entityContent,
        contentSecondary = entityContentSecondary,
    )

    return PlacemarkWithTags(
        placemark = entity,
        tags = tags.map { it.toTagDataEntity() }
    )
}

fun PlacemarkWithTags.toArPlacemark(): ArPlacemark {
    val arType = when (placemark.type) {
        PlacemarkType.SIMPLE -> ArPlacemark.Type.Simple
        PlacemarkType.TEXT -> ArPlacemark.Type.Text(placemark.content ?: "")
        PlacemarkType.PHOTO -> ArPlacemark.Type.Photo(placemark.content ?: "")
        PlacemarkType.AUDIO -> ArPlacemark.Type.Audio(placemark.content ?: "")
        PlacemarkType.TEXT_PHOTO -> ArPlacemark.Type.TextPhoto(
            text = placemark.content.orEmpty(),
            photoPath = placemark.contentSecondary.orEmpty(),
        )
    }

    return ArPlacemark(
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
        isWallAnchor = placemark.isWallAnchor,
    )
}