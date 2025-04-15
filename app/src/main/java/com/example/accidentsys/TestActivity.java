package com.example.accidentsys;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.accidentsys.databinding.ActivityTestBinding;
import com.example.accidentsys.utils.ApiTestUtil;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TestActivity extends AppCompatActivity {

    private ActivityTestBinding binding;
    private TextView textViewResults;
    private Button buttonRunTests;
    private Button buttonCreateAccident;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("System Tests");
        }

        // Initialize views
        textViewResults = binding.textViewTestResults;
        buttonRunTests = binding.buttonRunTests;
        buttonCreateAccident = binding.buttonCreateAccident;
        scrollView = binding.scrollView;

        // Set up test button
        buttonRunTests.setOnClickListener(v -> runApiTests());

        // Set up create accident button
        buttonCreateAccident.setOnClickListener(v -> createTestAccident());

        // Set up FCM token button
        binding.buttonGetFcmToken.setOnClickListener(v -> getFcmToken());

        // Initial log message
        appendToLog("Test console initialized\n");
        appendToLog("Ready to run tests\n");
    }

    private void runApiTests() {
        buttonRunTests.setEnabled(false);
        appendToLog("\n===== STARTING API TESTS =====\n");

        ApiTestUtil.runApiTests(this, new ApiTestUtil.ApiTestListener() {
            @Override
            public void onTestStart() {
                appendToLog("API tests started at " + getCurrentTime() + "\n");
            }

            @Override
            public void onTestResult(String testName, boolean success, String message) {
                String result = success ? "✅ PASS" : "❌ FAIL";
                appendToLog(testName + ": " + result + "\n");
                appendToLog("  " + message + "\n");
            }

            @Override
            public void onTestComplete(boolean allSuccessful, String message) {
                appendToLog("\nTest summary: " + message + "\n");
                appendToLog("Tests completed at " + getCurrentTime() + "\n");
                appendToLog("===== API TESTS COMPLETED =====\n");
                runOnUiThread(() -> buttonRunTests.setEnabled(true));
            }
        });
    }

    private void createTestAccident() {
        buttonCreateAccident.setEnabled(false);
        appendToLog("\n===== CREATING TEST ACCIDENT =====\n");

        ApiTestUtil.createTestAccidentReport(this, new ApiTestUtil.ApiTestListener() {
            @Override
            public void onTestStart() {
                // Not used for this test
            }

            @Override
            public void onTestResult(String testName, boolean success, String message) {
                String result = success ? "✅ PASS" : "❌ FAIL";
                appendToLog(testName + ": " + result + "\n");
                appendToLog("  " + message + "\n");
            }

            @Override
            public void onTestComplete(boolean allSuccessful, String message) {
                // Not used for this test
                runOnUiThread(() -> buttonCreateAccident.setEnabled(true));
            }
        });
    }

    private void getFcmToken() {
        binding.buttonGetFcmToken.setEnabled(false);
        appendToLog("\n===== RETRIEVING FCM TOKEN =====\n");

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        appendToLog("❌ FAIL: Fetching FCM registration token failed\n");
                        appendToLog("  " + task.getException() + "\n");
                        binding.buttonGetFcmToken.setEnabled(true);
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    appendToLog("✅ PASS: FCM Token retrieved\n");
                    appendToLog("  Token: " + token + "\n");
                    appendToLog("  (You can use this token to send test notifications from Firebase console)\n");
                    binding.buttonGetFcmToken.setEnabled(true);
                });
    }

    private void appendToLog(String message) {
        runOnUiThread(() -> {
            textViewResults.append(message);
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        });
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
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
