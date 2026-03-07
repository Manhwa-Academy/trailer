package com.mari.magic.home;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mari.magic.R;
import com.mari.magic.utils.ThemeManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Apply theme trước khi activity tạo UI
        ThemeManager.applyTheme(this);

        Log.d(TAG,"Theme applied before onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        Log.d(TAG,"MainActivity created");

        // mở Home mặc định
        if(savedInstanceState == null){
            loadFragment(new HomeFragment());
        }

        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment fragment;

            int id = item.getItemId();

            if(id == R.id.nav_home){
                fragment = new HomeFragment();
            }
            else if(id == R.id.nav_movies){
                fragment = new MoviesFragment();
            }
            else if(id == R.id.nav_tv){
                fragment = new TvSeriesFragment();
            }
            else if(id == R.id.nav_settings){
                fragment = new SettingsFragment();
            }
            else{
                return false;
            }

            Log.d(TAG,"Switch fragment -> " + fragment.getClass().getSimpleName());

            loadFragment(fragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment){

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}