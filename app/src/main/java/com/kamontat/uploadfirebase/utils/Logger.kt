package com.kamontat.uploadfirebase.utils

import android.util.Log

/**
 * Created by kamontat on 15/3/2018 AD.
 */
class Logger {
    companion object {
        private const val TAG: String = "UploadFirebase"

        fun debug(tag: String, msg: String?) {
            Log.d("$TAG-$tag", msg)
        }

        fun info(tag: String, msg: String?) {
            Log.i("$TAG-$tag", msg)
        }

        fun warning(tag: String, msg: String?) {
            Log.w("$TAG-$tag", msg)
        }

        fun error(tag: String, msg: String?) {
            Log.e("$TAG-$tag", msg)
        }
    }
}