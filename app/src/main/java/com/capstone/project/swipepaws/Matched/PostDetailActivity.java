package com.capstone.project.swipepaws.Matched;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.capstone.project.swipepaws.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class PostDetailActivity extends AppCompatActivity {


    private String postId;
    private String postUserId;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private ImageView back, optionsMenu;
    private TextView textViewPostContent, textViewAuthor, textViewTimestamp;
    private EditText editTextComment;
    private Button buttonPostComment;
    private RecyclerView commentsRecyclerView;
    private CommentsAdapter commentsAdapter;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);


        mAuth = FirebaseAuth.getInstance();
        back = findViewById(R.id.back);
        optionsMenu = findViewById(R.id.options_menu);
        textViewPostContent = findViewById(R.id.textViewPostContent);
        textViewAuthor = findViewById(R.id.textViewAuthor);
        textViewTimestamp = findViewById(R.id.textViewTimestamp);
        editTextComment = findViewById(R.id.editTextComment);
        buttonPostComment = findViewById(R.id.buttonPostComment);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser != null ? currentUser.getUid() : "";


        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsAdapter = new CommentsAdapter(this, new ArrayList<>(), currentUserId);
        commentsRecyclerView.setAdapter(commentsAdapter);


        optionsMenu.setOnClickListener(v -> showOptionsMenu(v));
        buttonPostComment.setOnClickListener(v -> postComment());
        String postIdFromIntent = getIntent().getStringExtra("postId");
        if (postIdFromIntent != null && !postIdFromIntent.isEmpty()) {
            postId = postIdFromIntent; // Set postId to the global variable
            loadPostDetails(postId);
            loadComments(getIntent().getStringExtra("postId"));


            // Check if the current user is the same as the post's user ID
            db.collection("forumPosts").document(postId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    postUserId = documentSnapshot.getString("userId"); // Save the post's user ID
                    if (currentUser != null && postUserId != null && postUserId.equals(currentUser.getUid())) {
                        // If the post's user ID and the current user's ID are the same, show the options menu
                        optionsMenu.setVisibility(View.VISIBLE);
                    } else {
                        // If the post's user ID and the current user's ID are not the same, hide the options menu
                        optionsMenu.setVisibility(View.GONE);
                    }
                    loadUserProfilePicture(postUserId);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(PostDetailActivity.this, "Failed to load post details", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Error: Missing post ID.", Toast.LENGTH_LONG).show();
        }


        back.setOnClickListener(v -> onBackPressed());
    }


    private void showOptionsMenu(View v) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && postUserId != null && postUserId.equals(currentUser.getUid())) {
            PopupMenu popup = new PopupMenu(this, v);
            popup.inflate(R.menu.post_option_menu); // menu should have edit & delete items
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menu_edit:
                        // Show a dialog box for editing the post content
                        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailActivity.this);
                        builder.setTitle("Edit Post");


                        // Set up the input
                        final EditText input = new EditText(PostDetailActivity.this);
                        // Specify the type of input expected
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        builder.setView(input);


                        // Set up the buttons
                        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newContent = input.getText().toString();
                                // Update the post content in Firestore
                                DocumentReference postRef = FirebaseFirestore.getInstance().collection("forumPosts").document(postId);
                                postRef.update("content", newContent)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Post content updated successfully
                                                Toast.makeText(PostDetailActivity.this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                                                // Update the TextView immediately with the new content
                                                textViewPostContent.setText(newContent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Failed to update post content
                                                Toast.makeText(PostDetailActivity.this, "Failed to update post", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });


                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });


                        builder.show();
                        break;
                    case R.id.menu_delete:
                        // Delete the post from Firestore
                        DocumentReference postRef = FirebaseFirestore.getInstance().collection("forumPosts").document(postId);
                        postRef.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Post deleted successfully
                                        Toast.makeText(PostDetailActivity.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                                        // Navigate back to previous activity
                                        finish(); // Finish the current activity
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Failed to delete post
                                        Toast.makeText(PostDetailActivity.this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        break;
                    default:
                        return false;
                }
                return true;


            });
            popup.show();
        } else {
            Toast.makeText(this, "You can only edit or delete your own posts.", Toast.LENGTH_SHORT).show();
        }
    }




    // Call this method to show progress
    private void showProgress() {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }


    // Call this method to hide progress
    private void hideProgress() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    @Override
    public void onBackPressed() {
        showProgress();
        // simulate a delay before dismissing the progress dialog and finishing the activity
        new Handler().postDelayed(() -> {
            hideProgress();
            super.onBackPressed();
        }, 1000);
    }




    private void loadPostDetails(String postId) {
        db.collection("forumPosts").document(postId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Post post = documentSnapshot.toObject(Post.class);
                if (post != null) {
                    textViewPostContent.setText(post.getContent());
                    textViewAuthor.setText(post.getAuthor());
                    postUserId = documentSnapshot.getString("userId"); // Save the post's user ID


                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                    String formattedDate = sdf.format(post.getTimestamp().toDate());
                    textViewTimestamp.setText(formattedDate);
                } else {
                    Toast.makeText(PostDetailActivity.this, "Post data is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PostDetailActivity.this, "Post does not exist", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(PostDetailActivity.this, "Failed to load post details", Toast.LENGTH_SHORT).show());
    }






    private void postComment() {
        showProgress();
        String commentContent = editTextComment.getText().toString().trim();
        if (!commentContent.isEmpty()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();


                db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String author = documentSnapshot.getString("username");


                        Map<String, Object> comment = new HashMap<>();
                        comment.put("postId", getIntent().getStringExtra("postId"));
                        comment.put("content", commentContent);
                        comment.put("author", author);
                        comment.put("userId", userId); // Add this line to include the userId
                        comment.put("timestamp", new com.google.firebase.Timestamp(new java.util.Date()));


                        db.collection("comments").add(comment)
                                .addOnSuccessListener(documentReference -> {
                                    hideProgress();
                                    Toast.makeText(PostDetailActivity.this, "Comment posted", Toast.LENGTH_SHORT).show();
                                    editTextComment.setText("");
                                    loadComments(getIntent().getStringExtra("postId"));
                                })
                                .addOnFailureListener(e -> Toast.makeText(PostDetailActivity.this, "Failed to post comment", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(PostDetailActivity.this, "User information is missing.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(PostDetailActivity.this, "Failed to fetch user information", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(this, "No signed-in user found", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void loadComments(String postId) {
        db.collection("comments")
                .whereEqualTo("postId", postId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Comment> comments = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Comment comment = document.toObject(Comment.class);
                            comment.setId(document.getId());
                            comment.setUserId(document.getString("userId")); // This is crucial
                            comments.add(comment);
                        }
                        commentsAdapter.updateComments(comments);
                    } else {
                        Toast.makeText(PostDetailActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserProfilePicture(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("profilePictureUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            ImageView profileImageView = findViewById(R.id.profileurl);
                            Glide.with(this)
                                    .load(imageUrl)
                                    .circleCrop()
                                    .into(profileImageView);
                        }
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching user profile", Toast.LENGTH_SHORT).show());
    }



}
