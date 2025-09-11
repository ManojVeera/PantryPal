// In SessionManager.java
package com.pantrypal;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;

    private static final String PREF_NAME = "PantryPalUserSession";
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_USER_TYPE = "userType"; // --- NEW: To store user type

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // --- MODIFIED: Now accepts user type ---
    public void createLoginSession(String email, String userType) {
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_USER_TYPE, userType); // --- NEW: Save the user type
        editor.commit();
    }

    public String getLoggedInUserEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    // --- NEW: Method to get the user type ---
    public String getUserType() {
        return pref.getString(KEY_USER_TYPE, null);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGGED_IN, false);
    }

    // --- MODIFIED: No longer handles redirect. It only clears the data. ---
    public void logoutUser() {
        editor.clear();
        editor.commit();
    }
}