package dev.vibebridge.core

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object Qr {
    fun bitmap(content: String, size: Int = 512): Bitmap? = try {
        val hints = mapOf<EncodeHintType, Any>(EncodeHintType.MARGIN to 2)
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (matrix.get(x, y)) 0xFF0A0C09.toInt() else 0xFFE6EAE4.toInt())
            }
        }
        bmp
    } catch (e: Exception) {
        null
    }
}
