package com.example.safetrack.ui.theme.locationTracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.safetrack.R
import java.util.Timer
import java.util.TimerTask

class ForegroundService : Service(){


    private lateinit var locationHelper: LocationHelper

    private var smsTimer: Timer? = null
    private var elapsedMinutes = 0

    private val emergencyNumbers = listOf(
        "8340611053",
//        "8102489714"
    )

    companion object{

        const val CHANNEL_ID = "SOS_channel"
        const val NOTIF_ID = 1234

        fun startService(context: Context){
            val intent = Intent(context, ForegroundService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            }
            else{
                context.startService(intent)
            }
        }

        fun stopService(context: Context){
            val intent = Intent(context, ForegroundService::class.java)

            context.stopService(intent)
        }
    }


    override fun onCreate() {
        super.onCreate()

        locationHelper = LocationHelper(this)

        createNotificationChannel()

        startForeground(NOTIF_ID, createNotification(null, null))

        locationHelper.startLocUpdates {
            startForeground(NOTIF_ID, createNotification(it.latitude, it.longitude))

            val lat = it.latitude
            val lng = it.longitude
            val googleLink = "https://maps.google.com/?q=$lat,$lng"

            startSmsTask(googleLink)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        smsTimer?.cancel()
        locationHelper.stopLocationUpdate()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SOS",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = ContextCompat.getSystemService(this, NotificationManager::class.java)
            manager!!.createNotificationChannel(channel)
        }
    }

    private fun createNotification(lat: Double?, lon: Double?): Notification{

        val text = if (lat != null && lon != null) "$lat , $lon" else "Fetching location..."

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText(text)
            .setContentTitle("SafeTrack Active")
            .setSmallIcon(R.drawable.ic_launcher_background)

            .setOngoing(true)  // // Cannot clear on swap
            .setAutoCancel(false)  // Cannot clear on tap
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)  // Show this notification immediately
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show full notification content even when the phone is locked
            .build()

//         Prevent swipe removal
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR

        return notification
    }

    private fun startSmsTask(locationLink: String) {

        if (smsTimer != null) return  // prevents duplicate timers

        smsTimer = Timer()
        smsTimer!!.schedule(object : TimerTask() {
            override fun run() {

                elapsedMinutes++

                val message = "SOS! My live location:\n$locationLink"

                emergencyNumbers.forEach { number ->
                    sendSms(number, message)
                }

                if (elapsedMinutes >= 15) {
                    smsTimer?.cancel()
                    stopSelf()
                }
            }
        }, 0, 15000L)
    }

    private fun sendSms(number: String, message: String){
        val smsManager = getSystemService(SmsManager::class.java)
        smsManager.sendTextMessage(number, null, message, null, null)
    }

}