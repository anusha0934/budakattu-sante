package com.mindmatrix.budakattusante.di

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.mindmatrix.budakattusante.data.local.UserPreferences
import com.mindmatrix.budakattusante.data.repository.AiRepository
import com.mindmatrix.budakattusante.ui.VoiceToTextParser
import com.mindmatrix.budakattusante.util.AudioPlayer
import com.mindmatrix.budakattusante.util.TextToSpeechHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    @Provides
    @Singleton
    fun provideAiRepository(): AiRepository {
        // Requirement 7: Production-grade Gemini integration
        return AiRepository(apiKey = "AIzaSyAYjdbY-Xp8YPrGU2g8iYNbX2W-E2huzQM")
    }

    @Provides
    @Singleton
    fun provideVoiceToTextParser(app: Application): VoiceToTextParser {
        // Requirement 8: Voice-based assistance for semi-literate users
        return VoiceToTextParser(app)
    }

    @Provides
    @Singleton
    fun provideTextToSpeechHelper(@ApplicationContext context: Context): TextToSpeechHelper {
        return TextToSpeechHelper(context)
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(@ApplicationContext context: Context): AudioPlayer {
        return AudioPlayer(context)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
}
