package com.mari.magic.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class FirebaseAnimeRepository {

    private FirebaseFirestore db;
    private String userId;

    public FirebaseAnimeRepository() {
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // FAVORITE
    public void addFavorite(String animeId, String title, String cover) {

        Map<String, Object> data = new HashMap<>();
        data.put("animeId", animeId);
        data.put("title", title);
        data.put("cover", cover);
        data.put("time", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(animeId)
                .set(data, SetOptions.merge());
    }

    public void removeFavorite(String animeId) {
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(animeId)
                .delete();
    }

    // HISTORY
    public void addHistory(String animeId, String title, String cover) {

        Map<String, Object> data = new HashMap<>();
        data.put("animeId", animeId);
        data.put("title", title);
        data.put("cover", cover);
        data.put("time", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .collection("history")
                .document(animeId)
                .set(data, SetOptions.merge());
    }
}