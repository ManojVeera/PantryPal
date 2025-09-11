package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editLoginEmail, editLoginPassword;
    private MaterialButton btnLogin;
    private TextView txtSignUpRedirect, txtSellerLoginRedirect;

    private DBHelper dbHelper;
    // --- NEW: Add SessionManager ---
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);
        // --- NEW: Initialize SessionManager ---
        sessionManager = new SessionManager(this);

        editLoginEmail = findViewById(R.id.editLoginEmail);
        editLoginPassword = findViewById(R.id.editLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUpRedirect = findViewById(R.id.txtSignUpRedirect);
        txtSellerLoginRedirect = findViewById(R.id.txtSellerLoginRedirect);

        btnLogin.setOnClickListener(v -> loginUser());

        txtSignUpRedirect.setOnClickListener(v -> {
            // Updated to use the combined SignUpActivity for customers
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            intent.putExtra("USER_TYPE", "customer");
            startActivity(intent);
        });

        txtSellerLoginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SellerLoginActivity.class));
        });
    }

    private void loginUser() {
        String email = editLoginEmail.getText().toString().trim();
        String password = editLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.checkUser(email, password)) {
            String type = dbHelper.getUserType(email);
            if ("customer".equals(type)) {
                // --- FIXED: Use SessionManager to create the login session ---
                sessionManager.createLoginSession(email, "customer");

                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "This is a seller account. Please use the seller login.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
