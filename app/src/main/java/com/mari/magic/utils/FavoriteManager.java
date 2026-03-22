package com.mari.magic.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mari.magic.R;
import com.mari.magic.model.Anime;

import java.util.HashMap;
import java.util.Map;

public class FavoriteManager {

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // ================= CALLBACK INTERFACE =================
    public interface FavoriteCallback {
        void onResult(boolean isFavorite);
    }

    // ================= CHECK FAVORITE =================
    public static void checkFavorite(String animeId, @NonNull ImageView btnFavorite, @NonNull TextView txtFavorite, FavoriteCallback callback) {

        if (auth.getCurrentUser() == null) {
            txtFavorite.setText("Yêu thích");
            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            if(callback != null) callback.onResult(false);
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(animeId)
                .get()
                .addOnSuccessListener(doc -> {
                    boolean isFav = doc.exists();
                    btnFavorite.setImageResource(isFav ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
                    txtFavorite.setText(isFav ? "Đã thích" : "Yêu thích");
                    if(callback != null) callback.onResult(isFav);
                })
                .addOnFailureListener(e -> {
                    btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                    txtFavorite.setText("Yêu thích");
                    if(callback != null) callback.onResult(false);
                });
    }

    // ================= TOGGLE FAVORITE =================
    public static void toggleFavorite(Context context,
                                      Anime anime,
                                      @NonNull ImageView btnFavorite,
                                      @NonNull TextView txtFavorite,
                                      String animeId,
                                      FavoriteCallback callback) {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show();
            if(callback != null) callback.onResult(false);
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        DocumentReference ref = db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(animeId);

        ref.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // ❌ REMOVE
                ref.delete()
                        .addOnSuccessListener(aVoid -> {
                            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                            txtFavorite.setText("Yêu thích");
                            if(callback != null) callback.onResult(false);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Lỗi xóa favorite", Toast.LENGTH_SHORT).show();
                            if(callback != null) callback.onResult(true);
                        });

            } else {
                // ✅ ADD
                Map<String, Object> data = buildAnimeData(anime, animeId);
                ref.set(data)
                        .addOnSuccessListener(aVoid -> {
                            btnFavorite.setImageResource(R.drawable.ic_favorite);
                            txtFavorite.setText("Đã thích");
                            if(callback != null) callback.onResult(true);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Lỗi thêm favorite", Toast.LENGTH_SHORT).show();
                            if(callback != null) callback.onResult(false);
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Lỗi kiểm tra favorite", Toast.LENGTH_SHORT).show();
            if(callback != null) callback.onResult(false);
        });
    }

    // ================= BUILD DATA =================
    public static Map<String, Object> buildAnimeData(Anime anime, String animeId) {

        Map<String, Object> data = new HashMap<>();
        data.put("animeId", animeId);
        data.put("title", anime.getTitle());
        data.put("englishTitle", anime.getEnglishTitle());
        data.put("romajiTitle", anime.getRomajiTitle());
        data.put("nativeTitle", anime.getNativeTitle());
        data.put("poster", anime.getPoster());
        data.put("rating", anime.getRating());
        data.put("trailer", anime.getTrailer());
        data.put("description", anime.getDescription());
        data.put("genres", anime.getGenres());
        data.put("studio", anime.getStudio());
        data.put("director", anime.getDirector());
        data.put("season", anime.getSeason());
        data.put("format", anime.getFormat());
        data.put("duration", anime.getDuration());
        data.put("episodes", anime.getEpisodes() > 0 ? anime.getEpisodes() : 0);
        data.put("status", anime.getStatus() != null ? anime.getStatus() : "");
        data.put("nextEpisode", anime.getNextEpisode());
        data.put("nextAiringAt", anime.getNextAiringAt());
        data.put("isAdult", anime.isAdult());
        data.put("views", anime.getViews());
        data.put("updatedAt", anime.getUpdatedAt() > 0 ? anime.getUpdatedAt() : 0);
        data.put("time", System.currentTimeMillis()); // dùng để sort trong FavoriteFragment
        return data;
    }

    // ================= UPDATE EPISODE =================
    public static void updateFavoriteEpisode(String animeId,
                                             int episodes,
                                             int nextEpisode,
                                             long nextAiringAt,
                                             String status,
                                             long updatedAt) {
        if(auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("episodes", episodes);
        updates.put("nextEpisode", nextEpisode);
        updates.put("nextAiringAt", nextAiringAt);
        updates.put("status", status);
        updates.put("updatedAt", updatedAt);
        db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(animeId)
                .update(updates);
    }
// ================= UPDATE EPISODES BASED ON NEXT EPISODE =================
    /**
     * Cập nhật episodes trong Firestore dựa vào nextEpisode.
     * Khi tập mới phát, episodes sẽ = nextEpisode - 1.
     */
    public static void updateEpisodesFromNext(String animeId, int nextEpisode) {
        if (auth.getCurrentUser() == null || nextEpisode <= 0) return;

        String uid = auth.getCurrentUser().getUid();
        DocumentReference ref = db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(animeId);

        // Luôn update episodes = nextEpisode - 1 và nextEpisode
        Map<String, Object> updates = new HashMap<>();
        updates.put("episodes", nextEpisode - 1);
        updates.put("nextEpisode", nextEpisode);

        ref.update(updates)
                .addOnSuccessListener(aVoid ->
                        android.util.Log.d("FAV_DEBUG",
                                "Updated episodes for animeId=" + animeId +
                                        " to " + (nextEpisode - 1) +
                                        " | nextEpisode=" + nextEpisode)
                )
                .addOnFailureListener(e ->
                        android.util.Log.e("FAV_DEBUG",
                                "Failed to update episodes: " + e.getMessage())
                );
    }
    // ================= REMOVE FAVORITE =================
    public static void removeFavorite(String animeId, @NonNull Context context) {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(animeId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // ✅ Xóa notification liên quan
                    NotificationHelper.removeNotificationForAnime(context, animeId);

                    // 🔹 Optionally, show Toast
                    Toast.makeText(context, "Đã xóa khỏi Yêu thích", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("FAV_DEBUG", "Failed to remove favorite: " + e.getMessage());
                    Toast.makeText(context, "Xóa favorite thất bại", Toast.LENGTH_SHORT).show();
                });
    }
}