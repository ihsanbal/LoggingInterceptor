package com.ihsanbal.logging

internal interface LogSink {
    fun log(type: Int, tag: String, message: String)
    fun close(type: Int, tag: String) {}
}

