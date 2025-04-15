package com.example.accidentsys.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SessionManager {
    private static final String TAG = "SessionManager";
    
    // Shared preferences file name
    private static final String PREF_NAME = "AccidentSysPrefs";
    
    // Shared preferences keys
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    
    // Shared Preferences
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    
    // Constructor
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }
    
    /**
     * Save user login session
     */
    public void saveSession(String token, int userId, String username) {
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
        
        Log.d(TAG, "Session saved for user: " + username);
    }
    
    /**
     * Get stored session token
     */
    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }
    
    /**
     * Get stored user ID
     */
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }
    
    /**
     * Get stored username
     */
    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return getToken() != null;
    }
    
    /**
     * Clear session details
     */
    public void logout() {
        editor.clear();
        editor.apply();
        
        Log.d(TAG, "Session cleared, user logged out");
    }
}
