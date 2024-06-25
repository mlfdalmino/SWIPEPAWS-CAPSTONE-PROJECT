package com.capstone.project.swipepaws.Main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.capstone.project.swipepaws.R;
import com.capstone.project.swipepaws.Utils.ProfileImagePagerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProfileCheckinMain extends AppCompatActivity {
    private static final String TAG = "ProfileCheckinMain";
    private FirebaseFirestore db;
    private String currentUserId;
    private Context mContext;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_checkin_main);

        mContext = ProfileCheckinMain.this;
        db = FirebaseFirestore.getInstance();

        TextView profileName = findViewById(R.id.name_main);
        TextView profileBio = findViewById(R.id.bio_beforematch);
        TextView profileDistance = findViewById(R.id.distance_main);
        TextView profileCity = findViewById(R.id.city);
        TextView temperamentsTextView = findViewById(R.id.temperaments_beforematch);
        TextView medicalRecordsVerified = findViewById(R.id.medicalRecordsVerified);
        Button downloadMedicalRecordsButton = findViewById(R.id.downloadMedicalRecordsButton);

        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");
        fetchTemperaments(userId, temperamentsTextView);
        String name = intent.getStringExtra("name");
        String bio = intent.getStringExtra("bio");
        String city = intent.getStringExtra("city");
        int distance = intent.getIntExtra("distance", -1);
        String distanceText = distance >= 0 ? (distance < 1 ? "Less than a Kilometer Away" : distance + " Kilometer" + (distance == 1 ? " away" : "s away")) : "Distance not available";

        profileDistance.setText(distanceText);
        profileName.setText(name);
        profileBio.setText(bio);
        profileCity.setText(city);

        List<String> profileImageUrls = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            String url = intent.getStringExtra("photo" + i);
            if (url != null && !url.isEmpty()) {
                profileImageUrls.add(url);
            }
        }

        ViewPager profileImagesViewPager = findViewById(R.id.profileImagesViewPager);
        ProfileImagePagerAdapter profileImageAdapter = new ProfileImagePagerAdapter(mContext, profileImageUrls);
        profileImagesViewPager.setAdapter(profileImageAdapter);

        if (userId != null) {
            DocumentReference userDocRef = db.collection("users").document(userId);
            userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.contains("medicalRecordUri")) {
                            String medicalRecordUri = document.getString("medicalRecordUri");
                            if (medicalRecordUri != null && !medicalRecordUri.isEmpty()) {
                                medicalRecordsVerified.setVisibility(View.VISIBLE);
                                downloadMedicalRecordsButton.setVisibility(View.VISIBLE);
                               downloadMedicalRecordsButton.setOnClickListener(v -> {
                                    Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                                    pdfIntent.setDataAndType(Uri.parse(medicalRecordUri), "application/pdf");
                                    pdfIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    startActivity(Intent.createChooser(pdfIntent, "Open PDF"));
                                });
                            }
                        }
                    } else {
                        Log.e(TAG, "Error fetching document: ", task.getException());
                    }
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
        fetchCurrentUserInformation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchTemperaments(String userId, TextView temperamentsTextView) {
        if (userId == null) return;

        DocumentReference temperamentRef = db.collection("users").document(userId).collection("dogTemperaments").document("temperamentData");
        temperamentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        StringBuilder temperaments = new StringBuilder();
                        document.getData().forEach((key, value) -> {
                            if (Boolean.TRUE.equals(value)) {
                                if (temperaments.length() > 0) temperaments.append(", ");
                                temperaments.append(key);
                            }
                        });
                        temperamentsTextView.setText(temperaments.toString());
                    } else {
                        temperamentsTextView.setText("No temperaments data available");
                    }
                } else {
                    Log.e(TAG, "Error fetching temperaments: ", task.getException());
                    temperamentsTextView.setText("Failed to load temperaments");
                }
            }
        });
    }

    private void fetchCurrentUserInformation() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            DocumentReference currentUserDocRef = db.collection("users").document(currentUserId);
            currentUserDocRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            });
        }
    }

    public void exitButtonClicked(View view) {
        finish();
    }
}
