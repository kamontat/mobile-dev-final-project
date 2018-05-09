package com.kamontat.uploadfirebase

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.kamontat.uploadfirebase.constants.*
import com.kamontat.uploadfirebase.utils.Logger
import java.util.*
import java.util.concurrent.TimeUnit
import android.widget.ArrayAdapter
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kamontat.uploadfirebase.models.FoodType
import com.kamontat.uploadfirebase.models.Menu


class MainActivity : AppCompatActivity() {

    // private const val ROOT_URL_OF_FIREBASE = "https://uploadfirebase-2.firebaseio.com/"
    private val MY_CODE_FOR_REQUEST_READ_EXTERNAL_STORAGE = 123
    private val MY_CODE_FOR_REQUEST_GALLERY_IMAGE = 122

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var image: Uri? = null

    private var error: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val head = resources.getString(R.string.your_login_is)
        val email = (mAuth.currentUser?.email ?: "unknown")
        val text = "$head $email"
        username_message.text = text

        input_type.adapter = ArrayAdapter<FoodType>(this, android.R.layout.simple_spinner_item, FoodType.values())

        signout.setOnClickListener {
            mAuth.signOut()
        }

        mAuth.addAuthStateListener {
            if (it.currentUser == null) {
                Logger.debug("auth", "sign out")
                finish()
            } else Logger.debug("auth", "stage change")
        }

        save_button.setOnClickListener(View.OnClickListener {
            if (!checkInput()) {
                Toast.makeText(this, error,
                        Toast.LENGTH_SHORT).show()
                Logger.error("save", error)
            }
            Logger.debug("save-name", input_name.text.toString())
            Logger.debug("save-image", input_image.text.toString())
            Logger.debug("save-type", input_type.selectedItem.toString())

            val menu = Menu.generate(input_name.text.toString(), input_type.selectedItem.toString(), input_image.text.toString())

            FirebaseDatabase.getInstance().getReference(ROOT_OF_MENU).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(err: DatabaseError?) {
                    Logger.error("database-read", err?.toString())
                }

                override fun onDataChange(data: DataSnapshot?) {
                    val list = data?.children?.toMutableList()
                    val menus = list?.map(Menu.Companion::generate)?.toMutableList()
                    menus?.add(menu)

                    SaveMenu(this@MainActivity).execute(menus)
                }
            })
        })
    }

    private fun checkInput(): Boolean {
        var isError = false
        if (!isExist(input_name.text.toString())) {
            error = "input name, "
            isError = true
        }
        if (!isExist(input_image.text.toString())) {
            error += "input image, "
            isError = true
        }
        if (!isExist(input_type.selectedItem.toString())) {
            error += "input type, "
            isError = true
        }
        if (isError) {
            error += " not exist."
        }
        return !isError
    }

    private fun isExist(text: String?): Boolean {
        return !text.isNullOrBlank()
    }

    private fun getInputError(): String {
        return if (error != null) error!! else "none"
    }

    class SaveMenu(private val context: Context) : AsyncTask<MutableList<Menu>, Unit, Unit>() {

        override fun doInBackground(vararg menus: MutableList<Menu>?) {
            val menu = menus[0] ?: return
            val lastMenu = menu.last()

            val root = FirebaseDatabase.getInstance().getReference(ROOT_OF_MENU)
            val index = root.child((menu.count() - 1).toString())

            val task = lastMenu.save(index)

            task.addOnCompleteListener {
                Toast.makeText(context, "Saved !!", Toast.LENGTH_SHORT).show()
                Logger.debug("save", "all completed")
            }
        }
    }
}
