package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserScore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    localScores: List<UserScore>,
    globalScores: List<UserScore> = emptyList(),
    firestoreSyncStatus: String = "Modo Local",
    onBackToMenu: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Local (Room), 1 = Global (Cloud DB Spec)
    var showArchitectureSpec by remember { mutableStateOf(false) }

    val difficulties = listOf("Todos", "Bajo", "Medio", "Alto", "Experto", "Super Pro")
    var selectedDifficultyFilter by remember { mutableStateOf("Todos") }

    // Neon Cosmic Gradient Background
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF090A1E),
            Color(0xFF13152F),
            Color(0xFF040508)
        )
    )

    // Simulated Global Competitors as backup demo data
    val simulatedGlobalScores = listOf(
        UserScore(id = 100, playerName = "Gauss_Legendre", score = 3200, levelReached = 5, maxStreak = 24, difficultyPlayed = "Super Pro"),
        UserScore(id = 101, playerName = "EulerFan_99", score = 2450, levelReached = 4, maxStreak = 18, difficultyPlayed = "Experto"),
        UserScore(id = 102, playerName = "BrainSpeedster", score = 1980, levelReached = 4, maxStreak = 15, difficultyPlayed = "Experto"),
        UserScore(id = 103, playerName = "MathWizard_Pro", score = 1650, levelReached = 3, maxStreak = 13, difficultyPlayed = "Alto"),
        UserScore(id = 104, playerName = "AcuMaster_Sigma", score = 1420, levelReached = 3, maxStreak = 11, difficultyPlayed = "Alto"),
        UserScore(id = 105, playerName = "NewtonRaphson", score = 1150, levelReached = 4, maxStreak = 9, difficultyPlayed = "Experto"),
        UserScore(id = 106, playerName = "MathLover_Arg", score = 980, levelReached = 2, maxStreak = 12, difficultyPlayed = "Medio"),
        UserScore(id = 107, playerName = "AlphaMatemago", score = 750, levelReached = 2, maxStreak = 8, difficultyPlayed = "Medio"),
        UserScore(id = 108, playerName = "Fibonacci_Spur", score = 610, levelReached = 5, maxStreak = 7, difficultyPlayed = "Super Pro")
    )

    val activeGlobalScores = if (globalScores.isNotEmpty()) globalScores else simulatedGlobalScores

    val filteredLocalScores = remember(localScores, selectedDifficultyFilter) {
        if (selectedDifficultyFilter == "Todos") {
            localScores
        } else {
            localScores.filter { it.difficultyPlayed.equals(selectedDifficultyFilter, ignoreCase = true) }
        }
    }

    val filteredGlobalScores = remember(activeGlobalScores, selectedDifficultyFilter) {
        if (selectedDifficultyFilter == "Todos") {
            activeGlobalScores
        } else {
            activeGlobalScores.filter { it.difficultyPlayed.equals(selectedDifficultyFilter, ignoreCase = true) }
        }
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
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToMenu,
                    modifier = Modifier.testTag("leaderboard_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Clasificación",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Tab Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF131525))
                    .border(BorderStroke(1.dp, Color(0xFF2C315E)), RoundedCornerShape(24.dp))
            ) {
                // Local Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = 0 }
                        .background(if (activeTab == 0) Color(0xFF1F2445) else Color.Transparent)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "💾",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = "LOCAL (Room)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (activeTab == 0) Color.White else Color.Gray
                        )
                    }
                }

                // Global Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeTab = 1 }
                        .background(if (activeTab == 1) Color(0xFF1F2445) else Color.Transparent)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "☁️",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = "GLOBAL",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (activeTab == 1) Color.White else Color.Gray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    when (firestoreSyncStatus) {
                                        "Conectado" -> Color(0xFF00FF66)
                                        "Conectando..." -> Color(0xFFFFB300)
                                        else -> Color(0xFF888888)
                                    }
                                )
                        )
                    }
                }
            }

            // Difficulty Filter Chips (Horizontal LazyRow)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(difficulties) { diff ->
                    val isSelected = selectedDifficultyFilter == diff
                    val chipBg = if (isSelected) Color(0xFF00E5FF) else Color(0xFF131525)
                    val chipTextColor = if (isSelected) Color.Black else Color.White
                    val chipBorderColor = if (isSelected) Color(0xFF00E5FF) else Color(0xFF2C315E)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(chipBg)
                            .border(BorderStroke(1.dp, chipBorderColor), RoundedCornerShape(16.dp))
                            .clickable { selectedDifficultyFilter = diff }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = diff.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = chipTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tabs Content Area
            if (activeTab == 0) {
                // LOCAL TAB (Room data)
                if (filteredLocalScores.isEmpty()) {
                    // Empty State View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🏆",
                                fontSize = 64.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (localScores.isEmpty()) "Aún no hay puntuaciones guardadas" else "No hay records en dificultad $selectedDifficultyFilter",
                                color = Color.Gray,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (localScores.isEmpty()) "¡Completa tu primera partida para inaugurar la tabla local!" else "¡Intenta jugar una partida en esta dificultad para registrar tu récord!",
                                color = Color.DarkGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 30.dp, end = 30.dp, top = 4.dp)
                            )
                        }
                    }
                } else {
                    // Score items
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(filteredLocalScores) { index, item ->
                            ScoreItemRow(rank = index + 1, item = item)
                        }
                    }
                }
            } else {
                // GLOBAL SIMULATED CLOUD LEADERBOARD
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Informative card about Firebase Firestore Sincronización
                    val cardBgColor = when (firestoreSyncStatus) {
                        "Conectado" -> Color(0xFF0F1E16)
                        "Conectando..." -> Color(0xFF231E15)
                        else -> Color(0xFF1F1218)
                    }
                    val cardBorderColor = when (firestoreSyncStatus) {
                        "Conectado" -> Color(0xFF00FF66)
                        "Conectando..." -> Color(0xFFFFB300)
                        else -> Color(0xFFFF007F)
                    }
                    val cardEmoji = when (firestoreSyncStatus) {
                        "Conectado" -> "🟢"
                        "Conectando..." -> "⏳"
                        else -> "🔴"
                    }
                    val cardTitle = when (firestoreSyncStatus) {
                        "Conectado" -> "Conexión Firestore Activa"
                        "Conectando..." -> "Estableciendo Conexión..."
                        else -> "Modo Local / Sin Conexión"
                    }
                    val cardDescription = when (firestoreSyncStatus) {
                        "Conectado" -> "Los récords mundiales se sincronizan en tiempo real vía Firebase Firestore. ¡Cualquier partida guardada se publicará al instante en la nube!"
                        "Conectando..." -> "Intentando establecer comunicación segura con Firebase. Si no hay conexión o no está configurada, se cargarán los récords de demostración locales."
                        else -> "No se pudo conectar a Firebase Firestore. Mostrando récords de demostración fuera de línea. Los tuyos se seguirán guardando en Room."
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, cardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = cardEmoji,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            Column {
                                Text(
                                    text = cardTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = cardBorderColor
                                )
                                Text(
                                    text = cardDescription,
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // Simulated scores
                    if (filteredGlobalScores.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay records simulados para esta dificultad",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(filteredGlobalScores) { index, item ->
                                ScoreItemRow(rank = index + 1, item = item, isSimulatedGlobal = true)
                            }
                        }
                    }

                    // Architecture expander button
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showArchitectureSpec = !showArchitectureSpec },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1D36)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Especificacion Arquitectonica",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showArchitectureSpec) "OCULTAR DISEÑO DE BACKEND" else "VER DISEÑO DE BACKEND GLOBAL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }
                }
            }
        }
    }

    // Architecture Dialog/Overlay
    if (showArchitectureSpec) {
        AlertDialog(
            onDismissRequest = { showArchitectureSpec = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌐",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Arquitectura de Sincronización Global",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                val specScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .verticalScroll(specScrollState)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Para conectar la persistencia local SQLite (Room) a un ranking mundial, se plantea la siguiente arquitectura desacoplada:",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )

                    Text(
                        text = "1. Capa Local (Offline First)\nEl usuario acumula sus récords en la base de datos Room de manera síncrona. Si el terminal recupera conexión de datos, un Worker en segundo plano (WorkManager) planifica la sincronización.",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "2. API Gateway & Backend Server\nSe implementan endpoints REST/WebSockets en Node.js (Ktor / Go) protegidos por tokens OAuth2 / Firebase Auth:\n" +
                                "• POST /api/scores : Registra un nuevo score con verificación criptográfica para evitar trampas en el cliente.\n" +
                                "• GET /api/leaderboard?difficulty=X : Descarga el TOP 100 global optimizado.",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "3. Base de Datos en la Nube (Firestore / Spanner)\nPara escalar de forma horizontal mundial, se plantea:\n" +
                                "• Google Cloud Spanner: Para consistencia relacional transaccional robusta.\n" +
                                "• Firebase Firestore: Almacenamiento NoSQL ágil de baja latencia con índices compuestos por 'score' y 'difficulty'.",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "• Ventaja: Esto asegura latencia cero durante el juego, manteniendo la tabla de líderes actualizada sin comprometer la velocidad de respuesta del jugador.",
                        fontSize = 11.sp,
                        color = Color(0xFFFF007F),
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showArchitectureSpec = false }) {
                    Text("ENTENDIDO", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF121424),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ScoreItemRow(
    rank: Int,
    item: UserScore,
    isSimulatedGlobal: Boolean = false
) {
    // Beautiful row styling
    val borderBrush = Brush.horizontalGradient(
        colors = when (rank) {
            1 -> listOf(Color(0xFFFFD700), Color(0x33FFD700)) // Gold border
            2 -> listOf(Color(0xFFC0C0C0), Color(0x33C0C0C0)) // Silver border
            3 -> listOf(Color(0xFFCD7F32), Color(0x33CD7F32)) // Bronze border
            else -> listOf(Color(0xFF1E213A), Color(0xFF1E213A))
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_item_$rank"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131528)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderBrush)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank number Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (rank) {
                                1 -> Color(0xFFFFD700)
                                2 -> Color(0xFFC0C0C0)
                                3 -> Color(0xFFCD7F32)
                                else -> Color(0xFF1E2242)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rank in 1..3) Color.Black else Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name and details
                Column {
                    Text(
                        text = item.playerName,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.difficultyPlayed,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Racha ${item.maxStreak}",
                            fontSize = 11.sp,
                            color = Color(0xFFFF007F),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Score with neon glow
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.score} PTS",
                    color = Color(0xFF00E5FF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                
                // Show date
                val dateStr = if (isSimulatedGlobal) {
                    "En línea"
                } else {
                    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                    sdf.format(Date(item.timestamp))
                }
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
