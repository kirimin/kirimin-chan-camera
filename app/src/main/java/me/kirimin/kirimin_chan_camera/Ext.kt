package me.kirimin.kirimin_chan_camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import android.graphics.BitmapFactory

fun Int.toDp(context: Context) = this / context.resources.displayMetrics.density

fun Uri.rotateFixedBitmap(context: Context): Bitmap {
    fun rotateImage(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return rotatedImg
    }

    val inputStream = context.contentResolver.openInputStream(this)!!
    val exifInterface = ExifInterface(inputStream)
    inputStream.close()
    val stream = context.contentResolver.openInputStream(this)!!
    val bitmap = BitmapFactory.decodeStream(stream)
    stream.close()
    val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
        else -> bitmap
    }
}