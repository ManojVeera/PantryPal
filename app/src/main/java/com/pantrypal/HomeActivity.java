package com.pantrypal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private TextInputEditText searchBar;
    private RecyclerView categoryRecycler, popularRecycler;
    private BottomNavigationView bottomNav;

    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;

    private DBHelper dbHelper;

    private FusedLocationProviderClient fusedLocationClient;
    private AlertDialog pincodeDialog;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DBHelper(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Cannot detect location.", Toast.LENGTH_LONG).show();
            }
        });

        topAppBar = findViewById(R.id.topAppBar);
        searchBar = findViewById(R.id.searchBar);
        categoryRecycler = findViewById(R.id.categoryRecycler);
        popularRecycler = findViewById(R.id.popularRecycler);
        bottomNav = findViewById(R.id.bottomNav);

        topAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_location) {
                showPincodeDialog();
                return true;
            } else if (id == R.id.action_cart) {
                Toast.makeText(this, "Cart clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_sign_out) {
                signOutUser();
                return true;
            }
            return false;
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (productAdapter != null) {
                    productAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupCategoryRecycler();

        setupPopularRecycler();
        fetchProductsFromDb();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_categories) {
                Toast.makeText(this, "Categories Clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void signOutUser() {
        dbHelper.clearLoggedInUser();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showPincodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_pincode_check, null);
        builder.setView(dialogView);

        final TextInputEditText pincodeInput = dialogView.findViewById(R.id.pincodeInput);
        Button checkDeliveryBtn = dialogView.findViewById(R.id.checkDeliveryBtn);
        Button detectLocationBtn = dialogView.findViewById(R.id.detectLocationBtn);

        pincodeDialog = builder.create();

        checkDeliveryBtn.setOnClickListener(v -> {
            String pincode = pincodeInput.getText().toString().trim();
            if (pincode.isEmpty() || pincode.length() != 6) {
                pincodeInput.setError("Please enter a valid 6-digit pincode");
                return;
            }
            checkAndShowDeliveryStatus(pincode);
            pincodeDialog.dismiss();
        });

        detectLocationBtn.setOnClickListener(v -> requestLocationPermission());

        pincodeDialog.show();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        Toast.makeText(this, "Detecting location...", Toast.LENGTH_SHORT).show();
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        getPincodeFromLocation(location);
                    } else {
                        Toast.makeText(this, "Could not get location. Make sure location is enabled.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("LocationError", "Failed to get location.", e);
                });
    }

    private void getPincodeFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String pincode = addresses.get(0).getPostalCode();
                if (pincode != null && !pincode.isEmpty()) {
                    checkAndShowDeliveryStatus(pincode);
                } else {
                    Toast.makeText(this, "Could not find pincode for this location.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "No address found for this location.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.e("GeocoderError", "Service not available", e);
            Toast.makeText(this, "Geocoder service not available.", Toast.LENGTH_LONG).show();
        } finally {
            if (pincodeDialog != null && pincodeDialog.isShowing()) {
                pincodeDialog.dismiss();
            }
        }
    }

    private void checkAndShowDeliveryStatus(String pincode) {
        if (isDeliverable(pincode)) {
            Toast.makeText(this, "Great! Delivery is available at " + pincode, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sorry, we do not deliver to " + pincode, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isDeliverable(String pincode) {
        return pincode.startsWith("6");
    }

    private void setupCategoryRecycler() {
        List<String> categories = dbHelper.getCategories();
        categoryAdapter = new CategoryAdapter(categories);
        categoryRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryRecycler.setAdapter(categoryAdapter);
    }

    private void setupPopularRecycler() {
        popularRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(dbHelper.getAllProducts(), this); // Pass context if needed for buy button
        popularRecycler.setAdapter(productAdapter);
    }

    private void fetchProductsFromDb() {
        // Already fetched in setup
    }
}