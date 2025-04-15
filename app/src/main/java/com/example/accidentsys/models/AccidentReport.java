package com.example.accidentsys.models;

import java.util.Map;

public class AccidentReport {
    private int id;
    private int userId;
    private String timestamp;
    private float accelerationX;
    private float accelerationY;
    private float accelerationZ;
    private boolean alcoholDetected;
    private boolean seatbeltFastened;
    private Map<String, Object> additionalData;
    private String location;
    private double latitude;
    private double longitude;
    private String status; // e.g., "reported", "verified", "resolved"

    public AccidentReport() {
        // Required empty constructor
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public float getAccelerationX() {
        return accelerationX;
    }

    public void setAccelerationX(float accelerationX) {
        this.accelerationX = accelerationX;
    }

    public float getAccelerationY() {
        return accelerationY;
    }

    public void setAccelerationY(float accelerationY) {
        this.accelerationY = accelerationY;
    }

    public float getAccelerationZ() {
        return accelerationZ;
    }

    public void setAccelerationZ(float accelerationZ) {
        this.accelerationZ = accelerationZ;
    }

    public boolean isAlcoholDetected() {
        return alcoholDetected;
    }

    public void setAlcoholDetected(boolean alcoholDetected) {
        this.alcoholDetected = alcoholDetected;
    }

    public boolean isSeatbeltFastened() {
        return seatbeltFastened;
    }

    public void setSeatbeltFastened(boolean seatbeltFastened) {
        this.seatbeltFastened = seatbeltFastened;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Helper method to get severity from additional data
    public String getSeverity() {
        if (additionalData != null && additionalData.containsKey("severity")) {
            return additionalData.get("severity").toString();
        }
        return "Unknown";
    }

    // Helper method to get total G-force magnitude
    public float getGForceMagnitude() {
        return (float) Math.sqrt(
                accelerationX * accelerationX +
                accelerationY * accelerationY +
                accelerationZ * accelerationZ);
    }

    // Helper method to get formatted G-force magnitude with 2 decimal places
    public String getFormattedGForce() {
        return String.format("%.2f G", getGForceMagnitude());
    }
}
