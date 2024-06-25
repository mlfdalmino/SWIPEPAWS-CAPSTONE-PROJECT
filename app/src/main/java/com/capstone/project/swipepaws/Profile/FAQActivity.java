package com.capstone.project.swipepaws.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.capstone.project.swipepaws.R;

public class FAQActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_form);

        // Find the ImageButton by ID
        ImageButton backButton = findViewById(R.id.back);

        // Set an OnClickListener for the ImageButton
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Finish the current activity to navigate back
                finish();
            }
        });

        // Find the Button (CHATBOT) by ID
        Button chatbotButton = findViewById(R.id.btnChatbot);

        // Set an OnClickListener for the CHATBOT Button
        chatbotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Redirect to the Chatbot screen
                redirectToChatbotScreen();
            }
        });
    }

    // Function to redirect to the Chatbot screen
    private void redirectToChatbotScreen() {
        // Create an Intent to start the ChatbotActivity
        Intent chatbotIntent = new Intent(this, ChatbotActivity.class); // Replace ChatbotActivity.class with the actual class name
        startActivity(chatbotIntent);
    }
}