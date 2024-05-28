package com.example.blueroom;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class AddProductFragment extends Fragment {

    private EditText productNameEditText;
    private EditText productAuthorEditText;
    private EditText productPriceEditText;
    private EditText productQuantityEditText;
    private Button saveButton;
    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get NavController from the activity
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

        productNameEditText = view.findViewById(R.id.productNameEditText);
        productAuthorEditText = view.findViewById(R.id.productAuthorEditText);
        productPriceEditText = view.findViewById(R.id.productPriceEditText);
        productQuantityEditText = view.findViewById(R.id.productQuantityEditText);
        saveButton = view.findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = productNameEditText.getText().toString();
                String author = productAuthorEditText.getText().toString();
                float price = Float.parseFloat(productPriceEditText.getText().toString());
                int quantity = (int) Float.parseFloat(productQuantityEditText.getText().toString());

                // Create a new product object
                products newProduct = new products("", name, author, price, quantity); // Assuming imageURL is empty

                // Save the new product to Firestore
                FirebaseFirestore.getInstance().collection("products")
                        .add(newProduct)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(getActivity(), "Product added successfully!", Toast.LENGTH_SHORT).show();

                            // Navigate back to HomeFragment
                            navController.navigateUp();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "Failed to add product.", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
}