package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import android.content.Context
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val folder: String = "General",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastMessage: String = "",
    val model: String = "gemini-3.5-flash",
    val isPinned: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String, // "user" or "assistant" or "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String? = null,
    val isThinking: Boolean = false,
    val thinkingProcess: String? = null
)

@Entity(tableName = "library_items")
data class LibraryItemEntity(
    @PrimaryKey val id: String,
    val type: String, // "writing", "prompt", "prd", "code", "productivity", "chat", "tool_result"
    val title: String,
    val content: String,
    val metaJson: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Entity(tableName = "daily_memories")
data class DailyMemoryEntity(
    @PrimaryKey val id: String,
    val category: String, // "Personal instructions", "Writing preferences", "Common prompts", "Frequently used templates", "Custom AI instructions"
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_tools")
data class CustomToolEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val instructions: String,
    val inputType: String = "Text",
    val outputFormat: String = "Markdown",
    val preferredModel: String = "gemini-2.5-flash",
    val isSavedToHome: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getMessageCount(): Int
}

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library_items ORDER BY isFavorite DESC, createdAt DESC")
    fun getAllItems(): Flow<List<LibraryItemEntity>>

    @Query("SELECT * FROM library_items WHERE type = :type ORDER BY isFavorite DESC, createdAt DESC")
    fun getItemsByType(type: String): Flow<List<LibraryItemEntity>>

    @Query("SELECT * FROM library_items WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteItems(): Flow<List<LibraryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LibraryItemEntity)

    @Query("UPDATE library_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM library_items WHERE id = :id")
    suspend fun deleteItem(id: String)

    @Query("SELECT * FROM library_items WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchItems(query: String): Flow<List<LibraryItemEntity>>
}

@Dao
interface DailyMemoryDao {
    @Query("SELECT * FROM daily_memories ORDER BY isPinned DESC, createdAt DESC")
    fun getAllMemories(): Flow<List<DailyMemoryEntity>>

    @Query("SELECT * FROM daily_memories WHERE category = :category ORDER BY isPinned DESC, createdAt DESC")
    fun getMemoriesByCategory(category: String): Flow<List<DailyMemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: DailyMemoryEntity)

    @Update
    suspend fun updateMemory(memory: DailyMemoryEntity)

    @Query("DELETE FROM daily_memories WHERE id = :id")
    suspend fun deleteMemory(id: String)
}

@Dao
interface CustomToolDao {
    @Query("SELECT * FROM custom_tools ORDER BY createdAt DESC")
    fun getAllCustomTools(): Flow<List<CustomToolEntity>>

    @Query("SELECT * FROM custom_tools WHERE isSavedToHome = 1 ORDER BY createdAt DESC")
    fun getHomeCustomTools(): Flow<List<CustomToolEntity>>

    @Query("SELECT * FROM custom_tools WHERE id = :id LIMIT 1")
    suspend fun getCustomToolById(id: String): CustomToolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomTool(tool: CustomToolEntity)

    @Query("DELETE FROM custom_tools WHERE id = :id")
    suspend fun deleteCustomTool(id: String)
}

@Database(
    entities = [
        ConversationEntity::class,
        ChatMessageEntity::class,
        LibraryItemEntity::class,
        DailyMemoryEntity::class,
        CustomToolEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun libraryDao(): LibraryDao
    abstract fun dailyMemoryDao(): DailyMemoryDao
    abstract fun customToolDao(): CustomToolDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "max_n_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
