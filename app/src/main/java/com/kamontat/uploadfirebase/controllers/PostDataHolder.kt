package com.kamontat.uploadfirebase.controllers

import android.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.TransitionOptions
import com.kamontat.uploadfirebase.R
import com.kamontat.uploadfirebase.models.Post
import java.util.logging.Logger
import android.widget.Toast
import com.kamontat.uploadfirebase.MainActivity
import android.content.DialogInterface


/**
 * Created by kamontat on 17/3/2018 AD.
 */
class PostDataHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var title: TextView? = null
    private var image: ImageButton? = null

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

    fun setDeleteAction(model: Post) {
        image?.setOnClickListener {
            AlertDialog.Builder(itemView.context)
                    .setTitle("Delete")
                    .setMessage("Do you really want to delete ${model.title} image?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { _, _ ->
                        model.delete()?.continueWith {
                            Toast.makeText(itemView.context, "DELETED IMAGE..", Toast.LENGTH_SHORT).show()
                            com.kamontat.uploadfirebase.utils.Logger.debug("delete model", "${model.toString()} had been deleted")
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show()
        }
    }
}