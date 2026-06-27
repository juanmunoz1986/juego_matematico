package com.example.data

import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val dao: UserScoreDao) {
    val topScores: Flow<List<UserScore>> = dao.getTopScores()

    suspend fun insertScore(score: UserScore) {
        dao.insertScore(score)
    }

    suspend fun getHighScoreGlobal(): Int {
        return dao.getHighScoreGlobal() ?: 0
    }

    suspend fun getUnsyncedScores(): List<UserScore> {
        return dao.getUnsyncedScores()
    }

    fun getUnsyncedCountFlow(): Flow<Int> {
        return dao.getUnsyncedCountFlow()
    }

    suspend fun markAsSynced(id: Int) {
        dao.markAsSynced(id)
    }
}
