package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class SellerLoginActivity extends AppCompatActivity {

    private TextInputEditText editLoginEmail, editLoginPassword;
    private MaterialButton btnLogin;
    private TextView txtSignUpRedirect;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_login);

        dbHelper = new DBHelper(this);

        editLoginEmail = findViewById(R.id.editLoginEmail);
        editLoginPassword = findViewById(R.id.editLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUpRedirect = findViewById(R.id.txtSignUpRedirect);

        btnLogin.setOnClickListener(v -> loginUser());

        txtSignUpRedirect.setOnClickListener(v -> {
            startActivity(new Intent(SellerLoginActivity.this, SellerSignUpActivity.class));
        });
    }

    private void loginUser() {
        String email = editLoginEmail.getText().toString().trim();
        String password = editLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editLoginEmail.setError("Enter your email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editLoginPassword.setError("Enter your password");
            return;
        }

        if (dbHelper.checkUser(email, password)) {
            String type = dbHelper.getUserType(email);
            if ("seller".equals(type)) {
                dbHelper.setLoggedInUser(email);
                Toast.makeText(this, "Seller Login Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SellerActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Not a seller account", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }
}