package pl.osmalek.bartek.player

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
class Song(
        val id: Long,
        val title: String,
        val artist: String,
        val albumName: String,
        val albumCoverUri: Uri,
        val durationMillis: Int
) : Parcelable