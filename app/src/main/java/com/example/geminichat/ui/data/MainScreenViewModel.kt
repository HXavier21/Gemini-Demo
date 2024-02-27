package com.example.geminichat.ui.data

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.geminichat.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.coroutines.coroutineContext

private const val TAG = "MainScreenViewModel"

class MainScreenViewModel : ViewModel() {
    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.apiKey
        )
    }

    private val chat by lazy {
        generativeModel.startChat()
    }

    val mutableConversationsFlowList: MutableList<MutableStateFlow<Conversation>> = mutableListOf()
    val updateIndex: MutableStateFlow<Int> = MutableStateFlow(0)
    val isGenerating: MutableStateFlow<Boolean> = MutableStateFlow(false)

    suspend fun sendMessage(message: String, context: Context) {
        try {
            val response = chat.sendMessage(message).text ?: "Something go wrong."
            addConversation(Conversation("GEMINI", response))
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
        changeGenerateState()
    }

    fun addConversation(conversation: Conversation) {
        mutableConversationsFlowList.add(MutableStateFlow(conversation))
        updateScreen()
    }

    fun updateScreen() {
        updateIndex.update { it + 1 }
    }

    fun changeGenerateState() {
        isGenerating.update { !it }
    }
}
