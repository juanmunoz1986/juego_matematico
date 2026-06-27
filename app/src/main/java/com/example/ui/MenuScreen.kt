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
    val ownerName by viewModel.ownerName.collectAsState()
    val isOnlineMode by viewModel.isOnlineMode.collectAsState()
    
    var inputOwnerName by remember(ownerName) { mutableStateOf(if (ownerName == "Propietario") "" else ownerName) }
    var inputPlayerName by remember(playerName) { mutableStateOf(if (playerName == "Invitado") "" else playerName) }
    
    val scrollState = rememberScrollState()

    var showConfigDialog by remember { mutableStateOf(false) }
    val defaultTime = selectedDiff.initialTimeSec
    var customTime by remember(showConfigDialog) { mutableStateOf(defaultTime.toFloat()) }
    var isDynamicReduction by remember(showConfigDialog) { mutableStateOf(selectedDiff == DifficultyLevel.SUPER_PRO) }

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

            // Connection / Online/Offline Mode Switcher
            val isOnlineMode by viewModel.isOnlineMode.collectAsState()
            val isSyncing by viewModel.isSyncing.collectAsState()
            val unsyncedCount by viewModel.unsyncedCount.collectAsState()
            val syncStatus by viewModel.firestoreSyncStatus.collectAsState()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131525)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2C315E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MODO DE JUEGO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )

                        // Status badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isOnlineMode) {
                                        if (syncStatus == "Conectado") Color(0x2200FF66) else Color(0x22FFB300)
                                    } else {
                                        Color(0x22888888)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isOnlineMode) syncStatus.uppercase() else "OFFLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnlineMode) {
                                    if (syncStatus == "Conectado") Color(0xFF00FF66) else Color(0xFFFFB300)
                                } else {
                                    Color.Gray
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab selector row for offline/online
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0C0D1A))
                            .border(BorderStroke(1.dp, Color(0xFF1E2242)), RoundedCornerShape(20.dp))
                    ) {
                        // Offline Selector Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { viewModel.setOnlineMode(false) }
                                .background(if (!isOnlineMode) Color(0xFF1F2445) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💾", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "OFFLINE (Local)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isOnlineMode) Color.White else Color.Gray
                                )
                            }
                        }

                        // Online Selector Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { viewModel.setOnlineMode(true) }
                                .background(if (isOnlineMode) Color(0xFF1F2445) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("☁️", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ONLINE (Nube)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOnlineMode) Color.White else Color.Gray
                                )
                            }
                        }
                    }

                    // Sync panel if we have unsynced scores
                    if (unsyncedCount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider(color = Color(0xFF1E2242), thickness = 1.dp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$unsyncedCount partida(s) sin subir",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF007F)
                                )
                                Text(
                                    text = "Tus récords se guardaron sin conexión.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Button(
                                onClick = { viewModel.syncOfflineScores() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1F3A)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color(0xFF00E5FF),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Subir", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("🔄", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
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

            if (isOnlineMode) {
                // Online mode: edit the owner name
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = inputOwnerName,
                        onValueChange = {
                            inputOwnerName = it
                            viewModel.updateOwnerName(it.ifBlank { "Propietario" })
                        },
                        placeholder = { Text("Nombre del Propietario...", color = Color.Gray) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_input_owner"),
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
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🔐 Nombre del dueño del celular. Récords se subirán a la nube con este nombre.",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            } else {
                // Offline mode: edit player name freely
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = inputPlayerName,
                        onValueChange = {
                            inputPlayerName = it
                            viewModel.setPlayerName(it.ifBlank { "Invitado" })
                        },
                        placeholder = { Text("Introduce tu nombre...", color = Color.Gray) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_input_player"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFF007F),
                            unfocusedBorderColor = Color(0xFF2C315E),
                            focusedContainerColor = Color(0xFF131524),
                            unfocusedContainerColor = Color(0xFF131524),
                            cursorColor = Color(0xFFFF007F)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🎮 Modo rotativo local. Los récords offline solo se guardan en el celular.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

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
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
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
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Play Button with Neon Glow
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (isOnlineMode) {
                        if (inputOwnerName.isBlank()) {
                            viewModel.updateOwnerName("Propietario")
                        }
                    } else {
                        if (inputPlayerName.isBlank()) {
                            viewModel.setPlayerName("Invitado")
                        }
                    }
                    showConfigDialog = true
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

        if (showConfigDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showConfigDialog = false }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131525)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFFFF007F))))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚙️",
                            fontSize = 32.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "AJUSTES DE TIEMPO",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Ajusta las reglas de tiempo para el nivel ${selectedDiff.displayName}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tiempo por pregunta",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${customTime.toInt()}s",
                                color = Color(0xFF00E5FF),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = customTime,
                            onValueChange = { customTime = it },
                            valueRange = 5f..60f,
                            steps = 10,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("time_slider"),
                            colors = SliderDefaults.colors(
                                activeTrackColor = Color(0xFF00E5FF),
                                inactiveTrackColor = Color(0xFF1F223D),
                                thumbColor = Color(0xFF00E5FF)
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "MODO DE CRONÓMETRO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDynamicReduction = false }
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (!isDynamicReduction) Color(0xFF1E213A) else Color(0xFF131524)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (!isDynamicReduction) 1.5.dp else 1.dp,
                                color = if (!isDynamicReduction) Color(0xFF00E5FF) else Color(0xFF1F223D)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = !isDynamicReduction,
                                    onClick = { isDynamicReduction = false },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFF00E5FF),
                                        unselectedColor = Color.Gray
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Modo Normal",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "El tiempo por pregunta siempre es fijo.",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDynamicReduction = true }
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDynamicReduction) Color(0xFF1E213A) else Color(0xFF131524)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (isDynamicReduction) 1.5.dp else 1.dp,
                                color = if (isDynamicReduction) Color(0xFFFF007F) else Color(0xFF1F223D)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isDynamicReduction,
                                    onClick = { isDynamicReduction = true },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Color(0xFFFF007F),
                                        unselectedColor = Color.Gray
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Modo Reducción",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "El tiempo disminuye a medida que avanzas.",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showConfigDialog = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                                border = BorderStroke(1.dp, Color(0xFF2C315E)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("CANCELAR", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.setGameTimerConfig(customTime.toInt(), isDynamicReduction)
                                    showConfigDialog = false
                                    onStartGame()
                                },
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1530)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFFFF007F)))
                                )
                            ) {
                                Text("¡COMENZAR!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
