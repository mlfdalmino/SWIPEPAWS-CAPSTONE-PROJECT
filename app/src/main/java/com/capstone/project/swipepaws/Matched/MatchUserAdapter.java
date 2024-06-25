package com.capstone.project.swipepaws.Matched;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.capstone.project.swipepaws.R;
import com.capstone.project.swipepaws.chat.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MatchUserAdapter extends RecyclerView.Adapter<MatchUserAdapter.MyViewHolder> {
    private List<Users> usersList;
    private Context context;
    private Matched_Activity activity;

    // Define an interface for item click events
    public interface OnItemClickListener {
        void onItemClick(String matchedUserId);
    }

    private OnItemClickListener onItemClickListener;

    // Constructor with an additional parameter for the listener
    public MatchUserAdapter(List<Users> usersList, Context context, Matched_Activity activity, OnItemClickListener listener) {
        this.usersList = usersList;
        this.context = context;
        this.activity = activity;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the RecyclerView item
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.matched_user_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchUserAdapter.MyViewHolder holder, int position) {
        // Get the user at the current position
        Users users = usersList.get(position);

        // Set the user's name and bio to the corresponding views
        holder.name.setText(users.getName());
        holder.profession.setText(users.getBio());

        // Load the user's profile image using Picasso library
        if (users.getProfileImageUrl() != null) {
            Picasso.get().load(users.getProfileImageUrl()).into(holder.imageView);
        }

        // Set an onClickListener for the item view
        // Inside your adapter's click listener
        // Inside onBindViewHolder method
        holder.itemView.setOnClickListener(view -> {
            // Get the matchedUserId from the Users object
            String matchedUserId = users.getUserId();

            // Check if matchedUserId is not null or empty
            if (matchedUserId != null && !matchedUserId.isEmpty()) {
                // Call the method to handle navigation to the ChatActivity
                navigateToChatActivity(users.getProfileImageUrl(), users.getName(), matchedUserId);

                // Notify the activity about the item click
                onItemClickListener.onItemClick(matchedUserId);
            } else {
                // Handle the case where matchedUserId is null or empty
                Log.e("MatchUserAdapter", "matchedUserId is null or empty");
                // You might want to log a warning or show a message to the user
            }
        });
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the RecyclerView
        return usersList.size();
    }

    // ViewHolder class to hold references to the views in each item
    public class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageView;
        TextView name, profession;

        // Constructor to initialize the views
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.mui_image);
            name = itemView.findViewById(R.id.mui_name);
            profession = itemView.findViewById(R.id.mui_profession);
        }
    }

    // Method to navigate to the ChatActivity
    // Use the activity field instead of context
    private void navigateToChatActivity(String profileImageUrl, String dogName, String matchedUserId) {
        // Create an Intent for the ChatActivity
        Intent intent = new Intent(context, ChatActivity.class);
        // Pass relevant data to the ChatActivity
        intent.putExtra("profileImageUrl", profileImageUrl);
        intent.putExtra("dogName", dogName);
        intent.putExtra("matchedUserId", matchedUserId);

        // Use the context from the itemView to start the activity
        if (context instanceof Activity) {
            // Start the activity using startActivityForResult to handle back navigation
            ((Activity) context).startActivityForResult(intent, 0);  // You can use any requestCode
        } else {
            // Handle the case where the context is not an instance of Activity
            // You may want to log a warning or handle it according to your needs
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}