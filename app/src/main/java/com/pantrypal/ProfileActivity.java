package com.pantrypal;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class ProfileActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DBHelper dbHelper;

    private EditText profileName, etPhone, etAddress;
    private TextView profileEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        dbHelper = new DBHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.topAppBarProfile);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        Button btnUpdate = findViewById(R.id.btnUpdateProfile);

        MaterialCardView cardMyOrders = findViewById(R.id.cardMyOrders);
        MaterialCardView cardLogout = findViewById(R.id.cardLogout);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        String email = sessionManager.getLoggedInUserEmail();
        if(email == null){
            Toast.makeText(this, "No user logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        profileEmail.setText(email);

        // Load profile data safely
        Cursor cursor = dbHelper.getUserProfile(email);
        if(cursor != null && cursor.moveToFirst()){
            profileName.setText(cursor.getString(0)); // name
            etPhone.setText(cursor.getString(2));     // phone
            etAddress.setText(cursor.getString(3));   // address
        }
        if(cursor != null) cursor.close();

        // Update profile
        btnUpdate.setOnClickListener(v -> {
            String name = profileName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String address = etAddress.getText().toString().trim();

            if(name.isEmpty()){
                profileName.setError("Enter name");
                return;
            }

            if(dbHelper.updateUserProfile(email, name, phone, address)){
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });

        // Card listeners
        cardMyOrders.setOnClickListener(v ->
                Toast.makeText(this, "Order History coming soon!", Toast.LENGTH_SHORT).show()
        );
        cardLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
