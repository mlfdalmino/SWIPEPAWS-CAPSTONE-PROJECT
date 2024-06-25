package com.capstone.project.swipepaws.Profile;


import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIAPI {
    @POST("chat/completions")
    Call<JsonObject> getChatbotResponse(@Body JsonObject requestBody);
}



