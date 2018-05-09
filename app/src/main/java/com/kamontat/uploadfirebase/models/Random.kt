package com.kamontat.uploadfirebase.models

import java.util.Random

/**
 * Created by kamontat on 9/5/18.
 */

class Random {

    companion object {
        fun inLength(from: Int = 0, to: Int = 10): Int {
            return from + Random(System.nanoTime()).nextInt(to)
        }
    }
}