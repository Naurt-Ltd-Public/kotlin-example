package com.naurt.demo

import android.R
import android.app.*
import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.naurt.Naurt
import com.naurt.NaurtEventListener
import com.naurt.NaurtEvents
import com.naurt.NaurtNewLocationEvent
import android.content.BroadcastReceiver

internal var mTerminatedApplicationLocationService: TerminatedApplicationLocationService = TerminatedApplicationLocationService()
internal lateinit var mServiceIntent: Intent

@Keep
class TerminatedApplicationLocationService: Service() {
    private var dlatitude: Double = 0.0
    private var dlongitude: Double = 0.0
    private val notification_id = 3895
    private var historyCount = 0

    private lateinit var locationManager: LocationManager
    private lateinit var sensorManager: SensorManager

    private val NOTIFICATION_CHANNEL_ID = "com.naurt"
    private val channelName = "Background Service"

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) createNotificationChanel() else startForeground(
            1,
            createNotification()
        )
    }

    private fun createNotification(): Notification {
        val stopIntent = Intent(
            this,
            StopBackgroundService::class.java
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopIntent.putExtra(EXTRA_NOTIFICATION_ID, 0)
        }
        val stopPendingIntent: PendingIntent =
            PendingIntent.getBroadcast(this, 0, stopIntent, 0)

        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notify = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setContentTitle("Location Service running")
            .setContentText("$dlatitude, $dlongitude | $historyCount ")
            .addAction(
                R.drawable.ic_menu_close_clear_cancel,
                "Stop Location Service",
                stopPendingIntent
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notify.priority = NotificationManager.IMPORTANCE_MIN
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notify.setCategory(Notification.CATEGORY_SERVICE)
        }

        return notify.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChanel() {
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager =
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)

        startForeground(notification_id, createNotification())
    }

    // Called by context.startService
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Naurt.initialiseManagedStandalone(
            BuildConfig.API_KEY,
            applicationContext,
            locationManager,
            sensorManager
        )

        Naurt.on(NaurtEvents.NEW_LOCATION, NaurtEventListener<NaurtNewLocationEvent> {
            dlatitude = it.newPoint.latitude
            dlongitude = it.newPoint.longitude
            historyCount = Naurt.getLocationHistory().size
            val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.notify(notification_id, createNotification())
        })

        Naurt.start()

        return START_STICKY
    }

    // Called by context.stopService
    override fun onDestroy() {
        super.onDestroy()
        Naurt.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

@Keep
class StopBackgroundService : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.stopService(mServiceIntent)
    }
}
