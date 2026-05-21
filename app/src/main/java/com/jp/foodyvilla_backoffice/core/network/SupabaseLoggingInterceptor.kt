package com.jp.foodyvilla_backoffice.core.network

import android.util.Log
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class SupabaseLoggingInterceptor(
    private val tag: String = TAG
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestStartedAt = System.nanoTime()

        Log.d(tag, ">>> ${request.method} ${request.url}")
        Log.d(tag, ">>> headers=${request.headers.redacted()}")

        val requestBody = request.body
        if (requestBody != null) {
            Log.d(tag, ">>> contentType=${requestBody.contentType()} contentLength=${requestBody.contentLengthSafely()}")
            Log.d(tag, ">>> body=${requestBody.readBodySafely()}")
        } else {
            Log.d(tag, ">>> body=<empty>")
        }

        val response = try {
            chain.proceed(request)
        } catch (throwable: IOException) {
            val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - requestStartedAt)
            Log.e(tag, "xxx network error after ${elapsedMs}ms: ${request.method} ${request.url}", throwable)
            throw throwable
        } catch (throwable: Throwable) {
            val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - requestStartedAt)
            Log.e(tag, "xxx unexpected network error after ${elapsedMs}ms: ${request.method} ${request.url}", throwable)
            throw throwable
        }

        val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - requestStartedAt)
        val responseBodyText = response.readResponseBodyForLog()
        val logMessage = "<<< ${response.code} ${response.message} ${request.method} ${request.url} (${elapsedMs}ms)\n" +
            "<<< headers=${response.headers.redacted()}\n" +
            "<<< body=$responseBodyText"

        if (response.isSuccessful) {
            Log.d(tag, logMessage)
        } else {
            Log.e(tag, logMessage)
        }

        return response
    }

    private fun Headers.redacted(): String {
        if (size == 0) return "{}"
        return buildString {
            append("{")
            for (index in 0 until size) {
                if (index > 0) append(", ")
                val name = name(index)
                append(name)
                append("=")
                append(if (name.isSensitiveHeader()) "<redacted>" else value(index))
            }
            append("}")
        }
    }

    private fun String.isSensitiveHeader(): Boolean {
        return equals("authorization", ignoreCase = true) ||
            equals("apikey", ignoreCase = true) ||
            equals("x-supabase-auth", ignoreCase = true)
    }

    private fun okhttp3.RequestBody.contentLengthSafely(): Long {
        return runCatching { contentLength() }.getOrDefault(-1L)
    }

    private fun okhttp3.RequestBody.readBodySafely(): String {
        if (isDuplex()) return "<duplex body omitted>"
        if (isOneShot()) return "<one-shot body omitted>"

        return runCatching {
            val buffer = Buffer()
            writeTo(buffer)
            val charset = contentType().charsetOrUtf8()
            buffer.readString(charset).ifBlank { "<empty>" }
        }.getOrElse { throwable ->
            "<could not read request body: ${throwable.message}>"
        }
    }

    private fun Response.readResponseBodyForLog(): String {
        val body = body ?: return "<empty>"
        val contentType = body.contentType()?.toString().orEmpty()

        if (request.header("Upgrade").equals("websocket", ignoreCase = true) ||
            header("Upgrade").equals("websocket", ignoreCase = true) ||
            contentType.contains("event-stream", ignoreCase = true)
        ) {
            return "<streaming body omitted>"
        }

        return runCatching {
            peekBody(MAX_LOG_BODY_BYTES).string().ifBlank { "<empty>" }
        }.getOrElse { throwable ->
            "<unreadable body omitted: ${throwable.message}>"
        }
    }

    private fun MediaType?.charsetOrUtf8(): Charset {
        return this?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
    }

    private companion object {
        const val TAG = "SupabaseHTTP"
        const val MAX_LOG_BODY_BYTES = 1024L * 1024L
    }
}
