package com.example.geminichat.util

import android.util.Log
import com.example.geminichat.BuildConfig
import com.example.geminichat.ui.data.Sentence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit


private const val TAG = "DeepSeekChatUtil"

object DeepSeekUtil {
    @Serializable
    data class DeepSeekRequestBody(
        val messages: List<Sentence> = listOf(),
        val model: String = "deepseek-coder",
        @SerialName("frequency_penalty") val frequencyPenalty: Double = 0.0,
        @SerialName("max_tokens") val maxTokens: Int = 2048,
        @SerialName("presence_penalty") val presencePenalty: Double = 0.0,
        @SerialName("response_format") val responseFormat: ResponseFormat = ResponseFormat(),
        val stop: String? = null,
        val stream: Boolean = false,
        @SerialName("stream_options") val streamOptions: String? = null,
        val temperature: Double = 1.0,
        @SerialName("top_p") val topP: Double = 1.0,
        val tools: String? = null,
        @SerialName("tool_choice") val toolChoice: String = "none",
        val logprobs: Boolean = false,
        @SerialName("top_logprobs") val topLogprobs: String? = null
    ) {
        @Serializable
        data class ResponseFormat(
            val type: String = "text"
        )
    }

    class Chat {
        private val history: MutableList<Sentence> = mutableListOf()

        suspend fun sendMessageWithHistory(
            message: String,
            stream: Boolean = false,
            onResponseCallback: (String) -> Unit
        ) {
            history.add(Sentence("user", message))
            sendMessage(history, stream) {
                history.add(Sentence("assistant", it))
                onResponseCallback(it)
            }
        }
    }

    private const val DEEP_SEEK_API_KEY = BuildConfig.deepSeekApiKey

    suspend fun sendMessage(
        sentences: List<Sentence>,
        stream: Boolean = false,
        onResponseCallback: (String) -> Unit,
    ) {
        withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient().newBuilder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()
                val mediaType = "application/json".toMediaTypeOrNull()
                val body =
                    encodeDeepSeekRequestBody(
                        DeepSeekRequestBody(
                            messages = sentences,
                            stream = stream
                        )
                    ).toRequestBody(
                        mediaType
                    )
                val request = Request.Builder().url("https://api.deepseek.com/chat/completions")
                    .method("POST", body).addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer $DEEP_SEEK_API_KEY").build()
                val response = client.newCall(request).execute()
                when (stream) {
                    true -> {
                        val responseBody = response.body
                        if (null != responseBody) {
                            try {
                                responseBody.byteStream().use { inputStream ->
                                    val buffer = ByteArray(1024)
                                    var len = 0
                                    while ((inputStream.read(buffer).also { len = it }) != -1) {
                                        val str =
                                            String(buffer, 0, len, Charset.defaultCharset())
                                        str.replace(" ".toRegex(), "") // 去除所有空格
                                        Log.i(TAG, "streamChat inputStream.read $str")
                                        str.split("data:").last().trim().let {
                                            if (it != "[DONE]") {
                                                onResponseCallback(
                                                    decodeDeepSeekResponse(
                                                        it, true
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            } catch (ioException: IOException) {
                                ioException.printStackTrace()
                            }
                        }
                    }

                    false -> {
                        onResponseCallback(decodeDeepSeekResponse(response.body?.string() ?: ""))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "sendMessage: ", e)
                onResponseCallback("Something go wrong.")
            }
        }
    }

    suspend fun sendMessage(
        sentence: Sentence,
        stream: Boolean = false,
        onResponseCallback: (String) -> Unit,
    ) {
        sendMessage(listOf(sentence), stream, onResponseCallback)
    }

    private fun encodeDeepSeekRequestBody(
        deepSeekRequestBody: DeepSeekRequestBody
    ): String {
        val json = Json { encodeDefaults = true }
        return json.encodeToString(deepSeekRequestBody)
    }

    private fun encodeDeepSeekMessage(sentences: List<Sentence>): String {
        return Json.encodeToString(sentences)
    }

    private fun encodeDeepSeekMessage(sentences: Sentence): String {
        return encodeDeepSeekMessage(listOf(sentences))
    }

    private fun decodeDeepSeekResponse(response: String, stream: Boolean = false): String {
        Log.d(TAG, "decodeDeepSeekResponse: $response")
        try {
            val jsonObject = JSONObject(response)
            val choicesArray = jsonObject.getJSONArray("choices")
            val firstChoice = choicesArray.getJSONObject(0)
            val messageObject = when (stream) {
                true -> firstChoice.getJSONObject("delta")
                false -> firstChoice.getJSONObject("message")
            }
            return messageObject.getString("content")
        } catch (e: Exception) {
            Log.e(TAG, "decodeDeepSeekResponse: ", e)
            return ""
        }
    }

    fun createChat(): Chat {
        return Chat()
    }
}