package com.ihsanbal.logging;


import java.util.logging.Level;

import okhttp3.internal.platform.Platform;

/**
 * @author ihsan on 10/02/2017.
 */
class I {
    private static String[] prefix = {". ", " ."};
    private static int index = 0;

    protected I() {
        throw new UnsupportedOperationException();
    }

    static void log(int type, String tag, String msg, final boolean isLogHackEnable) {
        final String finalTag = getFinalTag(tag, isLogHackEnable);
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(isLogHackEnable ? finalTag : tag);
        switch (type) {
            case Platform.INFO:
                logger.log(Level.INFO, msg);
                break;
            default:
                logger.log(Level.WARNING, msg);
                break;
        }
    }

    private static String getFinalTag(final String tag, final boolean isLogHackEnable) {
        if (isLogHackEnable) {
            index = index ^ 1;
            return prefix[index] + tag;
        } else {
            return tag;
        }
    }
}
