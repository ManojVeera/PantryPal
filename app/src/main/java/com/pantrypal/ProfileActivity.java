package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.topAppBarProfile);
        TextView profileName = findViewById(R.id.profileName);
        TextView profileEmail = findViewById(R.id.profileEmail);
        MaterialButton btnMyOrders = findViewById(R.id.btnMyOrders);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        String email = sessionManager.getLoggedInUserEmail();
        // You would typically get the name from the users table, but for now we can do this
        profileEmail.setText(email);
        profileName.setText("Hello!"); // Placeholder

        btnMyOrders.setOnClickListener(v -> {
            // This is where you would open the OrderHistoryActivity in the future
            Toast.makeText(this, "Order History coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
