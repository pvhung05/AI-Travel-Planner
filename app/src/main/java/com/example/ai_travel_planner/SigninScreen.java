package com.example.ai_travel_planner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class SigninScreen extends Activity {
    ImageButton btnBack, btnGoogle, btnFacebook, btnApple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signinscreen);

        btnBack = findViewById(R.id.btnBack);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        btnApple = findViewById(R.id.btnApple);

        actionButton(btnBack, StartScreen.class);
    }
    void actionButton(View view, final Class<?> destination) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SigninScreen.this, destination);
                startActivity(intent);
                finish();
            }
        });
    }
}
