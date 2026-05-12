package com.jp.foodyvilla_backoffice.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

fun compressImage(context: Context, uri: Uri): ByteArray {
    val inputStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(inputStream)

    val outputStream = ByteArrayOutputStream()

    bitmap.compress(
        Bitmap.CompressFormat.JPEG,
        60, // 🔥 compression quality (0–100)
        outputStream
    )

    return outputStream.toByteArray()
}