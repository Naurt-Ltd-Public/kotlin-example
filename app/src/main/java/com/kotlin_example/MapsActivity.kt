package com.kotlin_example

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import com.naurt_kotlin_sdk.Naurt.INSTANCE as Naurt

class MapsActivity : AppCompatActivity() {
    private var naurtSwitch: SwitchCompat? = null
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )

    // ================================== Application Functions ====================================
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        println("~~~~~~~~~~~~~~~~~~~~~~ onSaveInstanceState ~~~~~~~~~~~~~~~~~~~~~~")
        // Save the title and initialisation state for use onResume
        outState.putString("title", title.toString())

        // Pause Naurt for use in future
        pauseNaurt()
    }

    override fun onResume() {
        super.onResume()

        println("~~~~~~~~~~~~~~~~~~~~~~ onResume ~~~~~~~~~~~~~~~~~~~~~~")
        resumeNaurt(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("~~~~~~~~~~~~~~~~~~~~~~ onCreate ~~~~~~~~~~~~~~~~~~~~~~")


        // Assume a fresh start, initialise as normal
        title = resources.getString(R.string.disabled_title)

        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        } else {
            if (savedInstanceState != null) {
                println("~~~~~~~~~~~~~~~~~~~~~~ Resuming Naurt...")
                title = savedInstanceState.getString("title")
                resumeNaurt(applicationContext)
            }
            else {
                initialiseNaurt(applicationContext)
            }
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val itemSwitch: MenuItem = menu.findItem(R.id.naurt_action_bar_switch)
        itemSwitch.setActionView(R.layout.use_switch)

        naurtSwitch = itemSwitch.actionView.findViewById(R.id.naurt_switch) as SwitchCompat

        naurtSwitch!!.setOnCheckedChangeListener { _, isChecked ->
            val hasPerms = hasPermissions(this, permissions)

            if (!hasPerms) {
                Toast.makeText(
                    applicationContext,
                    "The app does not have all required permissions, please enable them",
                    Toast.LENGTH_LONG
                ).show()
                naurtSwitch!!.isChecked = false
                title = resources.getString(R.string.disabled_title)
            } else {
                // If naurt is null, create it
                if (!Naurt.isInitialised.get()) {
                    initialiseNaurt(applicationContext)
                }

                // If we are attempting to turn naurt on
                // WARNING - Setting this programmatically WILL trigger this!
                if (isChecked && Naurt.isInitialised.get() && !Naurt.isRunning.get()) {
                    Naurt.start()
                    Log.i("naurt", "Naurt Starting...")

                    // If naurt cannot validate, toast & disable
                    if (!Naurt.isValidated.get()) {
                        Toast.makeText(
                            applicationContext,
                            "Naurt could not validate this app's API Key - Did you have internet when enabling Naurt?",
                            Toast.LENGTH_LONG
                        ).show()
                        naurtSwitch!!.isChecked = false
                        title = resources.getString(R.string.disabled_title)
                    }
                    // Else add a listener
                    else {
                        title = resources.getString(R.string.enabled_title)
                        Naurt.naurtPoint.addOnPropertyChanged(naurtCallback!!)
                        naurtSwitch!!.isChecked = true
                    }
                }
                // If we are attempting to turn naurt off, stop & remove
                else if (!isChecked && Naurt.isInitialised.get() && Naurt.isRunning.get()) {
                    Naurt.stop()
                    Naurt.naurtPoint.removeOnPropertyChanged(naurtCallback!!)
                    Log.i("naurt", "Naurt Stopping...")
                    title = resources.getString(R.string.disabled_title)
                    naurtSwitch!!.isChecked = false
                }
            }
        }

        return true
    }
    // ================================== Application Functions ====================================
}