package com.pantrypal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private TextView totalPriceText, emptyCartText;
    private LinearLayout checkoutLayout;
    private Button btnCheckout;

    private DBHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        totalPriceText = findViewById(R.id.totalPriceText);
        emptyCartText = findViewById(R.id.emptyCartText);
        checkoutLayout = findViewById(R.id.checkoutLayout);
        btnCheckout = findViewById(R.id.btnCheckout);
        recyclerView = findViewById(R.id.cartRecyclerView);

        MaterialToolbar toolbar = findViewById(R.id.topAppBarCart);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupRecyclerView();

        btnCheckout.setOnClickListener(v -> showCheckoutDialog());
    }

    private void setupRecyclerView() {
        String userEmail = sessionManager.getLoggedInUserEmail();
        cartItems = dbHelper.getCartItems(userEmail);

        if (cartItems.isEmpty()) {
            showEmptyCartView(true);
        } else {
            showEmptyCartView(false);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            cartAdapter = new CartAdapter(cartItems, this, this);
            recyclerView.setAdapter(cartAdapter);
            updateTotalPrice();
        }
    }

    private void showEmptyCartView(boolean show) {
        if (show) {
            recyclerView.setVisibility(View.GONE);
            checkoutLayout.setVisibility(View.GONE);
            emptyCartText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            checkoutLayout.setVisibility(View.VISIBLE);
            emptyCartText.setVisibility(View.GONE);
        }
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getProductPrice() * item.getQuantity();
        }
        totalPriceText.setText(String.format(Locale.getDefault(), "₹%.2f", total));
    }

    private void showCheckoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Order")
                .setMessage("Your order has been placed successfully!")
                .setPositiveButton("OK", (dialog, which) -> {
                    dbHelper.clearCart(sessionManager.getLoggedInUserEmail());
                    Toast.makeText(this, "Thank you for shopping!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    // ✅ quantity changed (from adapter → plus/minus buttons)
    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        String userEmail = sessionManager.getLoggedInUserEmail();
        if (newQuantity > 0) {
            dbHelper.updateCart(userEmail, item.getProductId(), newQuantity);
            item.setQuantity(newQuantity);
        } else {
            dbHelper.removeFromCart(userEmail, item.getProductId());
            cartItems.remove(item);
            cartAdapter.notifyDataSetChanged();
            if (cartItems.isEmpty()) {
                showEmptyCartView(true);
            }
        }
        updateTotalPrice();
    }

    // ✅ item removed (trash/delete button)
    @Override
    public void onItemRemoved(CartItem item) {
        String userEmail = sessionManager.getLoggedInUserEmail();
        dbHelper.removeFromCart(userEmail, item.getProductId());
        cartItems.remove(item);
        cartAdapter.notifyDataSetChanged();
        if (cartItems.isEmpty()) {
            showEmptyCartView(true);
        }
        updateTotalPrice();
    }
}
