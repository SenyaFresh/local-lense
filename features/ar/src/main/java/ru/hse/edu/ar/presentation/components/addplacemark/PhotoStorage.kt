package ru.hse.edu.ar.presentation.components.addplacemark

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

private const val PHOTO_DIR_NAME = "placemark_photos"
private const val PHOTO_EXTENSION = ".jpg"

internal fun copyImageUriToInternal(context: Context, uri: Uri): String? = runCatching {
    val dir = File(context.filesDir, PHOTO_DIR_NAME).apply { mkdirs() }
    val target = File(dir, "${UUID.randomUUID()}$PHOTO_EXTENSION")
    val copied = context.contentResolver.openInputStream(uri)?.use { input ->
        target.outputStream().use { input.copyTo(it) }
        true
    } ?: false
    if (copied) target.absolutePath else null
}.getOrNull()
