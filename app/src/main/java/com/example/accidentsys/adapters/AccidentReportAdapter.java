package com.example.accidentsys.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accidentsys.R;
import com.example.accidentsys.databinding.ItemAccidentReportBinding;
import com.example.accidentsys.models.AccidentReport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AccidentReportAdapter extends RecyclerView.Adapter<AccidentReportAdapter.AccidentViewHolder> {

    private final List<AccidentReport> accidentReports;
    private final OnAccidentClickListener listener;

    public interface OnAccidentClickListener {
        void onAccidentClick(AccidentReport report);
    }

    public AccidentReportAdapter(List<AccidentReport> accidentReports, OnAccidentClickListener listener) {
        this.accidentReports = accidentReports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AccidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAccidentReportBinding binding = ItemAccidentReportBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AccidentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AccidentViewHolder holder, int position) {
        AccidentReport report = accidentReports.get(position);
        holder.bind(report);
    }

    @Override
    public int getItemCount() {
        return accidentReports.size();
    }

    class AccidentViewHolder extends RecyclerView.ViewHolder {

        private final ItemAccidentReportBinding binding;

        public AccidentViewHolder(@NonNull ItemAccidentReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AccidentReport report) {
            // Format timestamp
            if (report.getTimestamp() != null && !report.getTimestamp().isEmpty()) {
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US);
                    
                    Date date = inputFormat.parse(report.getTimestamp());
                    String formattedDate = outputFormat.format(date != null ? date : new Date());
                    
                    binding.textViewTimestamp.setText(formattedDate);
                } catch (ParseException e) {
                    binding.textViewTimestamp.setText(report.getTimestamp());
                }
            } else {
                binding.textViewTimestamp.setText("Unknown time");
            }

            // Set location
            binding.textViewLocation.setText(report.getLocation() != null ? report.getLocation() : "Unknown location");

            // Set G-force
            binding.textViewGForce.setText(report.getFormattedGForce());

            // Set severity
            binding.textViewSeverity.setText("Severity: " + report.getSeverity());

            // Set status with appropriate color
            binding.textViewStatus.setText(report.getStatus().toUpperCase());
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
            binding.textViewStatus.setBackgroundResource(statusColor);

            // Show alcohol warning if detected
            if (report.isAlcoholDetected()) {
                binding.textViewAlcoholDetected.setVisibility(View.VISIBLE);
            } else {
                binding.textViewAlcoholDetected.setVisibility(View.GONE);
            }

            // Show seatbelt warning if unfastened
            if (!report.isSeatbeltFastened()) {
                binding.textViewSeatbelt.setVisibility(View.VISIBLE);
            } else {
                binding.textViewSeatbelt.setVisibility(View.GONE);
            }

            // Set click listener for details button
            binding.buttonViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAccidentClick(report);
                }
            });
        }
    }
}
