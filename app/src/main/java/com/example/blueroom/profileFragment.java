package com.example.blueroom;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class profileFragment extends Fragment {

    private EditText countryEditText;
    private EditText phoneNumberEditText;
    private EditText addressEditText;
    private EditText postalCodeEditText;
    private EditText cityEditText;
    private EditText idEditText;

    private EditText textoEditable;

    private Button editButton;
    private ImageButton dehaze_button;

    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String COUNTRY_KEY = "country";
    private static final String PHONE_KEY = "phone";
    private static final String ADDRESS = "address";
    private static final String POSTAL_CODE = "postal_code";
    private static final String CITY = "city";
    private static final String ID = "id";
    private static final String TEXTO_KEY = "textoEditable";

    private boolean isEditMode = false;

    public profileFragment() {
        // Required empty public constructor
    }

    public static profileFragment newInstance() {
        return new profileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        countryEditText = view.findViewById(R.id.countryet);
        phoneNumberEditText = view.findViewById(R.id.phonenumberet);
        addressEditText = view.findViewById(R.id.addresset);
        postalCodeEditText = view.findViewById(R.id.postalcodeet);
        cityEditText = view.findViewById(R.id.cityet);
        idEditText = view.findViewById(R.id.idet);
        textoEditable = view.findViewById(R.id.texto_editable);

        editButton = view.findViewById(R.id.edit);
        ImageButton menuImageButton = view.findViewById(R.id.dehaze_button);

        menuImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(requireContext(), v);
                popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId(); // Store the item ID
                    if (itemId == R.id.action_shopping_history) {
                        // Navigate to shopping history
                        Navigation.findNavController(getView()).navigate(R.id.historyFragment);
                        return true;
                    } else if (itemId == R.id.action_logout) {
                        // Perform logout
                        FirebaseAuth.getInstance().signOut();
                        Navigation.findNavController(getView()).navigate(R.id.signInFragment);
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditMode();
            }
        });

        // Load saved data
        loadData();

        // Initially set EditText fields to be non-editable
        setFieldsEditable(false);

        return view;
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;

        if (isEditMode) {
            editButton.setText("Save Info");
            setFieldsEditable(true);
        } else {
            editButton.setText("Edit Info");
            setFieldsEditable(false);
            saveData();
        }
    }


    private void setFieldsEditable(boolean editable) {
        countryEditText.setEnabled(editable);
        countryEditText.setInputType(editable ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
        phoneNumberEditText.setEnabled(editable);
        phoneNumberEditText.setInputType(editable ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_NULL);
        addressEditText.setEnabled(editable);
        addressEditText.setInputType(editable ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
        postalCodeEditText.setEnabled(editable);
        postalCodeEditText.setInputType(editable ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_NULL);
        cityEditText.setEnabled(editable);
        cityEditText.setInputType(editable ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
        idEditText.setEnabled(editable);
        idEditText.setInputType(editable ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);

        // Enable/disable editing for the TextView
        textoEditable.setEnabled(editable);
        textoEditable.setInputType(editable ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
    }

    private void saveData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userEmail = user.getEmail(); // Obtener el correo electrónico del usuario

            DocumentReference userDocRef = db.collection("users").document(userEmail);

            Map<String, Object> profileData = new HashMap<>();
            profileData.put(COUNTRY_KEY, countryEditText.getText().toString().trim());
            profileData.put(PHONE_KEY, phoneNumberEditText.getText().toString().trim());
            profileData.put(ADDRESS, addressEditText.getText().toString().trim());
            profileData.put(POSTAL_CODE, postalCodeEditText.getText().toString().trim());
            profileData.put(CITY, cityEditText.getText().toString().trim());
            profileData.put(ID, idEditText.getText().toString().trim());
            profileData.put(TEXTO_KEY, textoEditable.getText().toString().trim());

            userDocRef.set(profileData)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Datos del perfil guardados correctamente en Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error al guardar los datos del perfil en Firestore", e));
        } else {
            Log.e(TAG, "El correo electrónico del usuario es nulo o está vacío. No se pueden guardar los datos del perfil.");
        }
    }




    private void loadData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userEmail = user.getEmail(); // Get the user's email

            DocumentReference userDocRef = db.collection("users").document(userEmail);

            userDocRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String country = documentSnapshot.getString(COUNTRY_KEY);
                            String phoneNumber = documentSnapshot.getString(PHONE_KEY);
                            String address = documentSnapshot.getString(ADDRESS);
                            String postalCode = documentSnapshot.getString(POSTAL_CODE);
                            String city = documentSnapshot.getString(CITY);
                            String id = documentSnapshot.getString(ID);
                            String editableText = documentSnapshot.getString(TEXTO_KEY);

                            // Set the EditText and TextView values
                            countryEditText.setText(country);
                            phoneNumberEditText.setText(phoneNumber);
                            addressEditText.setText(address);
                            postalCodeEditText.setText(postalCode);
                            cityEditText.setText(city);
                            idEditText.setText(id);
                            textoEditable.setText(editableText);
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error fetching profile data from Firestore", e));
        } else {
            Log.e(TAG, "User email is null or empty. Profile data cannot be loaded.");
        }
    }
}