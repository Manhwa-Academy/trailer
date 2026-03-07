package com.mari.magic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    private static final String TAG = "ThemeManager";

    // dùng chung với SettingsFragment
    private static final String PREF = "settings";
    private static final String KEY = "theme";

    // áp dụng theme khi app start
    public static void applyTheme(Context context){

        SharedPreferences pref =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        int mode = pref.getInt(KEY, AppCompatDelegate.MODE_NIGHT_NO);

        Log.d(TAG,"applyTheme -> mode = " + mode);

        AppCompatDelegate.setDefaultNightMode(mode);
    }

    // lưu theme mới
    public static void saveTheme(Context context, int mode){

        SharedPreferences pref =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        pref.edit().putInt(KEY, mode).apply();

        Log.d(TAG,"saveTheme -> mode = " + mode);

        AppCompatDelegate.setDefaultNightMode(mode);
    }

    // lấy theme hiện tại
    public static int getTheme(Context context){

        SharedPreferences pref =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        int mode = pref.getInt(KEY, AppCompatDelegate.MODE_NIGHT_NO);

        Log.d(TAG,"getTheme -> mode = " + mode);

        return mode;
    }
}