package com.capstone.project.swipepaws.Profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.project.swipepaws.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText userInput;
    private Button sendButton;
    private ChatBotAdapter chatAdapter;
    private OpenAIAPI openAIAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatbot);

        chatRecyclerView = findViewById(R.id.chatLayout);
        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton);

        chatAdapter = new ChatBotAdapter();
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        openAIAPI = retrofit.create(OpenAIAPI.class);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String userMessage = userInput.getText().toString().trim();
        if (!userMessage.isEmpty()) {
            chatAdapter.addMessage("You", userMessage);

            int estimatedPromptTokens = userMessage.split("\\s+").length; // Estimate prompt tokens
            int maxResponseTokens = 500 - estimatedPromptTokens; // Deduct prompt tokens from total budget

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", "gpt-3.5-turbo");
            requestBody.addProperty("max_tokens", maxResponseTokens); // Dynamic token limit based on the prompt

            JsonArray messages = new JsonArray();
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", userMessage);
            messages.add(message);
            requestBody.add("messages", messages);

            Call<JsonObject> call = openAIAPI.getChatbotResponse(requestBody);
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        JsonArray choicesArray = response.body().getAsJsonArray("choices");
                        if (choicesArray.size() > 0) {
                            JsonObject choice = choicesArray.get(0).getAsJsonObject();
                            if (choice.has("message")) {
                                JsonObject message = choice.getAsJsonObject("message");
                                if (message.has("content")) {
                                    String chatbotAnswer = message.get("content").getAsString();
                                    chatAdapter.addMessage("Chatbot", chatbotAnswer);
                                } else {
                                    chatAdapter.addMessage("Chatbot", "No content in message");
                                }
                            } else {
                                chatAdapter.addMessage("Chatbot", "No message object found");
                            }
                        } else {
                            chatAdapter.addMessage("Chatbot", "No choices available");
                        }
                    } else {
                        Log.e("API Error", "Response Code: " + response.code() + " Message: " + response.message());
                        chatAdapter.addMessage("Chatbot", "Error: Unable to get a response, code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.e("API Failure", "Error: " + t.getMessage());
                    chatAdapter.addMessage("Chatbot", "Error: " + t.getMessage());
                }
            });

            userInput.getText().clear();
        }
    }
}