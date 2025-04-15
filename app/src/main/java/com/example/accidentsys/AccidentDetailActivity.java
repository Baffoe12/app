package com.example.accidentsys;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.accidentsys.api.AccidentReportService;
import com.example.accidentsys.api.ApiClient;
import com.example.accidentsys.databinding.ActivityAccidentDetailBinding;
import com.example.accidentsys.models.AccidentReport;
import com.example.accidentsys.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccidentDetailActivity extends AppCompatActivity {

    private ActivityAccidentDetailBinding binding;
    private SessionManager sessionManager;
    private AccidentReportService accidentReportService;
    private MapView mapView;
    private int accidentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize OSMDroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        
        binding = ActivityAccidentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Accident Details");
        }

        // Initialize session manager and API service
        sessionManager = new SessionManager(this);
        accidentReportService = ApiClient.getClient().create(AccidentReportService.class);

        // Set up map view
        mapView = binding.mapViewDetail;
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);

        // Get accident ID from intent
        accidentId = getIntent().getIntExtra("accident_id", -1);
        if (accidentId == -1) {
            Toast.makeText(this, "Error: Invalid accident report", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch accident details
        fetchAccidentDetails();
    }

    private void fetchAccidentDetails() {
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login to view accident details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show progress bar
        binding.progressBarDetail.setVisibility(View.VISIBLE);

        String authHeader = "Bearer " + token;
        Call<AccidentReport> call = accidentReportService.getAccidentReport(authHeader, accidentId);

        call.enqueue(new Callback<AccidentReport>() {
            @Override
            public void onResponse(@NonNull Call<AccidentReport> call, @NonNull Response<AccidentReport> response) {
                binding.progressBarDetail.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    Toast.makeText(AccidentDetailActivity.this, 
                            "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccidentReport> call, @NonNull Throwable t) {
                binding.progressBarDetail.setVisibility(View.GONE);
                Toast.makeText(AccidentDetailActivity.this, 
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUI(AccidentReport report) {
        // Set title
        binding.textViewDetailTitle.setText("Accident Report #" + report.getId());

        // Format and set timestamp
        if (report.getTimestamp() != null && !report.getTimestamp().isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss", Locale.US);
                
                Date date = inputFormat.parse(report.getTimestamp());
                String formattedDate = outputFormat.format(date != null ? date : new Date());
                
                binding.textViewDetailTimestamp.setText(formattedDate);
            } catch (ParseException e) {
                binding.textViewDetailTimestamp.setText(report.getTimestamp());
            }
        } else {
            binding.textViewDetailTimestamp.setText("Unknown time");
        }

        // Set status with appropriate color
        binding.textViewDetailStatus.setText(report.getStatus().toUpperCase());
        int statusColor;
        switch (report.getStatus().toLowerCase()) {
            case "verified":
                statusColor = R.color.status_verified;
                break;
            case "resolved":
                statusColor = R.color.status_resolved;
                break;
            case "false_alarm":
                statusColor = R.color.status_false_alarm;
                break;
            default: // reported
                statusColor = R.color.status_reported;
                break;
        }
        binding.textViewDetailStatus.setBackgroundResource(statusColor);

        // Set location
        binding.textViewDetailLocation.setText(report.getLocation() != null ? 
                report.getLocation() : "Unknown location");

        // Set map marker if coordinates are available
        if (report.getLatitude() != 0 && report.getLongitude() != 0) {
            GeoPoint point = new GeoPoint(report.getLatitude(), report.getLongitude());
            mapView.getController().setCenter(point);
            
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle("Accident Location");
            
            mapView.getOverlays().clear();
            mapView.getOverlays().add(marker);
            mapView.invalidate();
        }

        // Set sensor data
        binding.textViewDetailGForce.setText(report.getFormattedGForce());
        binding.textViewDetailAccX.setText(String.format(Locale.US, "%.2f G", report.getAccelerationX()));
        binding.textViewDetailAccY.setText(String.format(Locale.US, "%.2f G", report.getAccelerationY()));
        binding.textViewDetailAccZ.setText(String.format(Locale.US, "%.2f G", report.getAccelerationZ()));
        binding.textViewDetailSeverity.setText(report.getSeverity());

        // Set safety flags
        binding.textViewDetailAlcohol.setText(report.isAlcoholDetected() ? "Yes" : "No");
        if (report.isAlcoholDetected()) {
            binding.textViewDetailAlcohol.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        
        binding.textViewDetailSeatbelt.setText(report.isSeatbeltFastened() ? "Yes" : "No");
        if (!report.isSeatbeltFastened()) {
            binding.textViewDetailSeatbelt.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        }

        // Set additional data
        StringBuilder additionalData = new StringBuilder();
        if (report.getAdditionalData() != null && !report.getAdditionalData().isEmpty()) {
            for (Map.Entry<String, Object> entry : report.getAdditionalData().entrySet()) {
                if (!entry.getKey().equals("severity")) { // Skip severity as it's shown separately
                    additionalData.append(entry.getKey())
                            .append(": ")
                            .append(entry.getValue())
                            .append("\n");
                }
            }
        }
        
        if (additionalData.length() > 0) {
            binding.textViewDetailAdditionalData.setText(additionalData.toString());
        } else {
            binding.textViewDetailAdditionalData.setText("No additional data available");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
