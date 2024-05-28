package com.example.blueroom;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignInFragment extends Fragment {

    NavController navController;
    private EditText emailEditText, passwordEditText;
    private Button emailSignInButton;
    private LinearLayout signInForm;
    private ProgressBar signInProgressBar;
    private FirebaseAuth mAuth;
    private SignInButton googleSignInButton;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        navController = Navigation.findNavController(view);
        sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Activity.MODE_PRIVATE);

        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        emailSignInButton = view.findViewById(R.id.emailSignInButton);
        signInForm = view.findViewById(R.id.signInForm);
        signInProgressBar = view.findViewById(R.id.signInProgressBar);
        googleSignInButton = view.findViewById(R.id.googleSignInButton);

        view.findViewById(R.id.gotoCreateAccountTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.registerFragment);
            }
        });

        emailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accederConEmail();
            }
        });

        emailSignInButton.setOnClickListener(v -> accederConEmail()); // Lambda for conciseness
        googleSignInButton.setOnClickListener(v -> accederConGoogle());

        // Initialize activityResultLauncher here to ensure it's available when needed
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            // Handle successful sign-in using a try-catch block
                            firebaseAuthWithGoogle(task.getResult(ApiException.class));
                        } catch (ApiException e) {
                            Log.e("ABCD", "signInResult:failed code=" + e.getStatusCode());
                            // Handle sign-in failure gracefully (e.g., show an error message)
                        }
                    }
                });
    }

    private void accederConEmail() {
        if (emailEditText != null && passwordEditText != null) { // Null check before proceeding
            signInForm.setVisibility(View.GONE);
            signInProgressBar.setVisibility(View.VISIBLE);

            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) { // Additional input validation
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity(), task -> {
                            if (task.isSuccessful()) {
                                // ... (your success logic) ...
                            } else {
                                // ... (your error handling logic) ...
                            }
                            signInForm.setVisibility(View.VISIBLE);
                            signInProgressBar.setVisibility(View.GONE);
                        });
            } else {
                // Show an error message if fields are empty
                Snackbar.make(requireView(), "Please enter email and password", Snackbar.LENGTH_LONG).show();
                signInForm.setVisibility(View.VISIBLE);
                signInProgressBar.setVisibility(View.GONE);
            }
        } else {
            // Handle the case where EditText views are still null (perhaps layout inflation is delayed)
            Log.e("SignInFragment", "EditText views are null!");
        }
    }

    private void actualizarUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            navController.navigate(R.id.homeFragment);
        }
    }

    private void accederConGoogle() {
        GoogleSignInClient googleSignInClient =
                GoogleSignIn.getClient(requireActivity(), new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build());
        activityResultLauncher.launch(googleSignInClient.getSignInIntent());
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        if (acct == null) return;
        signInProgressBar.setVisibility(View.VISIBLE);
        signInForm.setVisibility(View.GONE);

        String email = acct.getEmail(); // Obtener el correo electrónico del usuario

        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(acct.getIdToken(), null))
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Aquí puedes usar el correo electrónico para guardar o actualizar la información en la base de datos
                            saveUserToDatabase(email); // Llama a tu método para guardar la información en la base de datos
                            actualizarUI(mAuth.getCurrentUser());
                        } else {
                            Log.e("ABCD", "signInWithCredential:failure", task.getException());
                            signInProgressBar.setVisibility(View.GONE);
                            signInForm.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    private void saveUserToDatabase(String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("address", ""); // Valores por defecto o existentes
        user.put("city", "");
        user.put("country", "");
        user.put("id", "");
        user.put("phone", "");
        user.put("postal_code", "");
        user.put("textoEditable", "");

        db.collection("users").document(email)
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d("SaveUser", "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w("SaveUser", "Error writing document", e));
    }

}
