package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GameOverScreen
import com.example.ui.GameScreen
import com.example.ui.GameState
import com.example.ui.LeaderboardScreen
import com.example.ui.MainViewModel
import com.example.ui.MenuScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: MainViewModel = viewModel()
        val gameState by viewModel.gameState.collectAsState()
        val playerName by viewModel.playerName.collectAsState()
        val difficulty by viewModel.difficulty.collectAsState()
        val currentEquation by viewModel.currentEquation.collectAsState()
        val score by viewModel.score.collectAsState()
        val streak by viewModel.streak.collectAsState()
        val lives by viewModel.lives.collectAsState()
        val timeRemaining by viewModel.timeRemaining.collectAsState()
        val secondsRemaining by viewModel.secondsRemaining.collectAsState()
        val selectedAnswer by viewModel.selectedAnswer.collectAsState()
        val answerStatus by viewModel.answerStatus.collectAsState()
        val highScore by viewModel.highScore.collectAsState()
        val topScores by viewModel.topScores.collectAsState()
        val globalScores by viewModel.globalScores.collectAsState()
        val firestoreSyncStatus by viewModel.firestoreSyncStatus.collectAsState()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          when (gameState) {
            is GameState.MainMenu -> {
              MenuScreen(
                viewModel = viewModel,
                playerName = playerName,
                selectedDiff = difficulty,
                highScore = highScore,
                onStartGame = { viewModel.startGame() },
                onNavigateToLeaderboard = { viewModel.navigateTo(GameState.Leaderboard) }
              )
            }
            is GameState.Playing -> {
              GameScreen(
                viewModel = viewModel,
                currentEquation = currentEquation,
                score = score,
                streak = streak,
                lives = lives,
                timeRemaining = timeRemaining,
                secondsRemaining = secondsRemaining,
                selectedAnswer = selectedAnswer,
                answerStatus = answerStatus,
                onAnswerSubmitted = { viewModel.submitAnswer(it) },
                onQuitGame = { viewModel.navigateTo(GameState.MainMenu) }
              )
            }
            is GameState.GameOver -> {
              GameOverScreen(
                playerName = playerName,
                score = score,
                streak = streak,
                difficulty = difficulty,
                onRetry = { viewModel.startGame() },
                onGoToMenu = { viewModel.navigateTo(GameState.MainMenu) }
              )
            }
            is GameState.Leaderboard -> {
              LeaderboardScreen(
                localScores = topScores,
                globalScores = globalScores,
                firestoreSyncStatus = firestoreSyncStatus,
                onBackToMenu = { viewModel.navigateTo(GameState.MainMenu) }
              )
            }
          }
        }
      }
    }
  }
}
