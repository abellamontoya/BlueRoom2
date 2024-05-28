package com.example.blueroom;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    private RecyclerView recyclerView;
    private NavController navController;
    private AppViewModel appViewModel;
    private ImageView photoImageView;
    private ProductAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        Spinner spinner2 = view.findViewById(R.id.spinner2);
        CheckBox checkVinyl = view.findViewById(R.id.checkVinyl);
        CheckBox checkCd = view.findViewById(R.id.checkCd);


        appViewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);

        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        Query query = FirebaseFirestore.getInstance().collection("products").orderBy("name");

        FirestoreRecyclerOptions<products> options = new FirestoreRecyclerOptions.Builder<products>()
                .setQuery(query, products.class)
                .setLifecycleOwner(this)
                .build();

        Spinner spinner = view.findViewById(R.id.spinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Query newQuery;
                switch (position) {
                    case 0: // Price Up
                        newQuery = FirebaseFirestore.getInstance().collection("products").orderBy("price", Query.Direction.ASCENDING);
                        break;
                    case 1: // Price Down
                        newQuery = FirebaseFirestore.getInstance().collection("products").orderBy("price", Query.Direction.DESCENDING);
                        break;
                    case 2: // Author
                        newQuery = FirebaseFirestore.getInstance().collection("products").orderBy("author");
                        break;
                    case 3: // Default (by name)
                    default:
                        newQuery = FirebaseFirestore.getInstance().collection("products").orderBy("name");
                        break;
                }

                FirestoreRecyclerOptions<products> newOptions = new FirestoreRecyclerOptions.Builder<products>()
                        .setQuery(newQuery, products.class)
                        .setLifecycleOwner(HomeFragment.this)
                        .build();

                adapter.updateOptions(newOptions);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed here
            }
        });


        adapter = new ProductAdapter(options, this);
        recyclerView.setAdapter(adapter);

        photoImageView = view.findViewById(R.id.image);
        Uri photoUri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        if (photoUri != null && photoImageView != null) {
            String url = photoUri.toString();
            Glide.with(requireContext()).load(url).circleCrop().into(photoImageView);
        } else {
            if (photoImageView != null) {
                photoImageView.setImageResource(R.drawable.user);
            }
        }
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

    @Override
    public void onProductClick(products product) {
        Bundle bundle = new Bundle();
        bundle.putString("author", product.getAuthor());
        bundle.putString("imageurl", product.getImageurl());
        bundle.putString("name", product.getName());
        bundle.putFloat("price", product.getPrice());
        bundle.putDouble("quantity", product.getQuantity());
        bundle.putString("type", product.getType()); // Obtener el tipo del producto
        bundle.putStringArrayList("tag", new ArrayList<>(product.getTag())); // Obtener la lista de tags del producto

        navController.navigate(R.id.showProduct, bundle);
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageurl;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageurl = itemView.findViewById(R.id.imageurl);
        }

        public void bind(products product, ProductAdapter.OnProductClickListener listener) {
            Glide.with(itemView.getContext()).load(product.getImageurl()).into(imageurl);
            imageurl.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}
