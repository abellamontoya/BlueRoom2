package com.example.blueroom;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Purchase {
    private Timestamp timestamp;
    private List<products> products;
    private double totalSpent;
    private String userEmail;

    public Purchase() {
        // Required empty public constructor for Firestore
    }

    public Purchase(Timestamp timestamp, List<products> products, double totalSpent, String userEmail) {
        this.timestamp = timestamp;
        this.products = products;
        this.totalSpent = totalSpent;
        this.userEmail = userEmail;
    }

    public List<products> getProducts() {
        return products;
    }

    public void setProducts(List<products> products) {
        this.products = products;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    // Modified to return a string representation of the timestamp
    public String getFormattedTimestamp() {
        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = timestamp.toDate();
            return sdf.format(date);
        } else {
            return "";
        }
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
