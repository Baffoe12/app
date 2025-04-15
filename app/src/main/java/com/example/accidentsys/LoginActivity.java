package com.example.accidentsys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.accidentsys.api.ApiClient;
import com.example.accidentsys.databinding.ActivityLoginBinding;
import com.example.accidentsys.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    // API interface for login
    interface LoginService {
        @POST("login")
        Call<ResponseBody> loginUser(@Body LoginRequest loginRequest);
    }

    // Login request model
    static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize views
        editTextUsername = binding.editTextUsername;
        editTextPassword = binding.editTextPassword;
        buttonLogin = binding.buttonLogin;
        progressBar = binding.progressBar;

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // User is already logged in, go to main activity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // Set up login button
        buttonLogin.setOnClickListener(v -> attemptLogin());

        // Set up test credentials button
        binding.buttonTestCredentials.setOnClickListener(v -> {
            editTextUsername.setText("test_user");
            editTextPassword.setText("test_password");
        });
    }

    private void attemptLogin() {
        // Get username and password
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate input
        if (username.isEmpty()) {
            editTextUsername.setError("Username is required");
            editTextUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        buttonLogin.setEnabled(false);

        // Create login service
        LoginService loginService = ApiClient.getClient().create(LoginService.class);

        // Create login request
        LoginRequest loginRequest = new LoginRequest(username, password);

        // Make login request
        Call<ResponseBody> call = loginService.loginUser(loginRequest);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                buttonLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Parse response
                        String responseString = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseString);
                        String token = jsonObject.getString("token");
                        int userId = jsonObject.getInt("user_id");

                        // Save session
                        sessionManager.saveSession(token, userId, username);

                        // Show success message
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        // Go to main activity
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } catch (IOException | JSONException e) {
                        Toast.makeText(LoginActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show error message
                    String errorMessage = "Login failed";
                    if (response.code() == 401) {
                        errorMessage = "Invalid username or password";
                    } else if (response.code() == 500) {
                        errorMessage = "Server error";
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                buttonLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
