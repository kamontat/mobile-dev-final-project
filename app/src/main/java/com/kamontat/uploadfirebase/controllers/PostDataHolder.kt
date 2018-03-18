package com.kamontat.uploadfirebase.controllers

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.TransitionOptions
import com.kamontat.uploadfirebase.R

/**
 * Created by kamontat on 17/3/2018 AD.
 */
class PostDataHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var title: TextView? = null
    private var image: ImageView? = null

    init {
        title = itemView.findViewById(R.id.elem_title)
        image = itemView.findViewById(R.id.elem_image)
    }

    fun setTitle(value: String) {
        title?.text = value
    }

    fun setImageUrl(value: String) {
        image?.let { Glide.with(itemView.context).load(value).into(it) }
    }
}