package com.mindmatrix.budakattusante.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepository @Inject constructor(val apiKey: String) {

    private val config = generationConfig {
        temperature = 0.7f
        topK = 32
        topP = 1f
        maxOutputTokens = 2048
    }

    private val safetySettings = listOf(
        SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
        SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
    )

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = apiKey,
        generationConfig = config,
        safetySettings = safetySettings
    )

    suspend fun askAi(question: String): String = withContext(Dispatchers.IO) {
        val prompt = "You are a Tribal Assistant for Budakattu Sante. Answer this concisely: $question"
        try { 
            model.generateContent(prompt).text ?: "I'm sorry, I couldn't generate a response."
        } catch (e: Exception) { 
            "I'm currently having trouble connecting. Please check your internet or try again later."
        }
    }

    suspend fun generateProductDescription(productName: String, category: String, region: String): String = withContext(Dispatchers.IO) {
        val prompt = "Generate a rich, cultural, and appetizing product description for a tribal forest product named '$productName' from the '$category' category, sourced from '$region'. Focus on its organic nature and tribal heritage."
        try { 
            model.generateContent(prompt).text ?: "" 
        } catch (e: Exception) { 
            "" 
        }
    }

    suspend fun predictSeasonalDemand(productName: String, month: String): String = withContext(Dispatchers.IO) {
        val prompt = "Predict the market demand for '$productName' in '$month' for urban India. Is it a peak harvest season? Provide a short analysis."
        try { 
            model.generateContent(prompt).text ?: "Moderate demand expected." 
        } catch (e: Exception) { 
            "" 
        }
    }

    suspend fun getSmartRecommendations(interests: List<String>, availableProducts: List<String>): String = withContext(Dispatchers.IO) {
        val prompt = "Based on user interests $interests, which of these tribal products $availableProducts would you recommend? Give a 1-sentence reason."
        try {
            model.generateContent(prompt).text ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun translateToKannada(text: String): String = withContext(Dispatchers.IO) {
        val prompt = "Translate the following to simple spoken Kannada for a forest tribal community: $text"
        try { 
            model.generateContent(prompt).text ?: text 
        } catch (e: Exception) { 
            text 
        }
    }

    suspend fun parseNavigationIntent(text: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            Map the user speech to one of the following app routes: 'home', 'cart', 'orders', 'profile', 'categories', 'notifications'.
            Speech: "$text"
            Return only the route name in lowercase. If it's a product search, return 'search'.
        """.trimIndent()
        try {
            model.generateContent(prompt).text?.trim()?.lowercase() ?: "search"
        } catch (e: Exception) {
            "search"
        }
    }
}
