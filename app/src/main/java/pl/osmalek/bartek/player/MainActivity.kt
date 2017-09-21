package pl.osmalek.bartek.player

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SongChangeListener {
    private var musicPlayer: MusicPlayer? = null

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) = Unit

        override fun onServiceConnected(name: ComponentName?, binder: IBinder) {
            musicPlayer = (binder as MusicService.MusicBinder).getMusicPlayer().apply {
                setSongChangeListener(this@MainActivity)
            }
            enableButtons()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prepareListeners()
        val intent = Intent(this, MusicService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, 0)
    }

    private fun enableButtons() {
        ivPlay.isEnabled = true
        ivStop.isEnabled = true
    }

    private fun prepareListeners() {
        ivPlay.setOnClickListener { musicPlayer?.play() }
        ivStop.setOnClickListener { musicPlayer?.stop() }
    }

    override fun onSongChanged(song: Song) {
        tvTitle.text = song.title
        tvArtist.text = song.artist
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }
}

