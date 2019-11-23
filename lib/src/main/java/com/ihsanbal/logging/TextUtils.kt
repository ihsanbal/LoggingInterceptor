package com.ihsanbal.logging

/**
 * @author ihsan on 30/03/2017.
 */
internal object TextUtils {
    @JvmStatic
    fun isEmpty(str: CharSequence?): Boolean {
        return str == null || str.isEmpty()
    }
}