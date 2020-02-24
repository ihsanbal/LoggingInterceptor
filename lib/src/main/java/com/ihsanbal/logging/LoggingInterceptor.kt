package com.ihsanbal.logging

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.platform.Platform.Companion.INFO
import java.util.*
import java.util.concurrent.Executor

/**
 * @author ihsan on 09/02/2017.
 */
class LoggingInterceptor private constructor(private val builder: Builder) : Interceptor {

    private val isDebug: Boolean

    override fun intercept(chain: Interceptor.Chain): Response {
        TODO()
    }

    @Suppress("unused")
    class Builder {
        val headers: HashMap<String, String> = HashMap()
        val httpUrl: HashMap<String, String> = HashMap()
        var isLogHackEnable = false
            private set
        var isDebug = false
        var type: Int = INFO
            private set
        private var requestTag: String? = null
        private var responseTag: String? = null
        var level = Level.BASIC
            private set
        var logger: Logger? = null
            private set
        var executor: Executor? = null
            private set
        var isMockEnabled = false
        var sleepMs: Long = 0
        var listener: BufferListener? = null

        /**
         * @param level set log level
         * @return Builder
         * @see Level
         */
        fun setLevel(level: Level): Builder {
            this.level = level
            return this
        }

        fun getTag(isRequest: Boolean): String {
            return when (isRequest) {
                true -> if (requestTag.isNullOrEmpty()) TAG else requestTag!!
                false -> if (responseTag.isNullOrEmpty()) TAG else responseTag!!
            }
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
        fun request(tag: String?): Builder {
            requestTag = tag
            return this
        }

        /**
         * Set response log tag
         *
         * @param tag response log tag
         * @return Builder
         */
        fun response(tag: String?): Builder {
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
         * @see okhttp3.internal.platform.Platform
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
        fun enableAndroidStudioV3LogsHack(useHack: Boolean): Builder {
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

    init {
        isDebug = builder.isDebug
    }
}