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
}
