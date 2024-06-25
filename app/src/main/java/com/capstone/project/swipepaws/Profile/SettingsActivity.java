package com.capstone.project.swipepaws.Profile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.capstone.project.swipepaws.Introduction.IntroductionMain;
import com.capstone.project.swipepaws.Login.Login;
import com.capstone.project.swipepaws.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private SeekBar distance;
    private TextView distance_text;
    private Spinner breedSpinner;

    private String userBreed;
    private String userPreferredBreed;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private DocumentReference userDocument;

    // SharedPreferences for storing user preferences
    private SharedPreferences preferences;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String PREF_MAX_DISTANCE = "MaxDistance";
    private static final String PREF_DOG_BREED = "DogBreed";
    private static final String PREF_TEMPERAMENTS = "DogTemperaments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView toolbar = findViewById(R.id.toolbartag);
        toolbar.setText("Profile");
        ImageButton back = findViewById(R.id.back);
        distance = findViewById(R.id.distance);
        distance_text = findViewById(R.id.distance_text);
        breedSpinner = findViewById(R.id.breedSpinner);

        findViewById(R.id.help_support_button).setOnClickListener(this::openFAQ);
        findViewById(R.id.logout_button).setOnClickListener(this::Logout);
        findViewById(R.id.delete_account_button).setOnClickListener(this::deleteAccount);

        Button cancelDeletionButton = findViewById(R.id.cancel_deletion_button);
        cancelDeletionButton.setOnClickListener(this::cancelAccountDeletion);

        Button resetFiltersButton = findViewById(R.id.reset_filters_button);
        resetFiltersButton.setOnClickListener(v -> resetFilters());

        Button saveFiltersButton = findViewById(R.id.save_filters);
        saveFiltersButton.setOnClickListener(this::saveFilters);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userDocument = db.collection("users").document(userId);
        }

        loadUserData();
        setupCheckboxListeners();

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int maxDistance = preferences.getInt(PREF_MAX_DISTANCE, 50);
        String dogBreed = preferences.getString(PREF_DOG_BREED, "");
        Set<String> savedTemperaments = preferences.getStringSet(PREF_TEMPERAMENTS, new HashSet<>());
        loadTemperamentPreferences(savedTemperaments);

        boolean deleteRequested = preferences.getBoolean("deleteRequested", false);
        if (deleteRequested) {
            cancelDeletionButton.setVisibility(View.VISIBLE);
        } else {
            cancelDeletionButton.setVisibility(View.GONE);
        }

        distance.setProgress(maxDistance);
        distance_text.setText(maxDistance + " Km");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.breed_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        breedSpinner.setAdapter(adapter);
        breedSpinner.setSelection(getIndex(breedSpinner, dogBreed));

        back.setOnClickListener(v -> finish());

        distance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance_text.setText(progress + " Km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Removed the call to savePreferences() from here
            }
        });

        breedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Removed the call to savePreferences() from here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void savePreferences() {
        int maxDistance = distance.getProgress();
        String dogBreed = breedSpinner.getSelectedItem().toString();
        Set<String> temperaments = getSelectedTemperaments();
        saveUserFilterPreferences(maxDistance, dogBreed, temperaments);
    }

    private void setupCheckboxListeners() {
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> savePreferences();

        ((CheckBox) findViewById(R.id.checkbox_friendly)).setOnCheckedChangeListener(listener);
        ((CheckBox) findViewById(R.id.checkbox_stable)).setOnCheckedChangeListener(listener);
        ((CheckBox) findViewById(R.id.checkbox_intelligent)).setOnCheckedChangeListener(listener);
        ((CheckBox) findViewById(R.id.checkbox_loyal)).setOnCheckedChangeListener(listener);
        ((CheckBox) findViewById(R.id.checkbox_energetic)).setOnCheckedChangeListener(listener);
        ((CheckBox) findViewById(R.id.checkbox_gentle)).setOnCheckedChangeListener(listener);
        ((CheckBox) findViewById(R.id.checkbox_confident)).setOnCheckedChangeListener(listener);
    }

    private void saveUserFilterPreferences(int maxDistance, String dogBreed, Set<String> temperaments) {
        Log.d(TAG, "Attempting to save filter preferences");
        if (userDocument != null) {
            List<String> temperamentList = new ArrayList<>(temperaments);

            WriteBatch batch = db.batch();
            Map<String, Object> updates = new HashMap<>();
            updates.put("preferredMaxDistance", maxDistance);
            updates.put("preferredDogBreed", dogBreed);
            updates.put("preferredTemperaments", temperamentList);

            batch.update(userDocument, updates);

            batch.commit().addOnSuccessListener(aVoid -> {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(PREF_MAX_DISTANCE, maxDistance);
                editor.putString(PREF_DOG_BREED, dogBreed);
                editor.putStringSet(PREF_TEMPERAMENTS, temperaments);
                editor.apply();

                Log.d(TAG, "Filter preferences successfully saved");
            }).addOnFailureListener(e -> {
                showToast("Failed to save filter preferences: " + e.getMessage());
            });
        } else {
            showToast("User is not authenticated. Please log in.");
            Log.d(TAG, "Attempted to save preferences without user authentication.");
        }
    }

    private Set<String> getSelectedTemperaments() {
        Set<String> selectedTemperaments = new HashSet<>();
        if (((CheckBox) findViewById(R.id.checkbox_friendly)).isChecked()) selectedTemperaments.add("friendly");
        if (((CheckBox) findViewById(R.id.checkbox_stable)).isChecked()) selectedTemperaments.add("stable");
        if (((CheckBox) findViewById(R.id.checkbox_intelligent)).isChecked()) selectedTemperaments.add("intelligent");
        if (((CheckBox) findViewById(R.id.checkbox_loyal)).isChecked()) selectedTemperaments.add("loyal");
        if (((CheckBox) findViewById(R.id.checkbox_energetic)).isChecked()) selectedTemperaments.add("energetic");
        if (((CheckBox) findViewById(R.id.checkbox_gentle)).isChecked()) selectedTemperaments.add("gentle");
        if (((CheckBox) findViewById(R.id.checkbox_confident)).isChecked()) selectedTemperaments.add("confident");
        return selectedTemperaments;
    }

    private void loadTemperamentPreferences(Set<String> savedTemperaments) {
        ((CheckBox) findViewById(R.id.checkbox_friendly)).setChecked(savedTemperaments.contains("friendly"));
        ((CheckBox) findViewById(R.id.checkbox_stable)).setChecked(savedTemperaments.contains("stable"));
        ((CheckBox) findViewById(R.id.checkbox_intelligent)).setChecked(savedTemperaments.contains("intelligent"));
        ((CheckBox) findViewById(R.id.checkbox_loyal)).setChecked(savedTemperaments.contains("loyal"));
        ((CheckBox) findViewById(R.id.checkbox_energetic)).setChecked(savedTemperaments.contains("energetic"));
        ((CheckBox) findViewById(R.id.checkbox_gentle)).setChecked(savedTemperaments.contains("gentle"));
        ((CheckBox) findViewById(R.id.checkbox_confident)).setChecked(savedTemperaments.contains("confident"));
    }
    public void deleteAccount(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Account");
            builder.setMessage("To confirm account deletion, please type 'DELETE' in the box below:");

            final EditText confirmationEditText = new EditText(this);
            builder.setView(confirmationEditText);

            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String confirmationText = confirmationEditText.getText().toString().trim();
                    if (confirmationText.equalsIgnoreCase("DELETE")) {
                        userDocument.update("deleteRequested", true, "deletionRequestDate", System.currentTimeMillis())
                                .addOnSuccessListener(aVoid -> {
                                    showToast("Your account deletion request has been received. Your account will be permanently deleted after a 30-day cooldown period.");
                                    Button deleteAccountButton = findViewById(R.id.delete_account_button);
                                    deleteAccountButton.setVisibility(View.GONE);

                                    Button cancelDeletionButton = findViewById(R.id.cancel_deletion_button);
                                    cancelDeletionButton.setVisibility(View.VISIBLE);
                                })
                                .addOnFailureListener(e -> {
                                    showToast("Failed to request account deletion: " + e.getMessage());
                                });
                    } else {
                        showToast("Confirmation text is incorrect. Account deletion request not confirmed.");
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else {
            showToast("User is not authenticated. Please log in.");
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }
    }

    public void cancelAccountDeletion(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userDocument.update("deleteRequested", false, "deletionRequestDate", null)
                    .addOnSuccessListener(aVoid -> {
                        showToast("Account deletion request has been canceled.");
                        Button cancelDeletionButton = findViewById(R.id.cancel_deletion_button);
                        cancelDeletionButton.setVisibility(View.GONE);

                        Button deleteAccountButton = findViewById(R.id.delete_account_button);
                        deleteAccountButton.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(e -> {
                        showToast("Failed to cancel account deletion request: " + e.getMessage());
                    });
        } else {
            showToast("User is not authenticated. Please log in.");
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }
    }

    private void setupBreedSpinnerBasedOnSize(String dogSize) {
        List<String> breeds = new ArrayList<>();
        breeds.add("Any Available Breed"); // Add this as the first item to prompt selection

        // Retrieve the previously selected breed from preferences
        String previouslySelectedBreed = preferences.getString(PREF_DOG_BREED, "");

        // Fetch all breeds
        String[] allBreeds = getResources().getStringArray(R.array.breed_array);

        // Filter breeds based on the current user's dog size
        for (String breed : allBreeds) {
            if (determineDogSize(breed).equals(dogSize)) {
                breeds.add(breed);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, breeds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        breedSpinner.setAdapter(adapter);

        // Set the selected breed to the previously selected breed, if available
        if (!previouslySelectedBreed.isEmpty()) {
            breedSpinner.setSelection(adapter.getPosition(previouslySelectedBreed));
        } else {
            // Optionally set "Select a breed" as the selected item
            breedSpinner.setSelection(0);
        }
    }


    private String determineDogSize(String breed) {
        if (breed.matches("Chihuahua|Corgi|Pomeranian|Poodle|Pug|Shih Tzu")) {
            return "Small";
        } else if (breed.matches("Askal/Aspin|Beagle|Dachshund|Pit Bull")) {
            return "Medium";
        } else {
            return "Large";
        }
    }

    public void resetFilters() {
        // Reset distance
        TextView distanceText = findViewById(R.id.distance_text);
        SeekBar distanceSeekBar = findViewById(R.id.distance);
        distanceSeekBar.setProgress(0); // Assuming 0 is the default value indicating "No Specific Distance"
        distanceText.setText("No Specific Distance");

        // Reset breed selection to the current set dog breed size
        Spinner breedSpinner = findViewById(R.id.breedSpinner);
        String dogSize = preferences.getString("dogSize", ""); // Retrieve the current dog size from preferences
        if (dogSize != null) {
            setupBreedSpinnerBasedOnSize(dogSize); // Set up the breed spinner based on the current dog size
        }

        // Reset temperaments checkboxes to unchecked
        ((CheckBox) findViewById(R.id.checkbox_friendly)).setChecked(false);
        ((CheckBox) findViewById(R.id.checkbox_stable)).setChecked(false);
        ((CheckBox) findViewById(R.id.checkbox_intelligent)).setChecked(false);
        ((CheckBox) findViewById(R.id.checkbox_loyal)).setChecked(false);
        ((CheckBox) findViewById(R.id.checkbox_energetic)).setChecked(false);
        ((CheckBox) findViewById(R.id.checkbox_gentle)).setChecked(false);
        ((CheckBox) findViewById(R.id.checkbox_confident)).setChecked(false);

        // After resetting all the filters, save the user's preferences
        savePreferences(); // This should call a method that saves these default preferences

        showToast("Filters reset to default values.");
    }

    public void saveFilters(View view) {
        // Call savePreferences() to save filter preferences
        savePreferences();
    }

    public void Logout(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), IntroductionMain.class));
                finishAffinity(); // Finish all activities in the task stack
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void loadUserData() {
        userDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    userBreed = documentSnapshot.getString("breed");
                    userPreferredBreed = documentSnapshot.getString("preferredDogBreed");
                    String dogSize = documentSnapshot.getString("dogSize");

                    Log.d(TAG, "The User Breed is " + userBreed);
                    Log.d(TAG, "The User Preferred Breed is " + userPreferredBreed);

                    setupBreedSpinnerBasedOnSize(dogSize);
                } else {
                    Log.d(TAG, "User document does not exist");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error retrieving user data: " + e.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void openFAQ(View view) {
        startActivity(new Intent(getApplicationContext(), FAQActivity.class));
    }

    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0; // Default to the first item if not found
    }
}
