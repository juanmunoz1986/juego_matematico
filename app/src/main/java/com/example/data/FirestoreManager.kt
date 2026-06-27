package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object FirestoreManager {
    private const val TAG = "FirestoreManager"
    private const val COLLECTION_SCORES = "scores"

    private var isFirebaseAvailable = false
    private var firestore: FirebaseFirestore? = null

    private val _globalScores = MutableStateFlow<List<UserScore>>(emptyList())
    val globalScores: StateFlow<List<UserScore>> = _globalScores

    private val _syncStatus = MutableStateFlow("Iniciando...")
    val syncStatus: StateFlow<String> = _syncStatus

    fun initialize(context: Context) {
        if (isFirebaseAvailable) return

        try {
            // Check if Firebase is already initialized
            val apps = FirebaseApp.getApps(context)
            if (apps.isEmpty()) {
                FirebaseApp.initializeApp(context)
            }

            if (FirebaseApp.getApps(context).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                isFirebaseAvailable = true
                _syncStatus.value = "Conectado"
                listenToGlobalScores()
            } else {
                Log.i(TAG, "Firebase no está inicializado. Corriendo en Modo Local.")
                _syncStatus.value = "Modo Local"
            }
        } catch (e: Exception) {
            Log.i(TAG, "Firebase no disponible: ${e.message}. Corriendo en Modo Local.")
            _syncStatus.value = "Modo Local"
        }
    }

    private fun listenToGlobalScores() {
        val db = firestore ?: return
        try {
            db.collection(COLLECTION_SCORES)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error escuchando Firestore: ", error)
                        _syncStatus.value = "Error de Conexión"
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val scoresList = mutableListOf<UserScore>()
                        for (doc in snapshot.documents) {
                            try {
                                val playerName = doc.getString("playerName") ?: "Invitado"
                                val score = doc.getLong("score")?.toInt() ?: 0
                                val levelReached = doc.getLong("levelReached")?.toInt() ?: 1
                                val maxStreak = doc.getLong("maxStreak")?.toInt() ?: 0
                                val difficultyPlayed = doc.getString("difficultyPlayed") ?: "Bajo"
                                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()

                                scoresList.add(
                                    UserScore(
                                        id = doc.id.hashCode(), // temporary hash code
                                        playerName = playerName,
                                        score = score,
                                        levelReached = levelReached,
                                        maxStreak = maxStreak,
                                        difficultyPlayed = difficultyPlayed,
                                        timestamp = timestamp
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error mapeando documento de Firestore", e)
                            }
                        }
                        _globalScores.value = scoresList
                        _syncStatus.value = "Conectado"
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error al escuchar Firestore", e)
            _syncStatus.value = "Modo Local"
        }
    }

    suspend fun submitScore(score: UserScore): Boolean = suspendCancellableCoroutine { continuation ->
        val db = firestore ?: run {
            Log.d(TAG, "Firebase no disponible, omitiendo submitScore.")
            if (continuation.isActive) continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        try {
            val scoreMap = hashMapOf(
                "playerName" to score.playerName,
                "score" to score.score,
                "levelReached" to score.levelReached,
                "maxStreak" to score.maxStreak,
                "difficultyPlayed" to score.difficultyPlayed,
                "timestamp" to score.timestamp
            )

            db.collection(COLLECTION_SCORES)
                .add(scoreMap)
                .addOnSuccessListener { ref ->
                    Log.d(TAG, "Puntaje subido con éxito ID: ${ref.id}")
                    if (continuation.isActive) continuation.resume(true)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al subir puntaje a Firestore", e)
                    if (continuation.isActive) continuation.resume(false)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error en submitScore", e)
            if (continuation.isActive) continuation.resume(false)
        }
    }
}
