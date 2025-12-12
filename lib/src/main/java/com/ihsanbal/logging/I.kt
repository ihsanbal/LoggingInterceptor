package com.ihsanbal.logging

import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author ihsan on 10/02/2017.
 */
internal open class I protected constructor() {
    companion object {
        private val prefix = arrayOf(". ", " .")
        private var index = 0

        fun log(type: Int, tag: String, msg: String?, isLogHackEnable: Boolean, sink: LogSink? = null) {
            if (sink != null) {
                sink.log(type, tag, msg ?: "")
                return
            }

            val finalTag = getFinalTag(tag, isLogHackEnable)

            if (!logWithAndroid(type, finalTag, msg)) {
                val logger = Logger.getLogger(if (isLogHackEnable) finalTag else tag)
                logger.log(mapJavaLevel(type), msg)
            }
        }

        private fun logWithAndroid(type: Int, tag: String, msg: String?): Boolean {
            return try {
                val logClass = Class.forName("android.util.Log")
                val printlnMethod = logClass.getMethod(
                    "println",
                    Int::class.javaPrimitiveType,
                    String::class.java,
                    String::class.java
                )
                printlnMethod.invoke(null, type, tag, msg ?: "")
                true
            } catch (_: Throwable) {
                false
            }
        }

        private fun mapJavaLevel(type: Int): Level =
            when (type) {
                2, 3 -> Level.FINE // VERBOSE/DEBUG
                4 -> Level.INFO
                5 -> Level.WARNING
                6, 7, 8, 9 -> Level.SEVERE
                else -> Level.INFO
            }

        private fun getFinalTag(tag: String, isLogHackEnable: Boolean): String {
            return if (isLogHackEnable) {
                index = index xor 1
                prefix[index] + tag
            } else {
                tag
            }
        }
    }

    init {
        throw UnsupportedOperationException()
    }
}
