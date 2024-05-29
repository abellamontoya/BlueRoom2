package com.example.blueroom;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FavouritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private SharedPreferences favoritesPreferences;
    private SharedPreferences cartPreferences;
    private FavProductAdapter adapter;

    public FavouritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favoritesPreferences = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE);
        cartPreferences = requireContext().getSharedPreferences("cart", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favourites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.fav_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        loadFavorites();
    }

    private void loadFavorites() {
        List<String> favoriteIds = new ArrayList<>();
        for (String key : favoritesPreferences.getAll().keySet()) {
            if (favoritesPreferences.getBoolean(key, false)) {
                favoriteIds.add(key);
            }
        }
        if (favoriteIds.isEmpty()) {
            TextView emptyMessageTextView = requireView().findViewById(R.id.empty_message);
            emptyMessageTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        List<String> cartProductIds = new ArrayList<>();
        for (String key : cartPreferences.getAll().keySet()) {
            if (cartPreferences.getBoolean(key, false)) {
                cartProductIds.add(key);
            }
        }

        Query query = FirebaseFirestore.getInstance().collection("products")
                .whereIn("name", favoriteIds);

        FirestoreRecyclerOptions<products> options = new FirestoreRecyclerOptions.Builder<products>()
                .setQuery(query, products.class)
                .setLifecycleOwner(this)
                .build();

        adapter = new FavProductAdapter(options, product -> {
            // Navegar al fragmento ShowProduct
            Bundle bundle = new Bundle();
            bundle.putString("name", product.getName());
            getParentFragmentManager().setFragmentResult("show_product", bundle);
        }, cartProductIds, product -> {
            MyApp myApp = (MyApp) requireActivity().getApplication();
            myApp.addProductToCart(product);

            SharedPreferences.Editor editor = cartPreferences.edit();
            editor.putBoolean(product.getName(), true);
            editor.apply();

            Toast.makeText(requireContext(), product.getName() + " added to cart", Toast.LENGTH_SHORT).show();
            loadFavorites(); // Refresh the adapter to show the updated cart status
        }, product -> {
            SharedPreferences.Editor editor = favoritesPreferences.edit();
            editor.remove(product.getName());
            editor.apply();

            Toast.makeText(requireContext(), product.getName() + " removed from favorites", Toast.LENGTH_SHORT).show();
            loadFavorites(); // Refresh the adapter to remove the item
        });


        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
