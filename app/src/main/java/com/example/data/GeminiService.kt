package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiChallenge(
    val id: String,
    val domain: String,       // DevOps, Unity Game Dev, Node.js, ASP.NET
    val topic: String,        // e.g. "Docker Volumes"
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", "D"
    val explanation: String,
    val lesson: String = "" // In-depth education module text teaching the concepts
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: okhttp3.RequestBody
    ): ResponseBody
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun generateChallenge(domain: String, difficulty: String = "Profound"): GeminiChallenge? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        val prompt = "" +
            "Generate a highly professional, interactive learning class module for a software developer. " +
            "The subject must be: $domain. " +
            "The difficulty must be: $difficulty. \n" +
            "First, write a detailed 'lesson' that teaches the theory behind this specific topic. The lesson should be a structured text formatted strictly in Markdown, containing a '1. Concept Overview', '2. Architectural Mechanics & Examples' (with example configurations, code snippets, or definitions), and '3. Summary Cheat Sheet'. \n" +
            "Second, provide a dynamic quiz question to test the student immediately on that lesson. Provide four options (A, B, C, D) and specify the correct answer as a single character (A, B, C, or D). " +
            "Explain in depth why the answer is correct and why other options are incorrect.\n" +
            "You MUST output raw JSON matching this structure exactly, with absolutely no markdown framing, no ```json formatting, just pure JSON text block:\n" +
            "{\n" +
            "  \"id\": \"${domain.lowercase().replace(" ", "_")}_${System.currentTimeMillis()}\",\n" +
            "  \"domain\": \"$domain\",\n" +
            "  \"topic\": \"Specific subtopic generated on-the-fly\",\n" +
            "  \"lesson\": \"Thorough academic lesson text structured in markdown with headings (### 1. Concept Overview) to teach the concepts detail by detail to the developer BEFORE they take the quiz.\",\n" +
            "  \"question\": \"A question testing the concepts explicitly taught in the lesson above\",\n" +
            "  \"optionA\": \"First Option\",\n" +
            "  \"optionB\": \"Second Option\",\n" +
            "  \"optionC\": \"Third Option\",\n" +
            "  \"optionD\": \"Fourth Option\",\n" +
            "  \"correctAnswer\": \"A\",\n" +
            "  \"explanation\": \"Deep educational breakdown of the correct answer directly addressing the facts taught in the lesson.\"\n" +
            "}"

        // Build the contents JSON as a raw manual request to avoid serializable plugin mismatch
        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            // Configure response format to application/json
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.8)
            })
        }

        val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())

        try {
            val responseBody = service.generateContent(apiKey, requestBody)
            val jsonStr = responseBody.string()
            val responseJson = JSONObject(jsonStr)
            
            // Extract text from the candidate
            val candidates = responseJson.getJSONArray("candidates")
            val candidate = candidates.getJSONObject(0)
            val content = candidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            var text = parts.getJSONObject(0).getString("text").trim()
            
            // Just in case LLM added markdown formatting
            if (text.startsWith("```json")) {
                text = text.substringAfter("```json").substringBeforeLast("```").trim()
            } else if (text.startsWith("```")) {
                text = text.substringAfter("```").substringBeforeLast("```").trim()
            }

            val quizJson = JSONObject(text)
            GeminiChallenge(
                id = quizJson.getString("id"),
                domain = quizJson.getString("domain"),
                topic = quizJson.getString("topic"),
                question = quizJson.getString("question"),
                optionA = quizJson.getString("optionA"),
                optionB = quizJson.getString("optionB"),
                optionC = quizJson.getString("optionC"),
                optionD = quizJson.getString("optionD"),
                correctAnswer = quizJson.getString("correctAnswer").uppercase(),
                explanation = quizJson.getString("explanation"),
                lesson = if (quizJson.has("lesson")) quizJson.getString("lesson") else "Welcome to this specialized dive into ${quizJson.optString("topic", "Software Engineering")}. Learn key design principles, standard components, and lifecycle details."
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
