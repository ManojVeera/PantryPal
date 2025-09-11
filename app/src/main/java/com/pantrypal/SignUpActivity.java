package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText editName, editEmail, editPassword, editConfirmPassword;
    private MaterialButton btnSignUp;
    private TextView txtLoginRedirect, txtSellerSignUpRedirect;

    private DBHelper dbHelper;
    // --- NEW: Add SessionManager ---
    private SessionManager sessionManager;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        dbHelper = new DBHelper(this);
        // --- NEW: Initialize SessionManager ---
        sessionManager = new SessionManager(this);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtLoginRedirect = findViewById(R.id.txtLoginRedirect);
        txtSellerSignUpRedirect = findViewById(R.id.txtSellerSignUpRedirect);

        userType = getIntent().getStringExtra("USER_TYPE");
        if (userType == null) {
            userType = "customer";
        }

        TextView pageTitle = findViewById(R.id.txtTitle);
        if ("seller".equals(userType)) {
            pageTitle.setText("Seller Sign Up");
            txtSellerSignUpRedirect.setVisibility(View.GONE);
        } else {
            pageTitle.setText("Create Account");
        }

        btnSignUp.setOnClickListener(v -> validateAndSignUp());

        txtLoginRedirect.setOnClickListener(v -> {
            if ("seller".equals(userType)) {
                startActivity(new Intent(SignUpActivity.this, SellerLoginActivity.class));
            } else {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });

        txtSellerSignUpRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignUpActivity.class);
            intent.putExtra("USER_TYPE", "seller");
            startActivity(intent);
        });
    }

    private void validateAndSignUp() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editName.setError("Enter your name");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Enter your email");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Enter valid email");
            return;
        }
        if (dbHelper.isEmailExists(email)) {
            editEmail.setError("Email already registered");
            return;
        }
        if (password.length() < 6) {
            editPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Passwords do not match");
            return;
        }

        if (dbHelper.insertUser(name, email, password, userType)) {
            // --- FIXED: Use SessionManager to create the login session ---
            sessionManager.createLoginSession(email, userType);

            Toast.makeText(this, "Sign Up Successful", Toast.LENGTH_SHORT).show();

            Intent intent;
            if ("seller".equals(userType)) {
                intent = new Intent(this, SellerActivity.class);
            } else {
                intent = new Intent(this, HomeActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
        }
    }
}
