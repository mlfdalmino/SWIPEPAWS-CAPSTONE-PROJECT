package com.capstone.project.swipepaws.Main;



import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.core.app.NotificationCompat;



import com.capstone.project.swipepaws.Matched.UserMatches;
import com.capstone.project.swipepaws.R;
import com.capstone.project.swipepaws.Utils.PulsatorLayout;
import com.capstone.project.swipepaws.Utils.TopNavigationViewHelper;
import com.google.firebase.firestore.QuerySnapshot;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int ACTIVITY_NUM = 1;
    final private int MY_PERMISSIONS_REQUEST_LOCATION = 123;
    ListView listView;
    List<Cards> rowItems;
    FrameLayout cardFrame, moreFrame;
    private Context mContext = MainActivity.this;
    private NotificationHelper mNotificationHelper;
    private PhotoAdapter arrayAdapter;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private int currentCardIndex = 0;
    private String currentUserId; // Declare currentUserId at the class level
    private String currentUserDogGender; // Declare currentUserDogGender at the class level
    private String currentUserPreferredBreed;
    private String currentUserDogSize;
    private DocumentReference currentUserDocRef;
    private DocumentReference likedUserDocRef;
    private Double currentUserLatitude;
    private Double currentUserLongitude;
    private Double currentUserDistance;
    private Set<String> addedUserIds = new HashSet<>();

    private Set<String> currentUserPreferredTemperaments;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cardFrame = findViewById(R.id.card_frame);
        moreFrame = findViewById(R.id.more_frame);
        // Start pulsator
        PulsatorLayout mPulsator = findViewById(R.id.pulsator);
        mPulsator.start();
        mNotificationHelper = new NotificationHelper(this);
        db = FirebaseFirestore.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        setupTopNavigationView();
        rowItems = new ArrayList<Cards>();
        arrayAdapter = new PhotoAdapter(MainActivity.this, R.layout.item, rowItems); // Initialize adapter
        // Fetch the current user's information and cards
        fetchCurrentUserInformation();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Fetch cards again when the activity is resumed (e.g., after login)
        updateSwipeCard();
    }


    private void fetchCurrentUserInformation() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            currentUserDocRef = db.collection("users").document(currentUserId);
            currentUserDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            currentUserDogGender = document.getString("dogGender");
                            currentUserPreferredBreed = document.getString("preferredDogBreed");
                            currentUserDogSize = document.getString("dogSize");
                            currentUserLatitude = document.getDouble("latitude");
                            currentUserLongitude = document.getDouble("longitude");
                            currentUserDistance = document.getDouble("preferredMaxDistance");
                            List<String> tempList = (List<String>) document.get("preferredTemperaments");
                            currentUserPreferredTemperaments = new HashSet<>(tempList != null ? tempList : new ArrayList<String>());
                            fetchCardsFromFirebase();
                        }
                    } else {
                        Log.d(TAG, "Error getting document: ", task.getException());
                    }
                }
            });
        }
    }
    private void fetchCardsFromFirebase() {
        Log.d(TAG, "Starting to fetch cards from Firebase.");
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Successfully fetched data. Number of documents: " + task.getResult().size());
                for (DocumentSnapshot userDocument : task.getResult()) {
                    String userId = userDocument.getId();
                    if (userId.equals(currentUserId)) {
                        // Skip the current user's profile
                        Log.d(TAG, "Skipping current user: " + userId);
                        continue;
                    }

                    // Extract user details
                    String dogName = userDocument.getString("dogName");
                    String breed = userDocument.getString("breed");
                    String dogGender = userDocument.getString("dogGender");
                    String dogSize = userDocument.getString("dogSize");
                    String profileImageUrl1 = userDocument.getString("profileImageUrl1");
                    String profileImageUrl2 = userDocument.getString("profileImageUrl2");
                    String profileImageUrl3 = userDocument.getString("profileImageUrl3");
                    String profileImageUrl4 = userDocument.getString("profileImageUrl4");
                    String profileImageUrl5 = userDocument.getString("profileImageUrl5");
                    String profileImageUrl6 = userDocument.getString("profileImageUrl6");
                    String city = userDocument.getString("city");
                    String bio = userDocument.getString("dogBio");
                    Double userLatitude = userDocument.getDouble("latitude");
                    Double userLongitude = userDocument.getDouble("longitude");
                    int distance = calculateDistance(currentUserLatitude, currentUserLongitude, userLatitude, userLongitude);

                    if (currentUserDistance != null) {
                        if (distance >= currentUserDistance) {
                            Log.d(TAG, "User " + userId + " is outside of the preferred distance range.");
                            continue; // Skip if outside of the preferred distance range
                        }
                    }

                    if (currentUserDogGender != null && currentUserDogGender.equals(dogGender)) {
                        Log.d(TAG, "User " + userId + " have the same gender.");
                        continue; // Skip if outside of the preferred distance range
                    }


                    if (currentUserDogSize != null && !currentUserDogSize.equals(dogSize)) {
                        Log.d(TAG, "User " + userId + " size does not match preferred size.");
                        continue; // Skip if the size does not match the preferred size
                    }

                    if (currentUserPreferredBreed != null) {
                        if (!"Any Available Breed".equals(currentUserPreferredBreed) && (!breed.equals(currentUserPreferredBreed) || !dogSize.equals(currentUserDogSize))) {
                            Log.d(TAG, "User " + userId + " breed or size does not match preferred breed/size.");
                            continue; // Skip if the breed or size does not match the preferred breed/size
                        }
                    }

                    DocumentReference temperamentRef = db.collection("users").document(userId)
                            .collection("dogTemperaments").document("temperamentData");


                    temperamentRef.get().addOnCompleteListener(temperamentTask -> {
                        if (temperamentTask.isSuccessful()) {
                            DocumentSnapshot temperamentDocument = temperamentTask.getResult();
                            if (temperamentDocument != null && temperamentDocument.exists()) {
                                List<String> dogTemperaments = extractTemperaments(temperamentDocument);
                                Log.d(TAG, "Fetched temperaments for user " + userId + ": " + dogTemperaments);

                                boolean matches = matchesAtLeastOnePreferredTemperament(dogTemperaments, currentUserPreferredTemperaments);
                                if (matches || currentUserPreferredTemperaments.isEmpty()) {
                                    Log.d(TAG, "User " + userId + " matches preferred temperaments or no preferred temperaments set.");
                                    Cards card = new Cards(userId, dogName, breed, dogGender, profileImageUrl1, profileImageUrl2, profileImageUrl3, profileImageUrl4, profileImageUrl5, profileImageUrl6, city, distance, bio, dogTemperaments);
                                    rowItems.add(card);
                                    runOnUiThread(() -> arrayAdapter.notifyDataSetChanged());
                                } else {
                                    Log.d(TAG, "User " + userId + " does not match preferred temperaments.");
                                }
                            } else {
                                Log.d(TAG, "No temperaments data found for user " + userId);
                            }
                        } else {
                            Log.e(TAG, "Error fetching temperaments for user " + userId, temperamentTask.getException());
                        }
                    });
                }
            } else {
                Log.e(TAG, "Error fetching users", task.getException());
            }
        });
    }



    private List<String> extractTemperaments(DocumentSnapshot temperamentDocument) {
        Map<String, Object> temperamentsMap = temperamentDocument.getData();
        List<String> dogTemperaments = new ArrayList<>();
        if (temperamentsMap != null) {
            for (Map.Entry<String, Object> entry : temperamentsMap.entrySet()) {
                if (entry.getValue() instanceof Boolean && (Boolean) entry.getValue()) {
                    dogTemperaments.add(entry.getKey());
                }
            }
        }
        return dogTemperaments;
    }

    private boolean matchesAtLeastOnePreferredTemperament(List<String> dogTemperaments, Set<String> currentUserPreferredTemperaments) {
        // If there are no preferred temperaments set, return true as we are not filtering by this criterion.
        if (currentUserPreferredTemperaments.isEmpty()) {
            return true;
        }

        // If there are preferred temperaments, check for at least one match.
        for (String temperament : dogTemperaments) {
            if (currentUserPreferredTemperaments.contains(temperament)) {
                return true; // Found a match
            }
        }
        return false; // No matching temperaments
    }

    private void isUserLiked(String userId, UserLikedCallback callback) {
        // Check if the user is already liked by the current user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String currentUserId = firebaseUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference currentUserDocRef = db.collection("users").document(currentUserId);
            currentUserDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            List<String> likedUserIds = (List<String>) document.get("likedUserIds");
                            List<String> dislikedUserIds = (List<String>) document.get("dislikedUserIds");


                            if (likedUserIds != null && likedUserIds.contains(userId)) {
                                // User is already liked by the current user
                                callback.onCallback(true);
                            } else if (dislikedUserIds != null && dislikedUserIds.contains(userId)) {
                                callback.onCallback(true);
                            } else {
                                callback.onCallback(false);
                            }
                        }
                    }
                }
            });
        }
    }


    // Define a callback interface to handle the result
    interface UserLikedCallback {
        void onCallback(boolean isLiked);
    }


    private void updateSwipeCard() {

        final SwipeFlingAdapterView flingContainer = findViewById(R.id.frame);
        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
            }


            @Override
            public void onLeftCardExit(Object dataObject) {
                Cards obj = (Cards) dataObject;
                Cards card_item = rowItems.get(0);
                rowItems.remove(0);
                arrayAdapter.notifyDataSetChanged();
                String userId = card_item.getUserId();
                showNextCard();
                Intent btnClick = new Intent(mContext, BtnDislikeActivity.class);
                btnClick.putExtra("url", card_item.getProfileImageUrl1());
                checkRowItem();
            }


            @Override
            public void onRightCardExit(Object dataObject) {
                if (rowItems.size() != 0) {
                    Cards likedUser = rowItems.get(0); // Get the liked user from the top of the list
                    rowItems.remove(0);
                    arrayAdapter.notifyDataSetChanged();
                    showNextCard();

                    // Store the liked user's ID in the current user's likedUserIds
                    String likedUserId = likedUser.getUserId(); // Get the liked user's ID

                    // Add the liked user's ID to the current user's likedUserIds
                    if (currentUserDocRef != null) {
                        currentUserDocRef.update("likedUserIds", FieldValue.arrayUnion(likedUserId))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Liked user's ID added to the current user's likedUserIds
                                            // Check for mutual like
                                            checkIfMutualLike(likedUserId);
                                        } else {
                                            Log.d(TAG, "Error updating likedUserIds: " + task.getException());
                                        }
                                    }
                                });
                    }

                    Intent btnClick = new Intent(mContext, BtnLikeActivity.class);
                    btnClick.putExtra("url", likedUser.getProfileImageUrl1());
                    checkRowItem();
                }
            }





            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here if needed
            }


            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });




        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(getApplicationContext(), "Clicked", Toast.LENGTH_LONG).show();
            }
        });
    }




    public void DislikeBtn(View v) {
        if (rowItems.size() != 0)  {
            Cards dislikedUser = rowItems.get(0);
            rowItems.remove(0);
            arrayAdapter.notifyDataSetChanged();


            showNextCard();


            String dislikedUserId = dislikedUser.getUserId(); // Get the liked user's ID
            if (currentUserDocRef != null) {
                currentUserDocRef.update("dislikedUserIds", FieldValue.arrayUnion(dislikedUserId))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                } else {
                                    Log.d(TAG, "Error updating dislikedUserIds: " + task.getException());
                                }
                            }
                        });
            }
            Intent btnClick = new Intent(mContext, BtnDislikeActivity.class);
            btnClick.putExtra("url", dislikedUser.getProfileImageUrl1());
            startActivity(btnClick);
            checkRowItem();
        }
    }


    public void LikeBtn(View v) {
        if (rowItems.size() != 0) {
            Cards likedUser = rowItems.get(0); // Get the liked user from the top of the list
            rowItems.remove(0);
            arrayAdapter.notifyDataSetChanged();


            showNextCard();
            // Store the liked user's ID in the current user's likedUserIds
            String likedUserId = likedUser.getUserId(); // Get the liked user's ID


            // Add the liked user's ID to the current user's likedUserIds
            if (currentUserDocRef != null) {
                currentUserDocRef.update("likedUserIds", FieldValue.arrayUnion(likedUserId))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Liked user's ID added to the current user's likedUserIds
                                    // Now check if the liked user also liked the current user
                                    checkIfMutualLike(likedUserId);
                                } else {
                                    Log.d(TAG, "Error updating likedUserIds: " + task.getException());
                                }
                            }
                        });
            }
            Intent btnClick = new Intent(mContext, BtnLikeActivity.class);
            btnClick.putExtra("url", likedUser.getProfileImageUrl1());
            startActivity(btnClick);
            checkRowItem();
        }
    }


    private void clearDislikedUserIds() {
        if (currentUserDocRef != null) {
            currentUserDocRef.update("dislikedUserIds", new ArrayList<String>())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "DislikedUserIds cleared successfully.");
                            } else {
                                Log.e(TAG, "Error clearing DislikedUserIds: " + task.getException());
                            }
                        }
                    });
        }
    }






    private void showNextCard() {
        if (currentCardIndex < rowItems.size()) {
            currentCardIndex++;
        } else {
        }
    }




    private void checkRowItem() {
        if (rowItems.isEmpty()) {
            moreFrame.setVisibility(View.VISIBLE);
            cardFrame.setVisibility(View.GONE);
            clearDislikedUserIds();
        }
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




    @Override
    public void onBackPressed() {
        // Handle back button press
    }


    public int calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Radius of the Earth in kilometers
        double R = 6371.0;


        // Convert latitudes and longitudes from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);


        // Differences in latitudes and longitudes
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;


        // Haversine formula
        double a = Math.pow(Math.sin(deltaLat / 2), 2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(Math.sin(deltaLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));


        // Calculate the distance in kilometers
        double distance = R * c;
        int distanceint = (int) Math.round(distance);


        return distanceint;
    }

    private void checkIfMutualLike(String likedUserId) {
        // Check if the currentUserId is in the likedUserIds array of the liked user
        if (currentUserDocRef != null) {
            currentUserDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            List<String> likedUserIds = (List<String>) document.get("likedUserIds");
                            if (likedUserIds != null && likedUserIds.contains(likedUserId)) {
                                // Check if the liked user's likedUserIds contains the currentUserId
                                likedUserDocRef = db.collection("users").document(likedUserId);
                                likedUserDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot likedUserDocument = task.getResult();
                                            if (likedUserDocument != null) {
                                                List<String> likedByLikedUser = (List<String>) likedUserDocument.get("likedUserIds");
                                                if (likedByLikedUser != null && likedByLikedUser.contains(currentUserId)) {
                                                    // The current user is also liked by the liked user, so it's a mutual like
                                                    // You can perform any further actions here if needed
                                                    storeMutualLike(likedUserId);

                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        Log.d(TAG, "Error getting current user document: " + task.getException());
                    }
                }
            });
        }
    }



    private void storeMutualLike(String likedUserId) {
        // Store both user IDs in UserMatches
        if (currentUserDocRef != null) {
            UserMatches mutualLike = new UserMatches(currentUserId, likedUserId);
            db.collection("UserMatches")
                    .add(mutualLike)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                // Mutual like stored in UserMatches
                                // You can perform any further actions here if needed
                            } else {
                                Log.d(TAG, "Error storing mutual like: " + task.getException());
                            }
                        }
                    });
        }
    }
}





