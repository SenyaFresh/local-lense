package ru.hse.edu.ar.presentation.mapkit

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import java.util.Locale
import kotlin.math.abs

fun createPinBitmap(
    color: Int,
    label: String,
    sizePx: Int,
): Bitmap {
    val w = sizePx
    val h = (sizePx * 1.4f).toInt()
    val bitmap = createBitmap(w, h)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val r = w / 2f

    paint.color = color
    paint.style = Paint.Style.FILL

    val path = Path().apply {
        moveTo(r - r * 0.4f, r + r * 0.3f)
        lineTo(r, h.toFloat() - 2f)
        lineTo(r + r * 0.4f, r + r * 0.3f)
        close()
    }
    canvas.drawPath(path, paint)
    canvas.drawCircle(r, r, r - 2f, paint)

    paint.color = android.graphics.Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = sizePx * 0.04f
    canvas.drawCircle(r, r, r - 2f, paint)

    if (label.isNotBlank()) {
        paint.color = android.graphics.Color.WHITE
        paint.style = Paint.Style.FILL
        paint.textSize = r * 0.7f
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.CENTER
        val fm = paint.fontMetrics
        val textY = r - (fm.ascent + fm.descent) / 2f
        canvas.drawText(label.take(2).uppercase(), r, textY, paint)
    }

    return bitmap
}

fun formatCoordinates(latitude: Double, longitude: Double): String {
    val latDir = if (latitude >= 0) "N" else "S"
    val lonDir = if (longitude >= 0) "E" else "W"
    return String.format(
        Locale.ROOT,
        "%.4f° %s, %.4f° %s",
        abs(latitude), latDir,
        abs(longitude), lonDir,
    )
}