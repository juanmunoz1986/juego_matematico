package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FirestoreManager
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
    val globalScores: StateFlow<List<UserScore>> = FirestoreManager.globalScores
    val firestoreSyncStatus: StateFlow<String> = FirestoreManager.syncStatus

    init {
        FirestoreManager.initialize(application)
        val database = AppDatabase.getDatabase(application)
        repository = ScoreRepository(database.userScoreDao())
        topScores = repository.topScores.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private val _gameState = MutableStateFlow<GameState>(GameState.MainMenu)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _playerName = MutableStateFlow("Invitado")
    val playerName: StateFlow<String> = _playerName.asStateFlow()

    private val _difficulty = MutableStateFlow(DifficultyLevel.BAJO)
    val difficulty: StateFlow<DifficultyLevel> = _difficulty.asStateFlow()

    private val _currentSubDifficulty = MutableStateFlow(DifficultyLevel.BAJO)
    val currentSubDifficulty: StateFlow<DifficultyLevel> = _currentSubDifficulty.asStateFlow()

    private var questionsCorrect = 0

    // Game session states
    private val _currentEquation = MutableStateFlow<EquationGenerator.Equation?>(null)
    val currentEquation: StateFlow<EquationGenerator.Equation?> = _currentEquation.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

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
        if (name.isNotBlank()) {
            _playerName.value = name.trim()
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
        val totalSec = if (_difficulty.value == DifficultyLevel.SUPER_PRO) {
            val baseTime = 25
            // Reduce by 1 second per correct answer (progression) and 1 second per current streak
            val progressionReduction = questionsCorrect
            val streakReduction = _streak.value
            val calculated = baseTime - progressionReduction - streakReduction
            if (calculated < 8) 8 else calculated
        } else {
            _difficulty.value.initialTimeSec
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

    private fun saveScore() {
        viewModelScope.launch {
            val userScore = UserScore(
                playerName = _playerName.value,
                score = _score.value,
                levelReached = _difficulty.value.ordinal + 1,
                maxStreak = _streak.value,
                difficultyPlayed = _difficulty.value.displayName
            )
            repository.insertScore(userScore)
            FirestoreManager.submitScore(userScore)
            loadHighScore()
        }
    }
}
