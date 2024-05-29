package com.example.blueroom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.exoplayer2.Player;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerControlView;


import java.util.ArrayList;

public class ShowProduct extends Fragment {

    private static final String TAG = "ShowProduct";

    private ArrayList<products> cartProducts = new ArrayList<>();

    private String author;
    private String imageUrl;
    private String name;
    private float price;
    private int quantity;
    private int date;
    private String type;
    private ArrayList<String> tag;
    private String audioUrl;
    SharedPreferences favoritesPreferences;

    private NavController navController;
    private ExoPlayer exoPlayer;
    private PlayerControlView playerControlView;

    public ShowProduct() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            author = getArguments().getString("author", "");
            imageUrl = getArguments().getString("imageurl", "");
            name = getArguments().getString("name", "");
            price = getArguments().getFloat("price", 0);
            date = getArguments().getInt("date", 0);
            type = getArguments().getString("type", "");
            tag = getArguments().getStringArrayList("tag");
            audioUrl = getArguments().getString("musicurl", "");
            Log.d(TAG, "Music URL: " + audioUrl);
        }

        MyApp myApp = (MyApp) requireActivity().getApplication();
        cartProducts = myApp.getCartProducts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_product, container, false);

        TextView authorTextView = view.findViewById(R.id.author);
        ImageView imageView = view.findViewById(R.id.image);
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView typeTextView = view.findViewById(R.id.type);
        TextView tagTextView = view.findViewById(R.id.tag);
        TextView dateTextView = view.findViewById(R.id.date);

        authorTextView.setText(author);
        nameTextView.setText(name);
        String formattedPrice = String.format("%.2f€", price);
        priceTextView.setText(formattedPrice);
        typeTextView.setText(type);
        dateTextView.setText(String.valueOf(date));
        if (tag != null && !tag.isEmpty()) {
            StringBuilder tagBuilder = new StringBuilder();
            for (String tagItem : tag) {
                tagBuilder.append(tagItem).append(", ");
            }
            if (tagBuilder.length() > 0) {
                tagBuilder.deleteCharAt(tagBuilder.length() - 2);
            }
            tagTextView.setText(tagBuilder.toString());
        }

        Glide.with(requireContext()).load(imageUrl).into(imageView);

        // Configurar ExoPlayer
        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        playerControlView = view.findViewById(R.id.exo_player_view);
        playerControlView.setPlayer(exoPlayer);
        MediaItem mediaItem = MediaItem.fromUri(audioUrl);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    // Acción cuando el audio termina
                }
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        RecyclerView recyclerView = view.findViewById(R.id.related_recyclerview);

        Button buyButton = view.findViewById(R.id.buy);
        buyButton.setOnClickListener(v -> {
            MyApp myApp = (MyApp) requireActivity().getApplication();
            Log.d(TAG, "Adding product to cart: " + name);
            products newProduct = new products(imageUrl, name, author, price, quantity);
            myApp.addProductToCart(newProduct);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Product added to cart")
                    .setCancelable(false)
                    .setPositiveButton("Go to Cart", (dialog, id) -> openCartFragment())
                    .setNegativeButton("Continue Shopping", (dialog, id) -> {
                    });
            AlertDialog alert = builder.create();
            alert.show();
        });

        favoritesPreferences = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE);
        SharedPreferences preferences = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE);

        boolean isFavorite = preferences.getBoolean(name, false);

        ImageButton favoriteButton = view.findViewById(R.id.favorite_button);
        favoriteButton.setOnClickListener(v -> toggleFavoriteStatus());
        setFavoriteStatus(isFavorite);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        String currentProductId = getArguments().getString("name");
        ArrayList<String> originalTags = getArguments().getStringArrayList("tag");

        Query query = FirebaseFirestore.getInstance().collection("products");
        ArrayList<Query> tagQueries = new ArrayList<>();

        for (String tag : originalTags) {
            Query tagQuery = query.whereArrayContains("tag", tag);
            tagQueries.add(tagQuery);
        }

        if (!tagQueries.isEmpty()) {
            query = tagQueries.get(0);
            for (int i = 1; i < tagQueries.size(); i++) {
                query = FirebaseFirestore.getInstance().collectionGroup("products").whereEqualTo("tag", tagQueries.get(i));
            }
        }

        query = query.whereNotEqualTo("name", currentProductId);

        FirestoreRecyclerOptions<products> options = new FirestoreRecyclerOptions.Builder<products>()
                .setQuery(query, products.class)
                .setLifecycleOwner(this)
                .build();
        ProductAdapter.OnProductClickListener listener = product -> {
            Bundle bundle = new Bundle();
            bundle.putString("author", product.getAuthor());
            bundle.putString("imageurl", product.getImageurl());
            bundle.putString("name", product.getName());
            bundle.putFloat("price", product.getPrice());
            bundle.putFloat("quantity", product.getQuantity());
            bundle.putInt("date", product.getDate());
            bundle.putString("type", product.getType());
            bundle.putString("musicurl", product.getMusicurl());
            bundle.putStringArrayList("tag", new ArrayList<>(product.getTag()));

            navController.navigate(R.id.showProduct, bundle);
        };

        ProductAdapter adapter = new ProductAdapter(options, listener);
        recyclerView.setAdapter(adapter);
    }

    private void openCartFragment() {
        navController.navigate(R.id.cartFragment);
    }

    private void toggleFavoriteStatus() {
        // Load current favorite status
        boolean isFavorite = favoritesPreferences.getBoolean(name, false);
        isFavorite = !isFavorite; // Toggle favorite status

        // Save updated favorite status
        SharedPreferences.Editor editor = favoritesPreferences.edit();
        editor.putBoolean(name, isFavorite);
        editor.apply();

        // Update UI
        setFavoriteButtonImage(isFavorite);
    }

    private void setFavoriteStatus(boolean isFavorite) {
        setFavoriteButtonImage(isFavorite);
    }

    private void setFavoriteButtonImage(boolean isFavorite) {
        ImageButton favoriteButton = getView().findViewById(R.id.favorite_button);
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.fav);
        } else {
            favoriteButton.setImageResource(R.drawable.baseline_star_outline_24);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
