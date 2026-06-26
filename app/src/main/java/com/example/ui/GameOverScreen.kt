package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logic.DifficultyLevel

@Composable
fun GameOverScreen(
    playerName: String,
    score: Int,
    streak: Int,
    difficulty: DifficultyLevel,
    onRetry: () -> Unit,
    onGoToMenu: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Neon Cosmic Gradient Background
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF100918),
            Color(0xFF180D2E),
            Color(0xFF07040C)
        )
    )

    // Trophy animation
    val infiniteTransition = rememberInfiniteTransition(label = "trophy")
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Calculate rank tier based on final score
    val tierName: String
    val tierColor: Color
    val tierEmoji: String
    if (score >= 1000) {
        tierName = "Diamante Matemático 💎"
        tierColor = Color(0xFF00E5FF)
        tierEmoji = "💎"
    } else if (score >= 500) {
        tierName = "Oro Platino 🏆"
        tierColor = Color(0xFFFFD700)
        tierEmoji = "🏆"
    } else if (score >= 200) {
        tierName = "Agilidad Plata 🥈"
        tierColor = Color(0xFFC0C0C0)
        tierEmoji = "🥈"
    } else {
        tierName = "Bronce Iniciado 🥉"
        tierColor = Color(0xFFCD7F32)
        tierEmoji = "🥉"
    }

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Trophy Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(trophyScale)
                    .size(130.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(tierColor.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
            ) {
                Text(
                    text = tierEmoji,
                    fontSize = 72.sp,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "¡FIN DE LA PARTIDA!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "Rango: $tierName",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = tierColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Final Stats Panel Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF19132A)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF33204E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Player Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Jugador", color = Color.Gray, fontSize = 14.sp)
                        Text(playerName, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Divider(color = Color(0xFF2C1C44), thickness = 1.dp)

                    // Final Score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Puntuación Final", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = "$score PTS",
                            color = Color(0xFF00E5FF),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.testTag("game_over_final_score")
                        )
                    }

                    Divider(color = Color(0xFF2C1C44), thickness = 1.dp)

                    // Difficulty level
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dificultad Jugada", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = difficulty.displayName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Divider(color = Color(0xFF2C1C44), thickness = 1.dp)

                    // Max streak
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Máxima Racha", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = "$streak Aciertos",
                            color = Color(0xFFFF007F),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Play Again Button (Retry)
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFFFF007F))),
                        RoundedCornerShape(28.dp)
                    )
                    .testTag("retry_game_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1A47)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reintentar",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "REINTENTAR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Return to Main Menu Button
            OutlinedButton(
                onClick = onGoToMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("main_menu_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                border = BorderStroke(1.dp, Color(0xFF2C1C44)),
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Volver al Menu",
                        tint = Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VOLVER AL MENÚ",
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
