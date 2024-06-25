package com.capstone.project.swipepaws.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.capstone.project.swipepaws.Main.MainActivity;
import com.capstone.project.swipepaws.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class RegisterGenderPrefection extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView[] imageViews;
    private CardView[] cardViews;
    private Uri[] imageUris;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Button preferenceContinueButton;
    private int selectedImageIndex = -1;
    private FirebaseFirestore firestore;
    private static final String TAG = "RegisterGenderPrefection";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_gender_prefection);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        imageViews = new ImageView[]{
                findViewById(R.id.imageView1),
                findViewById(R.id.imageView2),
                findViewById(R.id.imageView3),
                findViewById(R.id.imageView4),
                findViewById(R.id.imageView5),
                findViewById(R.id.imageView6)
        };
        cardViews = new CardView[]{
                findViewById(R.id.cardView1),
                findViewById(R.id.cardView2),
                findViewById(R.id.cardView3),
                findViewById(R.id.cardView4),
                findViewById(R.id.cardView5),
                findViewById(R.id.cardView6)
        };
        imageUris = new Uri[6]; // Update the array size to 6

        preferenceContinueButton = findViewById(R.id.preferenceContinueButton);
        preferenceContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if at least one image has been uploaded
                boolean atLeastOneImageUploaded = false;
                for (Uri imageUri : imageUris) {
                    if (imageUri != null) {
                        atLeastOneImageUploaded = true;
                        break;
                    }
                }

                if (atLeastOneImageUploaded) {
                    // At least one image has been uploaded, continue to MainActivity
                    Intent intent = new Intent(RegisterGenderPrefection.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Optional: finish the current activity to prevent going back
                } else {
                    // No image uploaded, display a message to the user
                    Toast.makeText(RegisterGenderPrefection.this, "Please upload at least one image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        for (int i = 0; i < cardViews.length; i++) {
            final int index = i;
            cardViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedImageIndex = index;
                    openFileChooser();
                }
            });
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            if (selectedImageIndex >= 0 && selectedImageIndex < imageUris.length) {
                imageUris[selectedImageIndex] = imageUri;
                imageViews[selectedImageIndex].setImageURI(imageUri);

                // Upload the selected image to Firebase Storage
                uploadImage(selectedImageIndex);
            }
        }
    }

    // Upload image to Firebase Storage
    private void uploadImage(final int index) {
        if (imageUris[index] != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/" + FirebaseAuth.getInstance().getUid() + "/image" + index);

            ref.putFile(imageUris[index])
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterGenderPrefection.this, "Image uploaded", Toast.LENGTH_SHORT).show();

                            // Get the image URL from the taskSnapshot
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();

                                    // Store the image URL in Firestore under the user's profile
                                    storeImageUrlInFirestore(imageUrl, index);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterGenderPrefection.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Store the image URL in Firestore under the user's profile document
    private void storeImageUrlInFirestore(final String imageUrl, final int index) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            String fieldToUpdate = "profileImageUrl" + (index + 1); // Create the field name (e.g., profileImageUrl1, profileImageUrl2, etc.)
            firestore.collection("users")
                    .document(userId)
                    .update(fieldToUpdate, imageUrl)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Image URL successfully stored in Firestore
                            Log.d(TAG, "Image URL " + imageUrl + " stored in Firestore field " + fieldToUpdate);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error storing image URL " + imageUrl + " in Firestore field " + fieldToUpdate + ": " + e.getMessage());
                        }
                    });
        }
    }
}
