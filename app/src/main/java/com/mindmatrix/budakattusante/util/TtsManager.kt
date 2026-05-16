package com.mindmatrix.budakattusante.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = Locale("kn", "IN") // Default to Kannada
            }
        }
    }

    fun speak(text: String, language: String = "kn") {
        if (isInitialized) {
            tts?.language = if (language == "kn") Locale("kn", "IN") else Locale.US
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
    }
}
