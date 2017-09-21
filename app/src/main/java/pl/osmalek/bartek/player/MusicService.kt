package pl.osmalek.bartek.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import android.support.v4.app.NotificationCompat
import java.io.IOException

const val CLOSE = 2
const val PLAY = 1
const val STOP = 0
const val UNDEFINED = -1
const val CHANNEL_ID = "Music"

class MusicService : Service(), MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MusicPlayer {
    private val binder = MusicBinder()
    private val deviceSongListRetriever = DeviceSongListRetriever()

    private var songs: MutableList<Song> = mutableListOf()

    private var listener: SongChangeListener? = null

    private var currentlyPlayedSong: Song? = null

    val player by lazy {
        MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener(this@MusicService)
            setOnErrorListener(this@MusicService)
            setOnCompletionListener(this@MusicService)
        }
    }

    override fun onCreate() {
        super.onCreate()
        songs = deviceSongListRetriever.execute(contentResolver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getIntExtra("action", UNDEFINED) ?: UNDEFINED
        when (action) {
            PLAY -> play()
            STOP -> stop()
            CLOSE -> stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        player.reset()
        player.release()
        super.onDestroy()
    }

    fun prepareMediaPlayer() {
        player.reset()
        currentlyPlayedSong = songs.getOrNull(1)?.apply {
            val currentSongId = id
            val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSongId)
            try {
                player.setDataSource(applicationContext, trackUri)
                notifyListener()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            player.prepareAsync()
        }
    }

    private fun notifyListener() {
        currentlyPlayedSong?.let { listener?.onSongChanged(it) }
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) = Unit

    override fun onError(mediaPlayer: MediaPlayer, what: Int, extra: Int) = false

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        updateNotification(true)
        player.start()
    }

    private fun updateNotification(isPlaying: Boolean) {
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)
        val intent = Intent(applicationContext, MusicService::class.java)
        val actionText: String
        val action: Int
        if (isPlaying) {
            action = STOP
            actionText = "Stop"
        } else {
            action = PLAY
            actionText = "Start"
        }
        intent.putExtra("action", action)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .apply {
                    setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                    setContentTitle(currentlyPlayedSong?.title)
                    setContentText(currentlyPlayedSong?.artist)
                    setOnlyAlertOnce(true)
                    setContentIntent(PendingIntent.getActivity(applicationContext, 11, Intent(applicationContext, MainActivity::class.java), 0))
                    addAction(R.drawable.ic_stop_black_24dp, actionText,
                            PendingIntent.getService(applicationContext, 11, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                }.build()
        if (isPlaying) {
            startForeground(111, notification)
        } else {
            stopForeground(false)
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

    override fun setSongChangeListener(listener: SongChangeListener) {
        this.listener = listener
        notifyListener()
    }

    override fun play() {
        prepareMediaPlayer()
    }

    override fun stop() {
        updateNotification(false)
        player.reset()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }


    inner class MusicBinder : Binder() {
        fun getMusicPlayer(): MusicPlayer = this@MusicService
    }
}

