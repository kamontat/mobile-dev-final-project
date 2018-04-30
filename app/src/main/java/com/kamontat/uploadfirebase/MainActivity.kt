package com.kamontat.uploadfirebase

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.kamontat.uploadfirebase.constants.*
import com.kamontat.uploadfirebase.utils.Logger
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    // private const val ROOT_URL_OF_FIREBASE = "https://uploadfirebase-2.firebaseio.com/"
    private val MY_CODE_FOR_REQUEST_READ_EXTERNAL_STORAGE = 123
    private val MY_CODE_FOR_REQUEST_GALLERY_IMAGE = 122

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var image: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val head = resources.getString(R.string.your_login_is)
        val email = (mAuth.currentUser?.email ?: "unknown")
        val text = "$head $email"
        username_message.text = text

        signout.setOnClickListener {
            mAuth.signOut()
            finish()
        }

        mAuth.addAuthStateListener {
            if (it.currentUser == null) {
                Logger.debug("auth", "sign out")
                finish()
            } else Logger.debug("auth", "stage change")
        }

        image_button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Call for permission", Toast.LENGTH_SHORT).show()
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // callGallery()
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Logger.debug("permission request", "need to show explanation")
                } else {
                    // No explanation needed, we can request the permission.
                    Logger.debug("permission request", "requesting")
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            MY_CODE_FOR_REQUEST_READ_EXTERNAL_STORAGE)
                }
            } else {
                callGallery()
            }
        }

        save_button.setOnClickListener(View.OnClickListener {
            if (isUploading()) return@OnClickListener

            val name = input_message.text.toString().trim()
            val image: Uri? = this.image
            Logger.debug("save image:name", name)
            Logger.debug("save image:image", image.let { "null" })

            if (name.isEmpty()) {
                Toast.makeText(applicationContext, "Please enter title", Toast.LENGTH_SHORT).show()
                return@OnClickListener;
            }

            if (image == null) {
                Toast.makeText(applicationContext, "Please enter image", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            saveImageToDatabase(image, name)
        });

        view_button.setOnClickListener {
            startActivity(Intent(applicationContext, ViewActivity::class.java))
        }

        get_location_btn.setOnClickListener {
            startActivity(Intent(applicationContext, ViewLocationActivity::class.java))
        }
    }

    private fun callGallery() {
        Logger.debug("gallery", "called")
        val intent = Intent(Intent(Intent.ACTION_PICK)).setType("image/*")
        startActivityForResult(intent, MY_CODE_FOR_REQUEST_GALLERY_IMAGE)
    }

    private fun saveImageToDatabase(uri: Uri, message: String) {
        val uid = mAuth.currentUser?.uid ?: "null"
        val uuid: UUID = UUID.randomUUID()
        val database = FirebaseDatabase.getInstance().getReference(ROOT_OF_DATABASE).child(uid).child(uuid.toString())
        val storageRef = FirebaseStorage.getInstance().getReference(ROOT_OF_STORAGE).child(uid).child(uuid.toString())
        val timestamp: String = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
        startUpload()

        storageRef.putFile(uri).addOnCompleteListener {
            val downloadUri: Uri? = it.result.downloadUrl

            Logger.debug("Upload-image", "complete, $downloadUri!")

            database.child(DATABASE_KEY_TITLE).setValue(message).continueWith {
                Logger.debug("Upload-title", "complete!")
                database.child(DATABASE_KEY_IMAGE_URL).setValue(downloadUri?.toString()).continueWith {
                    Logger.debug("Upload-image-url", "complete!")
                    database.child(DATABASE_KEY_CREATED_AT).setValue(timestamp).continueWith {
                        Logger.debug("Upload-timestamp", "complete!")
                        endUpload()
                        Logger.debug("Upload-all", "complete!")

                        Toast.makeText(applicationContext, "Uploaded to the server...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun startUpload() {
        runOnUiThread {
            progressbar.visibility = View.VISIBLE
            image_button.isEnabled = false
            save_button.isEnabled = false
            view_button.isEnabled = false
            signout.isEnabled = false
        }
    }

    private fun endUpload() {
        runOnUiThread {
            progressbar.visibility = View.INVISIBLE
            image_button.isEnabled = true
            save_button.isEnabled = true
            view_button.isEnabled = true
            signout.isEnabled = true

            // clear text and image
            input_message.text.clear()
            image_button.setImageResource(android.R.drawable.ic_menu_report_image)
            image = null
        }
    }

    private fun isUploading(): Boolean {
        return progressbar.visibility == View.VISIBLE
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Logger.debug("permission", "requested by $requestCode")
        when (requestCode) {
            MY_CODE_FOR_REQUEST_READ_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Logger.debug("permission request", "granted permission")
                    callGallery()
                } else {
                    Logger.debug("permission request", "denied permission")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_CODE_FOR_REQUEST_GALLERY_IMAGE && resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri? = data?.data
            image_button.setImageURI(selectedImage)
            image = selectedImage
        }
    }
}