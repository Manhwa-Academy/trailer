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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
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
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1️⃣ Lưu history chi tiết
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

        db.collection("users")
                .document(uid)
                .collection("history")
                .document(animeId)
                .set(data);

        // 2️⃣ Cập nhật stats + streak
        DocumentReference userRef = db.collection("users").document(uid);
        long now = System.currentTimeMillis();
        userRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Map<String,Object> dataUser = doc.getData();

                int moviesWatched = dataUser.get("moviesWatched") != null ? ((Long)dataUser.get("moviesWatched")).intValue() : 0;
                int mangaRead = dataUser.get("mangaRead") != null ? ((Long)dataUser.get("mangaRead")).intValue() : 0;
                int streak = dataUser.get("streak") != null ? ((Long)dataUser.get("streak")).intValue() : 0;
                long lastRead = dataUser.get("lastRead") != null ? ((Long)dataUser.get("lastRead")) : 0;
                long lastWatch = dataUser.get("lastWatch") != null ? ((Long)dataUser.get("lastWatch")) : 0;

                long startOfToday = getStartOfDayMillis();

                Map<String,Object> updates = new HashMap<>();

                // Manga/Novel tăng mangaRead + streak
                if(format.equalsIgnoreCase("MANGA") || format.equalsIgnoreCase("NOVEL") || format.equalsIgnoreCase("ONE_SHOT")){
                    if(lastRead < startOfToday) streak += 1;
                    updates.put("mangaRead", mangaRead + 1);
                    updates.put("lastRead", now);
                }
                // Anime/Phim tăng moviesWatched + streak
                else {
                    if(lastWatch < startOfToday) streak += 1;
                    updates.put("moviesWatched", moviesWatched + 1);
                    updates.put("lastWatch", now);
                }

                updates.put("streak", streak);

                userRef.update(updates);
            }
        });
    }

    // Helper tính đầu ngày hôm nay
    private static long getStartOfDayMillis(){
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY,0);
        cal.set(java.util.Calendar.MINUTE,0);
        cal.set(java.util.Calendar.SECOND,0);
        cal.set(java.util.Calendar.MILLISECOND,0);
        return cal.getTimeInMillis();
    }
    public static void saveBannerHistory(
            Context context,
            String animeId,
            String title,
            String poster,
            String trailer,
            String studio,
            String director,
            String season,
            int duration,
            String format,
            int episodes,
            String status,
            long updatedAt,
            double rating,
            String description,
            String genres,
            int nextEpisode,
            long nextAiringAt,
            String romajiTitle,
            String nativeTitle
    ){

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if(auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        // tránh null id
        if(animeId == null || animeId.isEmpty()){
            animeId = String.valueOf(System.currentTimeMillis());
        }

        Map<String,Object> data = new HashMap<>();

        data.put("animeId", animeId);
        data.put("title", title != null ? title : "Unknown");
        data.put("poster", poster);
        data.put("trailer", trailer);
        data.put("romajiTitle", romajiTitle);
        data.put("nativeTitle", nativeTitle);
        data.put("studio", studio);
        data.put("director", director);
        data.put("season", season);
        data.put("duration", duration);
        data.put("format", format);
        data.put("episodes", episodes);
        data.put("status", status);
        data.put("updatedAt", updatedAt);

        data.put("rating", rating);
        data.put("description", description);
        data.put("genres", genres);

        data.put("nextEpisode", nextEpisode);
        data.put("nextAiringAt", nextAiringAt);

        data.put("time", System.currentTimeMillis());

        db.collection("users")
                .document(uid)
                .collection("history")
                .document(animeId)
                .set(data)
                .addOnSuccessListener(unused -> {
                    android.util.Log.d("HISTORY","Banner history saved");
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("HISTORY","Save failed",e);
                });

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