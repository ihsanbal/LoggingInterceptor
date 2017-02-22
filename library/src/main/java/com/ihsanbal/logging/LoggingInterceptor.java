package com.ihsanbal.logging;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
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
    private final boolean isDebug;
    private Builder builder;

    private LoggingInterceptor(Builder builder) {
        this.builder = builder;
        this.isDebug = builder.isDebug;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request;
        if (builder.getHeaders().size() > 0) {
            request = chain.request().newBuilder().headers(builder.getHeaders()).build();
        } else {
            request = chain.request();
        }
        if (!isDebug || builder.getLevel() == Level.NONE) {
            return chain.proceed(request);
        }

        Logger.printJsonRequest(builder, request);

        long st = System.nanoTime();
        Response response = chain.proceed(request);

        long chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st);
        String headers = response.headers().toString();
        int code = response.code();
        boolean isSuccessful = response.isSuccessful();
        String bodyString = Logger.getJsonString(response.body().string());
        Logger.printJsonResponse(builder, chainMs, isSuccessful, code, headers, bodyString);

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

        private static final String TAG_JSON = "LoggingI";
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

        public Builder addHeader(String name, String value) {
            builder.add(name, value);
            return this;
        }

        public Builder setLevel(Level level) {
            this.level = level;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder loggable(boolean isDebug) {
            this.isDebug = isDebug;
            return this;
        }

        public LoggingInterceptor build() {
            return new LoggingInterceptor(this);
        }

        public Builder log(int type) {
            this.type = type;
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
    }

}
