package com.example.geminichat.ui.data

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.geminichat.util.AvailableModel
import com.example.geminichat.util.ModelUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

private const val TAG = "MainScreenViewModel"

class MainScreenViewModel : ViewModel() {

    private val chat by lazy {
        ModelUtil.geminiModel.startChat()
    }


    private val mSentencesFlowList: MutableStateFlow<MutableList<Sentence>> =
        MutableStateFlow(mutableListOf())
    val sentencesListFlow = mSentencesFlowList.asStateFlow()
    val isGenerating: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val textToSelect: MutableStateFlow<String> = MutableStateFlow("")
    val generatedText: MutableStateFlow<String> = MutableStateFlow("")
    private val isOnline: MutableStateFlow<Boolean> = MutableStateFlow(true)

    suspend fun initializeGemini(context: Context) {
//        withContext(Dispatchers.IO) {
//            ModelUtil.initGemmaModel(
//                updateSentence = { updateSentence(Sentence("GEMINI", generatedText.value)) },
//                changeGenerateState = { changeGenerateState() },
//                setGeneratedText = { setGeneratedText(it) },
//                context = context
//            )
//            try {
//                val response =
//                    ModelUtil.geminiModel.generateContent("hello").text ?: "Something go wrong."
//                Log.d(TAG, "init: $response")
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Switch to online model", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "initializeGemini: ", e)
//            }
//        }
    }

    suspend fun sendMessage(message: String, context: Context) {
        withContext(Dispatchers.IO) {
            if (isOnline.value) {
//                try {
//                    addSentence(Sentence("GEMINI", ""))
//                    chat.sendMessageStream(message).collect { chunk ->
//                        chunk.text?.let {
//                            setGeneratedText(it)
//                            Log.d(TAG, "sendMessage: ${generatedText.value}")
//                        }
//                    }
//                    Log.d(TAG, "sendMessage: ${generatedText.value}")
//                    updateSentence(Sentence("GEMINI", generatedText.value))
//                } catch (e: Exception) {
//                    updateSentence(Sentence("GEMINI", generatedText.value))
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
//                    }
//                }
//                changeGenerateState()
                clearGeneratedText()
                addSentence(Sentence("GEMINI", ""))
                ModelUtil.onlineModelGenerateMessage(
                    AvailableModel.DeepSeek,
                    message
                ) {
                    setGeneratedText(it)
                }

                updateSentence(Sentence("GEMINI", generatedText.value))
                changeGenerateState()
            } else {
                clearGeneratedText()
                addSentence(Sentence("GEMINI", ""))
                ModelUtil.generateResponseAsync(message)
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
