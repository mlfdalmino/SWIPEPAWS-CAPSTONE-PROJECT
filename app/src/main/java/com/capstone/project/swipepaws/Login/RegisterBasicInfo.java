package com.capstone.project.swipepaws.Login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.capstone.project.swipepaws.R;
import com.capstone.project.swipepaws.Utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterBasicInfo extends AppCompatActivity {

    private static final String TAG = "RegisterBasicInfo";
    private Context mContext;
    private String email, username, password, mobileNumber;
    private EditText mEmail, mPassword, mUsername, mNumber;
    private Button btnRegister, btnUploadProfilePicture;
    private ImageView imgProfilePicture;
    private Uri selectedImageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private String mobileNumberPattern = "^(09|\\+639)\\d{9}$"; // Philippine mobile number pattern

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerbasic_info);
        mContext = RegisterBasicInfo.this;
        Log.d(TAG, "onCreate: started");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firestore = FirebaseFirestore.getInstance();

        initWidgets();
        init();
    }

    private void init() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEmail.getText().toString();
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();
                mobileNumber = mNumber.getText().toString();

                if (checkInputs(email, username, password, mobileNumber)) {
                    // Create user account in Firebase Authentication
                    createUserWithEmailAndPassword(email, password);
                }
            }
        });

        btnUploadProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });
    }

    private void createUserWithEmailAndPassword(String email, String password) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Account creation success. You can now proceed to store additional user data in Firebase Database or Firestore.

                            // Example: Storing user data in Firestore
                            saveUserDataToFirestore(username, email, mobileNumber);

                            // Upload profile picture
                            uploadProfilePicture();
                        } else {
                            // Account creation failed
                            Toast.makeText(mContext, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserDataToFirestore(String username, String email, String mobileNumber) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        String userId = mAuth.getCurrentUser().getUid();

        // Create a User object with basic info and set it in Firestore
        User user = new User(username, email, mobileNumber);

        db.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Data saved successfully
                        // You can start the next activity or perform any other actions here
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Data save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean checkInputs(String email, String username, String password, String mobileNumber) {
        Log.d(TAG, "checkInputs: checking inputs for null values.");

        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || mobileNumber.isEmpty()) {
            Toast.makeText(mContext, "All fields must be filled out.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!email.matches(emailPattern)) {
            Toast.makeText(getApplicationContext(), "Invalid email address. Please enter a valid email.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!mobileNumber.matches(mobileNumberPattern)) {
            Toast.makeText(getApplicationContext(), "Invalid mobile number. Please enter a valid Philippine mobile number.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Password validation
        if (password.length() < 8) {
            Toast.makeText(getApplicationContext(), "Password must be at least 8 characters long.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check for at least one uppercase letter in the password
        if (!password.matches(".*[A-Z].*")) {
            Toast.makeText(getApplicationContext(), "Password must contain at least one uppercase letter.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check for at least one special character in the password
        if (!password.matches(".*[!@#$%^&*()_+=|<>?{}\\[\\]~-].*")) {
            Toast.makeText(getApplicationContext(), "Password must contain at least one special character.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void initWidgets() {
        Log.d(TAG, "initWidgets: initializing widgets");

        mEmail = findViewById(R.id.input_email);
        mUsername = findViewById(R.id.input_username);
        mNumber = findViewById(R.id.input_number);
        btnRegister = findViewById(R.id.btn_register);
        mPassword = findViewById(R.id.input_password);
        btnUploadProfilePicture = findViewById(R.id.btn_upload_profile_picture);
        imgProfilePicture = findViewById(R.id.img_profile_picture);
        mContext = RegisterBasicInfo.this;

        // Set the default prefix "+63" in the mobile number field
        mNumber.setText("+63");
        mNumber.setSelection(mNumber.getText().length()); // Move the cursor to the end of the text

        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(13); // Limit to 13 characters (including "+63")
        mNumber.setFilters(filters);

        // Prevent erasing the "+63" prefix
        mNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    // Check if the cursor is at or after the "+63" prefix
                    int selectionStart = mNumber.getSelectionStart();
                    if (selectionStart <= 3) {
                        // Don't allow erasing the "+63" prefix
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(mContext, Login.class);
        startActivity(intent);
        finish(); // Close the current activity so the user can't go back to the registration page
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imgProfilePicture.setImageURI(selectedImageUri);
        }
    }

    private void uploadProfilePicture() {
        if (selectedImageUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading profile picture...");
            progressDialog.show();

            // Use the UID as the filename for the profile picture
            String uid = FirebaseAuth.getInstance().getUid();
            StorageReference ref = storageReference.child("profile_pictures/" + uid);

            ref.putFile(selectedImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(mContext, "Account Registered Successfully", Toast.LENGTH_SHORT).show();

                            // Get the profile picture URL from the taskSnapshot
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String profilePictureUrl = uri.toString();

                                    // Store the profile picture URL in Firestore under the user's profile
                                    storeProfilePictureUrlInFirestore(profilePictureUrl);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(mContext, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void storeProfilePictureUrlInFirestore(final String profilePictureUrl) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            firestore.collection("users")
                    .document(userId)
                    .update("profilePictureUrl", profilePictureUrl)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Profile picture URL successfully stored in Firestore
                            Log.d(TAG, "Profile picture URL " + profilePictureUrl + " stored in Firestore");
                            redirectToLogin(); // Redirect to the login activity after successful registration
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error storing profile picture URL " + profilePictureUrl + " in Firestore: " + e.getMessage());
                        }
                    });
        }
    }
}
