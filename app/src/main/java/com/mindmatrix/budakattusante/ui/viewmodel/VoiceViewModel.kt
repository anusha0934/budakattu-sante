package com.mindmatrix.budakattusante.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindmatrix.budakattusante.data.repository.AiRepository
import com.mindmatrix.budakattusante.ui.VoiceToTextParser
import com.mindmatrix.budakattusante.util.AudioPlayer
import com.mindmatrix.budakattusante.util.TextToSpeechHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val voiceToTextParser: VoiceToTextParser,
    private val ttsHelper: TextToSpeechHelper,
    private val audioPlayer: AudioPlayer,
    private val aiRepository: AiRepository
) : ViewModel() {

    val voiceState = voiceToTextParser.state

    private val _navigationIntent = MutableSharedFlow<String>()
    val navigationIntent = _navigationIntent.asSharedFlow()

    init {
        // Requirement 10: AI Voice-to-Navigation support
        voiceState.map { it.spokenText }
            .distinctUntilChanged()
            .onEach { text ->
                if (text.isNotBlank()) {
                    processVoiceIntent(text)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun processVoiceIntent(text: String) {
        viewModelScope.launch {
            val route = aiRepository.parseNavigationIntent(text)
            if (route != "search") {
                _navigationIntent.emit(route)
            }
        }
    }

    fun startListening(languageCode: String = "en-US") {
        voiceToTextParser.startListening(languageCode)
    }

    fun stopListening() {
        voiceToTextParser.stopListening()
    }

    fun speak(text: String) {
        ttsHelper.speak(text)
    }

    fun stopSpeaking() {
        ttsHelper.stop()
    }

    fun playAudio(url: String) {
        audioPlayer.play(url)
    }

    fun stopAudio() {
        audioPlayer.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
        audioPlayer.stop()
    }
}
