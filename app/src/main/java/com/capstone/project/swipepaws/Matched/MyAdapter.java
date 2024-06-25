package com.capstone.project.swipepaws.Matched;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.capstone.project.swipepaws.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.PostViewHolder> {
    private List<Post> postList;
    private OnPostClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

    // OnPostClickListener interface for handling clicks
    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    // Constructor updated to include listener for click events
    public MyAdapter(List<Post> postList, OnPostClickListener listener) {
        this.postList = postList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.postContent.setText(post.getContent());
        holder.postAuthor.setText(post.getAuthor());
        holder.postTimestamp.setText(dateFormat.format(post.getTimestamp().toDate()));

        loadUserProfilePicture(post.getUserId(), holder.profileImage);
        // Set the click listener for each post
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onPostClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        public TextView postContent;
        public TextView postAuthor;
        public TextView postTimestamp;
        public ImageView profileImage;

        public PostViewHolder(View view) {
            super(view);
            postContent = view.findViewById(R.id.postContent);
            postAuthor = view.findViewById(R.id.postAuthor);
            postTimestamp = view.findViewById(R.id.postTimestamp);
            profileImage = view.findViewById(R.id.profileurl);
        }
    }

    private void loadUserProfilePicture(String userId, ImageView imageView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageUrl = documentSnapshot.getString("profilePictureUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(imageView.getContext())
                                    .load(imageUrl)
                                    .circleCrop() // Applies a circular transformation
                                    .into(imageView);
                        }
                    } else {
                        Log.d("MyAdapter", "No profile image found for user: " + userId);
                    }
                })
                .addOnFailureListener(e -> Log.e("MyAdapter", "Error loading profile image for user: " + userId, e));
    }

}
