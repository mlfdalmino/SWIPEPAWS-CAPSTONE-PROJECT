package com.capstone.project.swipepaws.chat;

import android.os.Bundle;
import android.util.Log; // Add this import for logging
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.project.swipepaws.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity"; // Add this constant for logging

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private ImageButton back;
    private List<Message> messages;
    private EditText messageEditText;
    private ImageButton sendMessageButton;
    private String matchedUserId;

    private CircleImageView profileImageView;
    private TextView profileNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        matchedUserId = getIntent().getStringExtra("matchedUserId");
        String profileImageUrl = getIntent().getStringExtra("profileImageUrl");
        String dogName = getIntent().getStringExtra("dogName");

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendMessageButton = findViewById(R.id.sendButton);
        profileImageView = findViewById(R.id.profileImageView);
        profileNameTextView = findViewById(R.id.profileNameTextView);

        // Load the profile image using Picasso library
        Picasso.get().load(profileImageUrl).into(profileImageView);

        profileNameTextView.setText(dogName);

        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages, getCurrentUserId());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Call retrievePreviousMessages after setting up the adapter and layout manager
        retrievePreviousMessages(getCurrentUserId(), matchedUserId);

        sendMessageButton.setOnClickListener(view -> sendMessage());

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void sendMessage() {
        String currentUserId = getCurrentUserId();
        String messageText = messageEditText.getText().toString().trim();

        if (!messageText.isEmpty()) {
            // Set the receiverId as matchedUserId for sent messages
            Message message = new Message(currentUserId, matchedUserId, messageText, System.currentTimeMillis());

            // Add the new message to the list
            messages.add(message);

            // Notify adapter about the new message
            chatAdapter.notifyItemInserted(messages.size() - 1);

            // Scroll to the bottom of the RecyclerView to show the latest message
            recyclerView.scrollToPosition(messages.size() - 1);

            messageEditText.setText("");

            // Send the message to Firestore
            sendMessageToFirestore(message);
        }
    }

    private void sendMessageToFirestore(Message message) {
        CollectionReference messagesRef = FirebaseFirestore.getInstance().collection("messages");

        messagesRef.add(message)
                .addOnSuccessListener(documentReference -> {
                    // Handle success (optional)
                    Log.d(TAG, "Message sent successfully to Firestore");
                })
                .addOnFailureListener(e -> {
                    // Handle failure (optional)
                    Log.e(TAG, "Error sending message to Firestore", e);
                });
    }

    private void retrievePreviousMessages(String currentUserId, String matchedUserId) {
        CollectionReference messagesRef = FirebaseFirestore.getInstance().collection("messages");

        Query query = messagesRef
                .orderBy("timestamp", Query.Direction.ASCENDING);

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                // Handle errors while listening for updates
                Log.e(TAG, "Error listening for messages", error);
                return;
            }

            messages.clear();
            for (QueryDocumentSnapshot document : value) {
                String senderId = document.getString("senderId");
                String receiverId = document.getString("receiverId");
                String messageText = document.getString("content");
                long timestamp = document.getLong("timestamp");

                // Check if the sender or receiver is not null and matches the current user or matched user
                if (senderId != null && receiverId != null &&
                        (senderId.equals(currentUserId) && receiverId.equals(matchedUserId)
                                || senderId.equals(matchedUserId) && receiverId.equals(currentUserId))) {
                    Message message = new Message(senderId, receiverId, messageText, timestamp);
                    messages.add(message);
                }
            }
            chatAdapter.notifyDataSetChanged(); // Notify adapter about the changes
        });
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : "";
    }
}
