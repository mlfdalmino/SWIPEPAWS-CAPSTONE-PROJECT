package com.capstone.project.swipepaws.Login;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.capstone.project.swipepaws.Main.MainActivity;
import com.capstone.project.swipepaws.R;
import com.capstone.project.swipepaws.Utils.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private Context mContext;
    private EditText mEmail, mPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize SharedPreferences


        setContentView(R.layout.activity_login);
        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mContext = Login.this;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        init();
    }


    private boolean isStringNull(String string) {
        return string.equals("");
    }



    private void init() {
        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                if (isStringNull(email) || isStringNull(password)) {
                    Toast.makeText(mContext, "You must fill out all the fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Perform Firebase email/password authentication
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Login.this, task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser currentUser = mAuth.getCurrentUser();
                                    if (currentUser != null) {

                                        checkFirstTimeLogin(currentUser.getUid());

                                    }
                                } else {
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(Login.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        TextView linkSignUp = findViewById(R.id.link_signup);
        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, RegisterBasicInfo.class);
                startActivity(intent);
            }
        });

        TextView linkForgotPassword = findViewById(R.id.link_forgot_password);
        linkForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog();
            }
        });
    }

    private void checkFirstTimeLogin(String userId) {
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Boolean firstLogin = document.getBoolean("firstLogin");
                    if (firstLogin != null && firstLogin) {
                        // First time login, redirect to RegisterGender
                        Intent intent = new Intent(Login.this, RegisterGender.class);
                        startActivity(intent);
                    } else {
                        // Not the first time login, redirect to MainActivity
                        startActivity(new Intent(Login.this, MainActivity.class));
                    }
                } else {
                    // User document does not exist, create it and redirect to RegisterGender
                    User newUser = new User("", "", "", "", "", "", "", 0.0, 0.0, "", true);
                    userRef.set(newUser) // Create a User object with a boolean flag
                            .addOnCompleteListener(createTask -> {
                                if (createTask.isSuccessful()) {
                                    // User document created successfully, redirect to RegisterGender
                                    Intent intent = new Intent(Login.this, RegisterGender.class);
                                    startActivity(intent);
                                } else {
                                    // Handle document creation failure if needed
                                    Log.e(TAG, "Error creating user document: " + createTask.getException());
                                }
                            });
                }
                finish();
            } else {
                Log.e(TAG, "Error getting user document: " + task.getException());
                // Handle error if needed
            }
        });
    }


    private void showForgotPasswordDialog() {
        final EditText resetEmailEditText = new EditText(mContext);
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(mContext);
        passwordResetDialog.setTitle("Reset Password");
        passwordResetDialog.setMessage("Enter your email to receive reset link:");
        passwordResetDialog.setView(resetEmailEditText);

        passwordResetDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = resetEmailEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(email)) {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(mContext, "Password reset email sent", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(mContext, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        passwordResetDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Close the dialog
            }
        });

        passwordResetDialog.create().show();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back from the login screen
    }
}
