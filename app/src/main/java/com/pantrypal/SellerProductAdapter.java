package com.pantrypal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
// --- NEW: Import for the cache-busting signature ---
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.Locale;

public class SellerProductAdapter extends RecyclerView.Adapter<SellerProductAdapter.SellerProductViewHolder> {

    private final List<Product> productList;
    private final Context context;

    public SellerProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public SellerProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seller_product, parent, false);
        return new SellerProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.getName());
        holder.productPrice.setText(String.format(Locale.getDefault(), "₹%.2f", product.getPrice()));
        holder.productQuantity.setText(String.format(Locale.getDefault(), "Qty: %d", product.getQuantity()));

        // --- THE DEFINITIVE "PRO MAX" FIX ---
        // By adding a unique signature, we force Glide to ignore its cache and treat
        // this as a brand new image request every time the list is refreshed.
        // This is the most reliable way to solve stubborn image sync problems.
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                .placeholder(R.drawable.ic_menu_gallery)
                .error(R.drawable.ic_menu_gallery)
                .into(holder.productImage);

        View.OnClickListener editClickListener = v -> {
            Intent intent = new Intent(context, EditProductActivity.class);
            intent.putExtra("PRODUCT_ID", product.getId());
            context.startActivity(intent);
        };

        holder.btnEdit.setOnClickListener(editClickListener);
        holder.itemView.setOnClickListener(editClickListener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newProductList) {
        productList.clear();
        productList.addAll(newProductList);
        notifyDataSetChanged();
    }

    static class SellerProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productQuantity;
        MaterialButton btnEdit;

        public SellerProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.productQuantity);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}

