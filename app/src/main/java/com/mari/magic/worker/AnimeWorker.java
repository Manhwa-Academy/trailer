package com.mari.magic.worker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.*;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mari.magic.MainActivity;
import com.mari.magic.model.EpisodeModel;
import com.mari.magic.ui.notification.NotificationActivity;
import com.mari.magic.utils.FavoriteManager;
import com.mari.magic.utils.NotificationHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AnimeWorker extends Worker {

    private final FirebaseFirestore db;
    private boolean hasError = false;

    public AnimeWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("WORKER", "Worker started");

        CountDownLatch latch = new CountDownLatch(1);
        checkAnime(latch);

        try {
            latch.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            hasError = true;
        }

        Log.d("WORKER", "Worker finished");
        return hasError ? Result.retry() : Result.success();
    }

    // ================= API =================
    private void checkAnime(CountDownLatch latch) {
        String url = "https://graphql.anilist.co";

        // Query: lấy title, cover, nextAiringEpisode, format, season, duration, episodes, studio, staff, description, genres
        String query = "{ Page(page:1, perPage:25){ media(type:ANIME,status:RELEASING,sort:UPDATED_AT_DESC){ " +
                "id title { userPreferred romaji english native } coverImage { large } " +
                "format season seasonYear duration episodes description genres studios { nodes { name } } " +
                "staff { edges { role node { name { full } } } } trailer { id site } " +
                "nextAiringEpisode { episode airingAt } averageScore popularity } } }";

        JSONObject body = new JSONObject();
        try { body.put("query", query); } catch (Exception e){ e.printStackTrace(); }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> { handleResponse(response); latch.countDown(); },
                error -> { Log.e("WORKER", "API error: " + error); hasError = true; latch.countDown(); }
        );

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    // ================= HANDLE RESPONSE =================
    private void handleResponse(JSONObject response) {
        try {
            JSONArray list = response.getJSONObject("data")
                    .getJSONObject("Page")
                    .getJSONArray("media");

            for (int i = 0; i < list.length(); i++) {
                JSONObject anime = list.getJSONObject(i);
                if (anime.isNull("nextAiringEpisode")) continue;

                JSONObject airing = anime.getJSONObject("nextAiringEpisode");
                int nextEpisode = airing.getInt("episode");
                long airingAt = airing.getLong("airingAt");
                int currentEpisode = nextEpisode - 1;

                String title = anime.getJSONObject("title").optString("userPreferred", "Unknown");
                String poster = anime.getJSONObject("coverImage").optString("large", "");
                String animeId = title.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();

                // Lấy các thông tin chi tiết
                String format = anime.optString("format", "TV");
                String season = anime.optString("season", "");
                int seasonYear = anime.optInt("seasonYear", 0);
                if (!season.isEmpty() && seasonYear > 0) season = season + " " + seasonYear;
                int duration = anime.optInt("duration", 0);
                int episodes = anime.optInt("episodes", 0);
                String description = anime.optString("description", "");
                JSONArray genresArr = anime.optJSONArray("genres");
                StringBuilder genresBuilder = new StringBuilder();
                if(genresArr != null) {
                    for(int j=0;j<genresArr.length();j++){
                        genresBuilder.append(genresArr.optString(j));
                        if(j < genresArr.length()-1) genresBuilder.append(", ");
                    }
                }
                String genres = genresBuilder.toString();

                JSONArray studiosNodes = anime.optJSONObject("studios").optJSONArray("nodes");
                String studio = (studiosNodes != null && studiosNodes.length() > 0)
                        ? studiosNodes.getJSONObject(0).optString("name", "Unknown")
                        : "Unknown";

                String director = "Unknown";
                JSONArray staffEdges = anime.optJSONObject("staff").optJSONArray("edges");
                if(staffEdges != null){
                    for(int j=0;j<staffEdges.length();j++){
                        JSONObject edge = staffEdges.getJSONObject(j);
                        String role = edge.optString("role","").toLowerCase();
                        JSONObject node = edge.optJSONObject("node");
                        if(node == null) continue;
                        String name = node.getJSONObject("name").optString("full","Unknown");
                        if(role.contains("director")){
                            director = name;
                            break;
                        }
                    }
                }

                String trailer = "";
                if(anime.has("trailer") && !anime.isNull("trailer")){
                    JSONObject t = anime.getJSONObject("trailer");
                    if("youtube".equalsIgnoreCase(t.optString("site",""))) trailer = t.optString("id","");
                }

                double rating = anime.optDouble("averageScore",0);
                long views = anime.optLong("popularity",0);
                String englishTitle = anime.getJSONObject("title").optString("english","");
                String romajiTitle = anime.getJSONObject("title").optString("romaji","");
                String nativeTitle = anime.getJSONObject("title").optString("native","");

                checkAndNotify(
                        animeId, title, currentEpisode, nextEpisode, airingAt, poster,
                        format, season, studio, director, duration, rating, views, description,
                        genres, englishTitle, romajiTitle, nativeTitle, trailer
                );
            }
        } catch (Exception e){ e.printStackTrace(); }
    }

    // ================= CHECK + NOTIFY =================
    private void checkAndNotify(String animeId,
                                String title,
                                int currentEpisode,
                                int nextEpisode,
                                long airingAt,
                                String poster,
                                String format,
                                String season,
                                String studio,
                                String director,
                                int duration,
                                double rating,
                                long views,
                                String description,
                                String genres,
                                String englishTitle,
                                String romajiTitle,
                                String nativeTitle,
                                String trailer) {

        // Lấy danh sách favorites từ SharedPreferences
        Set<String> favs = new HashSet<>(getApplicationContext()
                .getSharedPreferences("app", Context.MODE_PRIVATE)
                .getStringSet("favorites", new HashSet<>()));

        if (!favs.contains(animeId)) {
            Log.d("WORKER", "Anime " + animeId + " không còn trong favorite, bỏ qua notify");
            return; // không push notification nếu đã xóa favorite
        }

        // Tiếp tục check episode + gửi notification
        db.collection("episodes")
                .document(animeId)
                .get()
                .addOnSuccessListener(doc -> {
                    int lastEpisode = 0;
                    long lastAiringAt = 0;

                    if(doc.exists()){
                        if(doc.getLong("episode") != null) lastEpisode = doc.getLong("episode").intValue();
                        if(doc.getLong("airingAt") != null) lastAiringAt = doc.getLong("airingAt");
                    }

                    if(currentEpisode > lastEpisode){
                        sendNotification(
                                title, currentEpisode, poster, airingAt,
                                format, season, studio, director, duration, rating, views,
                                description, genres, englishTitle, romajiTitle, nativeTitle, trailer
                        );

                        db.collection("episodes").document(animeId)
                                .set(new EpisodeModel(currentEpisode, nextEpisode, airingAt));
                    }
                });
    }

    // ================= SEND NOTIFICATION =================
    private void sendNotification(String title,
                                  int episode,
                                  String poster,
                                  long airingAt,
                                  String format,
                                  String season,
                                  String studio,
                                  String director,
                                  int duration,
                                  double rating,
                                  long views,
                                  String description,
                                  String genres,
                                  String englishTitle,
                                  String romajiTitle,
                                  String nativeTitle,
                                  String trailer) {

        // 🔹 Intent mở NotificationActivity thay vì MainActivity
        Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String message = "Tập " + episode + " vừa ra 🔥";

        NotificationHelper.showNotification(
                getApplicationContext(),
                title,
                message,
                intent,
                poster != null ? poster : "",
                airingAt > 0 ? airingAt : System.currentTimeMillis() / 1000,
                episode,
                format != null ? format : "",
                season != null ? season : "",
                studio != null ? studio : "",
                director != null ? director : "",
                duration,
                rating,
                views,
                description != null ? description : "",
                genres != null ? genres : "",
                englishTitle != null ? englishTitle : "",
                romajiTitle != null ? romajiTitle : "",
                nativeTitle != null ? nativeTitle : "",
                trailer != null ? trailer : ""
        );
    }
    // ================= ENQUEUE =================
    public static void enqueueExpedited(Context context) {
        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(AnimeWorker.class)
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build();

        WorkManager.getInstance(context).enqueue(request);
    }
}