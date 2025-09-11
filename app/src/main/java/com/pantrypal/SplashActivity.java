// In SplashActivity.java
package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends AppCompatActivity {

    private SessionManager sessionManager; // --- NEW

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this); // --- NEW

        // --- MODIFIED: All logic now uses SessionManager ---
        if (sessionManager.isLoggedIn()) {
            String userType = sessionManager.getUserType();
            if ("seller".equals(userType)) {
                startActivity(new Intent(this, SellerActivity.class));
            } else {
                startActivity(new Intent(this, HomeActivity.class));
            }
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}