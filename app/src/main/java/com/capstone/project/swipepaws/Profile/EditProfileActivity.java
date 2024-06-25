package com.capstone.project.swipepaws.Profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.capstone.project.swipepaws.Login.RegisterGender;
import com.capstone.project.swipepaws.Login.RegisterGenderPrefection;
import com.capstone.project.swipepaws.R;
import com.google.android.gms.internal.location.zzz;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private ImageButton back;
    private static final int PICK_PDF_REQUEST = 1;

    private boolean breedUpdated = false;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    ImageView[] imageViews = new ImageView[6];
    Bitmap[] myBitmaps = new Bitmap[6];
    Uri[] picUris = new Uri[6];
    String[] permissionsRequired = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Context mContext = EditProfileActivity.this;

    private String userId;
    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;
    private static final int REQUEST_CAMERA = 2;
    private static final int SELECT_FILE = 3;

    private int currentImageViewIndex;
    private Spinner dogBreedSpinner;
    private String selectedBreed;

    private Button updateLocationButton;
    private EditText dogBioEditText;
    private TextView dogLocationTextView;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);
        requestMultiplePermissions();

        imageViews[0] = findViewById(R.id.image_view_1);
        imageViews[1] = findViewById(R.id.image_view_2);
        imageViews[2] = findViewById(R.id.image_view_3);
        imageViews[3] = findViewById(R.id.image_view_4);
        imageViews[4] = findViewById(R.id.image_view_5);
        imageViews[5] = findViewById(R.id.image_view_6);

        dogBreedSpinner = findViewById(R.id.breedSpinner);
        dogBioEditText = findViewById(R.id.dogBio);
        dogLocationTextView = findViewById(R.id.dogLocation);
        updateLocationButton = findViewById(R.id.updateLocation);

        loadProfileImages();
        loadProfileData();
        getCityFromFirestore();
        loadDogTemperaments();

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        for (int i = 0; i < 6; i++) {
            final int currentIndex = i;
            imageViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentImageViewIndex = currentIndex;
                    proceedAfterPermission();
                }
            });
        }

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        dogBreedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected breed from the spinner
                selectedBreed = (String) parentView.getItemAtPosition(position);

                // Retrieve user data, including dog breed, from Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DocumentReference userRef = db.collection("users").document(userId);

                userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Retrieve the current dog breed from the database
                            String userDogBreed = documentSnapshot.getString("breed");

                            // Check if the selected breed is different from the current one
                            if (!selectedBreed.equals(userDogBreed)) {
                                // Update the Firestore document with the new breed value
                                updateBreedInFirestore(selectedBreed);
                            }
                        }
                    }
                });

                // Show the toast message only when the breed is updated
                if (breedUpdated) {
                    Toast.makeText(EditProfileActivity.this, "Breed updated successfully", Toast.LENGTH_SHORT).show();
                    breedUpdated = false; // Reset the flag
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });

        updateLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call the method to retrieve location and save to the database
                retrieveLocationAndSaveToDatabase();
            }
        });

        Button updateMedicalRecordsButton = findViewById(R.id.updateMedicalRecords);
        updateMedicalRecordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_PDF_REQUEST:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    Uri pdfUri = data.getData();
                    uploadFile(pdfUri);
                }
                break;
            case REQUEST_CAMERA:
                if (resultCode == RESULT_OK && data != null) {
                    onCaptureImageResult(data);
                }
                break;
            case SELECT_FILE:
                if (resultCode == RESULT_OK && data != null) {
                    onSelectFromGalleryResult(data);
                }
                break;
            case REQUEST_PERMISSION_SETTING:
                // Check if the permission request was successful
                if (ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                    // Handle the case where permission is granted
                    proceedAfterPermission();
                }
                break;
            default:
                // This can log an unexpected result which could help in debugging
                Log.e(TAG, "Unexpected request code");
        }
    }


    private void uploadFile(Uri pdfUri) {
        if (pdfUri != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String fileExtension = getFileExtension(pdfUri);
            StorageReference fileReference = FirebaseStorage.getInstance().getReference()
                    .child("medical_records/" + userId + "." + fileExtension);

            fileReference.putFile(pdfUri).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    updateMedicalRecordInFirestore(downloadUri.toString());
                    Toast.makeText(EditProfileActivity.this, "Medical record updated successfully", Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to get the download URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(EditProfileActivity.this, "Failed to upload file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } else {
            Toast.makeText(EditProfileActivity.this, "No file selected", Toast.LENGTH_LONG).show();
        }
    }


    private String getFileExtension(Uri uri) {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(getContentResolver().getType(uri));
    }

    private void updateMedicalRecordInFirestore(String newUri) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("medicalRecordUri", newUri)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Medical record updated successfully", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to update medical record", Toast.LENGTH_LONG).show();
                });
    }


    // Add this function to save dogBio when the user interacts with other parts of the activity
    private void saveDogBio() {
        String dogBio = dogBioEditText.getText().toString().trim();

        // Check if the user is authenticated
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Get the user's UID
            String userId = user.getUid();

            // Reference to the Firestore document
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(userId);

            // Create a map to update the dogBio field
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("dogBio", dogBio);

            // Update the Firestore document with the new dogBio value
            userRef.update(updateData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EditProfileActivity.this, "Dog Bio updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(EditProfileActivity.this, "Failed to update Dog Bio", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // User is not authenticated, handle this case accordingly
            Toast.makeText(EditProfileActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }


    private void loadDogTemperaments() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference temperamentsRef = db.collection("users").document(user.getUid()).collection("dogTemperaments").document("temperamentData");

            temperamentsRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        // Load the state of each temperament and set the checkbox accordingly
                        CheckBox checkboxFriendly = findViewById(R.id.checkbox_friendly);
                        Boolean isFriendly = documentSnapshot.getBoolean("friendly");
                        checkboxFriendly.setChecked(isFriendly != null && isFriendly);

                        CheckBox checkboxStable = findViewById(R.id.checkbox_stable);
                        Boolean isStable = documentSnapshot.getBoolean("stable");
                        checkboxStable.setChecked(isStable != null && isStable);

                        // Repeat for the rest of your checkboxes...
                        CheckBox checkboxIntelligent = findViewById(R.id.checkbox_intelligent);
                        Boolean isIntelligent = documentSnapshot.getBoolean("intelligent");
                        checkboxIntelligent.setChecked(isIntelligent != null && isIntelligent);

                        CheckBox checkboxLoyal = findViewById(R.id.checkbox_loyal);
                        Boolean isLoyal = documentSnapshot.getBoolean("loyal");
                        checkboxLoyal.setChecked(isLoyal != null && isLoyal);

                        CheckBox checkboxEnergetic = findViewById(R.id.checkbox_energetic);
                        Boolean isEnergetic = documentSnapshot.getBoolean("energetic");
                        checkboxEnergetic.setChecked(isEnergetic != null && isEnergetic);

                        CheckBox checkboxGentle = findViewById(R.id.checkbox_gentle);
                        Boolean isGentle = documentSnapshot.getBoolean("gentle");
                        checkboxGentle.setChecked(isGentle != null && isGentle);

                        CheckBox checkboxConfident = findViewById(R.id.checkbox_confident);
                        Boolean isConfident = documentSnapshot.getBoolean("confident");
                        checkboxConfident.setChecked(isConfident != null && isConfident);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error loading temperaments", e);
                }
            });
        }
    }

    private void saveDogTemperaments() {
        CheckBox checkboxFriendly = findViewById(R.id.checkbox_friendly);
        CheckBox checkboxStable = findViewById(R.id.checkbox_stable);
        CheckBox checkboxIntelligent = findViewById(R.id.checkbox_intelligent);
        CheckBox checkboxLoyal = findViewById(R.id.checkbox_loyal);
        CheckBox checkboxEnergetic = findViewById(R.id.checkbox_energetic);
        CheckBox checkboxGentle = findViewById(R.id.checkbox_gentle);
        CheckBox checkboxConfident = findViewById(R.id.checkbox_confident);

        Map<String, Object> temperaments = new HashMap<>();
        temperaments.put("friendly", checkboxFriendly.isChecked());
        temperaments.put("stable", checkboxStable.isChecked());
        temperaments.put("intelligent", checkboxIntelligent.isChecked());
        temperaments.put("loyal", checkboxLoyal.isChecked());
        temperaments.put("energetic", checkboxEnergetic.isChecked());
        temperaments.put("gentle", checkboxGentle.isChecked());
        temperaments.put("confident", checkboxConfident.isChecked());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference temperamentsRef = db.collection("users").document(user.getUid()).collection("dogTemperaments").document("temperamentData");

            temperamentsRef.set(temperaments, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Temperaments successfully written!");
                        showToast("Temperaments saved successfully.");
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error writing temperaments", e);
                        showToast("Failed to save temperaments: " + e.getMessage());
                    });
        } else {
            showToast("User not authenticated. Please log in again.");
        }
    }

    private void showToast(String message) {
        Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
    }



    private void updateBreedInFirestore(String newBreed) {
        // Check if the user is authenticated
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Get the user's UID
            String userId = user.getUid();

            // Reference to the Firestore document
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(userId);

            // Retrieve user data, including dog breed, from Firestore
            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        // Retrieve the current dog breed from the database
                        String userDogBreed = documentSnapshot.getString("breed");

                        // Check if the selected breed is different from the current one
                        if (!newBreed.equals(userDogBreed)) {
                            // Create a map to update the breed field
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("breed", newBreed);

                            // Update the Firestore document with the new breed value
                            userRef.update(updateData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Set the flag to true when the breed is updated
                                            breedUpdated = true;
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(EditProfileActivity.this, "Failed to update breed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }
            });
        } else {
            // User is not authenticated, handle this case accordingly
            Toast.makeText(EditProfileActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private int getDogBreedPosition(String userDogBreed) {
        // Retrieve the array of dog breeds from strings.xml
        String[] dogBreeds = getResources().getStringArray(R.array.breed_array);

        for (int i = 0; i < dogBreeds.length; i++) {
            if (dogBreeds[i].equals(userDogBreed)) {
                return i;
            }
        }

        // If the user's dog breed is not found, return a default position (0 in this case)
        return 0;
    }
    private void loadProfileData() {
        // Retrieve user data, including dog breed and dog bio, from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Retrieve the dog breed from the database
                    String userDogBreed = documentSnapshot.getString("breed");

                    // Retrieve the dog bio from the database
                    String userDogBio = documentSnapshot.getString("dogBio");

                    // Debugging: Print the retrieved userDogBreed to logcat
                    Log.d(TAG, "Retrieved userDogBreed: " + userDogBreed);

                    // Set the selected breed
                    selectedBreed = userDogBreed;

                    // Set the dog breed spinner selection
                    int dogBreedPosition = getDogBreedPosition(userDogBreed);
                    Log.d(TAG, "Dog breed position: " + dogBreedPosition);

                    if (dogBreedPosition >= 0) {
                        dogBreedSpinner.setSelection(dogBreedPosition);
                    } else {
                        // Debugging: Print a message if the position is not valid
                        Log.d(TAG, "Invalid dog breed position");

                        // Set a default breed position (e.g., 0) as the selection
                        dogBreedSpinner.setSelection(0);
                    }

                    // Set the text in the dogBioEditText
                    dogBioEditText.setText(userDogBio);
                }
            }
        });
    }

    private void retrieveLocationAndSaveToDatabase() {
        try {
            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        Location lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            double latitude = lastKnownLocation.getLatitude();
                            double longitude = lastKnownLocation.getLongitude();
                            String cityName = getCityName(latitude, longitude);

                            if (cityName != null) {
                                Map<String, Object> locationData = new HashMap<>();
                                locationData.put("latitude", latitude);
                                locationData.put("longitude", longitude);
                                locationData.put("city", cityName);

                                // Here, you can save the location data to the database or do whatever you need with it.
                                // For example, if you want to save it to the database:
                                String userId = mAuth.getCurrentUser().getUid();
                                db.collection("users").document(userId)
                                        .set(locationData, SetOptions.merge())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Location updated successfully
                                                    // You can inform the user if needed
                                                    // For example, show a Toast
                                                    Toast.makeText(EditProfileActivity.this, "Location updated", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    // Handle error
                                                    Toast.makeText(EditProfileActivity.this, "Failed to update location", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Failed to get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private String getCityName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                String cityName = addresses.get(0).getLocality();
                return cityName;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void getCityFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String city = documentSnapshot.getString("city");
                            if (city != null) {
                                // Do something with the city value, e.g., set it in a TextView
                                dogLocationTextView.setText(city);
                            } else {
                                // The "city" field is not available or is null
                                dogLocationTextView.setText("City not found");
                            }
                        } else {
                            // Document doesn't exist
                            dogLocationTextView.setText("User document not found");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Handle the failure to retrieve the data
                        Toast.makeText(EditProfileActivity.this, "Failed to get city data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadProfileImages() {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    for (int i = 0; i < 6; i++) {
                        String imageUrlKey = "profileImageUrl" + (i + 1);
                        String imageUrl = documentSnapshot.getString(imageUrlKey);
                        loadProfileImage(imageViews[i], imageUrl, R.drawable.insert_image);
                    }
                }
            }
        });
    }

    private void loadProfileImage(ImageView imageView, String imageUrl, int defaultImageResource) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(imageView);
        } else {
            imageView.setImageResource(defaultImageResource);
        }
    }

    private void requestMultiplePermissions() {
        if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(EditProfileActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(EditProfileActivity.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(EditProfileActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(EditProfileActivity.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(EditProfileActivity.this, permissionsRequired[2])) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Camera and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(EditProfileActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Camera and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant Camera and Location", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                ActivityCompat.requestPermissions(EditProfileActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.commit();
        } else {
            // Permissions already granted
        }
    }

    private void proceedAfterPermission() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    cameraIntent();
                } else if (options[item].equals("Choose from Gallery")) {
                    galleryIntent();
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        if (userId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(userId);
            String imageUrlKey = "profileImageUrl" + (currentImageViewIndex + 1);

            // Upload the new image and update the URL
            updateImageInFirestore(thumbnail, userRef, imageUrlKey);

            // Set the ImageView to display the uploaded image
            imageViews[currentImageViewIndex].setImageBitmap(thumbnail);
            myBitmaps[currentImageViewIndex] = thumbnail;
        } else {
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final Bitmap finalBitmap = bm; // Declare bm as final

        if (userId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("users").document(userId);
            String imageUrlKey = "profileImageUrl" + (currentImageViewIndex + 1);

            // Upload the new image and update the URL
            updateImageInFirestore(finalBitmap, userRef, imageUrlKey);

            // Set the ImageView to display the uploaded image
            imageViews[currentImageViewIndex].setImageBitmap(bm);
            myBitmaps[currentImageViewIndex] = bm;
        } else {
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateImageInFirestore(Bitmap imageBitmap, DocumentReference userRef, String imageUrlKey) {
        if (imageBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            // Specify a consistent filename for each image view
            String filename = "image" + currentImageViewIndex;

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("images/" + FirebaseAuth.getInstance().getUid() + "/" + filename);

            // Delete the previous image with the same filename
            storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Upload the new image and update the URL after deletion
                    UploadTask uploadTask = storageRef.putBytes(imageData);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String imageUrl = uri.toString();

                                    userRef.update(imageUrlKey, imageUrl)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(EditProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(Exception e) {
                                                    Toast.makeText(EditProfileActivity.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    // If the image does not exist, upload it directly
                    UploadTask uploadTask = storageRef.putBytes(imageData);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String imageUrl = uri.toString();

                                    userRef.update(imageUrlKey, imageUrl)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(EditProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(Exception e) {
                                                    Toast.makeText(EditProfileActivity.this, "Failed to update profile image", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            // Check if all permissions are granted
            boolean allGranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allGranted = true;
                } else {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                proceedAfterPermission();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(EditProfileActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(EditProfileActivity.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(EditProfileActivity.this, permissionsRequired[2])) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Camera and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(EditProfileActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        super.onResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted
                proceedAfterPermission();
            }
        }
    }


    @Override
    public void onBackPressed() {
        // Handle the back button event
        // You may want to check if any changes were made and show a confirmation dialog
        saveDogBio();
        saveDogTemperaments();
        super.onBackPressed();
    }
}
