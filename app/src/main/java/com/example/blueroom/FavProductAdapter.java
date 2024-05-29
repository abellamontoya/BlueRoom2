package com.example.blueroom;

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

public class FavProductAdapter extends FirestoreRecyclerAdapter<products, FavProductAdapter.ProductViewHolder> {

    private final OnProductClickListener clickListener;
    private final List<String> cartProductIds;
    private final OnAddToCartClickListener addToCartClickListener;
    private final OnRemoveFromFavoritesClickListener removeFromFavoritesClickListener;

    public interface OnProductClickListener {
        void onProductClick(products product);
    }

    public interface OnAddToCartClickListener {
        void onAddToCartClick(products product);
    }

    public interface OnRemoveFromFavoritesClickListener {
        void onRemoveFromFavoritesClick(products product);
    }

    public FavProductAdapter(@NonNull FirestoreRecyclerOptions<products> options, OnProductClickListener clickListener, List<String> cartProductIds, OnAddToCartClickListener addToCartClickListener, OnRemoveFromFavoritesClickListener removeFromFavoritesClickListener) {
        super(options);
        this.clickListener = clickListener;
        this.cartProductIds = cartProductIds;
        this.addToCartClickListener = addToCartClickListener;
        this.removeFromFavoritesClickListener = removeFromFavoritesClickListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull products model) {
        holder.name.setText(model.getName());
        holder.author.setText(model.getAuthor());
        holder.price.setText(String.format("%.2f", model.getPrice()));

        Glide.with(holder.itemView.getContext()).load(model.getImageurl()).into(holder.image);

        holder.itemView.setOnClickListener(v -> clickListener.onProductClick(model));

        holder.addToCartButton.setOnClickListener(v -> addToCartClickListener.onAddToCartClick(model));

        holder.removeFromFavoritesButton.setOnClickListener(v -> removeFromFavoritesClickListener.onRemoveFromFavoritesClick(model));
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fav_recycler, parent, false);
        return new ProductViewHolder(view);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name, author, price;
        ImageView image;
        Button addToCartButton;
        ImageButton removeFromFavoritesButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            author = itemView.findViewById(R.id.author);
            price = itemView.findViewById(R.id.price);
            image = itemView.findViewById(R.id.imageurl);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
            removeFromFavoritesButton = itemView.findViewById(R.id.removeFromFavoritesButton);
        }
    }
}
