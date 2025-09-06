package com.pantrypal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SignUpActivity extends AppCompatActivity {
    DBHelper dbHelper;
    private TextInputEditText editName, editEmail, editPassword, editConfirmPassword;
    private MaterialButton btnSignUp;
    private TextView txtLoginRedirect;

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        dbHelper = new DBHelper(this);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtLoginRedirect = findViewById(R.id.txtLoginRedirect);

        btnSignUp.setOnClickListener(v -> validateAndSignUp());
        txtLoginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
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

        // ✅ Insert into DB
        boolean inserted = dbHelper.insertUser(name, email, password);

        if (inserted) {
            // ✅ ADD THIS LINE: Automatically log the user in after signing up
            dbHelper.setLoggedInUser(email);

            Toast.makeText(this, "Sign Up Successful 🎉", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Error: Could not register", Toast.LENGTH_SHORT).show();
        }
    }
}
