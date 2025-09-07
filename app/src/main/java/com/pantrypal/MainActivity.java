package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            startActivity(new Intent(this, SignUpActivity.class));
        }
        finish();
    }
}