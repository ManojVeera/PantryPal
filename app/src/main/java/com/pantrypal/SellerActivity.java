package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

public class SellerActivity extends AppCompatActivity {

    private TextInputEditText editProductName, editProductDesc, editProductPrice, editProductImageUrl, editProductCategory;
    private MaterialButton btnUploadProduct;
    private RecyclerView myProductsRecycler;
    private ProductAdapter productAdapter;
    private MaterialToolbar topAppBar;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller);

        dbHelper = new DBHelper(this);

        editProductName = findViewById(R.id.editProductName);
        editProductDesc = findViewById(R.id.editProductDesc);
        editProductPrice = findViewById(R.id.editProductPrice);
        editProductImageUrl = findViewById(R.id.editProductImageUrl);
        editProductCategory = findViewById(R.id.editProductCategory);
        btnUploadProduct = findViewById(R.id.btnUploadProduct);
        myProductsRecycler = findViewById(R.id.myProductsRecycler);
        topAppBar = findViewById(R.id.topAppBar);

        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_sign_out) {
                signOutUser();
                return true;
            }
            return false;
        });
        topAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_sign_out) {
                signOutUser();
                return true;
            } else if (id == R.id.action_view_orders) {
                Toast.makeText(this, "View Orders clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_manage_inventory) {
                Toast.makeText(this, "Manage Inventory clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        btnUploadProduct.setOnClickListener(v -> uploadProduct());

        myProductsRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(dbHelper.getMyProducts(dbHelper.getLoggedInUser()), this);
        myProductsRecycler.setAdapter(productAdapter);
    }

    private void signOutUser() {
        dbHelper.clearLoggedInUser();
        Intent intent = new Intent(SellerActivity.this, SellerLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void uploadProduct() {
        String name = editProductName.getText().toString().trim();
        String desc = editProductDesc.getText().toString().trim();
        String priceStr = editProductPrice.getText().toString().trim();
        String imageUrl = editProductImageUrl.getText().toString().trim();
        String category = editProductCategory.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(imageUrl) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
            return;
        }

        String sellerEmail = dbHelper.getLoggedInUser();

        if (dbHelper.insertProduct(name, desc, price, imageUrl, category, sellerEmail)) {
            Toast.makeText(this, "Product uploaded", Toast.LENGTH_SHORT).show();
            productAdapter = new ProductAdapter(dbHelper.getMyProducts(sellerEmail), this);
            myProductsRecycler.setAdapter(productAdapter);
        } else {
            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
        }
    }
}