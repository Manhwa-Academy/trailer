package com.mari.magic.ui.detail;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mari.magic.R;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class AnimeTrailerActivity extends AppCompatActivity {

    private YouTubePlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer);

        playerView = findViewById(R.id.youtubePlayerView);

        // lifecycle observer
        getLifecycle().addObserver(playerView);

        String trailerId = getIntent().getStringExtra("trailer");

        if(trailerId == null || trailerId.isEmpty()){
            finish();
            return;
        }

        playerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {

            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(trailerId, 0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(playerView != null){
            playerView.release();
        }
    }
}