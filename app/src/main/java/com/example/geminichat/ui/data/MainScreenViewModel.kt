package com.example.geminichat.ui.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.geminichat.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

private const val TAG = "MainScreenViewModel"

class MainScreenViewModel : ViewModel() {

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.apiKey
        )
    }

    private val chat by lazy {
        generativeModel.startChat()
    }

    private lateinit var llmInference: LlmInference

    private val mSentencesFlowList: MutableStateFlow<MutableList<Sentence>> =
        MutableStateFlow(mutableListOf())
    val sentencesListFlow = mSentencesFlowList.asStateFlow()
    val isGenerating: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val textToSelect: MutableStateFlow<String> = MutableStateFlow("")
    val generatedText: MutableStateFlow<String> = MutableStateFlow("")
    private val isOnline: MutableStateFlow<Boolean> = MutableStateFlow(true)

    suspend fun initializeGemini(context: Context) {
        withContext(Dispatchers.IO) {
//            try {
//                val response =
//                    generativeModel.generateContent("hello").text ?: "Something go wrong."
//                Log.d(TAG, "init: $response")
//            } catch (e: Exception) {
            try {
                isOnline.update { false }
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath("/data/local/tmp/llm/gemma-2b-it-cpu-int4.bin")
                    .setMaxTokens(1000)
                    .setTopK(40)
                    .setTemperature(0.8f)
                    .setRandomSeed(101)
                    .setResultListener { partialResult, done ->
                        if (done) {
                            updateSentence(Sentence("GEMINI", generatedText.value))
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
                    Toast.makeText(context, "Error in loading offline model", Toast.LENGTH_LONG)
                        .show()
                }
                /*}*/
            }
        }
    }

    suspend fun sendMessage(message: String, context: Context) {
        withContext(Dispatchers.IO) {
            if (isOnline.value) {
                try {
                    addSentence(Sentence("GEMINI", ""))
                    chat.sendMessageStream(message).collect { chunk ->
                        chunk.text?.let {
                            setGeneratedText(it)
                            Log.d(TAG, "sendMessage: ${generatedText.value}")
                        }
                    }
                    Log.d(TAG, "sendMessage: ${generatedText.value}")
                    updateSentence(Sentence("GEMINI", generatedText.value))
                } catch (e: Exception) {
                    updateSentence(Sentence("GEMINI", generatedText.value))
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                changeGenerateState()
            } else {
                clearGeneratedText()
                addSentence(Sentence("GEMINI", ""))
                llmInference.generateResponseAsync(message)
            }
        }
    }

    fun addSentence(sentence: Sentence) {
        mSentencesFlowList.update { it.apply { add(sentence) } }
    }

    fun updateSentence(sentence: Sentence) {
        mSentencesFlowList.update { it.apply { set(size - 1, sentence) } }
    }

    fun setGeneratedText(text: String) {
        generatedText.update { it + text }
    }

    fun clearGeneratedText() {
        generatedText.update { "" }
    }

    fun changeGenerateState() {
        isGenerating.update { !it }
    }

    fun setTextToSelect(text: String) {
        textToSelect.update { text }
    }
}
