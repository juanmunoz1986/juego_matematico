package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserScoreDao {
    @Query("SELECT * FROM user_scores ORDER BY score DESC LIMIT 50")
    fun getTopScores(): Flow<List<UserScore>>

    @Query("SELECT * FROM user_scores WHERE playerName = :name ORDER BY score DESC")
    fun getScoresForPlayer(name: String): Flow<List<UserScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: UserScore)

    @Query("SELECT MAX(score) FROM user_scores")
    suspend fun getHighScoreGlobal(): Int?

    @Query("SELECT * FROM user_scores WHERE isSynced = 0 AND isOnlinePlay = 1")
    suspend fun getUnsyncedScores(): List<UserScore>

    @Query("SELECT COUNT(*) FROM user_scores WHERE isSynced = 0 AND isOnlinePlay = 1")
    fun getUnsyncedCountFlow(): Flow<Int>

    @Query("UPDATE user_scores SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)
}
