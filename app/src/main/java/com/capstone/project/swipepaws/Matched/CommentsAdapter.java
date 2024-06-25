package com.capstone.project.swipepaws.Matched;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.capstone.project.swipepaws.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private Context context;
    private String currentUserId; // Current user's ID



    public CommentsAdapter(Context context, List<Comment> comments, String currentUserId) {
        this.context = context;
        this.comments = comments;
        this.currentUserId = currentUserId; // Initialize currentUserId
    }


    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        final Comment comment = comments.get(position);
        holder.commentAuthorTextView.setText(comment.getAuthor());
        holder.commentContentTextView.setText(comment.getContent());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        holder.commentTimestampTextView.setText(dateFormat.format(comment.getTimestamp().toDate()));

        loadUserProfilePicture(comment.getUserId(), holder.profileImage);
        // Check if the current user ID matches the comment user ID
        if (currentUserId.equals(comment.getUserId())) {
            // If they match, show the options menu
            holder.optionsMenu.setVisibility(View.VISIBLE);
            holder.optionsMenu.setOnClickListener(v -> showCommentOptionsMenu(v, position, comment.getUserId()));
        } else {
            // If they don't match, hide the options menu
            holder.optionsMenu.setVisibility(View.GONE);
        }
    }


    private void showCommentOptionsMenu(View view, int position, String commentUserId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("CommentsAdapter", "Current User ID: " + (currentUser != null ? currentUser.getUid() : "null"));
        Log.d("CommentsAdapter", "Comment User ID: " + commentUserId);


        if (currentUser != null && currentUser.getUid().equals(commentUserId)) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.inflate(R.menu.post_option_menu); // Ensure this menu has options for edit and delete
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.menu_edit:
                        editComment(position);
                        return true;
                    case R.id.menu_delete:
                        deleteComment(position);
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();
        } else {
            Toast.makeText(context, "You can only edit or delete your own comments.", Toast.LENGTH_SHORT).show();
        }
    }


    private void editComment(int position) {
        Comment comment = comments.get(position);
        String documentId = comment.getId();
        // Ensure you have logic for editing the comment here...
        if (documentId == null) {
            Toast.makeText(context, "Cannot edit comment: No ID available", Toast.LENGTH_SHORT).show();
            return;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Comment");


        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(comment.getContent());
        builder.setView(input);


        builder.setPositiveButton("Save", (dialog, which) -> {
            String newContent = input.getText().toString();
            FirebaseFirestore.getInstance().collection("comments").document(documentId)
                    .update("content", newContent)
                    .addOnSuccessListener(aVoid -> {
                        comment.setContent(newContent);
                        notifyItemChanged(position);
                        Toast.makeText(context, "Comment updated successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update comment", Toast.LENGTH_SHORT).show());
        });


        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }


    private void deleteComment(int position) {
        Comment comment = comments.get(position);
        String documentId = comment.getId();
        // Ensure you have logic for deleting the comment here...
        if (documentId == null) {
            Toast.makeText(context, "Cannot delete comment: No ID available", Toast.LENGTH_SHORT).show();
            return;
        }


        FirebaseFirestore.getInstance().collection("comments").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    comments.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show());
    }


    @Override
    public int getItemCount() {
        return comments.size();
    }


    public void updateComments(List<Comment> newComments) {
        comments.clear();
        comments.addAll(newComments);
        notifyDataSetChanged();
    }


    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentAuthorTextView, commentContentTextView, commentTimestampTextView;
        ImageView optionsMenu;
        ImageView profileImage;


        CommentViewHolder(View itemView) {
            super(itemView);
            commentAuthorTextView = itemView.findViewById(R.id.commentAuthorTextView);
            commentContentTextView = itemView.findViewById(R.id.commentContentTextView);
            commentTimestampTextView = itemView.findViewById(R.id.commentTimestampTextView);
            profileImage = itemView.findViewById(R.id.profileurl);
            optionsMenu = itemView.findViewById(R.id.options_menu);
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
                                    .circleCrop()  // Applies a circular transformation
                                    .into(imageView);
                        }
                    } else {
                        Log.d("CommentsAdapter", "No profile image found for user: " + userId);
                    }
                })
                .addOnFailureListener(e -> Log.e("CommentsAdapter", "Error loading profile image for user: " + userId, e));
    }

}
