package com.pantrypal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView productImage;
    private TextView productName, productPrice, productDescription, tvQuantity;
    private Button btnAddToCart, btnIncrease, btnDecrease;
    private LinearLayout cartActionLayout;
    private MaterialToolbar toolbar;

    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private Product currentProduct;
    private int currentQuantity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        int productId = getIntent().getIntExtra("PRODUCT_ID", -1);
        if (productId == -1) {
            Toast.makeText(this, "Product not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentProduct = dbHelper.getProductById(productId);
        if (currentProduct == null) {
            Toast.makeText(this, "Product not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        populateData();

        // Back button
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        btnAddToCart.setOnClickListener(v -> addToCart());

        btnIncrease.setOnClickListener(v -> {
            String userEmail = sessionManager.getLoggedInUserEmail();
            if (userEmail != null) {
                dbHelper.updateCart(userEmail, currentProduct.getId(), currentQuantity + 1);
                currentQuantity++;
                tvQuantity.setText(String.valueOf(currentQuantity));
            }
        });

        btnDecrease.setOnClickListener(v -> {
            String userEmail = sessionManager.getLoggedInUserEmail();
            if (userEmail != null && currentQuantity > 1) {
                dbHelper.updateCart(userEmail, currentProduct.getId(), currentQuantity - 1);
                currentQuantity--;
                tvQuantity.setText(String.valueOf(currentQuantity));
            } else if (userEmail != null && currentQuantity == 1) {
                dbHelper.removeFromCart(userEmail, currentProduct.getId());
                currentQuantity = 0;
                loadCartStatus(); // switch back to "Add to Cart"
            }
        });

        loadCartStatus(); // show correct UI state
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.topAppBarProductDetail);
        productImage = findViewById(R.id.productImageDetail);
        productName = findViewById(R.id.productNameDetail);
        productPrice = findViewById(R.id.productPriceDetail);
        productDescription = findViewById(R.id.productDescriptionDetail);
        btnAddToCart = findViewById(R.id.btnAddToCartDetail);

        cartActionLayout = findViewById(R.id.cartActionLayout);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnIncrease = findViewById(R.id.btnIncrease);
        btnDecrease = findViewById(R.id.btnDecrease);
    }

    private void populateData() {
        productName.setText(currentProduct.getName());
        productPrice.setText(String.format(Locale.getDefault(), "₹%.2f", currentProduct.getPrice()));
        productDescription.setText(currentProduct.getDescription());

        toolbar.setTitle(currentProduct.getName());

        Glide.with(this)
                .load(currentProduct.getImageUrl())
                .placeholder(R.drawable.ic_menu_gallery)
                .into(productImage);
    }

    private void addToCart() {
        String userEmail = sessionManager.getLoggedInUserEmail();
        if (userEmail != null) {
            dbHelper.addToCart(userEmail, currentProduct.getId());
            currentQuantity = 1;
            Toast.makeText(this, currentProduct.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
            loadCartStatus();
        } else {
            Toast.makeText(this, "Please log in to add items to cart.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCartStatus() {
        String userEmail = sessionManager.getLoggedInUserEmail();
        if (userEmail == null) return;

        currentQuantity = dbHelper.getCartQuantity(userEmail, currentProduct.getId());

        if (currentQuantity > 0) {
            btnAddToCart.setVisibility(View.GONE);
            cartActionLayout.setVisibility(View.VISIBLE);
            tvQuantity.setText(String.valueOf(currentQuantity));
        } else {
            btnAddToCart.setVisibility(View.VISIBLE);
            cartActionLayout.setVisibility(View.GONE);
        }
    }
}
