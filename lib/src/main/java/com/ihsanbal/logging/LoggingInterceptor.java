package com.ihsanbal.logging;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.RealResponseBody;
import okhttp3.internal.platform.Platform;
import okio.GzipSource;
import okio.Okio;

/**
 * @author ihsan on 09/02/2017.
 */

public class LoggingInterceptor implements Interceptor {

    private boolean isDebug;
    private Builder builder;

    private LoggingInterceptor(Builder builder) {
        this.builder = builder;
        this.isDebug = builder.isDebug;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (builder.getHeaders().size() > 0) {
            Headers headers = request.headers();
            Set<String> names = headers.names();
            Iterator<String> iterator = names.iterator();
            Request.Builder requestBuilder = request.newBuilder();
            requestBuilder.headers(builder.getHeaders());
            while (iterator.hasNext()) {
                String name = iterator.next();
                requestBuilder.addHeader(name, headers.get(name));
            }
            request = requestBuilder.build();
        }

        if (!isDebug || builder.getLevel() == Level.NONE) {
            return chain.proceed(request);
        }
        RequestBody requestBody = request.body();

        String rSubtype = null;
        if (requestBody != null && requestBody.contentType() != null) {
            rSubtype = requestBody.contentType().subtype();
        }

        if (rSubtype != null && (rSubtype.contains("json")
                || rSubtype.contains("xml")
                || rSubtype.contains("plain")
                || rSubtype.contains("html"))) {
            Printer.printJsonRequest(builder, request);
        } else {
            Printer.printFileRequest(builder, request);
        }

        long st = System.nanoTime();
        Response response = chain.proceed(request);
        long chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st);


        // check gzip
        if ("gzip".equalsIgnoreCase(response.header("Content-Encoding")) && HttpHeaders.hasBody(response)) {

            Response.Builder responseBuilder = response.newBuilder()
                    .request(request);

            // decompress
            GzipSource responseBody = new GzipSource(response.body().source());

            // remove headers
            Headers strippedHeaders = response.headers().newBuilder()
                    .removeAll("Content-Encoding")
                    .removeAll("Content-Length")
                    .build();
            responseBuilder.headers(strippedHeaders);
            responseBuilder.body(new RealResponseBody(strippedHeaders, Okio.buffer(responseBody)));

            response = responseBuilder.build();
        }


        List<String> segmentList = request.url().encodedPathSegments();
        String header = response.headers().toString();
        int code = response.code();
        boolean isSuccessful = response.isSuccessful();
        String message = response.message();
        ResponseBody responseBody = response.body();
        MediaType contentType = responseBody.contentType();

        String subtype = null;
        ResponseBody body;

        if (contentType != null) {
            subtype = contentType.subtype();
        }

        if (subtype != null && (subtype.contains("json")
                || subtype.contains("xml")
                || subtype.contains("plain")
                || subtype.contains("html"))) {
            String bodyString = Printer.getJsonString(responseBody.string());
            Printer.printJsonResponse(builder, chainMs, isSuccessful, code, header, bodyString, segmentList, message);
            body = ResponseBody.create(contentType, bodyString);
        } else {
            Printer.printFileResponse(builder, chainMs, isSuccessful, code, header, segmentList, message);
            return response;
        }

        return response.newBuilder().body(body).build();
    }

    @SuppressWarnings({"unused", "SameParameterValue"})
    public static class Builder {

        private static String TAG = "LoggingI";
        private boolean isDebug;
        private int type = Platform.INFO;
        private String requestTag;
        private String responseTag;
        private Level level = Level.BASIC;
        private Headers.Builder builder;
        private Logger logger;

        public Builder() {
            builder = new Headers.Builder();
        }

        int getType() {
            return type;
        }

        Level getLevel() {
            return level;
        }

        /**
         * @param level set log level
         * @return Builder
         * @see Level
         */
        public Builder setLevel(Level level) {
            this.level = level;
            return this;
        }

        Headers getHeaders() {
            return builder.build();
        }

        String getTag(boolean isRequest) {
            if (isRequest) {
                return TextUtils.isEmpty(requestTag) ? TAG : requestTag;
            } else {
                return TextUtils.isEmpty(responseTag) ? TAG : responseTag;
            }
        }

        Logger getLogger() {
            return logger;
        }

        /**
         * @param name  Filed
         * @param value Value
         * @return Builder
         * Add a field with the specified value
         */
        public Builder addHeader(String name, String value) {
            builder.set(name, value);
            return this;
        }

        /**
         * Set request and response each log tag
         *
         * @param tag general log tag
         * @return Builder
         */
        public Builder tag(String tag) {
            TAG = tag;
            return this;
        }

        /**
         * Set request log tag
         *
         * @param tag request log tag
         * @return Builder
         */
        public Builder request(String tag) {
            this.requestTag = tag;
            return this;
        }

        /**
         * Set response log tag
         *
         * @param tag response log tag
         * @return Builder
         */
        public Builder response(String tag) {
            this.responseTag = tag;
            return this;
        }

        /**
         * @param isDebug set can sending log output
         * @return Builder
         */
        public Builder loggable(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        /**
         * @param type set sending log output type
         * @return Builder
         * @see Platform
         */
        public Builder log(int type) {
            this.type = type;
            return this;
        }

        /**
         * @param logger manuel logging interface
         * @return Builder
         * @see Logger
         */
        public Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public LoggingInterceptor build() {
            return new LoggingInterceptor(this);
        }
    }

}

