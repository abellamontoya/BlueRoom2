package com.example.blueroom;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Locale;

public class CartFragment extends Fragment {

    private ArrayList<products> cartProducts;
    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private Button buyButton;


    public CartFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApp myApp = (MyApp) requireActivity().getApplication();
        cartProducts = myApp.getCartProducts();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.cartFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartAdapter(cartProducts);
        recyclerView.setAdapter(adapter);

        buyButton = view.findViewById(R.id.buycart);

        // Initially, update the Buy button
        updateBuyButton();

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Calculate total price
                double totalPrice = calculateTotalPrice();

                // Navigate to BuyFragment and pass the total price as an argument
                Bundle args = new Bundle();
                args.putDouble("totalPrice", totalPrice);
                Navigation.findNavController(v).navigate(R.id.buyFragment, args);
            }
        });

        return view;
    }

    // Update the text and clickability of the Buy button
    private void updateBuyButton() {
        double totalPrice = calculateTotalPrice();
        String buttonText = String.format(Locale.getDefault(), "Buy (%.2f €)", totalPrice);
        buyButton.setText(buttonText);
        buyButton.setEnabled(!cartProducts.isEmpty());
    }

    // Calculate the total price of products in the cart
    private double calculateTotalPrice() {
        double totalPrice = 0;
        for (products product : cartProducts) {
            totalPrice += product.getPrice() * product.getQuantity();
        }
        return totalPrice;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {

        TextView name, author, price, quantity;
        ImageView imageurl;
        ImageButton deleteButton, incrementButton, decrementButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.albumname);
            author = itemView.findViewById(R.id.authorname);
            price = itemView.findViewById(R.id.priceoftheitem);
            imageurl = itemView.findViewById(R.id.albumcover);
            quantity = itemView.findViewById(R.id.quantity);
            deleteButton = itemView.findViewById(R.id.deleteitem);
            incrementButton = itemView.findViewById(R.id.increment);
            decrementButton = itemView.findViewById(R.id.decrement);
        }

        public void bind(products product, CartAdapter adapter) {
            name.setText(product.getName());
            author.setText(product.getAuthor());
            price.setText(String.format(Locale.getDefault(), "%.2f €", product.getPrice()));
            quantity.setText(String.format("Quantity: %.0f", product.getQuantity())); // Updated to display quantity as a whole number

            Glide.with(itemView.getContext()).load(product.getImageurl()).into(imageurl);

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    adapter.removeItem(position);
                }
            });

            incrementButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && product.getQuantity() < 10) { // Límite máximo de cantidad
                    product.setQuantity(product.getQuantity() + 1);
                    adapter.updateItem(position, product); // Actualizar el elemento en el adaptador
                } else {
                    Toast.makeText(v.getContext(), "Maximum quantity reached", Toast.LENGTH_SHORT).show(); // Mensaje de límite
                }
            });

            decrementButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && product.getQuantity() > 1) { // Límite mínimo de cantidad (1)
                    product.setQuantity(product.getQuantity() - 1);
                    adapter.updateItem(position, product); // Actualizar el elemento en el adaptador
                }
            });

            // Ocultar el botón de decremento si la cantidad es 1
            decrementButton.setVisibility(product.getQuantity() > 1 ? View.VISIBLE : View.GONE);
        }
    }


    class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {

        private ArrayList<products> cartProducts;
        private static final String TAG = "CartAdapter";

        public CartAdapter(ArrayList<products> cartProducts) {
            this.cartProducts = cartProducts;
        }

        public void addItem(products product) {
            Log.d(TAG, "addItem called with product: " + product.getName()); // Log entry
            // Check if the product is already in the cart
            for (products p : cartProducts) {
                if (p.getImageurl().equals(product.getImageurl())) {
                    // If yes, increment the quantity and update the adapter
                    p.setQuantity(p.getQuantity() + 1); // Increment the quantity
                    Log.d(TAG, "Product already in cart. Incrementing quantity to: " + p.getQuantity()); // Log quantity
                    notifyDataSetChanged();
                    updateBuyButton();
                    return;
                }
            }
            // If not, add the product to the cart (with default quantity 1)
            product.setQuantity(1);
            cartProducts.add(product);
            Log.d(TAG, "Product added to cart with quantity: " + product.getQuantity()); // Log new addition
            notifyItemInserted(cartProducts.size() - 1);
            updateBuyButton();
        }

        public void updateItem(int position, products updatedProduct) {
            cartProducts.set(position, updatedProduct);
            notifyItemChanged(position);
            updateBuyButton();
        }

        @NonNull
        @Override
        public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_recyclerview, parent, false);
            return new CartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
            holder.bind(cartProducts.get(position), this);
        }

        @Override
        public int getItemCount() {
            return cartProducts.size();
        }

        public void removeItem(int position) {
            cartProducts.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartProducts.size());
            updateBuyButton();
        }

        private void updateBuyButton() {
            CartFragment.this.updateBuyButton();
        }
    }
}
