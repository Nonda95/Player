package pl.osmalek.bartek.player

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns
import android.provider.MediaStore
import java.util.*

class DeviceSongListRetriever {
    fun execute(musicResolver: ContentResolver): MutableList<Song> {
        val songs: MutableList<Song> = mutableListOf()
        val musicCursor = musicResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null)

        if (musicCursor != null && musicCursor.moveToFirst()) {
            val titleColumn = musicCursor.getColumnIndex(MediaStore.MediaColumns.TITLE)
            val idColumn = musicCursor.getColumnIndex(BaseColumns._ID)
            val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)
            val albumCoverColumn = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID)
            val albumNameColumn = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)
            val durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION)
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
        return songs
    }
}