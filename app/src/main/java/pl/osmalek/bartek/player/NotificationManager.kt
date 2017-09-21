package pl.osmalek.bartek.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat

object NotificationManager {
    fun updateNotification(musicService: MusicService, isPlaying: Boolean, song: Song?) {
        val notificationManager: NotificationManager = musicService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)
        val intent = Intent(musicService.applicationContext, MusicService::class.java)
        val (action, actionText) =
                if (isPlaying) {
                    STOP to "Stop"
                } else {
                    PLAY to "Start"
                }
        intent.putExtra(ACTION_KEY, action)

        val notification = NotificationCompat.Builder(musicService, CHANNEL_ID)
                .apply {
                    setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                    setContentTitle(song?.title)
                    setContentText(song?.artist)
                    setOnlyAlertOnce(true)
                    setContentIntent(PendingIntent.getActivity(musicService.applicationContext, REQUEST_CODE, Intent(musicService.applicationContext, MainActivity::class.java), 0))
                    addAction(R.drawable.ic_stop_black_24dp, actionText,
                            PendingIntent.getService(musicService.applicationContext, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                }.build()
        if (isPlaying) {
            musicService.startForeground(111, notification)
        } else {
            musicService.stopForeground(false)
        }
        notificationManager.notify(111, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT)

            // Configure the notification channel.
            notificationChannel.apply {
                description = "Channel description"
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}