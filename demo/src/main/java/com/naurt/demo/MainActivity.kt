package com.naurt.demo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.RecyclerView
import com.naurt.Sdk
import com.naurt.events.*
import kotlinx.coroutines.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity(), ErrorRecycler.ItemClickListener {
    private lateinit var adapter: ErrorRecycler
    private lateinit var recyclerView: RecyclerView
    private lateinit var naurtButton: Button

    private val mainHandler: Handler = Handler(Looper.getMainLooper())
    private var errors: ArrayList<String> = arrayListOf()

    private lateinit var onlineError: String
    private lateinit var initialisedError: String
    private lateinit var validatedError: String
    private lateinit var locationError: String
    private lateinit var permissionError: String
    private lateinit var uploadingText: TextView
    private lateinit var runningText: TextView

    private lateinit var uploadJob: ScheduledFuture<*>

    private val scheduledExecutor = ScheduledThreadPoolExecutor(2)

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
    )

    private fun updateRecycler() {
        adapter = ErrorRecycler(this, errors)
        adapter.setClickListener(this)
        recyclerView.adapter = adapter
    }

    private lateinit var permissionRunnable: ScheduledFuture<*>
    private fun scheduleCacheClean() {
        uploadJob = scheduledExecutor.schedule({
            val dirtyCache = Sdk.isInitialised && !Sdk.isCacheClean().get(1000, TimeUnit.MILLISECONDS)

            uploadingText.isVisible = dirtyCache
            
            if (dirtyCache) {
                Sdk.cleanCache().get(1000, TimeUnit.MILLISECONDS)
            }

            scheduleCacheClean()
        }, 1000, TimeUnit.MILLISECONDS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setStrict()

        onlineError = resources.getString(R.string.online_error)
        initialisedError = resources.getString(R.string.initialised_error)
        validatedError = resources.getString(R.string.validated_error)
        locationError = resources.getString(R.string.location_error)
        permissionError = resources.getString(R.string.permission_error)

        uploadingText = findViewById(R.id.uploadNotification)
        uploadingText.isVisible = false

        runningText = findViewById(R.id.notificationMessage)
        runningText.isVisible = false

        scheduleCacheClean()

        this.title = "Naurt Data Collection"

        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }

        permissionRunnable = scheduledExecutor.scheduleAtFixedRate({
            if (!hasPermissions(this, permissions)) {
                if (!errors.contains(permissionError)) {
                    errors.add(permissionError)
                }
                ActivityCompat.requestPermissions(this, permissions, 1)
            } else {
                if (errors.contains(permissionError)) {
                    errors.remove(permissionError)
                }
                permissionRunnable.cancel(true)
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS)

        naurtButton = findViewById(R.id.naurtButton)
        naurtButton.setOnClickListener {
            if (Sdk.isInitialised) {
                when {
                    Sdk.isRunning -> {
                        scheduledExecutor.submit {
                            Sdk.stop().get()
                        }
                        naurtButton.text = resources.getString(R.string.start_naurt)
                        naurtButton.setBackgroundColor(Color.parseColor("#4CAF50"))
                        runningText.isVisible = false
//                        uploadingText.isVisible = true;
                    }
                    errors.isEmpty() && !Sdk.isRunning -> {
                        scheduledExecutor.submit {
                            Sdk.start().get()
                        }
                        naurtButton.text = resources.getString(R.string.stop_naurt)
                        naurtButton.setBackgroundColor(Color.parseColor("#F44336"))
                        runningText.isVisible = true
                    }
                    else -> {
                        Toast.makeText(this, "You cannot start Naurt, as you have errors", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

        // set up the RecyclerView
        recyclerView = findViewById(R.id.error_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        updateRecycler()

        addListeners()

        if (!Sdk.isInitialised) {
            scheduledExecutor.submit {
                // TODO: Handle blocking operation better
                val status = Sdk.initialise(BuildConfig.API_KEY, this, 6).get()
                Log.d("naurt", "Initialised SDK! $status")
            }
        }
    }

    override fun onItemClick(view: View?, position: Int) {
//        Toast.makeText(this, "You clicked " + adapter?.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show()
    }

    private fun addListeners() {
        Sdk.on("NAURT_IS_INITIALISED", EventListener<NaurtIsInitialisedEvent> {
            mainHandler.post {
                if (!it.isInitialised) {
                    if (!errors.contains(initialisedError)) {
                        errors.add(initialisedError)
                    }
                }
                else {
                    if (errors.contains(initialisedError)) {
                        errors.remove(initialisedError)
                    }
                }

                updateRecycler()
            }
        })
        Sdk.on("NAURT_IS_VALIDATED", EventListener<NaurtIsValidatedEvent> {
            mainHandler.post {
                if (!it.isValidated) {
                    if (!errors.contains(validatedError)) {
                        errors.add(validatedError)
                    }
                }
                else {
                    if (errors.contains(validatedError)) {
                        errors.remove(validatedError)
                    }
                }

                updateRecycler()
            }
        })
        Sdk.on("NAURT_IS_ONLINE", EventListener<NaurtIsOnlineEvent> {
            mainHandler.post {
                if (!it.isOnline) {
                    if (!errors.contains(onlineError)) {
                        errors.add(onlineError)
                    }
                }
                else {
                    if (errors.contains(onlineError)) {
                        errors.remove(onlineError)
                    }
                }

                updateRecycler()
            }
        })
        Sdk.on("NAURT_HAS_LOCATION", EventListener<NaurtHasLocationProviderEvent> {
            mainHandler.post {
                if (!it.hasLocationProvider) {
                    if (!errors.contains(locationError)) {
                        errors.add(locationError)
                    }
                }
                else {
                    if (errors.contains(locationError)) {
                        errors.remove(locationError)
                    }
                }

                updateRecycler()
            }
        })
    }

    /** Check to see if the given context has been granted all permissions in the input array */
    private fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
        if (context != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun setStrict() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
    }
}