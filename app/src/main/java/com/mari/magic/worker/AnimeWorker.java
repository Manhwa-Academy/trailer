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
import com.mari.magic.utils.FavoriteManager;
import com.mari.magic.utils.NotificationHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AnimeWorker extends Worker {

    private final FirebaseFirestore db;
    private boolean hasError = false;

    private static final boolean DEBUG_FORCE_NOTIFY = false;

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

        String query = "{ Page(page:1, perPage:25){ media(type:ANIME,status:RELEASING,sort:UPDATED_AT_DESC){ title{romaji} nextAiringEpisode{episode airingAt} coverImage{large} } } }";

        JSONObject body = new JSONObject();
        try {
            body.put("query", query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    handleResponse(response);
                    latch.countDown();
                },
                error -> {
                    Log.e("WORKER", "API error: " + error);
                    hasError = true;
                    latch.countDown();
                }
        );

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    // ================= HANDLE RESPONSE =================
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
                long airingAt = airing.getLong("airingAt"); // thời gian tập mới thực sự phát

                int currentEpisode = nextEpisode - 1;

                // SỬA: dùng userPreferred để hiển thị đúng tên anime
                String title = anime.getJSONObject("title")
                        .optString("userPreferred", "Unknown");

                String poster = anime.getJSONObject("coverImage")
                        .optString("large", "");

                // Dùng title làm id cũng ổn, nhưng nên chuẩn hóa
                String animeId = title.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();

                checkAndNotify(animeId, title, currentEpisode, nextEpisode, airingAt, poster);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CHECK + NOTIFY =================
    private void checkAndNotify(String animeId,
                                String title,
                                int currentEpisode,
                                int nextEpisode,
                                long airingAt,
                                String poster) {

        Log.d("WORKER_DEBUG", "Check animeId=" + animeId
                + " | title=" + title
                + " | currentEpisode=" + currentEpisode
                + " | nextEpisode=" + nextEpisode
                + " | airingAt=" + airingAt);

        db.collection("episodes")
                .document(animeId)
                .get()
                .addOnSuccessListener(doc -> {
                    int lastEpisode = 0;
                    long lastAiringAt = 0;

                    if (doc.exists()) {
                        if(doc.getLong("episode") != null) lastEpisode = doc.getLong("episode").intValue();
                        if(doc.getLong("airingAt") != null) lastAiringAt = doc.getLong("airingAt");
                    }

                    Log.d("WORKER_DEBUG", "Firestore lastEpisode=" + lastEpisode
                            + " | lastAiringAt=" + lastAiringAt);

                    // chỉ gửi notification khi tập mới > tập lưu trong DB
                    if(currentEpisode > lastEpisode){
                        Log.d("WORKER_DEBUG", "Send notification: EP " + currentEpisode
                                + " | airingAt=" + airingAt);

                        // gửi notification với thời gian airing thật
                        sendNotification(title, currentEpisode, poster, airingAt);

                        // lưu episode + nextEpisode + airingAt chuẩn từ API
                        EpisodeModel model = new EpisodeModel(currentEpisode, nextEpisode, airingAt);
                        db.collection("episodes")
                                .document(animeId)
                                .set(model);

                        // update favorite chỉ lưu trạng thái, KHÔNG thay đổi airingAt
                        updateFavorite(animeId, currentEpisode, nextEpisode, airingAt);
                    } else {
                        Log.d("WORKER_DEBUG", "No new episode, skip notification");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("WORKER_DEBUG", "Fetch episode fail: " + e.getMessage())
                );
    }
    // ================= SEND NOTIFICATION =================
    private void sendNotification(String title,
                                  int episode,
                                  String poster,
                                  long airingAt) {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        String message = "Tập " + episode + " vừa ra 🔥";

        // 🔥 FIX QUAN TRỌNG: truyền episode
        NotificationHelper.showNotification(
                getApplicationContext(),
                title,
                message,
                intent,
                poster,
                airingAt,
                episode
        );
    }

    // ================= UPDATE FAVORITE =================
    private void updateFavorite(String animeId,
                                int currentEpisode,
                                int nextEpisode,
                                long airingAt) {

        long updatedAt = System.currentTimeMillis() / 1000;

        try {
            FavoriteManager.updateFavoriteEpisode(
                    animeId,
                    currentEpisode,
                    nextEpisode,
                    airingAt,
                    "RELEASING",
                    updatedAt
            );
        } catch (Exception e) {
            Log.e("WORKER", "Local update fail: " + e.getMessage());
        }

        db.collectionGroup("favorites")
                .whereEqualTo("animeId", animeId)
                .get()
                .addOnSuccessListener(qs -> {
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        doc.getReference().update(
                                "episodes", currentEpisode,
                                "nextEpisode", nextEpisode,
                                "nextAiringAt", airingAt,
                                "updatedAt", updatedAt
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Log.e("WORKER", "Update favorites failed: " + e.getMessage())
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