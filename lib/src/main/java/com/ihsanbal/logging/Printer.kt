package com.ihsanbal.logging

import com.ihsanbal.logging.I.Companion.log
import okhttp3.Request
import okio.Buffer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

/**
 * @author ihsan on 09/02/2017.
 */
internal open class Printer protected constructor() {
    companion object {
        private const val JSON_INDENT = 3
        private val LINE_SEPARATOR = System.getProperty("line.separator")
        private val DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR
        private val OMITTED_RESPONSE = arrayOf(LINE_SEPARATOR, "Omitted response body")
        private val OMITTED_REQUEST = arrayOf(LINE_SEPARATOR, "Omitted request body")
        private const val N = "\n"
        private const val T = "\t"
        private const val REQUEST_UP_LINE = "┌────── Request ────────────────────────────────────────────────────────────────────────"
        private const val END_LINE = "└───────────────────────────────────────────────────────────────────────────────────────"
        private const val RESPONSE_UP_LINE = "┌────── Response ───────────────────────────────────────────────────────────────────────"
        private const val BODY_TAG = "Body:"
        private const val URL_TAG = "URL: "
        private const val METHOD_TAG = "Method: @"
        private const val HEADERS_TAG = "Headers:"
        private const val STATUS_CODE_TAG = "Status Code: "
        private const val RECEIVED_TAG = "Received in: "
        private const val CORNER_UP = "┌ "
        private const val CORNER_BOTTOM = "└ "
        private const val CENTER_LINE = "├ "
        private const val DEFAULT_LINE = "│ "
        private val OOM_OMITTED = LINE_SEPARATOR + "Output omitted because of Object size."
        private fun isEmpty(line: String): Boolean {
            return line.isEmpty() || N == line || T == line || line.trim { it <= ' ' }.isEmpty()
        }

        @JvmStatic
        fun printJsonRequest(builder: LoggingInterceptor.Builder, request: Request) {
            val requestBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + bodyToString(request)
            val tag = builder.getTag(true)
            if (builder.logger == null) log(builder.type, tag, REQUEST_UP_LINE, builder.isLogHackEnable)
            logLines(builder.type, tag, arrayOf(URL_TAG + request.url), builder.logger, false, builder.isLogHackEnable)
            logLines(builder.type, tag, getRequest(request, builder.level), builder.logger, true, builder.isLogHackEnable)
            if (builder.level === Level.BASIC || builder.level === Level.BODY) {
                logLines(builder.type, tag, requestBody.split(LINE_SEPARATOR).toTypedArray(), builder.logger, true, builder.isLogHackEnable)
            }
            if (builder.logger == null) log(builder.type, tag, END_LINE, builder.isLogHackEnable)
        }

        @JvmStatic
        fun printJsonResponse(builder: LoggingInterceptor.Builder, chainMs: Long, isSuccessful: Boolean,
                              code: Int, headers: String, bodyString: String, segments: List<String>, message: String, responseUrl: String) {
            val responseBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + getJsonString(bodyString)
            val tag = builder.getTag(false)
            val urlLine = arrayOf(URL_TAG + responseUrl, N)
            val response = getResponse(headers, chainMs, code, isSuccessful,
                    builder.level, segments, message)
            if (builder.logger == null) {
                log(builder.type, tag, RESPONSE_UP_LINE, builder.isLogHackEnable)
            }
            logLines(builder.type, tag, urlLine, builder.logger, true, builder.isLogHackEnable)
            logLines(builder.type, tag, response, builder.logger, true, builder.isLogHackEnable)
            if (builder.level === Level.BASIC || builder.level === Level.BODY) {
                logLines(builder.type, tag, responseBody.split(LINE_SEPARATOR).toTypedArray(), builder.logger,
                        true, builder.isLogHackEnable)
            }
            if (builder.logger == null) {
                log(builder.type, tag, END_LINE, builder.isLogHackEnable)
            }
        }

        @JvmStatic
        fun printFileRequest(builder: LoggingInterceptor.Builder, request: Request) {
            val tag = builder.getTag(true)
            if (builder.logger == null) log(builder.type, tag, REQUEST_UP_LINE, builder.isLogHackEnable)
            logLines(builder.type, tag, arrayOf(URL_TAG + request.url), builder.logger,
                    false, builder.isLogHackEnable)
            logLines(builder.type, tag, getRequest(request, builder.level), builder.logger,
                    true, builder.isLogHackEnable)
            if (builder.level === Level.BASIC || builder.level === Level.BODY) {
                logLines(builder.type, tag, OMITTED_REQUEST, builder.logger, true, builder.isLogHackEnable)
            }
            if (builder.logger == null) log(builder.type, tag, END_LINE, builder.isLogHackEnable)
        }

        @JvmStatic
        fun printFileResponse(builder: LoggingInterceptor.Builder, chainMs: Long, isSuccessful: Boolean,
                              code: Int, headers: String, segments: List<String>, message: String) {
            val tag = builder.getTag(false)
            if (builder.logger == null) log(builder.type, tag, RESPONSE_UP_LINE, builder.isLogHackEnable)
            logLines(builder.type, tag, getResponse(headers, chainMs, code, isSuccessful,
                    builder.level, segments, message), builder.logger, true, builder.isLogHackEnable)
            logLines(builder.type, tag, OMITTED_RESPONSE, builder.logger, true, builder.isLogHackEnable)
            if (builder.logger == null) log(builder.type, tag, END_LINE, builder.isLogHackEnable)
        }

        private fun getRequest(request: Request, level: Level): Array<String> {
            val log: String
            val header = request.headers.toString()
            val loggableHeader = level === Level.HEADERS || level === Level.BASIC
            log = METHOD_TAG + request.method + DOUBLE_SEPARATOR +
                    if (isEmpty(header)) "" else if (loggableHeader) HEADERS_TAG + LINE_SEPARATOR + dotHeaders(header) else ""
            return log.split(LINE_SEPARATOR).toTypedArray()
        }

        private fun getResponse(header: String, tookMs: Long, code: Int, isSuccessful: Boolean,
                                level: Level, segments: List<String>, message: String): Array<String> {
            val log: String
            val loggableHeader = level === Level.HEADERS || level === Level.BASIC
            val segmentString = slashSegments(segments)
            log = ((if (segmentString.isNotEmpty()) "$segmentString - " else "") + "is success : "
                    + isSuccessful + " - " + RECEIVED_TAG + tookMs + "ms" + DOUBLE_SEPARATOR + STATUS_CODE_TAG +
                    code + " / " + message + DOUBLE_SEPARATOR + when {
                isEmpty(header) -> ""
                loggableHeader -> HEADERS_TAG + LINE_SEPARATOR +
                        dotHeaders(header)
                else -> ""
            })
            return log.split(LINE_SEPARATOR).toTypedArray()
        }

        private fun slashSegments(segments: List<String>): String {
            val segmentString = StringBuilder()
            for (segment in segments) {
                segmentString.append("/").append(segment)
            }
            return segmentString.toString()
        }

        private fun dotHeaders(header: String): String {
            val headers = header.split(LINE_SEPARATOR).toTypedArray()
            val builder = StringBuilder()
            var tag = "─ "
            if (headers.size > 1) {
                for (i in headers.indices) {
                    tag = when (i) {
                        0 -> {
                            CORNER_UP
                        }
                        headers.size - 1 -> {
                            CORNER_BOTTOM
                        }
                        else -> {
                            CENTER_LINE
                        }
                    }
                    builder.append(tag).append(headers[i]).append("\n")
                }
            } else {
                for (item in headers) {
                    builder.append(tag).append(item).append("\n")
                }
            }
            return builder.toString()
        }

        private fun logLines(type: Int, tag: String, lines: Array<String>, logger: Logger?,
                             withLineSize: Boolean, useLogHack: Boolean) {
            for (line in lines) {
                val lineLength = line.length
                val maxLength = if (withLineSize) 110 else lineLength
                for (i in 0..lineLength / maxLength) {
                    val start = i * maxLength
                    var end = (i + 1) * maxLength
                    end = if (end > line.length) line.length else end
                    if (logger == null) {
                        log(type, tag, DEFAULT_LINE + line.substring(start, end), useLogHack)
                    } else {
                        logger.log(type, tag, line.substring(start, end))
                    }
                }
            }
        }

        private fun bodyToString(request: Request): String {
            return try {
                val copy = request.newBuilder().build()
                val buffer = Buffer()
                val body = copy.body ?: return ""
                body.writeTo(buffer)
                getJsonString(buffer.readUtf8())
            } catch (e: IOException) {
                "{\"err\": \"" + e.message + "\"}"
            }
        }

        @JvmStatic
        fun getJsonString(msg: String): String {
            val message: String
            message = try {
                when {
                    msg.startsWith("{") -> {
                        val jsonObject = JSONObject(msg)
                        jsonObject.toString(JSON_INDENT)
                    }
                    msg.startsWith("[") -> {
                        val jsonArray = JSONArray(msg)
                        jsonArray.toString(JSON_INDENT)
                    }
                    else -> {
                        msg
                    }
                }
            } catch (e: JSONException) {
                msg
            } catch (e1: OutOfMemoryError) {
                OOM_OMITTED
            }
            return message
        }
    }

    init {
        throw UnsupportedOperationException()
    }
}