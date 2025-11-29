package com.example.safetrack.ui.theme.locationTracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.safetrack.R

class ForegroundService : Service(){


    private lateinit var locationHelper: LocationHelper

    companion object{

        const val CHANNEL_ID = "SOS_channel"
        const val NOTIF_ID = 1234

        fun startService(context: Context){
            val intent = Intent(context, ForegroundService::class.java)

            context.startForegroundService(intent)
        }

        fun stopService(context: Context){
            val intent = Intent(context, ForegroundService::class.java)

            context.stopService(intent)
        }
    }


    override fun onCreate() {
        super.onCreate()

        locationHelper = LocationHelper.getInstance(this)

        createNotificationChannel()

        startForeground(NOTIF_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationHelper.startLocUpdates { location ->
            Log.d(
                "LocationService",
                "onStartCommand:  Lat: ${location.latitude} and Long: ${location.longitude}"
            )
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        locationHelper.stopLocationUpdate()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "SOS",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = ContextCompat.getSystemService(this, NotificationManager::class.java)
        manager!!.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification{
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText("Sending Location")
            .setContentTitle("SafeTrack Running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
        return notification
    }

}