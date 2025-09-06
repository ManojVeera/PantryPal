package com.pantrypal; // use your package name

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // ✅ Import ImageView
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // ✅ Import Glide
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable { // ✅ Implement Filterable

    private List<Product> productList;
    private List<Product> productListFull;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
        this.productListFull = new ArrayList<>(productList);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getName());
        holder.productDesc.setText(product.getDescription());
        holder.productPrice.setText("₹" + String.format("%.2f", product.getPrice()));

        // ✅ Use Glide to load the image from the URL
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_menu_gallery) // Optional: a placeholder image
                .into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // ✅ Add the ViewHolder class
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage; // ✅ Add ImageView
        TextView productName, productDesc, productPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage); // ✅ Find ImageView
            productName = itemView.findViewById(R.id.productName);
            productDesc = itemView.findViewById(R.id.productDesc);
            productPrice = itemView.findViewById(R.id.productPrice);
        }
    }

    // ✅ Add the filter logic
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
                for (Product product : productListFull) {
                    if (product.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(product);
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
            productList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    // Helper method to update the adapter's data, including the full list for filtering
    public void updateProducts(List<Product> newProducts) {
        productList.clear();
        productList.addAll(newProducts);
        productListFull.clear();
        productListFull.addAll(newProducts);
        notifyDataSetChanged();
    }
}