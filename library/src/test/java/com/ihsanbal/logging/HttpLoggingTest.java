package com.ihsanbal.logging;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * @author ihsan on 11/03/2017.
 */

public class HttpLoggingTest {

    private static final MediaType PLAIN = MediaType.parse("text/plain; charset=utf-8");

    @Rule
    public MockWebServer server = new MockWebServer();

    private OkHttpClient client;

    private final LoggingInterceptor loggingInterceptor =
            new LoggingInterceptor.Builder()
                    .addHeader("Version", BuildConfig.VERSION_NAME)
                    .build();

    @Before
    public void setUp() {
        client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
    }

    private Request.Builder request() {
        return new Request.Builder().url(server.url("/"));
    }

    @Test
    public void interceptor() {
        Assert.assertTrue(client.interceptors().size() > 0);
    }

    @Test
    public void headers() throws IOException {
        server.enqueue(new MockResponse());
        Request request = client.newCall(request().build()).execute().request();
        Assert.assertTrue(request.headers().get("Version").equals(BuildConfig.VERSION_NAME));
    }

    @Test
    public void post() throws IOException {
        final String body = "Post";
        server.enqueue(new MockResponse()
                .setBody(body)
                .setHeader("Cache-Control", "Custom-Max-Value=640000"));

        Response response = client.newCall(request()
                .post(RequestBody.create(PLAIN, "{\n" + "\"msg\": \"Hello World.\"\n" + "}"))
                .build())
                .execute();
        Assert.assertTrue(response.body().string().equals(body));
    }

    @Test
    public void get() throws IOException {
        final String body = "Get";
        server.enqueue(new MockResponse()
                .setBody(body));

        Response response = client.newCall(request()
                .get()
                .build())
                .execute();
        Assert.assertTrue(response.isSuccessful() && response.body().string() != null);
    }
}
