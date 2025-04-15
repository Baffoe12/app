package com.example.accidentsys.model

data class SensorData(
    val heartRate: Float = 0f,
    val spo2: Float = 0f,
    val alcoholLevel: Float = 0f,
    val acceleration: Float = 0f,
    val seatbeltWorn: Boolean = false,
    val engineRunning: Boolean = false
) 