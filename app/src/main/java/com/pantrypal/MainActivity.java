package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // --- MODIFIED: Use SessionManager instead of DBHelper ---
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- MODIFIED: Initialize SessionManager ---
        sessionManager = new SessionManager(this);

        // --- MODIFIED: All logic now uses SessionManager ---
        if (sessionManager.isLoggedIn()) {
            String userType = sessionManager.getUserType();
            if ("seller".equals(userType)) {
                startActivity(new Intent(this, SellerActivity.class));
            } else {
                startActivity(new Intent(this, HomeActivity.class));
            }
        } else {
            // If no one is logged in, default to the customer sign-up/login flow.
            startActivity(new Intent(this, SignUpActivity.class));
        }
        finish(); // Finish this activity so the user can't navigate back to it.
    }
}
