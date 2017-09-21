package pl.osmalek.bartek.player

import android.content.ContentUris
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.BaseColumns._ID
import android.provider.MediaStore.Audio.AudioColumns.*
import android.provider.MediaStore.Audio.Media
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.MediaColumns.TITLE
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private var songs: MutableList<Song> = mutableListOf()
    val player by lazy {
        MediaPlayer().apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setOnPreparedListener(this@MainActivity)
            setOnErrorListener(this@MainActivity)
            setOnCompletionListener(this@MainActivity)
        }
    }


    fun prepareMediaPlayer() {
        player.reset()
        val currentlyPlayedSong: Song = songs[0]
        val currentSongId = currentlyPlayedSong.id
        val trackUri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, currentSongId)
        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        player.prepareAsync()
    }

    override fun onCompletion(mediaPlayer: MediaPlayer) = Unit
    override fun onError(mediaPlayer: MediaPlayer, what: Int, extra: Int) = false

    override fun onPrepared(mediaPlayer: MediaPlayer) = player.start()

    private fun retrieveDeviceSongList() {
        songs = mutableListOf()
        val musicResolver = contentResolver

        val musicCursor = musicResolver.query(EXTERNAL_CONTENT_URI, null, null, null, null)

        if (musicCursor != null && musicCursor.moveToFirst()) {
            val titleColumn = musicCursor.getColumnIndex(TITLE)
            val idColumn = musicCursor.getColumnIndex(_ID)
            val artistColumn = musicCursor.getColumnIndex(ARTIST)
            val albumCoverColumn = musicCursor.getColumnIndex(ALBUM_ID)
            val albumNameColumn = musicCursor.getColumnIndex(ALBUM)
            val durationColumn = musicCursor.getColumnIndex(DURATION)
            do {
                val thisId = musicCursor.getLong(idColumn)
                val songTitle = musicCursor.getString(titleColumn)
                val songArtist = musicCursor.getString(artistColumn)
                val songAlbum = musicCursor.getString(albumNameColumn)
                val albumCoverId = musicCursor.getLong(albumCoverColumn)
                val albumCoverUriPath = Uri.parse("content://media/external/audio/albumart")
                val albumArtUri = ContentUris.withAppendedId(albumCoverUriPath, albumCoverId)
                val songDuration = musicCursor.getLong(durationColumn)
                songs.add(Song(thisId, songTitle, songArtist, songAlbum, albumArtUri, songDuration.toInt()))
            } while (musicCursor.moveToNext())
        }
        musicCursor?.close()

        Collections.sort<Song>(songs) { lhs, rhs -> lhs.title.compareTo(rhs.title) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        retrieveDeviceSongList()
        ivPlay.setOnClickListener { prepareMediaPlayer() }
        ivStop.setOnClickListener { player.reset() }
    }
}