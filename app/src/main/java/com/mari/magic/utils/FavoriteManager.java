package com.mari.magic.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    // ================= CHECK FAVORITE =================
    public static void checkFavorite(String animeId, ImageView btnFavorite, TextView txtFavorite) {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(animeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        btnFavorite.setImageResource(R.drawable.ic_favorite);
                        txtFavorite.setText("Xóa khỏi danh sách yêu thích");
                    } else {
                        btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                        txtFavorite.setText("Lưu vào danh sách yêu thích");
                    }
                });
    }

    // ================= TOGGLE FAVORITE =================
    public static void toggleFavorite(Context context, Anime anime, ImageView btnFavorite, TextView txtFavorite, String animeId) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        DocumentReference ref = db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(animeId);

        ref.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                // ❌ REMOVE FAVORITE
                ref.delete();
                btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                txtFavorite.setText("Lưu vào danh sách yêu thích");
            } else {
                // ✅ SAVE FAVORITE
                // ✅ SAVE FAVORITE
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

// 🔹 Quan trọng: kiểm tra episodes > 0
                data.put("episodes", anime.getEpisodes() > 0 ? anime.getEpisodes() : 0);

// 🔹 Status không null
                data.put("status", anime.getStatus() != null ? anime.getStatus() : "");

// Các trường còn lại
                data.put("nextEpisode", anime.getNextEpisode());
                data.put("nextAiringAt", anime.getNextAiringAt());
                data.put("isAdult", anime.isAdult());
                data.put("views", anime.getViews());
                data.put("updatedAt", anime.getUpdatedAt() > 0 ? anime.getUpdatedAt() : 0);
                data.put("time", System.currentTimeMillis());

                ref.set(data);
                btnFavorite.setImageResource(R.drawable.ic_favorite);
                txtFavorite.setText("Xóa khỏi danh sách yêu thích");
            }
        });
    }

    // ================= REMOVE FAVORITE =================
    public static void removeFavorite(String animeId) {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(animeId)
                .delete();
    }
}