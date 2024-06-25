package com.capstone.project.swipepaws.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.project.swipepaws.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<Message> messages;
    private String currentUserId;

    // Constructor to initialize the adapter with messages and current user ID
    public ChatAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // View holder for messages
    class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout leftMessageContainer;
        TextView leftMessageTextView;
        LinearLayout rightMessageContainer;
        TextView rightMessageTextView;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            leftMessageContainer = itemView.findViewById(R.id.leftMessageContainer);
            leftMessageTextView = itemView.findViewById(R.id.leftMessageTextView);
            rightMessageContainer = itemView.findViewById(R.id.rightMessageContainer);
            rightMessageTextView = itemView.findViewById(R.id.rightMessageTextView);
        }

        void bind(Message message) {
            if (message.getSenderId().equals(currentUserId)) {
                // Sent message
                leftMessageContainer.setVisibility(View.GONE);
                rightMessageContainer.setVisibility(View.VISIBLE);
                rightMessageTextView.setText(message.getContent());
            } else {
                // Received message
                leftMessageContainer.setVisibility(View.VISIBLE);
                rightMessageContainer.setVisibility(View.GONE);
                leftMessageTextView.setText(message.getContent());
            }
        }
    }
}
