package com.example.blueroom;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.blueroom.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    private ArrayList<products> cartProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        NavController navController = ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.mobile_navigation)).getNavController();

        // Setup BottomNavigationView with NavController
        NavigationUI.setupWithNavController(binding.bottomNavView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                             @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if (destination.getId() == R.id.signInFragment || destination.getId() == R.id.registerFragment || destination.getId() == R.id.searchFragment || destination.getId() == R.id.showProduct) {
                    binding.bottomNavView.setVisibility(View.GONE);
                } else {
                    binding.bottomNavView.setVisibility(View.VISIBLE);
                }
            }
        });

        // Verificar si el usuario ha iniciado sesión
        if (isLoggedIn()) {
            // Navegar al fragmento de inicio (home)
            navController.navigate(R.id.homeFragment);
        }
    }

    // Método para verificar si el usuario ha iniciado sesión
    private boolean isLoggedIn() {
        // Verificar si "isLoggedIn" es true en SharedPreferences
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }
}
