package com.example.geminichat.ui.data

import androidx.lifecycle.ViewModel
import com.example.geminichat.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

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

    suspend fun sendMessage(message: String) {
        val response = chat.sendMessage(message).text ?: "Something go wrong."
        addConversation(Conversation("GEMINI", response))
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
