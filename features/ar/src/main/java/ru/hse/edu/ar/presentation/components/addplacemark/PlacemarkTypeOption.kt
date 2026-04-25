package ru.hse.edu.ar.presentation.components.addplacemark

import androidx.annotation.StringRes
import ru.hse.edu.ar.R

enum class PlacemarkTypeOption(@StringRes val labelRes: Int) {
    SIMPLE(R.string.ar_type_simple),
    TEXT(R.string.ar_type_text),
    PHOTO_TEXT(R.string.ar_type_photo_text),
}
