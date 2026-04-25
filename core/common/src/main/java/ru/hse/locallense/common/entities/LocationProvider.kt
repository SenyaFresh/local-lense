package ru.hse.locallense.common.entities

interface LocationProvider {
    val current: LocationData?
}
