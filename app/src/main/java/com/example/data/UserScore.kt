package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_scores")
data class UserScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerName: String,
    val score: Int,
    val levelReached: Int,
    val maxStreak: Int,
    val difficultyPlayed: String,
    val timestamp: Long = System.currentTimeMillis()
)
