package com.example.blueroom;

import android.app.Application;
import java.util.ArrayList;

public class MyApp extends Application {
    private ArrayList<products> cartProducts = new ArrayList<>();

    public ArrayList<products> getCartProducts() {
        return cartProducts;
    }

    public void addProductToCart(products product) {
        cartProducts.add(product);
    }
}
