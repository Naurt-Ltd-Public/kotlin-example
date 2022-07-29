package com.naurt.demo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.*
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.naurt.*
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    override fun onDestroy() {
        super.onDestroy()
        if (selectedRuntime != "Terminable Service") {
            Naurt.stop()
        }
    }

    private lateinit var adapter: ErrorRecycler
    private lateinit var recyclerView: RecyclerView
    private lateinit var naurtButton: Button

    private val mainHandler: Handler = Handler(Looper.getMainLooper())
    private var errors: ArrayList<String> = arrayListOf()

    private lateinit var locationManager: LocationManager
    private lateinit var sensorManager: SensorManager

    private lateinit var onlineError: String
    private lateinit var initialisedError: String
    private lateinit var validatedError: String
    private lateinit var locationError: String
    private lateinit var permissionError: String
    private lateinit var uploadingText: TextView
    private lateinit var runningText: TextView
    private lateinit var engineDropdown: Spinner
    private lateinit var uploadJob: ScheduledFuture<*>
    private val scheduledExecutor = ScheduledThreadPoolExecutor(2)
    private lateinit var selectedRuntime: String

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE
    )

    private fun updateRecycler() {
        adapter = ErrorRecycler(this, errors)
        recyclerView.adapter = adapter
    }

    private lateinit var permissionRunnable: ScheduledFuture<*>

    private fun scheduleCacheClean() {
        uploadJob = scheduledExecutor.schedule({
            scheduleCacheClean()

            val dirtyCache = Naurt.getInitialised() && !Naurt.isCacheClean().get()

            mainHandler.post {
                uploadingText.isVisible = dirtyCache
            }

            if (dirtyCache) {
                Naurt.cleanCache()
            }
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

        engineDropdown = findViewById(R.id.engineDropdown)
        val engineTypes = arrayListOf("Default Standalone", "Default Service", "Managed Standalone", "Managed Service", "Terminable Service")
        val adapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, engineTypes)
        engineDropdown.adapter = adapter
        engineDropdown.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedRuntime = parent?.getItemAtPosition(position) as String

                when (selectedRuntime) {
                    "Default Standalone" -> {
                        scheduledExecutor.submit {
                            naurtButton.isVisible = true
                            Naurt.initialiseStandalone(BuildConfig.API_KEY, applicationContext)
                        }
                    }
                    "Default Service" -> {
                        scheduledExecutor.submit {
                            naurtButton.isVisible = true
                            Naurt.initialiseService(BuildConfig.API_KEY, applicationContext)
                        }
                    }
                    "Managed Standalone" -> {
                        naurtButton.isVisible = true
                        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

                        scheduledExecutor.submit {
                            Naurt.initialiseManagedStandalone(
                                BuildConfig.API_KEY,
                                applicationContext,
                                locationManager,
                                sensorManager
                            )
                        }
                    }
                    "Managed Service" -> {
                        naurtButton.isVisible = true
                        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

                        scheduledExecutor.submit {
                            Naurt.initialiseManagedService(
                                BuildConfig.API_KEY,
                                applicationContext,
                                locationManager,
                                sensorManager
                            )
                        }
                    }
                    "Terminable Service" -> {
                        mTerminatedApplicationLocationService = TerminatedApplicationLocationService()
                        mServiceIntent = Intent(applicationContext, mTerminatedApplicationLocationService.javaClass)
                        startService(mServiceIntent)
                        naurtButton.isVisible = false
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        this.title = "Naurt Data Collection"

        naurtButton = findViewById(R.id.naurtButton)
        naurtButton.setOnClickListener {
            if (Naurt.getInitialised()) {
                when {
                    Naurt.getRunning() -> {
                        scheduledExecutor.submit {
                            Naurt.stop()
                        }
                        naurtButton.text = resources.getString(R.string.start_naurt)
                        naurtButton.setBackgroundColor(Color.parseColor("#4CAF50"))
                        runningText.isVisible = false
                        uploadingText.isVisible = true;
                    }
                    errors.isEmpty() && !Naurt.getRunning() -> {
                        scheduledExecutor.submit {
                            Naurt.start()
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
        scheduleCacheClean()
        addListeners()

        permissionRunnable = scheduledExecutor.scheduleAtFixedRate({
            mainHandler.post {
                val hasPerms = hasPermissions(this, permissions)

                if (!hasPerms) {
                    requestPermissions()
                }
                else {
                    if (errors.contains(permissionError)) {
                        errors.remove(permissionError)
                    }
                    permissionRunnable.cancel(true)
                }
            }
        }, 0, 1, TimeUnit.SECONDS)
    }

    private fun addListeners() {
        Naurt.on(NaurtEvents.IS_INITIALISED, NaurtEventListener<NaurtIsInitialisedEvent> {
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
        Naurt.on(NaurtEvents.IS_VALIDATED, NaurtEventListener<NaurtIsValidatedEvent> {
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
        Naurt.on(NaurtEvents.IS_ONLINE, NaurtEventListener<NaurtIsOnlineEvent> {
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
        Naurt.on(NaurtEvents.HAS_LOCATION_PROVIDER, NaurtEventListener<NaurtHasLocationProviderEvent> {
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
        Naurt.on(NaurtEvents.NEW_LOCATION, NaurtEventListener<NaurtNewLocationEvent> {
            Log.e("naurt", "${it.newPoint.timestamp} | ${it.newPoint.latitude}, ${it.newPoint.longitude}")
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && context != null) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        return true
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                Log.e("demo", "Background Permission not granted")
            }
        }

    private fun requestPermissions() {
        requestPermissionsLauncher.launch(permissions)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
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