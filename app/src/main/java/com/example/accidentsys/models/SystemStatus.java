package com.example.accidentsys.models;

import java.util.List;
import java.util.Map;

public class SystemStatus {
    private int id;
    private int userId;
    private boolean isActive;
    private String lastUpdated;
    private String deviceId;
    private Map<String, Object> deviceInfo;

    public SystemStatus() {
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Map<String, Object> getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(Map<String, Object> deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    // Helper method to get model from device info
    public String getModel() {
        if (deviceInfo != null && deviceInfo.containsKey("model")) {
            return deviceInfo.get("model").toString();
        }
        return "Unknown";
    }

    // Helper method to get firmware version from device info
    public String getFirmwareVersion() {
        if (deviceInfo != null && deviceInfo.containsKey("firmware_version")) {
            return deviceInfo.get("firmware_version").toString();
        }
        return "Unknown";
    }

    // Helper method to get sensors from device info
    @SuppressWarnings("unchecked")
    public List<String> getSensors() {
        if (deviceInfo != null && deviceInfo.containsKey("sensors")) {
            return (List<String>) deviceInfo.get("sensors");
        }
        return null;
    }

    // Helper method to get sensors as a formatted string
    public String getSensorsString() {
        List<String> sensors = getSensors();
        if (sensors != null && !sensors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sensors.size(); i++) {
                sb.append(sensors.get(i));
                if (i < sensors.size() - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
        return "None";
    }
}
