package ru.hse.edu.geoar.location

sealed class LocationException(message: String) : Exception(message)

class PermissionDeniedException : LocationException("Разрешения ACCESS_FINE_LOCATION или ACCESS_COARSE_LOCATION не предоставлены")
class GpsDisabledException : LocationException("GPS отключен на девайсе")
class UnknownLocationException(exception: Throwable) : LocationException(exception.message ?: "Неизвестная ошибка")