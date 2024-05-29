package com.example.blueroom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private String currentUserEmail;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.historyFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserEmail = user.getEmail();
            loadPurchaseHistory();
        }

        return view;
    }

    private void loadPurchaseHistory() {
        db.collection("history")
                .whereEqualTo("userEmail", currentUserEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Purchase> purchaseList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Purchase purchase = documentSnapshot.toObject(Purchase.class);
                        purchaseList.add(purchase);
                    }
                    HistoryAdapter adapter = new HistoryAdapter(purchaseList);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load purchase history", Toast.LENGTH_SHORT).show();
                });
    }

    public static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

        private List<Purchase> purchaseList;

        public HistoryAdapter(List<Purchase> purchaseList) {
            this.purchaseList = purchaseList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_recyclerview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Purchase purchase = purchaseList.get(position);
            holder.bind(purchase);
        }

        @Override
        public int getItemCount() {
            return purchaseList.size();
        }


        public static class ViewHolder extends RecyclerView.ViewHolder {
            private TextView orderDateTextView;
            private TextView orderItemsTextView;
            private TextView totalPriceTextView;
            private DecimalFormat priceFormat; // DecimalFormat for formatting price

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                orderDateTextView = itemView.findViewById(R.id.orderDateTextView);
                orderItemsTextView = itemView.findViewById(R.id.orderItemsTextView);
                totalPriceTextView = itemView.findViewById(R.id.totalPriceTextView);
                priceFormat = new DecimalFormat("#0.00€"); // Define the price format
            }

            public void bind(Purchase purchase) {
                // Bind data to views
                orderDateTextView.setText("Order Date: " + purchase.getFormattedTimestamp());
                totalPriceTextView.setText("Total Price: " + priceFormat.format(purchase.getTotalSpent())); // Format the price

                // Handle order items
                StringBuilder itemsText = new StringBuilder();
                List<products> products = purchase.getProducts();
                if (products != null && !products.isEmpty()) {
                    for (products product : products) {
                        itemsText.append(product.getName()).append(" - ").append(priceFormat.format(product.getPrice())).append("\n");
                    }
                } else {
                    itemsText.append("No items");
                }
                orderItemsTextView.setText("Items: \n" + itemsText.toString());
            }
        }

    }
}