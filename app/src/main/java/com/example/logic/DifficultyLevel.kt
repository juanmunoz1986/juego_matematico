package com.example.logic

enum class DifficultyLevel(
    val displayName: String,
    val initialTimeSec: Int,
    val basePoints: Int,
    val termsCount: Int
) {
    BAJO("Bajo", 15, 10, 2),
    MEDIO("Medio", 25, 25, 3),
    ALTO("Alto", 20, 50, 4),
    EXPERTO("Experto", 20, 100, 5),
    SUPER_PRO("Super Pro", 25, 150, 5)
}
