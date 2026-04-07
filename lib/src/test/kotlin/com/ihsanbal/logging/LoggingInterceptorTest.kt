package com.ihsanbal.logging

import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Collections
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LoggingInterceptorTest {

    @Test
    fun `request logging does not include headers when disabled`() {
        val sink = CapturingSink()
        val builder = LoggingInterceptor.Builder()
                .setLevel(Level.BASIC)
                .logHeaders(false)
                .sink(sink)
        val headers = Headers.Builder()
                .add("authorization", "Bearer secret")
                .add("content-type", "application/json")
                .build()

        Printer.printJsonRequest(
                builder = builder,
                body = "{\"ok\":true}".toRequestBody("application/json".toMediaType()),
                url = "https://example.com/v1/profile",
                header = headers,
                method = "POST")

        val output = sink.output()
        assertFalse(output.contains("Headers:"))
        assertFalse(output.contains("authorization: Bearer secret"))
        assertTrue(output.contains("Body:"))
    }

    @Test
    fun `response logging does not include headers when disabled`() {
        val sink = CapturingSink()
        val builder = LoggingInterceptor.Builder()
                .setLevel(Level.BODY)
                .logHeaders(false)
                .sink(sink)
        val response = createResponse(
                headers = Headers.Builder()
                        .add("x-authentication-token", "secret")
                        .add("content-type", "application/json")
                        .build())

        Printer.printJsonResponse(
                builder = builder,
                chainMs = 15L,
                isSuccessful = true,
                code = 200,
                headers = response.headers,
                response = response,
                segments = response.request.url.encodedPathSegments,
                message = response.message,
                responseUrl = response.request.url.toString())

        val output = sink.output()
        assertTrue(output.contains("Status Code: 200 / OK (Received in: 15 ms)"))
        assertFalse(output.contains("Headers:"))
        assertFalse(output.contains("x-authentication-token: secret"))
        assertTrue(output.contains("Body:"))
    }

    @Test
    fun `response logging includes headers below status line by default`() {
        val sink = CapturingSink()
        val builder = LoggingInterceptor.Builder()
                .setLevel(Level.BODY)
                .sink(sink)
        val response = createResponse(
                headers = Headers.Builder()
                        .add("x-authentication-token", "secret")
                        .add("content-type", "application/json")
                        .build())

        Printer.printJsonResponse(
                builder = builder,
                chainMs = 15L,
                isSuccessful = true,
                code = 200,
                headers = response.headers,
                response = response,
                segments = response.request.url.encodedPathSegments,
                message = response.message,
                responseUrl = response.request.url.toString())

        val output = sink.output()
        val statusIndex = output.indexOf("Status Code: 200 / OK (Received in: 15 ms)")
        val headersIndex = output.indexOf("Headers:")
        val bodyIndex = output.indexOf("Body:")

        assertTrue(statusIndex >= 0)
        assertTrue(headersIndex > statusIndex)
        assertTrue(bodyIndex > headersIndex)
        assertTrue(output.contains("x-authentication-token: secret"))
        assertTrue(output.contains("content-type: application/json"))
    }

    @Test
    fun `response logging preserves repeated header lines`() {
        val sink = CapturingSink()
        val builder = LoggingInterceptor.Builder()
                .setLevel(Level.HEADERS)
                .sink(sink)
        val response = createResponse(
                headers = Headers.Builder()
                        .add("set-cookie", "a=1")
                        .add("set-cookie", "b=2")
                        .build())

        Printer.printJsonResponse(
                builder = builder,
                chainMs = 15L,
                isSuccessful = true,
                code = 200,
                headers = response.headers,
                response = response,
                segments = response.request.url.encodedPathSegments,
                message = response.message,
                responseUrl = response.request.url.toString())

        val output = sink.output()
        assertTrue(output.contains("set-cookie: a=1"))
        assertTrue(output.contains("set-cookie: b=2"))
        assertFalse(output.contains("Body:"))
    }

    @Test
    fun `batching sink isolates concurrent blocks for the same tag`() {
        val flushedBlocks = Collections.synchronizedList(mutableListOf<String>())
        val sink = BatchingSink(object : LogSink {
            override fun log(type: Int, tag: String, message: String) {
                flushedBlocks += message
            }
        })
        val startLatch = CountDownLatch(1)
        val finishLoggingLatch = CountDownLatch(2)
        val executor = Executors.newFixedThreadPool(2)

        val workerA = executor.submit(Callable {
            startLatch.await()
            sink.log(4, "shared-tag", "thread-a-1")
            sink.log(4, "shared-tag", "thread-a-2")
            finishLoggingLatch.countDown()
            finishLoggingLatch.await()
            sink.close(4, "shared-tag")
        })
        val workerB = executor.submit(Callable {
            startLatch.await()
            sink.log(4, "shared-tag", "thread-b-1")
            sink.log(4, "shared-tag", "thread-b-2")
            finishLoggingLatch.countDown()
            finishLoggingLatch.await()
            sink.close(4, "shared-tag")
        })

        startLatch.countDown()
        workerA.get(5, TimeUnit.SECONDS)
        workerB.get(5, TimeUnit.SECONDS)
        executor.shutdownNow()

        assertEquals(2, flushedBlocks.size)
        assertTrue(flushedBlocks.contains("thread-a-1\nthread-a-2"))
        assertTrue(flushedBlocks.contains("thread-b-1\nthread-b-2"))
    }

    private fun createResponse(headers: Headers): Response {
        val request = Request.Builder()
                .url("https://example.com/v1/profile")
                .build()

        return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .headers(headers)
                .body("{\"ok\":true}".toResponseBody("application/json".toMediaType()))
                .build()
    }

    private class CapturingSink : LogSink {
        private val messages = mutableListOf<String>()

        override fun log(type: Int, tag: String, message: String) {
            messages += message
        }

        fun output(): String = messages.joinToString("\n")
    }
}
