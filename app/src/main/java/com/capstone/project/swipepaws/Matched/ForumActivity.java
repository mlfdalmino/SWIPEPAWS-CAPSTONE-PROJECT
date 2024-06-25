package com.capstone.project.swipepaws.Matched;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.capstone.project.swipepaws.Login.Login;
import com.capstone.project.swipepaws.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForumActivity extends AppCompatActivity {

    private EditText editTextPost;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private ImageView back;
    private FirebaseFirestore db;
    private RecyclerView recyclerViewPosts;
    private List<Post> postList;
    private Button buttonPost;
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(ForumActivity.this, Login.class);
            startActivity(intent);
            finish();
            return;
        }


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..."); // Set your message here
        progressDialog.setCancelable(false);

        db = FirebaseFirestore.getInstance();
        back = findViewById(R.id.back);
        editTextPost = findViewById(R.id.editTextPost);
        buttonPost = findViewById(R.id.buttonPost); // Initialize buttonPost
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();

        adapter = new MyAdapter(postList, post -> {
            Intent intent = new Intent(ForumActivity.this, PostDetailActivity.class);
            intent.putExtra("postId", post.getId());
            startActivity(intent);
        });
        recyclerViewPosts.setAdapter(adapter);

        fetchPosts();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Fixed: Correctly setting OnClickListener for buttonPost
        buttonPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postToForum();
            }
        });
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
    private void fetchPosts() {
        showProgress();
        db.collection("forumPosts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    hideProgress();
                    if (task.isSuccessful()) {
                        postList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Post post = document.toObject(Post.class);
                            post.setId(document.getId());
                            postList.add(post);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ForumActivity.this, "Error getting posts: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchPosts();
    }

    public void postToForum() {
        String postContent = editTextPost.getText().toString().trim();
        if (!postContent.isEmpty()) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                db.collection("users").document(currentUser.getUid()).get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String username = documentSnapshot.getString("username");
                                Map<String, Object> post = new HashMap<>();
                                post.put("content", postContent);
                                post.put("timestamp", new Timestamp(new Date()));
                                post.put("userId", currentUser.getUid());
                                post.put("author", username);

                                db.collection("forumPosts")
                                        .add(post)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(ForumActivity.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                                            editTextPost.setText("");
                                            fetchPosts();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(ForumActivity.this, "Failed to add post", Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(ForumActivity.this, "Failed to fetch username", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(ForumActivity.this, "Error fetching user information", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(ForumActivity.this, "You need to be logged in to post", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ForumActivity.this, "Please enter a post", Toast.LENGTH_SHORT).show();
        }
    }

    // Implementation of MyAdapter and other methods...
}
