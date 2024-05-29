package com.example.blueroom;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.example.blueroom.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    private ArrayList<products> cartProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        NavController navController = ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.mobile_navigation)).getNavController();

        NavigationUI.setupWithNavController(binding.bottomNavView, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                             @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if (destination.getId() == R.id.signInFragment || destination.getId() == R.id.registerFragment
                        || destination.getId() == R.id.showProduct || destination.getId() == R.id.buyFragment || destination.getId() == R.id.favouritesFragment) {
                    binding.bottomNavView.setVisibility(View.GONE);
                } else {
                    binding.bottomNavView.setVisibility(View.VISIBLE);
                }
            }
        });

        if (isLoggedIn()) {
            navController.navigate(R.id.homeFragment);
        }
    }
    @Override
    public void onBackPressed() {
        NavController navController = Navigation.findNavController(this, R.id.mobile_navigation);
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.mobile_navigation)
                .getChildFragmentManager()
                .getFragments()
                .get(0);

        // Updated list of restricted fragments
        List<Integer> restrictedFragments = Arrays.asList(
                R.id.homeFragment, R.id.cartFragment, R.id.profileFragment);

        if (currentFragment != null && restrictedFragments.contains(currentFragment.getId())) {
            // Do nothing to disable back press on restricted fragments
            return;
        } else {
            // If not a restricted fragment, check bottom nav visibility
            if (binding.bottomNavView.getVisibility() == View.VISIBLE) {
                super.onBackPressed(); // Allow normal back behavior
            } else {
                // If bottom nav is hidden, navigate to the home fragment
                navController.navigate(R.id.homeFragment);
            }
        }
    }


    private boolean isLoggedIn() {
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }
}
