package com.example.geminichat.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.geminichat.BuildConfig
import com.example.geminichat.ui.data.Sentence
import com.google.ai.client.generativeai.GenerativeModel
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ModelUtil"

enum class AvailableModel(val isOnline: Boolean, var isAvailable: Boolean = false) {
    Gemini(true),
    DeepSeek(true),
    Gemma(false)
}

object ModelUtil {
    private const val GEMMA_MODEL_PATH = "/data/local/tmp/llm/gemma-2b-it-cpu-int4.bin"

    private lateinit var llmInference: LlmInference

    val geminiModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.geminiApiKey
        )
    }
    private val geminiChat by lazy {
        geminiModel.startChat()
    }

    private val deepSeekChat by lazy {
        DeepSeekUtil.createChat()
    }

    suspend fun initGemmaModel(
        updateSentence: () -> Unit,
        changeGenerateState: () -> Unit,
        setGeneratedText: (String) -> Unit,
        context: Context
    ) {
        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(GEMMA_MODEL_PATH)
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
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e(TAG, "initializeGemini: ", e)
                Toast.makeText(context, "Error in loading offline model", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    suspend fun testOnlineModel() {
        for (model in AvailableModel.entries) {
            when (model) {
                AvailableModel.DeepSeek -> {
                    try {
                        DeepSeekUtil.sendMessage(
                            Sentence("user", "hello")
                        ) {
                            model.isAvailable = true
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "initializeDeepSeek: ", e)
                    }
                }

                AvailableModel.Gemini -> {
                    try {
                        geminiModel.generateContent("hello")
                        model.isAvailable = true
                    } catch (e: Exception) {
                        Log.e(TAG, "initializeGemini: ", e)
                    }
                }

                else -> {
                }
            }
        }
    }

    suspend fun onlineModelGenerateMessage(
        model: AvailableModel,
        message: String,
        onResponseCallback: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            when (model) {
                AvailableModel.DeepSeek -> {
                    try {
                        deepSeekChat.sendMessageWithHistory(message, true) {
                            onResponseCallback(it)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "initializeDeepSeek: ", e)
                    }
                }

                AvailableModel.Gemini -> {
                    try {
                        geminiChat.sendMessageStream(message).collect { chunk ->
                            chunk.text?.let {
                                onResponseCallback(it)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "initializeGemini: ", e)
                    }
                }

                else -> {
                }
            }
        }
    }

    fun generateResponseAsync(message: String) {
        llmInference.generateResponseAsync(message)
    }

    fun closeGemmaModel() {
        llmInference.close()
    }


}