package com.ihsanbal.logging;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author ihsan on 09/02/2017.
 */

public class LoggingInterceptor implements Interceptor {

    private static final String TAG = "LoggingI";
    private final boolean isDebug;
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
            builder.addHeaders(headers);
            request = chain.request().newBuilder()
                    .headers(builder.getHeaders()).build();
        }

        if (!isDebug || builder.getLevel() == Level.NONE) {
            return chain.proceed(request);
        }

        Logger.printJsonRequest(builder, request);

        long st = System.nanoTime();
        Response response = chain.proceed(request);

        List<String> segmentList = ((Request) request.tag()).url().encodedPathSegments();
        long chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st);
        String headers = response.headers().toString();
        int code = response.code();
        boolean isSuccessful = response.isSuccessful();
        String bodyString = Logger.getJsonString(response.body().string());
        Logger.printJsonResponse(builder, chainMs, isSuccessful, code, headers, bodyString, segmentList);

        Request cloneRequest = chain.request();
        MediaType contentType = null;
        if (cloneRequest.body() != null)
            contentType = cloneRequest.body().contentType();
        ResponseBody body = ResponseBody.create(contentType, bodyString);

        return response.newBuilder().body(body).build();
    }

    boolean getLoggable() {
        return isDebug;
    }

    public static class Builder {

        private static final String TAG_JSON = TAG;
        private String tag = TAG_JSON;
        private boolean isDebug;
        private int type = Log.DEBUG;
        private String requestTag;
        private String responseTag;
        private Level level = Level.BASIC;
        private Headers.Builder builder;

        public Builder() {
            builder = new Headers.Builder();
        }

        int getType() {
            return type;
        }

        String getRequestTag() {
            return requestTag;
        }

        String getResponseTag() {
            return responseTag;
        }

        public Level getLevel() {
            return level;
        }

        public String getTag() {
            return tag;
        }

        public Headers getHeaders() {
            return builder.build();
        }

        String getTag(boolean isRequest) {
            if (isRequest) {
                return TextUtils.isEmpty(requestTag) ? tag : requestTag;
            } else {
                return TextUtils.isEmpty(responseTag) ? tag : responseTag;
            }
        }

        /**
         * @param name  Filed
         * @param value Value
         * @see Headers
         * <p>
         * Add a field with the specified value
         */
        public Builder addHeader(String name, String value) {
            builder.add(name, value);
            return this;
        }

        /**
         * @param level set log level
         * @see Level
         */
        public Builder setLevel(Level level) {
            this.level = level;
            return this;
        }

        /**
         * Set request & response each log tag
         */
        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        /**
         * Set request log tag
         */
        public Builder request(String tag) {
            this.requestTag = tag;
            return this;
        }

        /**
         * Set response log tag
         */
        public Builder response(String tag) {
            this.responseTag = tag;
            return this;
        }

        /**
         * @param isDebug set can sending log output
         */
        public Builder loggable(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        /**
         * @param type set sending log output type
         * @see Log
         */
        public Builder log(int type) {
            this.type = type;
            return this;
        }

        void addHeaders(Headers headers) {
            if (headers != null && headers.size() > 0) {
                Object[] names = headers.names().toArray();
                for (int i = 0; i < headers.size(); i++) {
                    addHeader(names[i].toString(), headers.get(names[i].toString()));
                }
            }
        }

        public LoggingInterceptor build() {
            return new LoggingInterceptor(this);
        }
    }

}
