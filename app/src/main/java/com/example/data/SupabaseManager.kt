package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseManager {
    private const val TAG = "SupabaseManager"
    private const val PREFS_NAME = "supabase_auth_prefs"
    private const val KEY_JWT = "access_token"
    private const val KEY_USER_ID = "user_id"

    private var apiService: ApiService? = null
    private var supabaseUrl: String = ""
    private var supabaseAnonKey: String = ""

    private var cachedUserId: String? = null
    private var cachedJwt: String? = null

    private val _globalScores = MutableStateFlow<List<UserScore>>(emptyList())
    val globalScores: StateFlow<List<UserScore>> = _globalScores

    private val _syncStatus = MutableStateFlow("Iniciando...")
    val syncStatus: StateFlow<String> = _syncStatus

    fun initialize(context: Context) {
        // Read keys from BuildConfig (injected via .env / Secrets)
        supabaseUrl = try { BuildConfig.SUPABASE_URL } catch (e: Exception) { "" }
        supabaseAnonKey = try { BuildConfig.SUPABASE_ANON_KEY } catch (e: Exception) { "" }

        if (supabaseUrl.isBlank() || supabaseUrl.startsWith("https://your-project") || supabaseAnonKey.isBlank() || supabaseAnonKey.startsWith("your-anon")) {
            Log.w(TAG, "Configuración de Supabase no válida o en blanco. Corriendo en Modo Local.")
            _syncStatus.value = "Modo Local"
            return
        }

        // Standardize URL: Must end with a slash for Retrofit
        if (!supabaseUrl.endsWith("/")) {
            supabaseUrl += "/"
        }

        try {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(supabaseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            apiService = retrofit.create(ApiService::class.java)

            // Load saved session
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            cachedUserId = prefs.getString(KEY_USER_ID, null)
            cachedJwt = prefs.getString(KEY_JWT, null)

            CoroutineScope(Dispatchers.IO).launch {
                authenticateAndSync(context)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando Supabase: ${e.message}", e)
            _syncStatus.value = "Modo Local"
        }
    }

    private suspend fun authenticateAndSync(context: Context) {
        val service = apiService ?: return

        try {
            if (cachedUserId == null || cachedJwt == null) {
                _syncStatus.value = "Autenticando..."
                Log.d(TAG, "No hay sesión activa. Iniciando registro anónimo.")
                val authRes = service.signUpAnonymous(supabaseAnonKey)
                
                cachedUserId = authRes.user.id
                cachedJwt = authRes.access_token

                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .putString(KEY_USER_ID, cachedUserId)
                    .putString(KEY_JWT, cachedJwt)
                    .apply()

                Log.d(TAG, "Registro anónimo exitoso. Usuario ID: $cachedUserId")

                // First time setup: sync initial owner name to Supabase players table
                val mainPrefs = context.getSharedPreferences("acumath_prefs", Context.MODE_PRIVATE)
                val ownerName = mainPrefs.getString("owner_name", "Propietario") ?: "Propietario"
                updatePlayerNameInDatabase(ownerName)
            } else {
                Log.d(TAG, "Sesión de Supabase cargada. ID de Usuario: $cachedUserId")
            }

            _syncStatus.value = "Conectado"
            fetchLeaderboard()

        } catch (e: Exception) {
            Log.e(TAG, "Error durante la autenticación de Supabase: ${e.message}", e)
            _syncStatus.value = "Modo Local (Offline)"
            // Try fallback fetch anyway in case only authentication was failed
            try {
                fetchLeaderboard()
            } catch (le: Exception) {
                // Ignore
            }
        }
    }

    suspend fun fetchLeaderboard() = withContext(Dispatchers.IO) {
        val service = apiService ?: return@withContext
        try {
            val rawLeaderboard = service.getLeaderboard(supabaseAnonKey)
            val mappedScores = rawLeaderboard.map { item ->
                UserScore(
                    id = item.id?.toInt() ?: item.hashCode(),
                    playerName = item.display_name ?: "Invitado",
                    score = item.score,
                    levelReached = item.level ?: 1,
                    maxStreak = item.max_streak ?: 0,
                    difficultyPlayed = item.difficulty ?: "Bajo",
                    timestamp = System.currentTimeMillis(),
                    isSynced = true,
                    isOnlinePlay = true
                )
            }
            _globalScores.value = mappedScores
            _syncStatus.value = "Conectado"
        } catch (e: Exception) {
            Log.e(TAG, "Error recuperando leaderboard: ${e.message}", e)
            // Keep existing values or set status
            _syncStatus.value = "Error de Conexión"
        }
    }

    suspend fun submitScore(score: UserScore): Boolean = withContext(Dispatchers.IO) {
        val service = apiService ?: return@withContext false
        val userId = cachedUserId ?: return@withContext false
        val token = cachedJwt ?: return@withContext false

        try {
            val req = SupabaseScoreRequest(
                player_id = userId,
                score = score.score,
                max_streak = score.maxStreak,
                difficulty = score.difficultyPlayed,
                level = score.levelReached
            )

            val bearer = "Bearer $token"
            val response = service.submitScore(supabaseAnonKey, bearer, req)

            if (response.isSuccessful) {
                Log.d(TAG, "Puntaje subido con éxito a Supabase")
                // Refresh leaderboard after successful submission
                fetchLeaderboard()
                return@withContext true
            } else {
                Log.e(TAG, "Error subiendo puntaje. Código: ${response.code()}, Mensaje: ${response.errorBody()?.string()}")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción subiendo puntaje a Supabase: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun updatePlayerName(newName: String): Boolean = withContext(Dispatchers.IO) {
        updatePlayerNameInDatabase(newName)
    }

    private suspend fun updatePlayerNameInDatabase(newName: String): Boolean {
        val service = apiService ?: return false
        val userId = cachedUserId ?: return false
        val token = cachedJwt ?: return false

        try {
            val bearer = "Bearer $token"
            val profile = SupabasePlayerProfile(display_name = newName)
            val filter = "eq.$userId"
            
            val response = service.updatePlayerProfile(supabaseAnonKey, bearer, filter, profile)
            if (response.isSuccessful) {
                Log.d(TAG, "Nombre de jugador actualizado en Supabase: $newName")
                return true
            } else {
                Log.e(TAG, "Error actualizando perfil. Código: ${response.code()}, Cuerpo: ${response.errorBody()?.string()}")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción actualizando perfil en Supabase: ${e.message}", e)
            return false
        }
    }
}
