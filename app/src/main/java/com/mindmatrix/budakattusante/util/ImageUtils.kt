package com.mindmatrix.budakattusante.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    /**
     * Requirement 8 & 16: Compress images offline before queuing for sync.
     * Reduces data usage for forest areas and optimizes storage.
     */
    fun compressImage(context: Context, uri: Uri, quality: Int = 70): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(outputFile)
            
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()
            
            outputFile
        } catch (e: Exception) {
            null
        }
    }
}
