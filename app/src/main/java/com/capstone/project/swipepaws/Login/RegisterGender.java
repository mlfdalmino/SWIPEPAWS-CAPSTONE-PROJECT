package com.capstone.project.swipepaws.Login;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.capstone.project.swipepaws.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegisterGender extends AppCompatActivity {

    private EditText dogNameEditText;
    private Spinner breedSpinner, ageSpinner;
    private RadioGroup dogGenderRadioGroup;
    private Button continueButton, uploadRecordsButton;
    private Uri pdfUri; // URI for the uploaded PDF

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private StorageReference mStorageRef;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 123;
    private static final int PICK_PDF_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_gender);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        dogNameEditText = findViewById(R.id.dogNameEditText);
        breedSpinner = findViewById(R.id.breedSpinner);
        ageSpinner = findViewById(R.id.ageSpinner);
        dogGenderRadioGroup = findViewById(R.id.genderRadioGroup);
        continueButton = findViewById(R.id.continueButton);
        uploadRecordsButton = findViewById(R.id.uploadRecordsButton);

        uploadRecordsButton.setOnClickListener(v -> openFileChooser());

        continueButton.setOnClickListener(v -> {
            if (validateInputs()) {
                checkLocationPermissionAndFetch();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            pdfUri = data.getData();
            uploadFile();
        }
    }

    private void uploadFile() {
        if (pdfUri != null) {
            String userId = mAuth.getCurrentUser().getUid(); // Get the current user's UID
            String fileExtension = getFileExtension(pdfUri); // Get the file extension of the PDF

            // Construct the path with the user's UID and the file extension
            final StorageReference fileReference = mStorageRef.child("medical_records/" + userId + "." + fileExtension);

            fileReference.putFile(pdfUri).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("medicalRecordUri", downloadUri.toString());
                    db.collection("users").document(userId)
                            .set(data, SetOptions.merge());
                    Toast.makeText(RegisterGender.this, "Medical record uploaded and link saved", Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(RegisterGender.this, "Failed to get the download URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(RegisterGender.this, "Failed to upload medical record: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    private String getFileExtension(Uri uri) {
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(getContentResolver().getType(uri));
    }

    private boolean validateInputs() {
        return !dogNameEditText.getText().toString().trim().isEmpty() &&
                !breedSpinner.getSelectedItem().toString().trim().equals("Select Breed") &&
                !ageSpinner.getSelectedItem().toString().trim().equals("Select Age") &&
                dogGenderRadioGroup.getCheckedRadioButtonId() != -1 &&
                pdfUri != null;
    }

    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this, task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                storeUserData(task.getResult());
            } else {
                Toast.makeText(RegisterGender.this, "Failed to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void storeUserData(Location lastKnownLocation) {
        double latitude = lastKnownLocation.getLatitude();
        double longitude = lastKnownLocation.getLongitude();
        String cityName = getCityName(latitude, longitude);
        String dogSize = determineDogSize(breedSpinner.getSelectedItem().toString().trim());

        Map<String, Object> userData = new HashMap<>();
        userData.put("dogName", dogNameEditText.getText().toString().trim());
        userData.put("breed", breedSpinner.getSelectedItem().toString().trim());
        userData.put("dogGender", ((RadioButton) findViewById(dogGenderRadioGroup.getCheckedRadioButtonId())).getText().toString());
        userData.put("dogAge", ageSpinner.getSelectedItem().toString().trim());
        userData.put("latitude", latitude);
        userData.put("longitude", longitude);
        userData.put("medicalRecordUri", pdfUri.toString());
        userData.put("dogSize", dogSize);
        if (cityName != null) {
            userData.put("city", cityName);
        }

        db.collection("users").document(mAuth.getCurrentUser().getUid())
                .set(userData, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update the firstLogin field in the user document
                        db.collection("users").document(mAuth.getCurrentUser().getUid())
                                .update("firstLogin", false)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        // Navigate to the next activity
                                        Intent intent = new Intent(RegisterGender.this, RegisterGenderPrefection.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterGender.this, "Error updating firstLogin field: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(RegisterGender.this, "Error saving user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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

    private String getCityName(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            return addresses.isEmpty() ? null : addresses.get(0).getLocality();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
