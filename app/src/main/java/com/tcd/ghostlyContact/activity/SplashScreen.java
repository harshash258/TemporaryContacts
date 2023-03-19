package com.tcd.ghostlyContact.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.mycompany.createtemporarycontact.R;


public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        ImageView backgroundImage = findViewById(R.id.image);
        Animation slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_anim);
        backgroundImage.startAnimation(slideAnimation);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashScreen.this, CreateContactActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}