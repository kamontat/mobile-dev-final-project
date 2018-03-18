package com.kamontat.uploadfirebase.models

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kamontat.uploadfirebase.constants.ROOT_OF_DATABASE

/**
 * Created by kamontat on 16/3/2018 AD.
 */

data class Post(val title: String = "", val imageUrl: String = "", val timestamp: String = "", val uid: String) {
    fun delete(): Task<Void>? {
        return FirebaseDatabase.getInstance()
                .getReference(ROOT_OF_DATABASE)
                .child(FirebaseAuth.getInstance().currentUser?.uid)
                .child(uid)
                .removeValue()
    }
}