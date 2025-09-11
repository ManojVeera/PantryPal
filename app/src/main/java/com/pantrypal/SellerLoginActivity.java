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
    private TextView txtSignUpRedirect, txtCustomerLoginRedirect; // --- NEW

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_login);

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        editLoginEmail = findViewById(R.id.editLoginEmail);
        editLoginPassword = findViewById(R.id.editLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignUpRedirect = findViewById(R.id.txtSignUpRedirect);
        txtCustomerLoginRedirect = findViewById(R.id.txtCustomerLoginRedirect); // --- NEW

        btnLogin.setOnClickListener(v -> loginUser());

        txtSignUpRedirect.setOnClickListener(v -> {
            Intent intent = new Intent(SellerLoginActivity.this, SignUpActivity.class);
            intent.putExtra("USER_TYPE", "seller");
            startActivity(intent);
        });

        // --- NEW: Handle click to go to customer login ---
        txtCustomerLoginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(SellerLoginActivity.this, LoginActivity.class));
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
            if ("seller".equals(type)) {
                sessionManager.createLoginSession(email, "seller");
                Toast.makeText(this, "Seller Login Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, SellerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Not a seller account. Please use customer login.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
