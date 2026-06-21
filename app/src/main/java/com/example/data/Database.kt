package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- entities ---

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val lastActiveTimestamp: Long = 0L,
    val totalXp: Int = 0,
    val devOpsXp: Int = 0,
    val unityXp: Int = 0,
    val nodeJsXp: Int = 0,
    val aspNetXp: Int = 0,
    val lastDailyChallengeDate: String = ""
)

@Entity(tableName = "leaderboard_opponents")
data class LeaderboardOpponentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val xp: Int,
    val badge: String,
    val isUser: Boolean = false
)

@Entity(tableName = "completed_challenges")
data class CompletedChallengeEntity(
    @PrimaryKey val id: String,
    val theme: String, // DevOps, Unity, NodeJs, AspNet
    val title: String,
    val xpPoints: Int,
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAOs ---

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun getUserProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getUserProgressDirect(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: UserProgressEntity)
}

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard_opponents ORDER BY xp DESC")
    fun getAllOpponents(): Flow<List<LeaderboardOpponentEntity>>

    @Query("SELECT * FROM leaderboard_opponents ORDER BY xp DESC")
    suspend fun getAllOpponentsDirect(): List<LeaderboardOpponentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpponents(opponents: List<LeaderboardOpponentEntity>)

    @Update
    suspend fun updateOpponent(opponent: LeaderboardOpponentEntity)

    @Query("DELETE FROM leaderboard_opponents")
    suspend fun deleteAll()
}

@Dao
interface CompletedChallengeDao {
    @Query("SELECT * FROM completed_challenges ORDER BY timestamp DESC")
    fun getCompletedChallenges(): Flow<List<CompletedChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompleted(challenge: CompletedChallengeEntity)
}

// --- App Database ---

@Database(
    entities = [
        UserProgressEntity::class,
        LeaderboardOpponentEntity::class,
        CompletedChallengeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProgressDao(): UserProgressDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun completedChallengeDao(): CompletedChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "devlingo_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
