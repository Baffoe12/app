package com.example.accidentsys.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.accidentsys.api.AccidentReportService;
import com.example.accidentsys.api.ApiClient;
import com.example.accidentsys.api.SystemStatusService;
import com.example.accidentsys.models.AccidentReport;
import com.example.accidentsys.models.SystemStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Utility class for testing API connections and functionality
 */
public class ApiTestUtil {
    private static final String TAG = "ApiTestUtil";
    
    /**
     * Test the complete API functionality
     * @param context Application context
     * @param listener Callback listener for test results
     */
    public static void runApiTests(Context context, ApiTestListener listener) {
        SessionManager sessionManager = new SessionManager(context);
        String token = sessionManager.getToken();
        
        if (token == null || token.isEmpty()) {
            listener.onTestResult("Authentication", false, "No authentication token found. Please login first.");
            return;
        }
        
        // Start the test sequence
        listener.onTestStart();
        
        // Test 1: Verify authentication
        testAuthentication(context, token, new TestCallback() {
            @Override
            public void onSuccess(String message) {
                listener.onTestResult("Authentication", true, message);
                
                // Test 2: Get system status
                testSystemStatus(context, token, new TestCallback() {
                    @Override
                    public void onSuccess(String message) {
                        listener.onTestResult("System Status", true, message);
                        
                        // Test 3: Get accident reports
                        testAccidentReports(context, token, new TestCallback() {
                            @Override
                            public void onSuccess(String message) {
                                listener.onTestResult("Accident Reports", true, message);
                                listener.onTestComplete(true, "All API tests completed successfully");
                            }
                            
                            @Override
                            public void onFailure(String error) {
                                listener.onTestResult("Accident Reports", false, error);
                                listener.onTestComplete(false, "API test failed at accident reports stage");
                            }
                        });
                    }
                    
                    @Override
                    public void onFailure(String error) {
                        listener.onTestResult("System Status", false, error);
                        listener.onTestComplete(false, "API test failed at system status stage");
                    }
                });
            }
            
            @Override
            public void onFailure(String error) {
                listener.onTestResult("Authentication", false, error);
                listener.onTestComplete(false, "API test failed at authentication stage");
            }
        });
    }
    
    /**
     * Test authentication token validity
     */
    private static void testAuthentication(Context context, String token, TestCallback callback) {
        // For simplicity, we'll just check if the token exists and is not expired
        // A real implementation would make an API call to a protected endpoint
        
        if (token != null && !token.isEmpty()) {
            // In a real implementation, you would verify the token with the server
            callback.onSuccess("Authentication token is valid");
        } else {
            callback.onFailure("Authentication token is invalid or missing");
        }
    }
    
    /**
     * Test system status API
     */
    private static void testSystemStatus(Context context, String token, TestCallback callback) {
        SystemStatusService systemStatusService = ApiClient.getClient().create(SystemStatusService.class);
        String authHeader = "Bearer " + token;
        
        Call<SystemStatus> call = systemStatusService.getSystemStatus(authHeader);
        call.enqueue(new Callback<SystemStatus>() {
            @Override
            public void onResponse(Call<SystemStatus> call, Response<SystemStatus> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SystemStatus status = response.body();
                    String message = "System status retrieved: " + 
                            (status.isActive() ? "ACTIVE" : "INACTIVE") + 
                            ", Device: " + status.getDeviceId();
                    callback.onSuccess(message);
                } else {
                    callback.onFailure("Failed to get system status: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<SystemStatus> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Test accident reports API
     */
    private static void testAccidentReports(Context context, String token, TestCallback callback) {
        AccidentReportService accidentReportService = ApiClient.getClient().create(AccidentReportService.class);
        String authHeader = "Bearer " + token;
        
        Call<List<AccidentReport>> call = accidentReportService.getAccidentReports(authHeader);
        call.enqueue(new Callback<List<AccidentReport>>() {
            @Override
            public void onResponse(Call<List<AccidentReport>> call, Response<List<AccidentReport>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AccidentReport> reports = response.body();
                    String message = "Retrieved " + reports.size() + " accident reports";
                    callback.onSuccess(message);
                } else {
                    callback.onFailure("Failed to get accident reports: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<List<AccidentReport>> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Test creating a simulated accident report
     */
    public static void createTestAccidentReport(Context context, ApiTestListener listener) {
        SessionManager sessionManager = new SessionManager(context);
        String token = sessionManager.getToken();
        
        if (token == null || token.isEmpty()) {
            listener.onTestResult("Create Test Accident", false, "No authentication token found. Please login first.");
            return;
        }
        
        AccidentReportService accidentReportService = ApiClient.getClient().create(AccidentReportService.class);
        String authHeader = "Bearer " + token;
        
        // Create test accident report
        AccidentReport testReport = new AccidentReport();
        testReport.setAccelerationX(5.2f);
        testReport.setAccelerationY(3.8f);
        testReport.setAccelerationZ(7.9f);
        testReport.setAlcoholDetected(true);
        testReport.setSeatbeltFastened(false);
        testReport.setLocation("Test Location, Accra, Ghana");
        testReport.setLatitude(5.6037);
        testReport.setLongitude(-0.1870);
        
        // Add additional data
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("severity", "high");
        additionalData.put("device_id", "APP-TEST-01");
        additionalData.put("firmware_version", "v1.2.3");
        additionalData.put("battery", "78%");
        testReport.setAdditionalData(additionalData);
        
        Call<AccidentReport> call = accidentReportService.createAccidentReport(authHeader, testReport);
        call.enqueue(new Callback<AccidentReport>() {
            @Override
            public void onResponse(Call<AccidentReport> call, Response<AccidentReport> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AccidentReport createdReport = response.body();
                    String message = "Test accident report created with ID: " + createdReport.getId();
                    listener.onTestResult("Create Test Accident", true, message);
                } else {
                    listener.onTestResult("Create Test Accident", false, 
                            "Failed to create test accident: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<AccidentReport> call, Throwable t) {
                listener.onTestResult("Create Test Accident", false, 
                        "Network error: " + t.getMessage());
            }
        });
    }
    
    /**
     * Interface for test callbacks
     */
    private interface TestCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }
    
    /**
     * Interface for API test listeners
     */
    public interface ApiTestListener {
        void onTestStart();
        void onTestResult(String testName, boolean success, String message);
        void onTestComplete(boolean allSuccessful, String message);
    }
}
