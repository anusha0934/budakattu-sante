package com.mindmatrix.budakattusante.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun play(uriString: String) {
        stop()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(context, Uri.parse(uriString))
            prepareAsync()
            setOnPreparedListener { start() }
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
