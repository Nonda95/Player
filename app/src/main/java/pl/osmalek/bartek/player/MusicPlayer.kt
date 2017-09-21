package pl.osmalek.bartek.player

interface MusicPlayer {
    fun stop()
    fun play()
    fun setSongChangeListener(listener: SongChangeListener)
}