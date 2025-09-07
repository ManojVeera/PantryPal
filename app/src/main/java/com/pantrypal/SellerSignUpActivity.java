package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SellerSignUpActivity extends AppCompatActivity {

    private TextInputEditText editName, editEmail, editPassword, editConfirmPassword;
    private MaterialButton btnSignUp;
    private TextView txtLoginRedirect;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_sign_up);

        dbHelper = new DBHelper(this);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtLoginRedirect = findViewById(R.id.txtLoginRedirect);

        btnSignUp.setOnClickListener(v -> validateAndSignUp());

        txtLoginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(SellerSignUpActivity.this, SellerLoginActivity.class));
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

        if (dbHelper.insertUser(name, email, password, "seller")) {
            dbHelper.setLoggedInUser(email);
            Toast.makeText(this, "Seller Sign Up Successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SellerActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
        }
    }
}