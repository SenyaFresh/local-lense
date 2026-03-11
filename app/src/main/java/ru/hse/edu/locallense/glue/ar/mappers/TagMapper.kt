package ru.hse.edu.locallense.glue.ar.mappers

import ru.hse.edu.placemarks.entities.TagDataEntity
import ru.hse.locallense.common.entities.Tag

fun Tag.toTagDataEntity(): TagDataEntity {
    return TagDataEntity(
        id = id,
        name = name,
    )
}

fun TagDataEntity.toTag(): Tag {
    return Tag(
        id = id,
        name = name,
    )
}