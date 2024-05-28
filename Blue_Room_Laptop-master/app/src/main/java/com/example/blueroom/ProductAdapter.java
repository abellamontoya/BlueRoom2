package com.example.blueroom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.List;

public class ProductAdapter extends FirestoreRecyclerAdapter<products, ProductAdapter.ProductViewHolder> {

    private OnProductClickListener listener;

    public ProductAdapter(@NonNull FirestoreRecyclerOptions<products> options, OnProductClickListener listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull products model) {
        holder.bind(model, listener);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_recyclerview, parent, false);
        return new ProductViewHolder(view);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageurl;
        TextView nameTextView;
        TextView author;
        TextView priceTextView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageurl = itemView.findViewById(R.id.imageurl);
            nameTextView = itemView.findViewById(R.id.name); // Agrega TextView para el nombre
            priceTextView = itemView.findViewById(R.id.price); // Agrega TextView para el precio
            author = itemView.findViewById(R.id.author);
        }

        public void bind(products product, OnProductClickListener listener) {
            Glide.with(itemView.getContext()).load(product.getImageurl()).into(imageurl);
            imageurl.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });

            // Muestra el nombre, autor y precio del producto
            nameTextView.setText(product.getName());
            author.setText(product.getAuthor());
            priceTextView.setText(String.valueOf(product.getPrice()));
        }

    }

    public interface OnProductClickListener {
        void onProductClick(products product);
    }
}

