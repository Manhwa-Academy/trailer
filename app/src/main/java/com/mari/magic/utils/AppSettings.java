package com.mari.magic.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {

    private static final String PREF = "app_settings";

    // helper: tránh null context
    private static SharedPreferences prefs(Context ctx){

        if(ctx == null) return null;

        return ctx.getApplicationContext()
                .getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    // ================= LANGUAGE =================

    public static void setLanguage(Context ctx, String lang){

        SharedPreferences sp = prefs(ctx);
        if(sp == null) return;

        sp.edit().putString("lang", lang).apply();

        LocaleManager.setLocale(ctx, lang);
    }

    public static String getLanguage(Context ctx){

        SharedPreferences sp = prefs(ctx);
        if(sp == null) return "en";

        return sp.getString("lang", "en");
    }

    // ================= CONTENT FILTER =================

    public static void setContentFilter(Context ctx, String filter){

        SharedPreferences sp = prefs(ctx);
        if(sp == null) return;

        sp.edit().putString("filter", filter).apply();
    }

    public static String getContentFilter(Context ctx){

        SharedPreferences sp = prefs(ctx);
        if(sp == null) return "all";

        return sp.getString("filter", "all");
    }

    // ================= FIRST SETUP POPUP =================

    public static void setSetupDone(Context ctx){

        SharedPreferences sp = prefs(ctx);
        if(sp == null) return;

        sp.edit().putBoolean("setup_done", true).apply();
    }

    public static boolean isSetupDone(Context ctx){

        SharedPreferences sp = prefs(ctx);
        if(sp == null) return false;

        return sp.getBoolean("setup_done", false);
    }

    // ================= SEARCH HISTORY =================

    public static void setSearchHistoryEnabled(Context ctx, boolean enabled){

        SharedPreferences sp = prefs(ctx);
        if(sp == null) return;

        sp.edit().putBoolean("search_history", enabled).apply();
    }

    public static boolean isSearchHistoryEnabled(Context ctx){

        SharedPreferences sp = prefs(ctx);
        if(sp == null) return true;

        return sp.getBoolean("search_history", true);
    }
    // ================= BACKGROUND IMAGE =================
    public static void setBackground(Context ctx, String bg){
        SharedPreferences sp = prefs(ctx);
        if(sp == null) return;
        sp.edit().putString("background", bg).apply();
    }

    public static String getBackground(Context ctx){
        SharedPreferences sp = prefs(ctx);
        if(sp == null) return "default"; // mặc định là hình đen
        return sp.getString("background", "default");
    }
}

