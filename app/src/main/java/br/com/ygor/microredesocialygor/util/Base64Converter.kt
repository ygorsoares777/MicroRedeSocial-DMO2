package br.com.ygor.microredesocialygor.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Base64
import java.io.ByteArrayOutputStream

fun Bitmap.scale(width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(this, width, height, true)
}

class Base64Converter {
    companion object {

        fun drawableToString(drawable: Drawable): String {
            val pictureDrawable = drawable as BitmapDrawable
            val bitmap = pictureDrawable.bitmap.scale(150, 150)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            return Base64.encodeToString(outputStream.toByteArray(), 0)
        }

        fun drawableToStringPost(drawable: Drawable): String {
            val pictureDrawable = drawable as BitmapDrawable
            val bitmapOriginal = pictureDrawable.bitmap

            val width = if (bitmapOriginal.width > 800) 800 else bitmapOriginal.width
            val height = (bitmapOriginal.height * width / bitmapOriginal.width)
            val bitmap = bitmapOriginal.scale(width, height)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            return Base64.encodeToString(outputStream.toByteArray(), 0)
        }

        fun stringToBitmap(imageString: String): Bitmap {
            val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }
}