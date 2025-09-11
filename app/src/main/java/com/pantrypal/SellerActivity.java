package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;

public class SellerActivity extends AppCompatActivity {

    private TextInputEditText editProductName, editProductDesc, editProductPrice, editProductImageUrl, editProductQuantity;
    private AutoCompleteTextView autoCompleteCategory;
    private MaterialButton btnUploadProduct;
    private RecyclerView myProductsRecycler;
    // --- MODIFIED: Use the new SellerProductAdapter ---
    private SellerProductAdapter sellerProductAdapter;
    private MaterialToolbar topAppBar;

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller);

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Initialize views
        editProductName = findViewById(R.id.editProductName);
        editProductDesc = findViewById(R.id.editProductDesc);
        editProductPrice = findViewById(R.id.editProductPrice);
        editProductImageUrl = findViewById(R.id.editProductImageUrl);
        editProductQuantity = findViewById(R.id.editProductQuantity);
        autoCompleteCategory = findViewById(R.id.autoCompleteCategory);
        btnUploadProduct = findViewById(R.id.btnUploadProduct);
        myProductsRecycler = findViewById(R.id.myProductsRecycler);
        topAppBar = findViewById(R.id.topAppBar);

        // Setup for the category dropdown
        String[] categories = new String[]{"Dairy", "Veg", "Grocery"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        autoCompleteCategory.setAdapter(adapter);

        topAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_sign_out) {
                sessionManager.logoutUser();
                Intent intent = new Intent(SellerActivity.this, SellerLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            // --- This is where you would handle the "View Orders" click ---
            // else if (id == R.id.action_view_orders) { ... }
            return false;
        });

        btnUploadProduct.setOnClickListener(v -> uploadProduct());

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        myProductsRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        String sellerEmail = sessionManager.getLoggedInUserEmail();
        // --- MODIFIED: Initialize and set the new SellerProductAdapter ---
        sellerProductAdapter = new SellerProductAdapter(dbHelper.getMyProducts(sellerEmail), this);
        myProductsRecycler.setAdapter(sellerProductAdapter);
    }

    private void uploadProduct() {
        String name = editProductName.getText().toString().trim();
        String desc = editProductDesc.getText().toString().trim();
        String priceStr = editProductPrice.getText().toString().trim();
        String imageUrl = editProductImageUrl.getText().toString().trim();
        String category = autoCompleteCategory.getText().toString().trim();
        String quantityStr = editProductQuantity.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(priceStr) ||
                TextUtils.isEmpty(imageUrl) || TextUtils.isEmpty(category) || TextUtils.isEmpty(quantityStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        int quantity;
        try {
            price = Double.parseDouble(priceStr);
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price or quantity format", Toast.LENGTH_SHORT).show();
            return;
        }

        String sellerEmail = sessionManager.getLoggedInUserEmail();

        if (sellerEmail == null) {
            Toast.makeText(this, "Error: Not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.insertProduct(name, desc, price, imageUrl, category, quantity, sellerEmail)) {
            Toast.makeText(this, "Product uploaded successfully", Toast.LENGTH_SHORT).show();

            // Clear fields after a successful upload
            editProductName.getText().clear();
            editProductDesc.getText().clear();
            editProductPrice.getText().clear();
            editProductImageUrl.getText().clear();
            editProductQuantity.getText().clear();
            autoCompleteCategory.setText("", false);
            editProductName.requestFocus();

            // --- MODIFIED: Update the new sellerProductAdapter ---
            List<Product> updatedProducts = dbHelper.getMyProducts(sellerEmail);
            sellerProductAdapter.updateList(updatedProducts);

        } else {
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
        }
    }
}
