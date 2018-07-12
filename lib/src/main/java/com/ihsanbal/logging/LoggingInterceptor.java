package com.ihsanbal.logging;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.platform.Platform;

/**
 * @author ihsan on 09/02/2017.
 */

@SuppressWarnings("NullableProblems")
public class LoggingInterceptor implements Interceptor {

    private final boolean isDebug;
    private final Builder builder;

    private LoggingInterceptor(Builder builder) {
        this.builder = builder;
        this.isDebug = builder.isDebug;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HashMap<String, String> headerMap = builder.getHeaders();
        if (headerMap.size() > 0) {
            Request.Builder requestBuilder = request.newBuilder();
            for (String key : headerMap.keySet()) {
                String value = headerMap.get(key);
                requestBuilder.addHeader(key, value);
            }
            request = requestBuilder.build();
        }

        HashMap<String, String> queryMap = builder.getHttpUrl();
        if (queryMap.size() > 0) {
            HttpUrl.Builder httpUrlBuilder = request.url().newBuilder(request.url().toString());
            if (httpUrlBuilder != null) {
                for (String key : queryMap.keySet()) {
                    String value = queryMap.get(key);
                    httpUrlBuilder.addQueryParameter(key, value);
                }
                request = request.newBuilder().url(httpUrlBuilder.build()).build();
            }
        }

        if (!isDebug || builder.getLevel() == Level.NONE) {
            return chain.proceed(request);
        }

        final RequestBody requestBody = request.body();

        String contentType = null;
        if (requestBody != null && requestBody.contentType() != null) {
            contentType = requestBody.contentType().subtype();
        }

        Executor executor = builder.executor;

        if (isJsonContentType(contentType)) {
            if (executor != null) {
                executor.execute(createPrintJsonRequestRunnable(builder, request));
            } else {
                Printer.printJsonRequest(builder, request);
            }
        } else {
            if (executor != null) {
                executor.execute(createFileRequestRunnable(builder, request));
            } else {
                Printer.printFileRequest(builder, request);
            }
        }

        final long startTime = System.nanoTime();
        final Response response = chain.proceed(request);
        final long responseTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        final List<String> segmentList = request.url().encodedPathSegments();
        final String header = response.headers().toString();
        final int code = response.code();
        final boolean isSuccessful = response.isSuccessful();
        final String message = response.message();
        final ResponseBody responseBody = response.body();
        final MediaType mediaType = responseBody.contentType();

        String subType = null;
        final ResponseBody executeResponseBody;

        if (mediaType != null) {
            subType = mediaType.subtype();
        }

        if (isJsonContentType(subType) && TextUtils.isEmpty(response.header("Content-Encoding"))) {
            final String bodyString = Printer.getJsonString(responseBody.string());
            final String url = response.request().url().toString();

            if (executor != null) {
                executor.execute(createPrintJsonResponseRunnable(builder, responseTime, isSuccessful, code, header, bodyString,
                        segmentList, message, url));
            } else {
                Printer.printJsonResponse(builder, responseTime, isSuccessful, code, header, bodyString,
                        segmentList, message, url);
            }
            executeResponseBody = ResponseBody.create(mediaType, bodyString);
        } else {
            if (executor != null) {
                executor.execute(createFileResponseRunnable(builder, responseTime, isSuccessful, code, header, segmentList, message));
            } else {
                Printer.printFileResponse(builder, responseTime, isSuccessful, code, header, segmentList, message);
            }
            return response;
        }

        return response.newBuilder().
                body(executeResponseBody).
                build();
    }

    private boolean isJsonContentType(final String subtype) {
        return subtype != null && (subtype.contains("json") || subtype.contains("text") || subtype.contains("text"));
    }

    private static Runnable createPrintJsonRequestRunnable(final LoggingInterceptor.Builder builder, final Request request) {
        return new Runnable() {
            @Override
            public void run() {
                Printer.printJsonRequest(builder, request);
            }
        };
    }

    private static Runnable createFileRequestRunnable(final LoggingInterceptor.Builder builder, final Request request) {
        return new Runnable() {
            @Override
            public void run() {
                Printer.printFileRequest(builder, request);
            }
        };
    }

    private static Runnable createPrintJsonResponseRunnable(final LoggingInterceptor.Builder builder, final long chainMs, final boolean isSuccessful,
                                                            final int code, final String headers, final String bodyString, final List<String> segments, final String message, final String responseUrl) {
        return new Runnable() {
            @Override
            public void run() {
                Printer.printJsonResponse(builder, chainMs, isSuccessful, code, headers, bodyString, segments, message, responseUrl);
            }
        };
    }

    private static Runnable createFileResponseRunnable(final LoggingInterceptor.Builder builder, final long chainMs, final boolean isSuccessful,
                                                       final int code, final String headers, final List<String> segments, final String message) {
        return new Runnable() {
            @Override
            public void run() {
                Printer.printFileResponse(builder, chainMs, isSuccessful, code, headers, segments, message);
            }
        };
    }

    @SuppressWarnings({"unused", "SameParameterValue"})
    public static class Builder {

        private static String TAG = "LoggingI";
        private final HashMap<String, String> headers;
        private final HashMap<String, String> queries;
        private boolean isDebug;
        private int type = Platform.INFO;
        private String requestTag;
        private String responseTag;
        private Level level = Level.BASIC;
        private Logger logger;
        private Executor executor;

        public Builder() {
            headers = new HashMap<>();
            queries = new HashMap<>();
        }

        int getType() {
            return type;
        }

        Level getLevel() {
            return level;
        }

        public Builder level(Level level) {
            this.level = level;
            return this;
        }

        HashMap<String, String> getHeaders() {
            return headers;
        }

        HashMap<String, String> getHttpUrl() {
            return queries;
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

        Executor getExecutor() {
            return executor;
        }

        public Builder addHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder addQueryParam(String name, String value) {
            queries.put(name, value);
            return this;
        }

        public Builder tag(String tag) {
            TAG = tag;
            return this;
        }

        public Builder request(String tag) {
            this.requestTag = tag;
            return this;
        }

        public Builder response(String tag) {
            this.responseTag = tag;
            return this;
        }

        public Builder loggable(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        public Builder log(int type) {
            this.type = type;
            return this;
        }

        public Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public LoggingInterceptor build() {
            return new LoggingInterceptor(this);
        }
    }

}
