package com.example.aveslens.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

object ImageCompressor {
    private const val MAX_BYTES = 2 * 1024 * 1024 // 2 MB
    private const val QUALITY_START = 90
    private const val QUALITY_STEP = 10
    private const val QUALITY_MIN = 40

    fun compress(context: Context, uri: Uri): ByteArray {
        val bitmap = context.contentResolver.openInputStream(uri).use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: return ByteArray(0)

        var quality = QUALITY_START
        var bytes: ByteArray
        do {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            bytes = baos.toByteArray()
            quality -= QUALITY_STEP
        } while (bytes.size > MAX_BYTES && quality >= QUALITY_MIN)

        return bytes
    }
}
