package com.example.blueroom;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BuyFragment extends Fragment {

    private TextView priceOfProducts;
    private EditText countryEditText, addressEditText, postalCodeEditText,
            cardNumberEditText, expirationDateEditText, securityCodeEditText;
    private TextView finalPriceTextView;
    private Button confirmButton;
    private double totalPriceFromCart;
    private FirebaseFirestore db;
    private List<products> productsPurchased;

    public BuyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buy, container, false);

        countryEditText = view.findViewById(R.id.countryet);
        addressEditText = view.findViewById(R.id.addresset);
        postalCodeEditText = view.findViewById(R.id.postalcodeet);
        cardNumberEditText = view.findViewById(R.id.cardnumber_edit);
        expirationDateEditText = view.findViewById(R.id.expirationdate_edit);
        securityCodeEditText = view.findViewById(R.id.securitycode_edit);
        confirmButton = view.findViewById(R.id.confirmbutton);
        priceOfProducts = view.findViewById(R.id.priceofprods);
        finalPriceTextView = view.findViewById(R.id.finalprice);

        if (getArguments() != null) {
            totalPriceFromCart = getArguments().getDouble("totalPrice", 0.0);
        }

        // Set the product price immediately
        setProductPrice();

        // Load user profile data
        loadProfileData();

        // Set up text change listener
        setUpTextChangeListener();

        // Initially disable the confirm button
        confirmButton.setEnabled(false);

        // Set up OnClickListener for the confirmButton
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (areAllFieldsFilled()) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Confirm Purchase")
                            .setMessage("Are you sure you want to confirm the purchase?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                savePurchaseData();
                                Navigation.findNavController(view).navigate(R.id.homeFragment);
                            })
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        db = FirebaseFirestore.getInstance();
        productsPurchased = ((MyApp) requireActivity().getApplication()).getCartProducts();
        if (productsPurchased == null) {
            productsPurchased = new ArrayList<>();
        }
        return view;
    }

    private void savePurchaseData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userUid = user.getUid();
            List<String> productNames = new ArrayList<>();
            for (products product : productsPurchased) {
                productNames.add(product.getName()); // Assuming you have a getName() method in your Product class
            }

            Map<String, Object> purchaseData = new HashMap<>();
            purchaseData.put("userUid", userUid);
            purchaseData.put("purchase", productNames);
            purchaseData.put("totalSpent", totalPriceFromCart);
            purchaseData.put("timestamp", FieldValue.serverTimestamp());

            db.collection("history")
                    .add(purchaseData)
                    .addOnSuccessListener(documentReference -> {
                        MyApp myApp = (MyApp) requireActivity().getApplication();
                        myApp.clearCart();
                        Toast.makeText(getContext(), "Purchase saved successfully", Toast.LENGTH_SHORT).show();
                        if (user != null) {
                            String userEmail = user.getEmail();
                            String subject = "Order Confirmation";
                            String messageContent = "Thank you for your purchase!";
                            SendGridHelper.sendEmail(userEmail, subject, messageContent);

                            Toast.makeText(getContext(), "Confirmation email sent", Toast.LENGTH_SHORT).show();
                        }
                        // Send confirmation email after successful save
                        sendConfirmationEmail(user.getEmail()); // <-- Add this line
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error saving purchase", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendConfirmationEmail(String userEmail) {
        String subject = "Order Confirmation";
        String message = "Thank you for your purchase!\n\n" +
                "Your order details:\n" +
                String.join("\n", productsPurchased.stream().map(products::getName).toArray(String[]::new)) +
                "\n\nTotal: " + String.format(Locale.getDefault(), "%.2f€", totalPriceFromCart);

        SendGridHelper.sendEmail(userEmail, subject, message);
    }

    // Helper method to check if all fields are filled
    private boolean areAllFieldsFilled() {
        return !countryEditText.getText().toString().trim().isEmpty() &&
                !addressEditText.getText().toString().trim().isEmpty() &&
                !postalCodeEditText.getText().toString().trim().isEmpty() &&
                !cardNumberEditText.getText().toString().trim().isEmpty() &&
                !expirationDateEditText.getText().toString().trim().isEmpty() &&
                !securityCodeEditText.getText().toString().trim().isEmpty();
    }

    private void setProductPrice() {
        priceOfProducts.setText(String.format(Locale.getDefault(), "%.2f€", totalPriceFromCart));
        double finalPrice = calculateFinalPrice(totalPriceFromCart);
        finalPriceTextView.setText(String.format(Locale.getDefault(), "%.2f€", finalPrice));
    }

    private void loadProfileData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userUid = user.getUid();

            DocumentReference userDocRef = db.collection("users").document(userUid);

            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String country = documentSnapshot.getString("country");
                    String address = documentSnapshot.getString("address");
                    String postalCode = documentSnapshot.getString("postal_code");

                    countryEditText.setText(country);
                    addressEditText.setText(address);
                    postalCodeEditText.setText(postalCode);
                } else {
                    Log.d("BuyFragment", "No such document");
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e("BuyFragment", "User is null. Profile data cannot be loaded.");
        }
    }


    private double calculateFinalPrice(double totalPrice) {
        // Add a fixed shipping fee or any other additional costs
        return totalPrice + 3.99;
    }

    private void setUpTextChangeListener() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                confirmButton.setEnabled(areAllFieldsFilled());
            }
        };

        countryEditText.addTextChangedListener(textWatcher);
        addressEditText.addTextChangedListener(textWatcher);
        postalCodeEditText.addTextChangedListener(textWatcher);
        cardNumberEditText.addTextChangedListener(textWatcher);
        expirationDateEditText.addTextChangedListener(textWatcher);
        securityCodeEditText.addTextChangedListener(textWatcher);
    }
}
