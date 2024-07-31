package com.example.geminichat.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.geminichat.BuildConfig
import com.example.geminichat.ui.data.AvailableModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody


private const val TAG = "ModelUtil"

object ModelUtil {
    private const val OFFLINE_MODEL_PATH = "/data/local/tmp/llm/gemma-2b-it-cpu-int4.bin"

    private lateinit var llmInference: LlmInference

    val geminiModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.geminiApiKey
        )
    }
    private const val deepSeekApiKey = BuildConfig.deepSeekApiKey

    suspend fun initGemmaModel(
        updateSentence: () -> Unit,
        changeGenerateState: () -> Unit,
        setGeneratedText: (String) -> Unit,
        context: Context
    ) {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(OFFLINE_MODEL_PATH)
            .setMaxTokens(1000)
            .setTopK(40)
            .setTemperature(0.8f)
            .setRandomSeed(101)
            .setResultListener { partialResult, done ->
                if (done) {
                    updateSentence()
                    changeGenerateState()
                } else {
                    setGeneratedText(partialResult)
                }
            }
            .build()
        llmInference = LlmInference.createFromOptions(context, options)
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Switch to offline model", Toast.LENGTH_SHORT)
                .show()
        }
    }

    suspend fun testOnlineModel(
        model: AvailableModel,
        context: Context
    ) {
        when (model) {
            AvailableModel.GEMINI -> {
                val response =
                    geminiModel.generateContent("hello").text ?: "Something go wrong."
                Log.d(TAG, "init: $response")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Switch to online model", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            AvailableModel.DEEP_SEEK -> {
                val client = OkHttpClient().newBuilder()
                    .build()
                val mediaType = "application/json".toMediaTypeOrNull()
                val body =
                    "{\n  \"messages\": [\n    {\n      \"content\": \"You are a helpful assistant\",\n      \"role\": \"system\"\n    },\n    {\n      \"content\": \"Hi\",\n      \"role\": \"user\"\n    }\n  ],\n  \"model\": \"deepseek-coder\",\n  \"frequency_penalty\": 0,\n  \"max_tokens\": 2048,\n  \"presence_penalty\": 0,\n  \"response_format\": {\n    \"type\": \"text\"\n  },\n  \"stop\": null,\n  \"stream\": false,\n  \"stream_options\": null,\n  \"temperature\": 1,\n  \"top_p\": 1,\n  \"tools\": null,\n  \"tool_choice\": \"none\",\n  \"logprobs\": false,\n  \"top_logprobs\": null\n}"
                        .toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://api.deepseek.com/chat/completions")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer <TOKEN>")
                    .build()
                val response = client.newCall(request).execute()
            }

            else -> {}
        }

    }

    fun generateResponseAsync(message: String) {
        llmInference.generateResponseAsync(message)
    }

    fun closeGemmaModel() {
        llmInference.close()
    }


}