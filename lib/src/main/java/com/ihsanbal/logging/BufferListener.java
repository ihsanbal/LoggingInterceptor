package com.ihsanbal.logging;

import java.io.IOException;

import okhttp3.Request;

/**
 * @author ihsan on 8/12/18.
 */
public interface BufferListener {
    String getJsonResponse(Request request) throws IOException;
}
