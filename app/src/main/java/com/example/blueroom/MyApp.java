package com.example.blueroom;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;

public class MyApp extends Application {
    private ArrayList<products> cartProducts = new ArrayList<>();

    public ArrayList<products> getCartProducts() {
        return cartProducts;
    }

    public void addProductToCart(products product) {
        Log.d("MyApp", "Adding product: " + product.getName());
        for (products p : cartProducts) {
            if (p.getImageurl().equals(product.getImageurl())) {
                p.setQuantity(p.getQuantity() + 1);
                Log.d("MyApp", "Product exists. New quantity: " + p.getQuantity());
                return;
            }
        }
        product.setQuantity(1);
        cartProducts.add(product);
        Log.d("MyApp", "New product added with quantity: 1");
    }
    public void clearCart() {
        cartProducts.clear();
    }
}
