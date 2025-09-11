package com.pantrypal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        DBHelper dbHelper = new DBHelper(this);
        MaterialToolbar toolbar = findViewById(R.id.topAppBarCategories);
        RecyclerView recyclerView = findViewById(R.id.categoriesGridRecycler);

        // 🔙 back button in toolbar
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // ✅ make RecyclerView use GridLayoutManager
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // fetch categories from DB
        List<String> categories = dbHelper.getCategories();

        // ✅ set adapter
        CategoryAdapter adapter = new CategoryAdapter(
                categories,
                R.layout.item_category_grid,   // 👈 specify the grid layout
                category -> {
                    // temporary: just show toast
                    Toast.makeText(this, "Showing all " + category, Toast.LENGTH_SHORT).show();

                    // ✅ future: open ProductsActivity filtered by category
                    // Intent intent = new Intent(this, ProductsActivity.class);
                    // intent.putExtra("CATEGORY_NAME", category);
                    // startActivity(intent);
                }
        );


        recyclerView.setAdapter(adapter);
    }
}
