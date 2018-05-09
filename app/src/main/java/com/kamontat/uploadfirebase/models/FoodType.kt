package com.kamontat.uploadfirebase.models

import android.support.annotation.Keep
import java.io.Serializable

/**
 * Created by kamontat on 9/5/18.
 */
@Keep
enum class FoodType : Serializable {
    Food, Dessert;

    companion object {
        fun convert(str: String): FoodType {
            return valueOf(str)
        }
    }

    override fun toString(): String {
        return this.name
    }
}