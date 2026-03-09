package com.mari.magic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryManager {

    // ================= SEARCH HISTORY =================
    private static final String SEARCH_PREF = "search_history_pref";
    private static final String SEARCH_KEY = "search_history";

    public static void saveSearch(Context context, String keyword) {
        try {
            SharedPreferences pref = context.getSharedPreferences(SEARCH_PREF, Context.MODE_PRIVATE);
            String json = pref.getString(SEARCH_KEY, "[]");
            JSONArray array = new JSONArray(json);

            // Avoid duplicates
            for (int i = 0; i < array.length(); i++) {
                if (array.getString(i).equalsIgnoreCase(keyword)) {
                    return;
                }
            }

            array.put(keyword);
            pref.edit().putString(SEARCH_KEY, array.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getSearchHistory(Context context) {
        List<String> list = new ArrayList<>();
        try {
            SharedPreferences pref = context.getSharedPreferences(SEARCH_PREF, Context.MODE_PRIVATE);
            String json = pref.getString(SEARCH_KEY, "[]");
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
            Collections.reverse(list); // Most recent first
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void removeSearch(Context context, String keyword) {
        try {
            SharedPreferences pref = context.getSharedPreferences(SEARCH_PREF, Context.MODE_PRIVATE);
            String json = pref.getString(SEARCH_KEY, "[]");
            JSONArray array = new JSONArray(json);
            JSONArray newArray = new JSONArray();

            for (int i = 0; i < array.length(); i++) {
                String item = array.getString(i);
                if (!item.equalsIgnoreCase(keyword)) {
                    newArray.put(item);
                }
            }

            pref.edit().putString(SEARCH_KEY, newArray.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= WATCH HISTORY (FIREBASE) =================
    public static void saveHistory(
            Context context,
            String animeId,
            String title,
            String poster,
            double rating,
            String trailer,
            String studio,
            String director,
            String season,
            int duration,
            String format,
            String romajiTitle,
            String nativeTitle,
            String desc,
            String genres,
            int episodes,
            int nextEpisode,
            long nextAiringAt,
            String status,
            boolean isAdult,
            long updatedAt,
            long views
    ) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        Map<String, Object> data = new HashMap<>();
        data.put("animeId", animeId);
        data.put("title", title);
        data.put("poster", poster);
        data.put("rating", rating);
        data.put("trailer", trailer);
        data.put("studio", studio);
        data.put("director", director);
        data.put("season", season);
        data.put("duration", duration);
        data.put("format", format);
        data.put("romajiTitle", romajiTitle);
        data.put("nativeTitle", nativeTitle);
        data.put("description", desc);
        data.put("genres", genres);
        data.put("episodes", episodes);
        data.put("nextEpisode", nextEpisode);
        data.put("nextAiringAt", nextAiringAt);
        data.put("status", status);
        data.put("isAdult", isAdult);
        data.put("updatedAt", updatedAt);
        data.put("views", views);
        data.put("time", System.currentTimeMillis());

        // Avoid duplicates by using animeId as document ID
        db.collection("users")
                .document(uid)
                .collection("history")
                .document(animeId)
                .set(data);
    }

    // ================= CLEAR HISTORY =================
    public static void clearWatchHistory(Context context) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(uid)
                .collection("history")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (var doc : snapshot) {
                        doc.getReference().delete();
                    }
                });
    }
    public static void removeHistory(String animeId) {

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if(auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("history")
                .document(animeId)
                .delete();
    }
}