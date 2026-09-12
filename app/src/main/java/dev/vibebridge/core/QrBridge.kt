package dev.vibebridge.core

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter

object QrBridge {

    fun encode(text: String, size: Int = 512): Bitmap? = try {
        val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).apply {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    setPixel(x, y, if (matrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
        }
    } catch (e: Exception) {
        null
    }

    fun decode(bmp: Bitmap): String? = try {
        val w = bmp.width
        val h = bmp.height
        val pixels = IntArray(w * h)
bmp.getPixels(pixels, 0, w, 0, 0, w, h)
        val source = RGBLuminanceSource(w, h, pixels)
        MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source))).text
    } catch (e: Exception) {
        null
    }
}
