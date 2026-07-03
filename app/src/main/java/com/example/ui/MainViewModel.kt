package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SupabaseManager
import com.example.data.ScoreRepository
import com.example.data.UserScore
import com.example.logic.DifficultyLevel
import com.example.logic.EquationGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class GameState {
    object MainMenu : GameState()
    object Playing : GameState()
    object GameOver : GameState()
    object Leaderboard : GameState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScoreRepository
    val topScores: StateFlow<List<UserScore>>
    val globalScores: StateFlow<List<UserScore>> = SupabaseManager.globalScores
    val firestoreSyncStatus: StateFlow<String> = SupabaseManager.syncStatus

    private val sharedPrefs = application.getSharedPreferences("acumath_prefs", Context.MODE_PRIVATE)

    private val _ownerName = MutableStateFlow(sharedPrefs.getString("owner_name", "Propietario") ?: "Propietario")
    val ownerName: StateFlow<String> = _ownerName.asStateFlow()

    private val _isOnlineMode = MutableStateFlow(true)
    val isOnlineMode: StateFlow<Boolean> = _isOnlineMode.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _playerName = MutableStateFlow("Invitado")
    val playerName: StateFlow<String> = _playerName.asStateFlow()

    val unsyncedCount: StateFlow<Int>

    init {
        SupabaseManager.initialize(application)
        val database = AppDatabase.getDatabase(application)
        repository = ScoreRepository(database.userScoreDao())
        topScores = repository.topScores.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        unsyncedCount = repository.getUnsyncedCountFlow().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
        // Lock player name to owner name if online mode is active
        _playerName.value = if (_isOnlineMode.value) _ownerName.value else "Invitado"
    }

    private val _gameState = MutableStateFlow<GameState>(GameState.MainMenu)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _difficulty = MutableStateFlow(DifficultyLevel.BAJO)
    val difficulty: StateFlow<DifficultyLevel> = _difficulty.asStateFlow()

    private val _currentSubDifficulty = MutableStateFlow(DifficultyLevel.BAJO)
    val currentSubDifficulty: StateFlow<DifficultyLevel> = _currentSubDifficulty.asStateFlow()

    private val _customTimeSec = MutableStateFlow(15)
    val customTimeSec: StateFlow<Int> = _customTimeSec.asStateFlow()

    private val _isDynamicReduction = MutableStateFlow(false)
    val isDynamicReduction: StateFlow<Boolean> = _isDynamicReduction.asStateFlow()

    private var questionsCorrect = 0

    fun setGameTimerConfig(timeSec: Int, isDynamic: Boolean) {
        _customTimeSec.value = timeSec
        _isDynamicReduction.value = isDynamic
    }

    // Game session states
    private val _currentEquation = MutableStateFlow<EquationGenerator.Equation?>(null)
    val currentEquation: StateFlow<EquationGenerator.Equation?> = _currentEquation.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _maxStreak = MutableStateFlow(0)
    val maxStreak: StateFlow<Int> = _maxStreak.asStateFlow()

    private val _lives = MutableStateFlow(3)
    val lives: StateFlow<Int> = _lives.asStateFlow()

    // Timer state: in milliseconds or seconds
    private val _timeRemaining = MutableStateFlow(0.0f) // 0.0 to 1.0
    val timeRemaining: StateFlow<Float> = _timeRemaining.asStateFlow()

    private val _secondsRemaining = MutableStateFlow(0)
    val secondsRemaining: StateFlow<Int> = _secondsRemaining.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<Int?>(null)
    val selectedAnswer: StateFlow<Int?> = _selectedAnswer.asStateFlow()

    private val _answerStatus = MutableStateFlow<Boolean?>(null) // true = correct, false = wrong, null = pending
    val answerStatus: StateFlow<Boolean?> = _answerStatus.asStateFlow()

    private val _highScore = MutableStateFlow(0)
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    private var timerJob: Job? = null
    private var isAnswering = false

    init {
        loadHighScore()
    }

    fun setPlayerName(name: String) {
        // Only allow changing player name in OFFLINE mode
        if (!_isOnlineMode.value && name.isNotBlank()) {
            _playerName.value = name.trim()
        }
    }

    fun updateOwnerName(name: String) {
        if (name.isNotBlank()) {
            val trimmed = name.trim()
            _ownerName.value = trimmed
            sharedPrefs.edit().putString("owner_name", trimmed).apply()
            if (_isOnlineMode.value) {
                _playerName.value = trimmed
                viewModelScope.launch {
                    SupabaseManager.updatePlayerName(trimmed)
                }
            }
        }
    }

    fun setDifficulty(diff: DifficultyLevel) {
        _difficulty.value = diff
    }

    private fun loadHighScore() {
        viewModelScope.launch {
            _highScore.value = repository.getHighScoreGlobal()
        }
    }

    fun navigateTo(state: GameState) {
        _gameState.value = state
        if (state == GameState.MainMenu) {
            loadHighScore()
        }
    }

    fun startGame() {
        _score.value = 0
        _streak.value = 0
        _maxStreak.value = 0
        questionsCorrect = 0
        _lives.value = 3
        _answerStatus.value = null
        _selectedAnswer.value = null
        isAnswering = false
        _gameState.value = GameState.Playing
        nextQuestion()
    }

    private fun nextQuestion() {
        _selectedAnswer.value = null
        _answerStatus.value = null
        isAnswering = false
        
        val currentLevel = _difficulty.value
        val dynamicLevel = if (currentLevel == DifficultyLevel.SUPER_PRO) {
            val subDiff = when {
                questionsCorrect < 3 -> DifficultyLevel.BAJO
                questionsCorrect < 7 -> DifficultyLevel.MEDIO
                questionsCorrect < 12 -> DifficultyLevel.ALTO
                else -> DifficultyLevel.EXPERTO
            }
            _currentSubDifficulty.value = subDiff
            subDiff
        } else {
            _currentSubDifficulty.value = currentLevel
            currentLevel
        }

        val eq = EquationGenerator.generate(dynamicLevel)
        _currentEquation.value = eq

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        val baseTime = _customTimeSec.value
        val totalSec = if (_isDynamicReduction.value) {
            // El tiempo se reduce a medida que avanza (1 racha + correctas)
            val reduction = questionsCorrect + _streak.value
            val calculated = baseTime - reduction
            if (calculated < 4) 4 else calculated
        } else {
            baseTime
        }
        _secondsRemaining.value = totalSec
        _timeRemaining.value = 1.0f

        timerJob = viewModelScope.launch {
            val totalMillis = totalSec * 1000L
            val interval = 50L
            var elapsed = 0L

            while (elapsed < totalMillis) {
                delay(interval)
                elapsed += interval
                
                val remainingRatio = (totalMillis - elapsed).toFloat() / totalMillis
                _timeRemaining.value = remainingRatio.coerceIn(0f, 1f)
                _secondsRemaining.value = ((totalMillis - elapsed) / 1000L).toInt() + 1
            }

            _timeRemaining.value = 0f
            _secondsRemaining.value = 0
            onTimeOut()
        }
    }

    private fun onTimeOut() {
        if (isAnswering) return
        isAnswering = true
        _answerStatus.value = false
        _streak.value = 0
        _lives.value = (_lives.value - 1).coerceAtLeast(0)

        viewModelScope.launch {
            delay(1500)
            checkLivesOrContinue()
        }
    }

    fun submitAnswer(answer: Int) {
        if (isAnswering) return
        isAnswering = true
        timerJob?.cancel()
        _selectedAnswer.value = answer

        val eq = _currentEquation.value ?: return
        val isCorrect = (answer == eq.result)
        _answerStatus.value = isCorrect

        if (isCorrect) {
            questionsCorrect++
            val currentDiff = _difficulty.value
            val timeBonus = (_secondsRemaining.value * 2)
            val base = currentDiff.basePoints
            val comboBonus = 1 + (_streak.value / 3).coerceAtMost(4) // combo multiplier from x1 to x5
            
            val pointsEarned = (base + timeBonus) * comboBonus
            _score.value += pointsEarned
            _streak.value += 1
            if (_streak.value > _maxStreak.value) {
                _maxStreak.value = _streak.value
            }
        } else {
            _streak.value = 0
            _lives.value = (_lives.value - 1).coerceAtLeast(0)
        }

        viewModelScope.launch {
            delay(1500)
            checkLivesOrContinue()
        }
    }

    private fun checkLivesOrContinue() {
        if (_lives.value <= 0) {
            endGame()
        } else {
            nextQuestion()
        }
    }

    private fun endGame() {
        timerJob?.cancel()
        _gameState.value = GameState.GameOver
        saveScore()
    }

    fun setOnlineMode(isOnline: Boolean) {
        _isOnlineMode.value = isOnline
        if (isOnline) {
            _playerName.value = _ownerName.value
            syncOfflineScores()
        } else {
            _playerName.value = "Invitado"
        }
    }

    fun syncOfflineScores() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val unsynced = repository.getUnsyncedScores()
                for (score in unsynced) {
                    val success = SupabaseManager.submitScore(score)
                    if (success) {
                        repository.markAsSynced(score.id)
                    }
                }
            } catch (e: Exception) {
                // Ignore sync errors, we keep unsynced state to retry later
            } finally {
                _isSyncing.value = false
            }
        }
    }

    private fun saveScore() {
        viewModelScope.launch {
            val isOnline = _isOnlineMode.value
            var success = false

            val tempUserScore = UserScore(
                playerName = _playerName.value,
                score = _score.value,
                levelReached = _difficulty.value.ordinal + 1,
                maxStreak = _maxStreak.value,
                difficultyPlayed = _difficulty.value.displayName,
                isSynced = false,
                isOnlinePlay = isOnline
            )

            if (isOnline) {
                success = SupabaseManager.submitScore(tempUserScore)
            }

            val finalUserScore = tempUserScore.copy(isSynced = success)
            repository.insertScore(finalUserScore)
            loadHighScore()
        }
    }
}
