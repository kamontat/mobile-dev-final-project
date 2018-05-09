package com.kamontat.uploadfirebase.models

import android.net.Uri
import android.support.annotation.Keep
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kamontat.uploadfirebase.RandomActivity
import com.kamontat.uploadfirebase.constants.ROOT_OF_MENU
import com.kamontat.uploadfirebase.utils.Logger
import kotlinx.android.synthetic.main.content_random.*
import java.io.Serializable
import java.net.URI
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

/**
 * Created by kamontat on 9/5/18.
 */
@Keep
data class Menu(
        var id: String = "",
        var name: String = "",
        var type: String = FoodType.Food.name,
        var image: String = "",
        var createAt: Long? = System.currentTimeMillis() / 1000,
        var updateAt: Long? = System.currentTimeMillis() / 1000,
        var vote: Int = 0,
        var adder: String = ""
) : Serializable {
    companion object {
        fun generate(data: DataSnapshot): Menu {
            return Menu(
                    id = data.child("id").value.toString(),
                    name = data.child("name").value.toString(),
                    type = data.child("type").value.toString(),
                    image = data.child("image").value.toString(),
                    adder = data.child("adder").value.toString(),
                    createAt = data.child("createAt").getValue(Long::class.java),
                    updateAt = data.child("updateAt").getValue(Long::class.java),
                    vote = data.child("vote").getValue(Int::class.java)!!
            )
        }

        fun generate(name: String, type: String, image: String, vote: Int = 0): Menu {
            return Menu(
                    id = FirebaseDatabase.getInstance().getReference(ROOT_OF_MENU).push().key,
                    name = name,
                    type = type,
                    image = image,
                    adder = FirebaseAuth.getInstance().currentUser?.uid!!,
                    vote = vote
            )
        }
    }

    fun getImageURL(): URL {
        return URL(image)
    }

    fun getFoodTypeEnum(): FoodType {
        return FoodType.convert(type)
    }

    fun save(ref: DatabaseReference): Task<Void> {
        val map = Collections.emptyMap<String, Any>().toMutableMap()

        map["id"] = id
        map["name"] = name
        map["type"] = type
        map["image"] = image
        map["adder"] = adder
        map["createAt"] = createAt
        map["updateAt"] = updateAt
        map["vote"] = vote

        return ref.setValue(map)
    }
}
