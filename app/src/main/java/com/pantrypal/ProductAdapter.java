package com.pantrypal;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {

    private final List<Product> productList;
    private List<Product> productListFull;
    private final Context context;
    private final FilterListener filterListener; // hold reference
    private final DBHelper dbHelper;
    private final SessionManager sessionManager;

    public interface FilterListener {
        void onFilterComplete();
    }

    public ProductAdapter(List<Product> productList, Context context, FilterListener filterListener) {
        this.productList = productList;
        this.productListFull = new ArrayList<>(productList);
        this.context = context;
        this.dbHelper = new DBHelper(context);
        this.sessionManager = new SessionManager(context);
        this.filterListener = filterListener; // ✅ properly initialized
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // ✅ Debug log to check the URL
        Log.d("IMAGE_URL_CHECK", product.getName() + " -> " + product.getImageUrl());

        holder.productName.setText(product.getName());
        holder.productDesc.setText(product.getDescription());
        holder.productPrice.setText("₹" + String.format("%.2f", product.getPrice()));

        // ✅ Glide with advanced error logging
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_menu_gallery)
                .error(R.drawable.ic_menu_gallery)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        // Log the detailed error from Glide
                        Log.e("GLIDE_ERROR", "Load failed for: " + model, e);
                        return false; // Return false to allow Glide to show the error drawable
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        // Log success
                        Log.d("GLIDE_SUCCESS", "Successfully loaded image: " + model);
                        return false;
                    }
                })
                .into(holder.productImage);

        // --- THE "ADD TO CART" BUTTON LOGIC (NOW FUNCTIONAL) ---
        holder.btnBuy.setText("Add to Cart"); // Change button text
        holder.btnBuy.setOnClickListener(v -> {
            String userEmail = sessionManager.getLoggedInUserEmail();
            if (userEmail != null) {
                dbHelper.addToCart(userEmail, product.getId());
                Toast.makeText(context, "Added " + product.getName() + " to cart", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Please log in to shop", Toast.LENGTH_SHORT).show();
            }
        });

        // --- THE CRITICAL FIX ---
        // This makes the entire card clickable and directs the user to the correct Detail screen.
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class); // ✅ CORRECTED ACTIVITY
            intent.putExtra("PRODUCT_ID", product.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newProductList) {
        productList.clear();
        productList.addAll(newProductList);
        productListFull = new ArrayList<>(newProductList);
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productDesc, productPrice;
        MaterialButton btnBuy;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productDesc = itemView.findViewById(R.id.productDesc);
            productPrice = itemView.findViewById(R.id.productPrice);
            btnBuy = itemView.findViewById(R.id.btnBuy);
        }
    }

    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private final Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Product> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(productListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Product item : productListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            productList.clear();
            productList.addAll((List<Product>) results.values);
            notifyDataSetChanged();

            // ✅ Notify HomeActivity when filtering is done
            if (filterListener != null) {
                filterListener.onFilterComplete();
            }
        }

    };
}

