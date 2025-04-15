package com.example.accidentsys.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.accidentsys.AccidentDetailActivity;
import com.example.accidentsys.adapters.AccidentReportAdapter;
import com.example.accidentsys.api.AccidentReportService;
import com.example.accidentsys.api.ApiClient;
import com.example.accidentsys.databinding.FragmentAccidentListBinding;
import com.example.accidentsys.models.AccidentReport;
import com.example.accidentsys.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccidentListFragment extends Fragment implements AccidentReportAdapter.OnAccidentClickListener {

    private FragmentAccidentListBinding binding;
    private AccidentReportAdapter adapter;
    private List<AccidentReport> accidentReports = new ArrayList<>();
    private List<AccidentReport> filteredReports = new ArrayList<>();
    private SessionManager sessionManager;
    private AccidentReportService accidentReportService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccidentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize session manager and API service
        sessionManager = new SessionManager(requireContext());
        accidentReportService = ApiClient.getClient().create(AccidentReportService.class);

        // Set up RecyclerView
        binding.recyclerViewAccidents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AccidentReportAdapter(filteredReports, this);
        binding.recyclerViewAccidents.setAdapter(adapter);

        // Set up filter and sort spinners
        binding.spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndSortReports();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        binding.spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterAndSortReports();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Fetch accident reports
        fetchAccidentReports();
    }

    public void fetchAccidentReports() {
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Please login to view accident reports", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress bar
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewAccidents.setVisibility(View.GONE);
        binding.textViewNoData.setVisibility(View.GONE);

        String authHeader = "Bearer " + token;
        Call<List<AccidentReport>> call = accidentReportService.getAccidentReports(authHeader);

        call.enqueue(new Callback<List<AccidentReport>>() {
            @Override
            public void onResponse(@NonNull Call<List<AccidentReport>> call, @NonNull Response<List<AccidentReport>> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    accidentReports.clear();
                    accidentReports.addAll(response.body());

                    if (accidentReports.isEmpty()) {
                        binding.textViewNoData.setVisibility(View.VISIBLE);
                        binding.recyclerViewAccidents.setVisibility(View.GONE);
                    } else {
                        binding.textViewNoData.setVisibility(View.GONE);
                        binding.recyclerViewAccidents.setVisibility(View.VISIBLE);
                        filterAndSortReports();
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    binding.textViewNoData.setVisibility(View.VISIBLE);
                    binding.recyclerViewAccidents.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AccidentReport>> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.textViewNoData.setVisibility(View.VISIBLE);
                binding.recyclerViewAccidents.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndSortReports() {
        if (accidentReports.isEmpty()) {
            return;
        }

        // Apply filter
        filteredReports.clear();
        int filterPosition = binding.spinnerFilter.getSelectedItemPosition();

        switch (filterPosition) {
            case 0: // All Reports
                filteredReports.addAll(accidentReports);
                break;
            case 1: // Alcohol Detected
                for (AccidentReport report : accidentReports) {
                    if (report.isAlcoholDetected()) {
                        filteredReports.add(report);
                    }
                }
                break;
            case 2: // Seatbelt Unfastened
                for (AccidentReport report : accidentReports) {
                    if (!report.isSeatbeltFastened()) {
                        filteredReports.add(report);
                    }
                }
                break;
            case 3: // High Severity
                for (AccidentReport report : accidentReports) {
                    if (report.getSeverity().equalsIgnoreCase("high")) {
                        filteredReports.add(report);
                    }
                }
                break;
        }

        // Apply sort
        int sortPosition = binding.spinnerSort.getSelectedItemPosition();

        switch (sortPosition) {
            case 0: // Newest First
                Collections.sort(filteredReports, (a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
                break;
            case 1: // Oldest First
                Collections.sort(filteredReports, Comparator.comparing(AccidentReport::getTimestamp));
                break;
            case 2: // Highest G-Force
                Collections.sort(filteredReports, (a, b) -> Float.compare(b.getGForceMagnitude(), a.getGForceMagnitude()));
                break;
            case 3: // By Status
                Collections.sort(filteredReports, Comparator.comparing(AccidentReport::getStatus));
                break;
        }

        // Update UI
        adapter.notifyDataSetChanged();

        if (filteredReports.isEmpty()) {
            binding.textViewNoData.setVisibility(View.VISIBLE);
            binding.recyclerViewAccidents.setVisibility(View.GONE);
        } else {
            binding.textViewNoData.setVisibility(View.GONE);
            binding.recyclerViewAccidents.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAccidentClick(AccidentReport report) {
        // Open accident detail activity
        Intent intent = new Intent(requireContext(), AccidentDetailActivity.class);
        intent.putExtra("accident_id", report.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
