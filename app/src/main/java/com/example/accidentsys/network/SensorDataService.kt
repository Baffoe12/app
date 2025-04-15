package com.example.accidentsys.network

import com.example.accidentsys.model.SensorData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class SensorDataService {
    private val baseUrl = "https://accident-detection-api.onrender.com"

    suspend fun getSensorData(): SensorData = withContext(Dispatchers.IO) {
        try {
            val response = URL("$baseUrl/sensor-data").readText()
            val json = JSONObject(response)
            
            SensorData(
                heartRate = json.getDouble("heartRate").toFloat(),
                spo2 = json.getDouble("spo2").toFloat(),
                alcoholLevel = json.getDouble("alcohol").toFloat(),
                acceleration = json.getDouble("acceleration").toFloat(),
                seatbeltWorn = json.getBoolean("seatbelt"),
                engineRunning = json.getBoolean("engine")
            )
        } catch (e: Exception) {
            // Return default values if there's an error
            SensorData()
        }
    }

    suspend fun sendEmergencyAlert() = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("alert", "Emergency Alert")
                put("type", "manual")
            }
            
            URL("$baseUrl/alert").openConnection().apply {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                outputStream.write(json.toString().toByteArray())
            }.getInputStream().readBytes()
        } catch (e: Exception) {
            // Handle error
        }
    }
} 