package app.vanam.ui
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface

fun loadPhoto(path: String): Bitmap? = runCatching {
    val b = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = 4 }) ?: return@runCatching null
    val deg = when (ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) { 6 -> 90f; 3 -> 180f; 8 -> 270f; else -> 0f }
    if (deg == 0f) b else Bitmap.createBitmap(b, 0, 0, b.width, b.height, Matrix().apply { postRotate(deg) }, true)
}.getOrNull()
