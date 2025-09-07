package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends AppCompatActivity {

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        dbHelper = new DBHelper(this);

        String loggedInEmail = dbHelper.getLoggedInUser();
        if (loggedInEmail != null) {
            String type = dbHelper.getUserType(loggedInEmail);
            if ("seller".equals(type)) {
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