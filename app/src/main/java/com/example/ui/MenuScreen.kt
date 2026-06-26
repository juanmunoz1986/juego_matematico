package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logic.DifficultyLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: MainViewModel,
    playerName: String,
    selectedDiff: DifficultyLevel,
    highScore: Int,
    onStartGame: () -> Unit,
    onNavigateToLeaderboard: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var inputName by remember { mutableStateOf(if (playerName == "Invitado") "" else playerName) }
    val scrollState = rememberScrollState()

    // Neon Cosmic Gradient Background
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F101E),
            Color(0xFF15182D),
            Color(0xFF090A10)
        )
    )

    // Pulsing logo animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
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
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Game Logo / Title
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(pulseScale)
                    .size(110.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x3300E5FF), Color.Transparent)
                        )
                    )
            ) {
                Text(
                    text = "🧠",
                    fontSize = 72.sp,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "AcuMath",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Agilidad Mental Acumulada",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFF007F),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // High Score Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E213A)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2C315E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "High Score",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Record Actual",
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "$highScore PTS",
                        color = Color(0xFF00E5FF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Profile Section
            Text(
                text = "PERFIL DEL JUGADOR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 16.dp, bottom = 8.dp, start = 4.dp)
            )

            OutlinedTextField(
                value = inputName,
                onValueChange = {
                    inputName = it
                    viewModel.setPlayerName(it.ifBlank { "Invitado" })
                },
                placeholder = { Text("Introduce tu nombre...", color = Color.Gray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF2C315E),
                    focusedContainerColor = Color(0xFF131524),
                    unfocusedContainerColor = Color(0xFF131524),
                    cursorColor = Color(0xFF00E5FF)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Difficulty Selector Title
            Text(
                text = "SELECCIONA DIFICULTAD",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 24.dp, bottom = 8.dp, start = 4.dp)
            )

            // Grid of difficulties
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DifficultyLevel.values().forEach { level ->
                    val isSelected = selectedDiff == level
                    val cardBorder = if (isSelected) {
                        BorderStroke(1.5.dp, Color(0xFF00E5FF))
                    } else {
                        BorderStroke(1.dp, Color(0xFF1F223D))
                    }
                    val cardBg = if (isSelected) Color(0xFF1B2342) else Color(0xFF131525)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                focusManager.clearFocus()
                                viewModel.setDifficulty(level)
                            }
                            .testTag("diff_${level.name.lowercase()}"),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(12.dp),
                        border = cardBorder
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = level.displayName,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when (level) {
                                        DifficultyLevel.BAJO -> "Suma y resta (2 términos)"
                                        DifficultyLevel.MEDIO -> "Agrega multi y () simples"
                                        DifficultyLevel.ALTO -> "4 términos con [], () combinados"
                                        DifficultyLevel.EXPERTO -> "5 términos con todo agrupado"
                                        DifficultyLevel.SUPER_PRO -> "Aventura: ¡Tiempo se reduce con tus rachas y puntaje!"
                                    },
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${level.initialTimeSec}s",
                                    color = Color(0xFFFF007F),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Play Button with Neon Glow
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (inputName.isBlank()) {
                        viewModel.setPlayerName("Invitado")
                    }
                    onStartGame()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFFFF007F))),
                        RoundedCornerShape(28.dp)
                    )
                    .testTag("play_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1530)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Jugar",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INICIAR PARTIDA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Leaderboard Screen Button
            OutlinedButton(
                onClick = onNavigateToLeaderboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("leaderboard_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                border = BorderStroke(1.dp, Color(0xFF2C315E)),
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "CLASIFICACIÓN",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
