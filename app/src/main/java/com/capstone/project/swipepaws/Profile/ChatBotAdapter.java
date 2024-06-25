package com.capstone.project.swipepaws.Profile;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.capstone.project.swipepaws.R;


import java.util.ArrayList;
import java.util.List;


public class ChatBotAdapter extends RecyclerView.Adapter<ChatBotAdapter.MessageViewHolder> {


    private List<Message> messageList;


    public ChatBotAdapter() {
        this.messageList = new ArrayList<>();
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public void addMessage(String sender, String message) {
        messageList.add(new Message(sender, message));
        notifyItemInserted(messageList.size() - 1);
    }


    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView;
        TextView messageTextView;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.commentAuthorTextView);
            messageTextView = itemView.findViewById(R.id.commentContentTextView);
        }


        public void bind(Message message) {
            senderTextView.setText(message.getSender());
            messageTextView.setText(message.getMessage());


            // Hide the options_menu ImageView
            itemView.findViewById(R.id.options_menu).setVisibility(View.GONE);
            itemView.findViewById(R.id.commentTimestampTextView).setVisibility(View.GONE);
        }
    }


    public static class Message {
        private String sender;
        private String message;


        public Message(String sender, String message) {
            this.sender = sender;
            this.message = message;
        }


        public String getSender() {
            return sender;
        }


        public String getMessage() {
            return message;
        }
    }
}
