package com.ihsanbal.logging;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Request;
import okio.Buffer;

/**
 * @author ihsan on 09/02/2017.
 */

class Logger {

    Logger() {
        throw new UnsupportedOperationException("you can't instantiate me");
    }

    private static final int JSON_INDENT = 4;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;


    private static boolean isEmpty(String line) {
        return TextUtils.isEmpty(line) || line.equals("\n") || line.equals("\t") || TextUtils.isEmpty(line.trim());
    }

    static void printJsonRequest(LoggingInterceptor.Builder builder, Request request) {
        String requestBody = LINE_SEPARATOR + "Body:" + LINE_SEPARATOR + bodyToString(request);
        String tag = builder.getTag(true);
        I.Log(builder.getType(), tag,
                "╔══════ Request ════════════════════════════════════════════════════════════════════════");

        logLines(builder.getType(), tag, getRequest(request, builder.getLevel()));
        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY) {
            logLines(builder.getType(), tag, requestBody.split(LINE_SEPARATOR));
        }
        I.Log(builder.getType(), tag,

                "╚═══════════════════════════════════════════════════════════════════════════════════════");
    }

    static void printJsonResponse(LoggingInterceptor.Builder builder, long chainMs, boolean isSuccessful, int code, String headers, String bodyString) {
        String responseBody = LINE_SEPARATOR + "Body:" + LINE_SEPARATOR + getJsonString(bodyString);
        String tag = builder.getTag(false);
        I.Log(builder.getType(), tag,
                "╔══════ Response ═══════════════════════════════════════════════════════════════════════");

        logLines(builder.getType(), tag, getResponse(headers, chainMs, code, isSuccessful, builder.getLevel()));
        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY) {
            logLines(builder.getType(), tag, responseBody.split(LINE_SEPARATOR));
        }
        I.Log(builder.getType(), tag,

                "╚═══════════════════════════════════════════════════════════════════════════════════════");
    }

    private static String[] getRequest(Request request, Level level) {
        String message;
        String header = request.headers().toString();
        boolean loggableHeader = level == Level.HEADERS || level == Level.BASIC;
        message = "URL: " + request.url() + DOUBLE_SEPARATOR + "Method: @" + request.method() + DOUBLE_SEPARATOR +
                (isEmpty(header) ? "" : loggableHeader ? "Headers:" + LINE_SEPARATOR + dotHeaders(header) : "");
        return message.split(LINE_SEPARATOR);
    }

    private static String[] getResponse(String header, long tookMs, int code, boolean isSuccessful, Level level) {
        String message;
        boolean loggableHeader = level == Level.HEADERS || level == Level.BASIC;
        message = ("Result is Successful: " + isSuccessful + DOUBLE_SEPARATOR + "Status Code: " +
                code + DOUBLE_SEPARATOR + (isEmpty(header) ? "" : loggableHeader ? "Headers:" + LINE_SEPARATOR +
                dotHeaders(header) : "") + LINE_SEPARATOR + "Received in: " + tookMs + "ms");
        return message.split(LINE_SEPARATOR);
    }

    private static String dotHeaders(String header) {
        String[] headers = header.split(LINE_SEPARATOR);
        StringBuilder builder = new StringBuilder();
        for (String item : headers) {
            builder.append("- ").append(item).append("\n");
        }
        return builder.toString();
    }

    private static void logLines(int type, String tag, String[] lines) {
        for (String line : lines) {
            I.Log(type, tag, "║ " + line);
        }
    }

    private static String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            if (copy.body() == null)
                return "";
            copy.body().writeTo(buffer);
            return getJsonString(buffer.readUtf8());
        } catch (final IOException e) {
            return "{\"err\": \"" + e.getMessage() + "\"}";
        }
    }

    static String getJsonString(String msg) {
        String message;
        try {
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(JSON_INDENT);
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(JSON_INDENT);
            } else {
                message = msg;
            }
        } catch (JSONException e) {
            message = msg;
        }
        return message;
    }

}
