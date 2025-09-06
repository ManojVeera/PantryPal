package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen; // <-- Important: new import

public class SplashActivity extends AppCompatActivity {

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // This must be called BEFORE super.onCreate()
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // NO MORE setContentView() or Handler needed! The theme handles the view.

        dbHelper = new DBHelper(this);

        // Navigate based on login state
        if (isUserLoggedIn()) {
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        // Finish this activity so the user cannot navigate back to it
        finish();
    }

    private boolean isUserLoggedIn() {
        String loggedInUser = dbHelper.getLoggedInUser();
        return loggedInUser != null && !loggedInUser.isEmpty();
    }
}