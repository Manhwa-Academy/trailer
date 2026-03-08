package com.mari.magic.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {

    private static final String PREF = "app_settings";

    // LANGUAGE
    public static void setLanguage(Context ctx, String lang){

        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString("lang", lang).apply();

        LocaleManager.setLocale(ctx, lang);
    }

    public static String getLanguage(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getString("lang", "en");
    }


    // CONTENT FILTER
    public static void setContentFilter(Context ctx, String filter){
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putString("filter", filter).apply();
    }

    public static String getContentFilter(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getString("filter", "all");
    }


    // FIRST SETUP POPUP
    public static void setSetupDone(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().putBoolean("setup_done", true).apply();
    }

    public static boolean isSetupDone(Context ctx){
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getBoolean("setup_done", false);
    }

}