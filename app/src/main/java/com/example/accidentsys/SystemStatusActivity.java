package com.example.accidentsys;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.accidentsys.api.ApiClient;
import com.example.accidentsys.api.SystemStatusService;
import com.example.accidentsys.models.SystemStatus;
import com.example.accidentsys.utils.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SystemStatusActivity extends AppCompatActivity {

    private TextView textViewStatusValue;
    private TextView textViewLastUpdated;
    private TextView textViewDeviceId;
    private TextView textViewDeviceModel;
    private TextView textViewFirmwareVersion;
    private TextView textViewSensors;
    private ImageView imageViewStatusIcon;
    private Button buttonRefresh;
    
    private SessionManager sessionManager;
    private SystemStatusService systemStatusService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_status);
        
        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("System Status");
        }

        // Initialize views
        textViewStatusValue = findViewById(R.id.textViewStatusValue);
        textViewLastUpdated = findViewById(R.id.textViewLastUpdated);
        textViewDeviceId = findViewById(R.id.textViewDeviceId);
        textViewDeviceModel = findViewById(R.id.textViewDeviceModel);
        textViewFirmwareVersion = findViewById(R.id.textViewFirmwareVersion);
        textViewSensors = findViewById(R.id.textViewSensors);
        imageViewStatusIcon = findViewById(R.id.imageViewStatusIcon);
        buttonRefresh = findViewById(R.id.buttonRefresh);

        // Initialize session manager and API service
        sessionManager = new SessionManager(this);
        systemStatusService = ApiClient.getClient().create(SystemStatusService.class);

        // Set up refresh button
        buttonRefresh.setOnClickListener(v -> fetchSystemStatus());

        // Fetch system status on activity creation
        fetchSystemStatus();
    }

    private void fetchSystemStatus() {
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login to view system status", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = "Bearer " + token;
        Call<SystemStatus> call = systemStatusService.getSystemStatus(authHeader);

        call.enqueue(new Callback<SystemStatus>() {
            @Override
            public void onResponse(@NonNull Call<SystemStatus> call, @NonNull Response<SystemStatus> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else if (response.code() == 404) {
                    // No system status found for user
                    showNoSystemStatusFound();
                } else {
                    Toast.makeText(SystemStatusActivity.this, 
                            "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SystemStatus> call, @NonNull Throwable t) {
                Toast.makeText(SystemStatusActivity.this, 
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(SystemStatus status) {
        // Update status value and icon
        if (status.isActive()) {
            textViewStatusValue.setText("ACTIVE");
            textViewStatusValue.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            imageViewStatusIcon.setImageResource(R.drawable.ic_status_active);
        } else {
            textViewStatusValue.setText("INACTIVE");
            textViewStatusValue.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            imageViewStatusIcon.setImageResource(R.drawable.ic_status_inactive);
        }

        // Update last updated time
        if (status.getLastUpdated() != null && !status.getLastUpdated().isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US);
                
                Date date = inputFormat.parse(status.getLastUpdated());
                String formattedDate = outputFormat.format(date != null ? date : new Date());
                
                textViewLastUpdated.setText("Last updated: " + formattedDate);
            } catch (ParseException e) {
                textViewLastUpdated.setText("Last updated: " + status.getLastUpdated());
            }
        } else {
            textViewLastUpdated.setText("Last updated: Unknown");
        }

        // Update device information
        textViewDeviceId.setText(status.getDeviceId() != null ? status.getDeviceId() : "Unknown");
        textViewDeviceModel.setText(status.getModel());
        textViewFirmwareVersion.setText(status.getFirmwareVersion());
        textViewSensors.setText(status.getSensorsString());
    }

    private void showNoSystemStatusFound() {
        textViewStatusValue.setText("NOT CONNECTED");
        textViewStatusValue.setTextColor(getResources().getColor(android.R.color.darker_gray));
        imageViewStatusIcon.setImageResource(R.drawable.ic_status_inactive);
        textViewLastUpdated.setText("Last updated: Never");
        textViewDeviceId.setText("No device connected");
        textViewDeviceModel.setText("Unknown");
        textViewFirmwareVersion.setText("Unknown");
        textViewSensors.setText("None");
        
        Toast.makeText(this, "No system status found. Connect your device first.", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
