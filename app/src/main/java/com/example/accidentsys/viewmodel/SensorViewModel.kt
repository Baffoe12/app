package com.example.accidentsys.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.accidentsys.model.SensorData
import com.example.accidentsys.network.SensorDataService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SensorViewModel : ViewModel() {
    private val _sensorData = MutableLiveData<SensorData>()
    val sensorData: LiveData<SensorData> = _sensorData

    private val _accidentAlert = MutableLiveData<Boolean>()
    val accidentAlert: LiveData<Boolean> = _accidentAlert

    private val sensorService = SensorDataService()

    init {
        startDataCollection()
    }

    private fun startDataCollection() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val data = sensorService.getSensorData()
                    _sensorData.postValue(data)
                    checkForAccident(data)
                } catch (e: Exception) {
                    // Handle error
                }
                kotlinx.coroutines.delay(1000) // Update every second
            }
        }
    }

    private fun checkForAccident(data: SensorData) {
        val isAccident = data.acceleration > 30.0f || data.alcoholLevel > 0.08f
        if (isAccident) {
            _accidentAlert.postValue(true)
        }
    }

    fun sendEmergencyAlert() {
        CoroutineScope(Dispatchers.IO).launch {
            sensorService.sendEmergencyAlert()
        }
    }
} 