package com.example.geminichat.ui.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.serialization.Serializable

@Entity
data class Conversation(
    @PrimaryKey(autoGenerate = true) val id: Int, val sentences: List<Sentence>
)

@Serializable
data class Sentence(
    val role: String,
    val content: String
)

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation")
    fun getConversation(): List<Conversation>

    @Insert
    fun insertConversation(conversation: Conversation)

    @Delete
    fun deleteConversation(conversation: Conversation)

    @Update
    fun updateConversation(conversation: Conversation)
}

