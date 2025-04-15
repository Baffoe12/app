package com.example.accidentsys

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.accidentsys.viewmodel.SensorViewModel

class SensorMonitorActivity : AppCompatActivity() {
    private lateinit var viewModel: SensorViewModel
    private lateinit var heartRateText: TextView
    private lateinit var spo2Text: TextView
    private lateinit var alcoholText: TextView
    private lateinit var accelerationText: TextView
    private lateinit var seatbeltText: TextView
    private lateinit var engineText: TextView
    private lateinit var emergencyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[SensorViewModel::class.java]

        // Initialize UI components
        heartRateText = findViewById(R.id.heartRateText)
        spo2Text = findViewById(R.id.spo2Text)
        alcoholText = findViewById(R.id.alcoholText)
        accelerationText = findViewById(R.id.accelerationText)
        seatbeltText = findViewById(R.id.seatbeltText)
        engineText = findViewById(R.id.engineText)
        emergencyButton = findViewById(R.id.emergencyButton)

        // Set up emergency button click listener
        emergencyButton.setOnClickListener {
            showEmergencyConfirmationDialog()
        }

        // Observe sensor data changes
        viewModel.sensorData.observe(this) { data ->
            updateUI(data)
        }

        // Observe accident alerts
        viewModel.accidentAlert.observe(this) { alert ->
            if (alert) {
                showAccidentAlert()
            }
        }
    }

    private fun updateUI(data: SensorData) {
        heartRateText.text = "Heart Rate: ${data.heartRate} BPM"
        spo2Text.text = "SpO2: ${data.spo2}%"
        alcoholText.text = "Alcohol Level: ${data.alcoholLevel}"
        accelerationText.text = "Acceleration: ${data.acceleration} m/s²"
        seatbeltText.text = "Seatbelt: ${if (data.seatbeltWorn) "Worn" else "Not Worn"}"
        engineText.text = "Engine: ${if (data.engineRunning) "Running" else "Off"}"
    }

    private fun showAccidentAlert() {
        AlertDialog.Builder(this)
            .setTitle("Accident Detected!")
            .setMessage("An accident has been detected. Emergency services have been notified.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showEmergencyConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Emergency Alert")
            .setMessage("Are you sure you want to send an emergency alert?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.sendEmergencyAlert()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}