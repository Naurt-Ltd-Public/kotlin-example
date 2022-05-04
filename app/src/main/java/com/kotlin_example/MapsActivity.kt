package com.kotlin_example

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.kotlin_example.utils.*
import com.kotlin_example.utils.colourSessionSignal
import kotlinx.coroutines.launch

import com.naurt.Sdk.INSTANCE as Sdk

internal var hasInternetSignal: RadioButton? = null
internal var hasLocationSignal: RadioButton? = null
internal var hasPermissionsSignal: RadioButton? = null

internal var isInitialisedSignal: RadioButton? = null
internal var isValidatedSignal: RadioButton? = null
internal var isRunningSignal: RadioButton? = null

internal var isSessionSignal: RadioButton? = null

internal var forceUploadButton: Button? = null

internal var red: Int = 0
internal var green: Int = 0
internal var grey: Int = 0

class MapsActivity : AppCompatActivity() {
    private var naurtSwitch: SwitchCompat? = null
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // ================================== Application Functions ====================================
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the title and initialisation state for use onResume
        outState.putString("title", title.toString())

        // Pause Naurt for use in future
        pauseNaurt()
    }

    override fun onResume() {
        super.onResume()
        resumeNaurt(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        red = ResourcesCompat.getColor(resources, R.color.red, null)
        green = ResourcesCompat.getColor(resources, R.color.green, null)
        grey = ResourcesCompat.getColor(resources, R.color.grey, null)

        hasInternetSignal = findViewById(R.id.hasInternet)
        hasLocationSignal = findViewById(R.id.hasLocation)
        hasPermissionsSignal = findViewById(R.id.hasPermissions)

        isInitialisedSignal = findViewById(R.id.isInitialised)
        isValidatedSignal = findViewById(R.id.isValidated)
        isRunningSignal = findViewById(R.id.isRunning)

        isSessionSignal = findViewById(R.id.sessionStatus)

        forceUploadButton = findViewById(R.id.flushButton)
        forceUploadButton?.let { fub ->
            fub.setOnClickListener {
                Sdk.cleanCache()
                colourSessionSignal()
            }
        }

        setStrict()

        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }

        dispatchPermissionSignal(applicationContext, permissions)


        if (savedInstanceState != null) {
            title = savedInstanceState.getString("title")
            resumeNaurt(applicationContext)
        }
        else {
            initialiseNaurt(applicationContext)
        }
        colourSessionSignal()
    }

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
            } else {
                if (isChecked && Sdk.isInitialised && Sdk.isValidated) {
                    unorderedScope.launch {
                        Sdk.start().thenAccept { status ->
                            println("Sdk start: $status")
                        }
                    }
                    forceUploadButton?.let { it.isEnabled = false }
                }
                else if (!isChecked && Sdk.isInitialised && Sdk.isValidated) {
                    unorderedScope.launch {
                        Sdk.stop().thenAccept { status ->
                            println("Sdk Stop: $status")
                        }
                    }
                    forceUploadButton?.let { it.isEnabled = true }
                }
                else {
                    naurtSwitch!!.isChecked = !isChecked
                }
            }
        }

        return true
    }
    // ================================== Application Functions ====================================
}