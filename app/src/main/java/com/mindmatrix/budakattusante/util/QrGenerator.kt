package com.mindmatrix.budakattusante.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

/**
 * Requirement 4: Offline QR generation support for batch tracking.
 * Provides tribal users the ability to generate traceability QR codes even without internet.
 */
object QrGenerator {
    fun generateQrCode(content: String, size: Int = 512): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                content, 
                BarcodeFormat.QR_CODE, 
                size, 
                size
            )
            val w = bitMatrix.width
            val h = bitMatrix.height
            val pixels = IntArray(w * h)
            for (y in 0 until h) {
                val offset = y * w
                for (x in 0 until w) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
