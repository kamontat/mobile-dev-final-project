package com.kamontat.uploadfirebase

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kamontat.uploadfirebase.constants.ROOT_OF_DATABASE
import com.kamontat.uploadfirebase.controllers.PostDataHolder
import com.kamontat.uploadfirebase.models.Post
import com.kamontat.uploadfirebase.utils.Logger
import kotlinx.android.synthetic.main.activity_main.*

import kotlinx.android.synthetic.main.activity_view.*
import android.view.LayoutInflater
import android.view.Window
import com.kamontat.uploadfirebase.constants.DATABASE_KEY_CREATED_AT
import com.kamontat.uploadfirebase.constants.DATABASE_KEY_IMAGE_URL
import com.kamontat.uploadfirebase.constants.DATABASE_KEY_TITLE
import kotlinx.android.synthetic.main.content_view.*


class ViewActivity : AppCompatActivity() {
    private var recyclerAdapter: FirebaseRecyclerAdapter<Post, PostDataHolder>? = null

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)
        setSupportActionBar(toolbar_on_view)

        signout_on_view.setOnClickListener {
            mAuth.signOut()
            finish()
        }

        val query = FirebaseDatabase.getInstance().getReference(ROOT_OF_DATABASE).child(mAuth.currentUser?.uid) // .limitToLast(50)

        val fireOpt: FirebaseRecyclerOptions<Post> = FirebaseRecyclerOptions.Builder<Post>().setQuery(query, SnapshotParser {
            Logger.debug("downloading", "key: ${it.key}")
            return@SnapshotParser Post(
                    it.child(DATABASE_KEY_TITLE).value.toString(),
                    it.child(DATABASE_KEY_IMAGE_URL).value.toString(),
                    it.child(DATABASE_KEY_CREATED_AT).value.toString()
            )
        }).build()

        // val adapter = FirebaseRecyclerAdapter.Builder

        recyclerAdapter = object : FirebaseRecyclerAdapter<Post, PostDataHolder>(fireOpt) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostDataHolder {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.element_of_view, parent, false)
                return PostDataHolder(view)
            }

            override fun onBindViewHolder(holder: PostDataHolder, position: Int, model: Post) {
                holder.setImageUrl(model.imageUrl)
                holder.setTitle(model.title)
            }
        }

        recycle_view.adapter = recyclerAdapter

        val manager = LinearLayoutManager(applicationContext)
        recycle_view.layoutManager = manager
    }

    override fun onStart() {
        super.onStart()
        recyclerAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        recyclerAdapter?.stopListening()
    }
}
