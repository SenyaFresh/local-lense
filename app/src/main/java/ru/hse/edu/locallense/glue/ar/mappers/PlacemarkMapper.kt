package ru.hse.edu.locallense.glue.ar.mappers

import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.edu.placemarks.entities.PlacemarkDataEntity
import ru.hse.edu.placemarks.entities.PlacemarkType
import ru.hse.locallense.common.entities.LocationData

fun ArPlacemark.toPlacemarkDataEntity(): PlacemarkDataEntity {
    val arType = type

    val entityType = when (arType) {
        is ArPlacemark.Type.Simple -> PlacemarkType.SIMPLE
        is ArPlacemark.Type.Text -> PlacemarkType.TEXT
        is ArPlacemark.Type.Photo -> PlacemarkType.PHOTO
        is ArPlacemark.Type.Audio -> PlacemarkType.AUDIO
    }

    val entityContent = when (arType) {
        is ArPlacemark.Type.Text -> arType.text
        is ArPlacemark.Type.Photo -> arType.filepath
        is ArPlacemark.Type.Audio -> arType.filepath
        is ArPlacemark.Type.Simple -> null
    }

    return PlacemarkDataEntity(
        id = id,
        name = name,
        longitude = locationData.longitude,
        latitude = locationData.latitude,
        altitude = locationData.altitude,
        isWallAnchor = isWallAnchor,
        type = entityType,
        content = entityContent,
    )
}

fun PlacemarkDataEntity.toArPlacemark(): ArPlacemark {
    val arType = when (type) {
        PlacemarkType.SIMPLE -> ArPlacemark.Type.Simple
        PlacemarkType.TEXT -> ArPlacemark.Type.Text(content ?: "")
        PlacemarkType.PHOTO -> ArPlacemark.Type.Photo(content ?: "")
        PlacemarkType.AUDIO -> ArPlacemark.Type.Audio(content ?: "")
    }

    return ArPlacemark(
        id = id,
        name = name,
        type = arType,
        locationData = LocationData(
            latitude = latitude,
            longitude = longitude,
            altitude = altitude,
        ),
        isWallAnchor = isWallAnchor,
    )
}