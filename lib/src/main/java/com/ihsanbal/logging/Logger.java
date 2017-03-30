package com.ihsanbal.logging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Request;
import okio.Buffer;

/**
 * @author ihsan on 09/02/2017.
 */

class Logger {

    private static final int JSON_INDENT = 3;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String[] OMITTED_REQUEST = {LINE_SEPARATOR, "Omitted request body"};
    private static final String[] OMITTED_RESPONSE = {LINE_SEPARATOR, "Omitted response body"};
    private static final String DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR;
    private static final int MAX_LONG_SIZE = 110;
    private static final String N = "\n";
    private static final String T = "\t";

    protected Logger() {
        throw new UnsupportedOperationException();
    }

    private static boolean isEmpty(String line) {
        return TextUtils.isEmpty(line) || N.equals(line) || T.equals(line) || TextUtils.isEmpty(line.trim());
    }

    static void printJsonRequest(LoggingInterceptor.Builder builder, Request request) {
        String requestBody = LINE_SEPARATOR + "Body:" + LINE_SEPARATOR + bodyToString(request);
        String tag = builder.getTag(true);
        I.log(builder.getType(), tag,
                "╔══════ Request ════════════════════════════════════════════════════════════════════════");

        logLines(builder.getType(), tag, getRequest(request, builder.getLevel()));
        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY) {
            logLines(builder.getType(), tag, requestBody.split(LINE_SEPARATOR));
        }
        I.log(builder.getType(), tag,

                "╚═══════════════════════════════════════════════════════════════════════════════════════");
    }

    static void printFileRequest(LoggingInterceptor.Builder builder, Request request) {
        String tag = builder.getTag(true);
        I.log(builder.getType(), tag,
                "╔══════ Request ════════════════════════════════════════════════════════════════════════");

        logLines(builder.getType(), tag, getRequest(request, builder.getLevel()));
        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY) {
            logLines(builder.getType(), tag, OMITTED_REQUEST);
        }
        I.log(builder.getType(), tag,

                "╚═══════════════════════════════════════════════════════════════════════════════════════");
    }

    static void printJsonResponse(LoggingInterceptor.Builder builder, long chainMs, boolean isSuccessful,
                                  int code, String headers, String bodyString, List<String> segments) {
        String responseBody = LINE_SEPARATOR + "Body:" + LINE_SEPARATOR + getJsonString(bodyString);
        String tag = builder.getTag(false);
        I.log(builder.getType(), tag,
                "╔══════ Response ═══════════════════════════════════════════════════════════════════════");

        logLines(builder.getType(), tag, getResponse(headers, chainMs, code, isSuccessful,
                builder.getLevel(), segments));
        if (builder.getLevel() == Level.BASIC || builder.getLevel() == Level.BODY) {
            logLines(builder.getType(), tag, responseBody.split(LINE_SEPARATOR));
        }
        I.log(builder.getType(), tag,

                "╚═══════════════════════════════════════════════════════════════════════════════════════");
    }

    static void printFileResponse(LoggingInterceptor.Builder builder, long chainMs, boolean isSuccessful,
                                  int code, String headers, List<String> segments) {
        String tag = builder.getTag(false);
        I.log(builder.getType(), tag,
                "╔══════ Response ═══════════════════════════════════════════════════════════════════════");

        logLines(builder.getType(), tag, getResponse(headers, chainMs, code, isSuccessful,
                builder.getLevel(), segments));
        logLines(builder.getType(), tag, OMITTED_RESPONSE);
        I.log(builder.getType(), tag,

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

    private static String[] getResponse(String header, long tookMs, int code, boolean isSuccessful,
                                        Level level, List<String> segments) {
        String message;
        boolean loggableHeader = level == Level.HEADERS || level == Level.BASIC;
        String segmentString = slashSegments(segments);
        message = ((!TextUtils.isEmpty(segmentString) ? segmentString + " - " : "") + "is success : "
                + isSuccessful + " - " + "Received in: " + tookMs + "ms" + DOUBLE_SEPARATOR + "Status Code: " +
                code + DOUBLE_SEPARATOR + (isEmpty(header) ? "" : loggableHeader ? "Headers:" + LINE_SEPARATOR +
                dotHeaders(header) : ""));
        return message.split(LINE_SEPARATOR);
    }

    private static String slashSegments(List<String> segments) {
        StringBuilder segmentString = new StringBuilder();
        for (String segment : segments) {
            segmentString.append("/").append(segment);
        }
        return segmentString.toString();
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
            int lineLength = line.length();
            for (int i = 0; i <= lineLength / MAX_LONG_SIZE; i++) {
                int start = i * MAX_LONG_SIZE;
                int end = (i + 1) * MAX_LONG_SIZE;
                end = end > line.length() ? line.length() : end;
                I.log(type, tag, "║ " + line.substring(start, end));
            }
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
