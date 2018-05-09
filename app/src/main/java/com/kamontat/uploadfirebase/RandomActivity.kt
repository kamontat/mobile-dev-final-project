package com.kamontat.uploadfirebase

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kamontat.uploadfirebase.R.id.foodImage
import com.kamontat.uploadfirebase.constants.ROOT_OF_MENU
import com.kamontat.uploadfirebase.models.Menu
import com.kamontat.uploadfirebase.models.Random
import com.kamontat.uploadfirebase.utils.Logger

import kotlinx.android.synthetic.main.activity_random.*
import kotlinx.android.synthetic.main.content_random.*
import java.lang.reflect.Executable
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class RandomActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random)
        setSupportActionBar(toolbar)

        randomFood()

        randBtn.setOnClickListener({ _ ->
            randomFood()
        })

        fab.setOnClickListener { _ ->
            startActivity(Intent(applicationContext, LoginActivity::class.java))
        }
    }

    private fun randomFood() {
        FirebaseDatabase.getInstance().getReference(ROOT_OF_MENU).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(err: DatabaseError?) {
                Logger.error("database-read", err?.toString())
            }

            override fun onDataChange(data: DataSnapshot?) {
                val list = data?.children?.toList()
                if (list != null) {
                    val elem = list.shuffled(java.util.Random(System.nanoTime())).last()
                    if (!elem.exists()) {
                        Logger.error("ele", elem.toString() + " not exist")
                        return
                    }
                    val menu = Menu.generate(elem)

                    Logger.debug("menu", menu.id)
                    Logger.debug("menu", menu.name)

                    foodImage.setOnClickListener {
                        Logger.debug("image", "CLICK!")
                    }

                    val result = FetchImage(foodImage, randBtn, menu.getImageURL()).execute()
                    result.get(12, TimeUnit.SECONDS)
                    foodName.text = menu.name
                } else {
                    Logger.error("list", "is null")
                }
            }
        })
    }

    class FetchImage(v: ImageButton, b: Button, i: URL) : AsyncTask<Unit, Unit, Bitmap>() {
        private val image = i

        @SuppressLint("StaticFieldLeak")
        private val view = v

        @SuppressLint("StaticFieldLeak")
        private val button = b

        override fun onPreExecute() {
            super.onPreExecute()

            view.isEnabled = false
            button.isEnabled = false
        }

        override fun doInBackground(vararg params: Unit?): Bitmap {
            return BitmapFactory.decodeStream(image.openConnection().getInputStream());
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)

            view.setImageBitmap(result)

            view.isEnabled = true
            button.isEnabled = true
        }
    }
}
