package com.mari.magic.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.mari.magic.R;
import com.mari.magic.auth.WelcomeActivity;

public class SplashActivity extends AppCompatActivity {

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();

        ImageView logo = findViewById(R.id.logo);

        Glide.with(this)
                .asGif()
                .load(R.drawable.app_logo)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontTransform()
                .into(logo);

        new Handler().postDelayed(() -> {

            if(auth.getCurrentUser() != null){

                // Đã đăng nhập
                startActivity(new Intent(
                        SplashActivity.this,
                        MainActivity.class
                ));

            }else{

                // Chưa login
                startActivity(new Intent(
                        SplashActivity.this,
                        WelcomeActivity.class
                ));

            }

            finish();

        },2000);
    }
}