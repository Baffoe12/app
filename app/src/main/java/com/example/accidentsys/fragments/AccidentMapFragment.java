package com.example.accidentsys.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.accidentsys.AccidentDetailActivity;
import com.example.accidentsys.R;
import com.example.accidentsys.api.AccidentReportService;
import com.example.accidentsys.api.ApiClient;
import com.example.accidentsys.databinding.FragmentAccidentMapBinding;
import com.example.accidentsys.models.AccidentReport;
import com.example.accidentsys.utils.SessionManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccidentMapFragment extends Fragment {

    private FragmentAccidentMapBinding binding;
    private MapView mapView;
    private SessionManager sessionManager;
    private AccidentReportService accidentReportService;
    private List<AccidentReport> accidentReports = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Initialize OSMDroid configuration
        Context ctx = requireActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        
        binding = FragmentAccidentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize session manager and API service
        sessionManager = new SessionManager(requireContext());
        accidentReportService = ApiClient.getClient().create(AccidentReportService.class);

        // Set up map view
        mapView = binding.mapView;
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(10.0);
        
        // Default location (Ghana)
        GeoPoint startPoint = new GeoPoint(5.6037, -0.1870);
        mapView.getController().setCenter(startPoint);

        // Set up my location button
        binding.fabMyLocation.setOnClickListener(v -> {
            // Center map on Ghana
            mapView.getController().animateTo(startPoint);
            mapView.getController().setZoom(10.0);
        });

        // Fetch accident reports
        fetchAccidentReports();
    }

    public void fetchAccidentReports() {
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Please login to view accident locations", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar
        binding.mapProgressBar.setVisibility(View.VISIBLE);
        binding.textViewMapNoData.setVisibility(View.GONE);

        String authHeader = "Bearer " + token;
        Call<List<AccidentReport>> call = accidentReportService.getAccidentReports(authHeader);

        call.enqueue(new Callback<List<AccidentReport>>() {
            @Override
            public void onResponse(@NonNull Call<List<AccidentReport>> call, @NonNull Response<List<AccidentReport>> response) {
                binding.mapProgressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    accidentReports.clear();
                    accidentReports.addAll(response.body());

                    if (accidentReports.isEmpty()) {
                        binding.textViewMapNoData.setVisibility(View.VISIBLE);
                    } else {
                        binding.textViewMapNoData.setVisibility(View.GONE);
                        updateMapMarkers();
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    binding.textViewMapNoData.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AccidentReport>> call, @NonNull Throwable t) {
                binding.mapProgressBar.setVisibility(View.GONE);
                binding.textViewMapNoData.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMapMarkers() {
        // Clear existing markers
        mapView.getOverlays().clear();
        
        boolean hasValidLocation = false;

        // Add markers for each accident report with valid coordinates
        for (AccidentReport report : accidentReports) {
            if (report.getLatitude() != 0 && report.getLongitude() != 0) {
                hasValidLocation = true;
                
                // Create marker
                Marker marker = new Marker(mapView);
                marker.setPosition(new GeoPoint(report.getLatitude(), report.getLongitude()));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                
                // Set marker title and snippet
                marker.setTitle("Accident Report #" + report.getId());
                marker.setSnippet(report.getFormattedGForce() + " | " + report.getTimestamp());
                
                // Set marker icon based on severity
                Drawable icon;
                if (report.getSeverity().equalsIgnoreCase("high")) {
                    icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_dialog_alert);
                } else if (report.getSeverity().equalsIgnoreCase("medium")) {
                    icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_dialog_info);
                } else {
                    icon = ContextCompat.getDrawable(requireContext(), android.R.drawable.ic_menu_info_details);
                }
                marker.setIcon(icon);
                
                // Set marker click listener
                final int reportId = report.getId();
                marker.setOnMarkerClickListener((marker1, mapView) -> {
                    Intent intent = new Intent(requireContext(), AccidentDetailActivity.class);
                    intent.putExtra("accident_id", reportId);
                    startActivity(intent);
                    return true;
                });
                
                // Add marker to map
                mapView.getOverlays().add(marker);
            }
        }
        
        // If no valid locations found, show message
        if (!hasValidLocation) {
            binding.textViewMapNoData.setVisibility(View.VISIBLE);
        } else {
            binding.textViewMapNoData.setVisibility(View.GONE);
            
            // If we have at least one valid location, try to center the map on the first one
            for (AccidentReport report : accidentReports) {
                if (report.getLatitude() != 0 && report.getLongitude() != 0) {
                    mapView.getController().animateTo(new GeoPoint(report.getLatitude(), report.getLongitude()));
                    break;
                }
            }
        }
        
        // Refresh map
        mapView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
