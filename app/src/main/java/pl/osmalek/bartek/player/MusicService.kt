package pl.osmalek.bartek.player

import android.app.Service
import android.content.ContentUris
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.provider.MediaStore
import java.io.IOException

const val PLAY = 1
const val STOP = 0
const val UNDEFINED = -1
const val CHANNEL_ID = "Music"
const val ACTION_KEY = "action"
const val REQUEST_CODE = 11

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
        val action = intent?.getIntExtra(ACTION_KEY, UNDEFINED) ?: UNDEFINED
        when (action) {
            PLAY -> play()
            STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        player.reset()
        player.release()
        super.onDestroy()
    }

    private fun prepareMediaPlayer() {
        player.reset()
        currentlyPlayedSong = songs.getOrNull(1)?.apply {
            val currentSongId = id
            val trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSongId)
            try {
                player.setDataSource(applicationContext, trackUri)
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
        notifyListener()
        NotificationManager.updateNotification(this, true, currentlyPlayedSong)
        player.start()
    }

    override fun setSongChangeListener(listener: SongChangeListener) {
        this.listener = listener
        notifyListener()
    }

    override fun play() {
        prepareMediaPlayer()
    }

    override fun stop() {
        NotificationManager.updateNotification(this, false, currentlyPlayedSong)
        player.reset()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }


    inner class MusicBinder : Binder() {
        fun getMusicPlayer(): MusicPlayer = this@MusicService
    }

}


