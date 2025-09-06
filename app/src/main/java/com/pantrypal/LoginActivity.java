package com.pantrypal;
import android.widget.TextView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editLoginEmail, editLoginPassword;
    private MaterialButton btnLogin;
    private TextView txtSignUpRedirect;

    private DBHelper dbHelper;

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 🔗 DB
        dbHelper = new DBHelper(this);

        // ✅ Proper UI bindings (initialize class-level variables)
        editLoginEmail = findViewById(R.id.editLoginEmail);
        editLoginPassword = findViewById(R.id.editLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUpRedirect = findViewById(R.id.txtSignUpRedirect);

        // Login button action
        btnLogin.setOnClickListener(v -> loginUser());

        // Redirect to SignUp
        txtSignUpRedirect.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            finish();
        });
    }

    private void loginUser() {
        String email = editLoginEmail.getText().toString().trim();
        String password = editLoginPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(email)) {
            editLoginEmail.setError("Enter your email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editLoginPassword.setError("Enter your password");
            return;
        }

        // 🔍 DB check (email + hashed password match)
        boolean valid = dbHelper.checkUser(email, password); // hashes internally

        if (valid) {
            // ✅ Store logged-in user in DB
            dbHelper.setLoggedInUser(email); // <-- add this

            Toast.makeText(this, "Login Successful 🎉", Toast.LENGTH_SHORT).show();

            // Move to HomeActivity
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Invalid Email or Password ❌", Toast.LENGTH_SHORT).show();
        }
    }

}
