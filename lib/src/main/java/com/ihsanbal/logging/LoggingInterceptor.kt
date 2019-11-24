package com.ihsanbal.logging

import com.ihsanbal.logging.Printer.Companion.getJsonString
import com.ihsanbal.logging.Printer.Companion.printFileRequest
import com.ihsanbal.logging.Printer.Companion.printFileResponse
import com.ihsanbal.logging.Printer.Companion.printJsonRequest
import com.ihsanbal.logging.Printer.Companion.printJsonResponse
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.platform.Platform
import java.io.IOException
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * @author ihsan on 09/02/2017.
 */
class LoggingInterceptor private constructor(private val builder: Builder) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val headerMap = builder.headers
        request = buildRequest(headerMap, request)
        return when (!builder.isDebug || builder.level === Level.NONE) {
            true -> chain.proceed(request)
            else -> {
                val executor = builder.executor
                printRequest(request.body?.contentType()?.subtype, executor, request)
                val st = System.nanoTime()
                val response: Response = when (builder.isMockEnabled && builder.listener != null) {
                    true -> returnMockResponse(request, chain)
                    else -> chain.proceed(request)
                }
                val chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st)
                val segmentList = request.url.encodedPathSegments
                val header = response.headers.toString()
                val code = response.code
                val isSuccessful = response.isSuccessful
                val message = response.message
                val responseBody = response.body
                val contentType = responseBody!!.contentType()
                var subtype: String? = null
                val body: ResponseBody
                if (contentType != null) {
                    subtype = contentType.subtype
                }
                body = if (isNotFileRequest(subtype)) {
                    val bodyString = getJsonString(responseBody.string())
                    val url = response.request.url.toString()
                    printLog(executor, chainMs, isSuccessful, code, header, bodyString, segmentList, message, url)
                    bodyString.toResponseBody(contentType)
                } else {
                    printLog(executor, chainMs, isSuccessful, code, header, segmentList, message)
                    return response
                }
                return response.newBuilder().body(body).build()
            }
        }
    }

    private fun returnMockResponse(request: Request, chain: Interceptor.Chain): Response {
        delay()
        return Response.Builder()
                .body(builder.listener!!.getJsonResponse(request)!!.toResponseBody("application/json".toMediaTypeOrNull()))
                .request(chain.request())
                .protocol(Protocol.HTTP_2)
                .message("Mock")
                .code(200)
                .build()
    }

    private fun printRequest(rSubtype: String?, executor: Executor?, request: Request) {
        if (isNotFileRequest(rSubtype)) {
            if (executor != null) {
                executor.execute(createPrintJsonRequestRunnable(builder, request))
            } else {
                printJsonRequest(builder, request)
            }
        } else {
            if (executor != null) {
                executor.execute(createFileRequestRunnable(builder, request))
            } else {
                printFileRequest(builder, request)
            }
        }
    }

    private fun buildRequest(headerMap: HashMap<String, String>, request: Request): Request {
        var managedRequest = request
        managedRequest = addHeadersToRequest(headerMap, managedRequest)
        val queryMap = builder.httpUrl
        managedRequest = addQueryParamsToRequest(queryMap, managedRequest)
        return managedRequest
    }

    private fun addQueryParamsToRequest(queryMap: HashMap<String, String>, request: Request): Request {
        var managedRequest = request
        if (queryMap.isNotEmpty()) {
            val httpUrlBuilder = managedRequest.url.newBuilder(managedRequest.url.toString())
            for (key in queryMap.keys) {
                val value = queryMap[key]
                httpUrlBuilder!!.addQueryParameter(key, value)
            }
            managedRequest = managedRequest.newBuilder().url(httpUrlBuilder!!.build()).build()
        }
        return managedRequest
    }

    private fun addHeadersToRequest(headerMap: HashMap<String, String>, request: Request): Request {
        var managedRequest = request
        if (headerMap.isNotEmpty()) {
            val requestBuilder = managedRequest.newBuilder()
            for (key in headerMap.keys) {
                val value = headerMap[key]
                requestBuilder.addHeader(key, value!!)
            }
            managedRequest = requestBuilder.build()
        }
        return managedRequest
    }

    private fun printLog(executor: Executor?, chainMs: Long, isSuccessful: Boolean, code: Int, header: String, segmentList: List<String>, message: String) {
        if (executor != null) {
            executor.execute(createFileResponseRunnable(builder, chainMs, isSuccessful, code, header, segmentList, message))
        } else {
            printFileResponse(builder, chainMs, isSuccessful, code, header, segmentList, message)
        }
    }

    private fun printLog(executor: Executor?, chainMs: Long, isSuccessful: Boolean, code: Int, header: String, bodyString: String, segmentList: List<String>, message: String, url: String) {
        if (executor != null) {
            executor.execute(createPrintJsonResponseRunnable(builder, chainMs, isSuccessful, code, header, bodyString,
                    segmentList, message, url))
        } else {
            printJsonResponse(builder, chainMs, isSuccessful, code, header, bodyString,
                    segmentList, message, url)
        }
    }

    private fun delay() {
        try {
            TimeUnit.MILLISECONDS.sleep(builder.sleepMs)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun isNotFileRequest(subtype: String?): Boolean {
        return subtype != null && (subtype.contains("json")
                || subtype.contains("xml")
                || subtype.contains("plain")
                || subtype.contains("html"))
    }

    @Suppress("unused")
    class Builder {
        var headers: HashMap<String, String> = HashMap()
        var httpUrl: HashMap<String, String> = HashMap()
        var isLogHackEnable = false
            private set
        var isDebug = false
        var type: Int = Platform.INFO
            private set
        private var requestTag: String = TAG
        private var responseTag: String = TAG
        var level = Level.BASIC
            private set
        var logger: Logger? = null
            private set
        var executor: Executor? = null
            private set
        var isMockEnabled = false
        var sleepMs: Long = 0
        var listener: BufferListener? = null

        fun getTag(isRequest: Boolean): String = when (isRequest) {
            true -> requestTag
            else -> responseTag
        }

        /**
         * @param level set log level
         * @return Builder
         * @see Level
         */
        fun setLevel(level: Level): Builder {
            this.level = level
            return this
        }

        /**
         * @param name  Filed
         * @param value Value
         * @return Builder
         * Add a field with the specified value
         */
        fun addHeader(name: String, value: String): Builder {
            headers[name] = value
            return this
        }

        /**
         * @param name  Filed
         * @param value Value
         * @return Builder
         * Add a field with the specified value
         */
        fun addQueryParam(name: String, value: String): Builder {
            httpUrl[name] = value
            return this
        }

        /**
         * Set request and response each log tag
         *
         * @param tag general log tag
         * @return Builder
         */
        fun tag(tag: String): Builder {
            TAG = tag
            return this
        }

        /**
         * Set request log tag
         *
         * @param tag request log tag
         * @return Builder
         */
        fun request(tag: String): Builder {
            requestTag = tag
            return this
        }

        /**
         * Set response log tag
         *
         * @param tag response log tag
         * @return Builder
         */
        fun response(tag: String): Builder {
            responseTag = tag
            return this
        }

        /**
         * @param isDebug set can sending log output
         * @return Builder
         */
        fun loggable(isDebug: Boolean): Builder {
            this.isDebug = isDebug
            return this
        }

        /**
         * @param type set sending log output type
         * @return Builder
         * @see Platform
         */
        fun log(type: Int): Builder {
            this.type = type
            return this
        }

        /**
         * @param logger manuel logging interface
         * @return Builder
         * @see Logger
         */
        fun logger(logger: Logger?): Builder {
            this.logger = logger
            return this
        }

        /**
         * @param executor manual executor for printing
         * @return Builder
         * @see Logger
         */
        fun executor(executor: Executor?): Builder {
            this.executor = executor
            return this
        }

        /**
         * @param useMock let you use json file from asset
         * @param sleep   let you see progress dialog when you request
         * @return Builder
         * @see LoggingInterceptor
         */
        fun enableMock(useMock: Boolean, sleep: Long, listener: BufferListener?): Builder {
            isMockEnabled = useMock
            sleepMs = sleep
            this.listener = listener
            return this
        }

        /**
         * Call this if you want to have formatted pretty output in Android Studio logCat.
         * By default this 'hack' is not applied.
         *
         * @param useHack setup builder to use hack for Android Studio v3+ in order to have nice
         * output as it was in previous A.S. versions.
         * @return Builder
         * @see Logger
         */
        fun enableAndroidStudioLogHack(useHack: Boolean): Builder {
            isLogHackEnable = useHack
            return this
        }

        fun build(): LoggingInterceptor {
            return LoggingInterceptor(this)
        }

        companion object {
            private var TAG = "LoggingI"
        }

    }

    private fun createPrintJsonRequestRunnable(builder: Builder, request: Request): Runnable {
        return Runnable { printJsonRequest(builder, request) }
    }

    private fun createFileRequestRunnable(builder: Builder, request: Request): Runnable {
        return Runnable { printFileRequest(builder, request) }
    }

    private fun createPrintJsonResponseRunnable(builder: Builder, chainMs: Long, isSuccessful: Boolean,
                                                code: Int, headers: String, bodyString: String, segments: List<String>, message: String, responseUrl: String): Runnable {
        return Runnable { printJsonResponse(builder, chainMs, isSuccessful, code, headers, bodyString, segments, message, responseUrl) }
    }

    private fun createFileResponseRunnable(builder: Builder, chainMs: Long, isSuccessful: Boolean,
                                           code: Int, headers: String, segments: List<String>, message: String): Runnable {
        return Runnable { printFileResponse(builder, chainMs, isSuccessful, code, headers, segments, message) }
    }
}