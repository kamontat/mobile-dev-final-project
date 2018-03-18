package com.kamontat.uploadfirebase.models

import android.content.Context
import com.google.firebase.storage.StorageReference
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import java.io.InputStream


/**
 * Created by kamontat on 16/3/2018 AD.
 */
@GlideModule
class AppGlideModuleImp : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // Register FirebaseImageLoader to handle StorageReference
        registry.append(StorageReference::class.java, InputStream::class.java, FirebaseImageLoader.Factory())

    }
}