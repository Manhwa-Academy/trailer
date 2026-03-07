package com.mari.magic.home;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.mari.magic.R;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class AnimeTrailerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer);

        YouTubePlayerView playerView = findViewById(R.id.youtubePlayerView);

        getLifecycle().addObserver(playerView);

        String trailerId = getIntent().getStringExtra("trailer");

        playerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {

                if(trailerId != null && !trailerId.isEmpty()){
                    youTubePlayer.loadVideo(trailerId, 0);
                }
            }
        });
    }
}