package com.example.blueroom;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.List;

public class FavProductAdapter extends FirestoreRecyclerAdapter<products, FavProductAdapter.FavProductViewHolder> {

    private OnFavProductClickListener favProductClickListener;
    private OnAddToCartClickListener addToCartClickListener;
    private List<String> cartProductIds;

    public FavProductAdapter(@NonNull FirestoreRecyclerOptions<products> options, OnFavProductClickListener favProductClickListener, List<String> cartProductIds, OnAddToCartClickListener addToCartClickListener) {
        super(options);
        this.favProductClickListener = favProductClickListener;
        this.cartProductIds = cartProductIds;
        this.addToCartClickListener = addToCartClickListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull FavProductViewHolder holder, int position, @NonNull products model) {
        holder.bind(model, favProductClickListener, cartProductIds, addToCartClickListener);
    }

    @NonNull
    @Override
    public FavProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fav_recycler, parent, false);
        return new FavProductViewHolder(view);
    }

    static class FavProductViewHolder extends RecyclerView.ViewHolder {
        ImageButton imageurl;
        TextView nameTextView;
        TextView authorTextView;
        TextView priceTextView;
        ImageView checkImageView;
        Button addToCartButton;

        public FavProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageurl = itemView.findViewById(R.id.imageurl);
            nameTextView = itemView.findViewById(R.id.name);
            authorTextView = itemView.findViewById(R.id.author);
            priceTextView = itemView.findViewById(R.id.price);
            checkImageView = itemView.findViewById(R.id.check);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
        }

        public void bind(products product, OnFavProductClickListener favProductClickListener, List<String> cartProductIds, OnAddToCartClickListener addToCartClickListener) {
            Glide.with(itemView.getContext()).load(product.getImageurl()).into(imageurl);
            itemView.setOnClickListener(v -> {
                if (favProductClickListener != null) {
                    favProductClickListener.onFavProductClick(product);
                }
            });

            nameTextView.setText(product.getName());
            authorTextView.setText(product.getAuthor());
            priceTextView.setText(String.valueOf(product.getPrice()));

            if (cartProductIds.contains(product.getName())) {
                checkImageView.setVisibility(View.VISIBLE);
            } else {
                checkImageView.setVisibility(View.GONE);
            }

            addToCartButton.setOnClickListener(v -> {
                if (addToCartClickListener != null) {
                    addToCartClickListener.onAddToCartClick(product);
                }
            });
        }
    }

    public interface OnFavProductClickListener {
        void onFavProductClick(products product);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(products product);
    }
}
