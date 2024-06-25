package com.capstone.project.swipepaws.Introduction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.capstone.project.swipepaws.Login.Login;
import com.capstone.project.swipepaws.Login.RegisterBasicInfo;
import com.capstone.project.swipepaws.Main.MainActivity;
import com.capstone.project.swipepaws.R;

public class IntroductionMain extends AppCompatActivity {

    private Button signupButton;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


            // If not logged in previously, proceed with IntroductionMain functionality
            setContentView(R.layout.activity_introduction_main);

            signupButton = findViewById(R.id.signup_button);
            signupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openEmailAddressEntryPage();
                }
            });

            loginButton = findViewById(R.id.login_button);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openLoginPage();
                }
            });

    }

    public void openLoginPage() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

    public void openEmailAddressEntryPage() {
        Intent intent = new Intent(this, RegisterBasicInfo.class);
        startActivity(intent);
        finish();
    }
}
