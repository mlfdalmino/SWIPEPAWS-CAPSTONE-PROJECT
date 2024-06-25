package com.capstone.project.swipepaws.Matched;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.project.swipepaws.R;
import com.capstone.project.swipepaws.Utils.TopNavigationViewHelper;
import com.capstone.project.swipepaws.chat.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Matched_Activity extends AppCompatActivity {

    private static final String TAG = "Matched_Activity";
    private static final int ACTIVITY_NUM = 2;
    private List<Users> matchList = new ArrayList<>();
    private MatchUserAdapter mAdapter;
    private Context mContext = Matched_Activity.this;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matched);

        // Setup top navigation view
        setupTopNavigationView();

        // Setup search functionality
        searchFunc();

        // Initialize RecyclerView and its adapter
        mRecyclerView = findViewById(R.id.matche_recycler_view);
        mAdapter = new MatchUserAdapter(matchList, getApplicationContext(), this, matchedUserId -> {
            // Handle item click here, e.g., navigate to ChatActivity
            Log.d("Matched_Activity", "Clicked item with matchedUserId: " + matchedUserId);
            Intent intent = new Intent(Matched_Activity.this, ChatActivity.class);
            intent.putExtra("matchedUserId", matchedUserId);
            startActivity(intent);
        });

        RecyclerView.LayoutManager mLayoutManager1 = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager1);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        // Prepare and load match data
        prepareMatchData();

        // Set up button click listener for forum
        Button btnForum = findViewById(R.id.btnForum);
        btnForum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle button click here
                Intent intent = new Intent(Matched_Activity.this, ForumActivity.class);
                startActivity(intent);
            }
        });
    }

    private void prepareMatchData() {
        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("users");
        String currentUserId = getCurrentUserId();

        if (currentUserId != null) {
            usersRef.document(currentUserId).get().addOnSuccessListener(likedUsersDoc -> {
                List<String> likedUserIds = (List<String>) likedUsersDoc.get("likedUserIds");

                if (likedUserIds != null && !likedUserIds.isEmpty()) {
                    for (String likedUserId : likedUserIds) {
                        checkIfCurrentUserLikedUser(currentUserId, likedUserId, isMutual -> {
                            if (isMutual) {
                                usersRef.document(likedUserId).get().addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful()) {
                                        String userId = userTask.getResult().getId();
                                        String dogName = userTask.getResult().getString("dogName");
                                        String profileImageUrl1 = userTask.getResult().getString("profileImageUrl1");
                                        String dogBio = userTask.getResult().getString("dogBio");
                                        String city = userTask.getResult().getString("city");

                                        Users user = new Users(userId, dogName, profileImageUrl1, dogBio, city);
                                        matchList.add(user);

                                        mAdapter.notifyDataSetChanged();
                                    } else {
                                        Log.d(TAG, "Error getting user details: ", userTask.getException());
                                    }
                                });
                            }
                        });
                    }
                }
            }).addOnFailureListener(e -> {
                Log.d(TAG, "Error fetching likedUserIds for current user: ", e);
            });
        } else {
            // Handle the case where the user is not logged in
        }
    }

    private void checkIfCurrentUserLikedUser(String currentUserId, String otherUserId, OnCompleteListener<Boolean> onCompleteListener) {
        CollectionReference usersRef = FirebaseFirestore.getInstance().collection("users");

        usersRef.document(currentUserId).get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                List<String> likedUserIdsCurrentUser = (List<String>) task1.getResult().get("likedUserIds");

                boolean isLikedByCurrentUser = likedUserIdsCurrentUser != null && likedUserIdsCurrentUser.contains(otherUserId);

                if (isLikedByCurrentUser) {
                    usersRef.document(otherUserId).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            List<String> likedUserIdsOtherUser = (List<String>) task2.getResult().get("likedUserIds");
                            boolean isLikedByOtherUser = likedUserIdsOtherUser != null && likedUserIdsOtherUser.contains(currentUserId);
                            onCompleteListener.onComplete(isLikedByOtherUser);
                        } else {
                            Log.d(TAG, "Error fetching likedUserIds for other user: ", task2.getException());
                            onCompleteListener.onComplete(false);
                        }
                    });
                } else {
                    onCompleteListener.onComplete(false);
                }
            } else {
                Log.d(TAG, "Error fetching likedUserIds for current user: ", task1.getException());
                onCompleteListener.onComplete(false);
            }
        });
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return null;
        }
    }

    public interface OnCompleteListener<T> {
        void onComplete(T result);
    }

    private void searchText(String text) {
        text = text.toLowerCase();
        if (!text.isEmpty()) {
            List<Users> filteredList = new ArrayList<>();
            for (Users user : matchList) {
                if (user.getName().toLowerCase().contains(text)) {
                    filteredList.add(user);
                }
            }
            matchList.clear();
            matchList.addAll(filteredList);
        } else {
            matchList.clear();
            prepareMatchData();
        }

        mAdapter.notifyDataSetChanged();
    }

    private void searchFunc() {
        EditText search = findViewById(R.id.searchBar);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupTopNavigationView() {
        Log.d(TAG, "setupTopNavigationView: setting up TopNavigationView");
        BottomNavigationViewEx tvEx = findViewById(R.id.topNavViewBar);
        TopNavigationViewHelper.setupTopNavigationView(tvEx);
        TopNavigationViewHelper.enableNavigation(mContext, tvEx);
        Menu menu = tvEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
