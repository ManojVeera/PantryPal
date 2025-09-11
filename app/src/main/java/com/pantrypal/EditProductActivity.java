package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProductActivity extends AppCompatActivity {

    private TextInputEditText editProductName, editProductDesc, editProductPrice, editProductImageUrl, editProductCategory, editProductQuantity;
    private MaterialButton btnUpdateProduct, btnDeleteProduct;
    private DBHelper dbHelper;
    private int productId;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        dbHelper = new DBHelper(this);
        productId = getIntent().getIntExtra("PRODUCT_ID", -1);

        if (productId == -1) {
            Toast.makeText(this, "Error: Product not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadProductData();

        btnUpdateProduct.setOnClickListener(v -> updateProduct());
        btnDeleteProduct.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void initializeViews() {
        // --- FIXED: Find all views by their ID ---
        editProductName = findViewById(R.id.editProductName);
        editProductDesc = findViewById(R.id.editProductDesc);
        editProductPrice = findViewById(R.id.editProductPrice);
        editProductImageUrl = findViewById(R.id.editProductImageUrl);
        editProductCategory = findViewById(R.id.editProductCategory);
        editProductQuantity = findViewById(R.id.editProductQuantity);
        btnUpdateProduct = findViewById(R.id.btnUpdateProduct);
        btnDeleteProduct = findViewById(R.id.btnDeleteProduct);
    }

    private void loadProductData() {
        currentProduct = dbHelper.getProductById(productId);
        if (currentProduct != null) {
            // --- FIXED: Pre-fill all fields with existing data ---
            editProductName.setText(currentProduct.getName());
            editProductDesc.setText(currentProduct.getDescription());
            editProductPrice.setText(String.valueOf(currentProduct.getPrice()));
            editProductImageUrl.setText(currentProduct.getImageUrl());
            editProductCategory.setText(currentProduct.getCategory());
            editProductQuantity.setText(String.valueOf(currentProduct.getQuantity()));
        }
    }

    private void updateProduct() {
        // --- FIXED: Read the new values from all the EditText fields ---
        String name = editProductName.getText().toString().trim();
        String desc = editProductDesc.getText().toString().trim();
        String priceStr = editProductPrice.getText().toString().trim();
        String imageUrl = editProductImageUrl.getText().toString().trim();
        String category = editProductCategory.getText().toString().trim();
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

        // --- FIXED: All variables are now correctly defined before being used ---
        if (dbHelper.updateProduct(productId, name, desc, price, imageUrl, category, quantity)) {
            Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show();
            // Go back to SellerActivity and force it to restart to see changes
            Intent intent = new Intent(this, SellerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Update failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete this product? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProduct())
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteProduct() {
        if (dbHelper.deleteProduct(productId)) {
            Toast.makeText(this, "Product deleted.", Toast.LENGTH_SHORT).show();
            // Go back to SellerActivity and force it to restart to see changes
            Intent intent = new Intent(this, SellerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to delete product.", Toast.LENGTH_SHORT).show();
        }
    }
}

