package com.pantrypal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItems;
    private final Context context;
    private final CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, Context context, CartItemListener listener) {
        this.cartItems = cartItems;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        DBHelper dbHelper = new DBHelper(context);

        holder.productName.setText(item.getProductName());
        holder.productPrice.setText(String.format(Locale.getDefault(), "₹%.2f", item.getProductPrice()));
        holder.quantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_menu_gallery)
                .into(holder.productImage);

        // ✅ Increment
        // Increment
        holder.btnIncrement.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            item.setQuantity(newQuantity);
            dbHelper.updateCartItemQuantity(item.getUserEmail(), item.getProductId(), newQuantity); // ✅ fixed
            listener.onQuantityChanged(item, newQuantity);
            notifyItemChanged(position);
        });

// Decrement
        holder.btnDecrement.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                int newQuantity = item.getQuantity() - 1;
                item.setQuantity(newQuantity);
                dbHelper.updateCartItemQuantity(item.getUserEmail(), item.getProductId(), newQuantity); // ✅ fixed
                listener.onQuantityChanged(item, newQuantity);
                notifyItemChanged(position);
            }
        });

// Remove
        holder.btnRemove.setOnClickListener(v -> {
            dbHelper.removeCartItem(item.getUserEmail(), item.getProductId()); // ✅ fixed
            listener.onItemRemoved(item);
        });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, quantity;
        MaterialButton btnIncrement, btnDecrement;
        ImageButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.cartProductImage);
            productName = itemView.findViewById(R.id.cartProductName);
            productPrice = itemView.findViewById(R.id.cartProductPrice);
            quantity = itemView.findViewById(R.id.cartProductQuantity);
            btnIncrement = itemView.findViewById(R.id.btnIncrement);
            btnDecrement = itemView.findViewById(R.id.btnDecrement);
            btnRemove = itemView.findViewById(R.id.btnRemoveItem);
        }
    }
}
