package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logic.EquationGenerator
import com.example.logic.DifficultyLevel

@Composable
fun GameScreen(
    viewModel: MainViewModel,
    currentEquation: EquationGenerator.Equation?,
    score: Int,
    streak: Int,
    lives: Int,
    timeRemaining: Float,
    secondsRemaining: Int,
    selectedAnswer: Int?,
    answerStatus: Boolean?,
    onAnswerSubmitted: (Int) -> Unit,
    onQuitGame: () -> Unit
) {
    val selectedDifficulty by viewModel.difficulty.collectAsState()
    val currentSubDiff by viewModel.currentSubDifficulty.collectAsState()

    // Neon Cosmic Gradient Background
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF090A15),
            Color(0xFF12142B),
            Color(0xFF06070B)
        )
    )

    // Fire/glowing animation for high streaks
    val isStreakActive = streak >= 3
    val infiniteTransition = rememberInfiniteTransition(label = "fire_pulse")
    val fireScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Dynamic timer bar color
    val timerColor by animateColorAsState(
        targetValue = when {
            timeRemaining > 0.6f -> Color(0xFF00E5FF) // Neon Cyan
            timeRemaining > 0.3f -> Color(0xFFFFB300) // Warning Amber
            else -> Color(0xFFFF0055) // Critical Red
        },
        animationSpec = tween(150),
        label = "timerColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit / Quit
                TextButton(
                    onClick = onQuitGame,
                    modifier = Modifier.testTag("exit_game_button")
                ) {
                    Text("SALIR", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Hearts (Lives) Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("lives_hud")
                ) {
                    (1..3).forEach { index ->
                        val active = index <= lives
                        val heartIcon = if (active) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder
                        val heartColor = if (active) Color(0xFFFF0055) else Color(0xFF333857)
                        
                        Icon(
                            imageVector = heartIcon,
                            contentDescription = "Vida $index",
                            tint = heartColor,
                            modifier = Modifier
                                .size(28.dp)
                                .scale(if (active) 1f else 0.85f)
                        )
                    }
                }
            }

            // Score and Streak HUD Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131525)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF232742))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PUNTUACIÓN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$score PTS",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.testTag("score_display")
                        )
                    }

                    // Streak Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isStreakActive) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .scale(fireScale)
                                    .padding(end = 6.dp)
                            ) {
                                Text(
                                    text = "🔥",
                                    fontSize = 28.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "RACHA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            val comboMultiplier = 1 + (streak / 3).coerceAtMost(4)
                            Text(
                                text = "x$streak (Combo x$comboMultiplier)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (streak > 0) Color(0xFFFF007F) else Color.White,
                                modifier = Modifier.testTag("streak_display")
                            )
                        }
                    }
                }
            }

            // Main Equation Display Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (currentEquation != null) {
                        // Display Difficulty Category tag
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF211E3A), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (selectedDifficulty == DifficultyLevel.SUPER_PRO) {
                                    "MODO SUPER PRO (${currentSubDiff.displayName.uppercase()})"
                                } else {
                                    "RESUELVE LA OPERACIÓN"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF007F),
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Polished Slate Board for Math presentation
                        val displayEquation = currentEquation.text.replace("*", "×")
                        val calculatedFontSize = when {
                            displayEquation.length > 35 -> 13.sp
                            displayEquation.length > 28 -> 16.sp
                            displayEquation.length > 20 -> 20.sp
                            displayEquation.length > 12 -> 25.sp
                            else -> 30.sp
                        }
                        val horizontalPadding = if (displayEquation.length > 25) 12.dp else 20.dp

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C071C)),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.6f),
                                        Color(0xFFFF007F).copy(alpha = 0.6f)
                                    )
                                )
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding, vertical = 28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayEquation,
                                    fontSize = calculatedFontSize,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    softWrap = true,
                                    modifier = Modifier.testTag("equation_display")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Success / Error / Timeout message overlays
                        AnimatedVisibility(visible = answerStatus != null) {
                            val text = if (answerStatus == true) "¡CORRECTO! 🔥" else "¡INCORRECTO! ❌"
                            val color = if (answerStatus == true) Color(0xFF00FF66) else Color(0xFFFF0055)
                            Text(
                                text = text,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = color,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    } else {
                        CircularProgressIndicator(color = Color(0xFF00E5FF))
                    }
                }
            }

            // Timer bar section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIEMPO RESTANTE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${secondsRemaining}s",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = timerColor
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))

                // Beautiful smooth progress bar
                LinearProgressIndicator(
                    progress = { timeRemaining },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .testTag("timer_progress_bar"),
                    color = timerColor,
                    trackColor = Color(0xFF1E213D),
                )
            }

            // Answers Grid Options (2x2)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentEquation != null) {
                    // Split the 4 options into rows of 2
                    val rows = currentEquation.options.chunked(2)
                    rows.forEachIndexed { rowIndex, rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { option ->
                                val isChosen = selectedAnswer == option
                                val isCorrectOption = option == currentEquation.result

                                // Compute glowing background and border colors for selected answer states
                                val borderStroke = when {
                                    answerStatus != null && isCorrectOption -> {
                                        BorderStroke(2.dp, Color(0xFF00FF66)) // Always glow green for correct answer
                                    }
                                    answerStatus != null && isChosen && !isCorrectOption -> {
                                        BorderStroke(2.dp, Color(0xFFFF0055)) // Glow red if wrong option chosen
                                    }
                                    isChosen -> {
                                        BorderStroke(2.dp, Color(0xFF00E5FF)) // Highlight cyan while evaluating
                                    }
                                    else -> {
                                        BorderStroke(1.dp, Color(0xFF252949)) // Standard card border
                                    }
                                }

                                val containerColor = when {
                                    answerStatus != null && isCorrectOption -> Color(0xFF0D2D1B)
                                    answerStatus != null && isChosen && !isCorrectOption -> Color(0xFF330D1B)
                                    isChosen -> Color(0xFF12233C)
                                    else -> Color(0xFF121424)
                                }

                                val textColor = when {
                                    answerStatus != null && isCorrectOption -> Color(0xFF00FF66)
                                    answerStatus != null && isChosen && !isCorrectOption -> Color(0xFFFF0055)
                                    isChosen -> Color(0xFF00E5FF)
                                    else -> Color.White
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(76.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(containerColor)
                                        .border(borderStroke, RoundedCornerShape(16.dp))
                                        .clickable(enabled = selectedAnswer == null) {
                                            onAnswerSubmitted(option)
                                        }
                                        .testTag("answer_option_$option"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = option.toString(),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        // Optional check/cancel icon
                                        if (answerStatus != null) {
                                            if (isCorrectOption) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = "Correcto",
                                                    tint = Color(0xFF00FF66),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else if (isChosen && !isCorrectOption) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = "Incorrecto",
                                                    tint = Color(0xFFFF0055),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
