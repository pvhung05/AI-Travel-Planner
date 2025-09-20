package com.example.ai_travel_planner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class LoginScreen extends Activity {
    ImageButton btnBack, btnGoogle, btnFacebook, btnApple;
    Button btnSignin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginscreen);

        btnBack = findViewById(R.id.btnBack);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnApple = findViewById(R.id.btnApple);
        btnSignin = findViewById(R.id.btnSignIn);

        actionButton(btnBack, StartScreen.class);
        actionButton(btnSignin, HomeScreen.class);

    }
    void actionButton(View view, final Class<?> destination) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginScreen.this, destination);
                startActivity(intent);
                finish();
            }
        });
    }
}
