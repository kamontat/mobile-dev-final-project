package com.kamontat.uploadfirebase

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.kamontat.uploadfirebase.utils.Logger

import kotlinx.android.synthetic.main.activity_view_location.*
import kotlinx.android.synthetic.main.content_view_location.*

private const val MY_CODE_FOR_REQUEST_ACCESS_COARSE_LOCATION = 201

class ViewLocationActivity : AppCompatActivity(), android.location.LocationListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationManager: LocationManager

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_location)
        setSupportActionBar(toolbar_on_view_location)

        signout_on_view_location.setOnClickListener { view ->
            FirebaseAuth.getInstance().signOut()
            finish()
        }

        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        callForPermission()

        update_button.setOnClickListener {
            if (isPermissionGranted())
                fusedLocationClient.lastLocation.addOnCompleteListener {
                    if (it.result != null) {
                        setLocation(it.result)
                        Logger.debug("location", "force update")
                    }
                }
            else
                update_button.isEnabled = false
        }
    }

    private fun callForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getLocation()
        }
        if (isPermissionGranted()) {
            Toast.makeText(applicationContext, "Call for permission", Toast.LENGTH_SHORT).show()
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Logger.debug("permission request", "need to show explanation")
            } else {
                // No explanation needed, we can request the permission.
                Logger.debug("permission request", "requesting")
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_CODE_FOR_REQUEST_ACCESS_COARSE_LOCATION)
            }
        } else {
            getLocation()
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val locReq: LocationRequest = LocationRequest.create()
        locReq.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locReq.interval = 10 * 1000
        locReq.fastestInterval = 1 * 1000

        mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 10 * 1000, 1 * 10F, this)

        setLocation(mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER))
    }

    private fun setLocation(location: Location?) {
        prov_text.text = location?.provider ?: "unknown"
        long_text.text = location?.longitude.toString()
        lat_text.text = location?.latitude.toString()
        acc_text.text = location?.accuracy.toString()
        alt_text.text = location?.altitude.toString()
        bear_text.text = location?.bearing.toString()
        spd_text.text = location?.speed.toString()
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Logger.debug("status", "change.. $status $provider")
    }

    override fun onProviderEnabled(provider: String?) {
        Logger.debug("provider", "enabled")
    }

    override fun onProviderDisabled(provider: String?) {
        Logger.debug("provider", "disabled")
    }

    override fun onLocationChanged(location: Location?) {
        setLocation(location)
        Logger.debug("location", "changed")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Logger.debug("permission", "requested by $requestCode code")
        when (requestCode) {
            MY_CODE_FOR_REQUEST_ACCESS_COARSE_LOCATION -> {
                if ((grantResults.isNotEmpty() &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                                grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    Logger.debug("permission request", "granted permission")
                    getLocation()
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Logger.debug("permission request", "denied permission")
                    Toast.makeText(applicationContext, "Permission has been denied!", Toast.LENGTH_SHORT).show()
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
        }
    }
}
