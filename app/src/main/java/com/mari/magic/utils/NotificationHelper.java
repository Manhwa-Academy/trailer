package com.mari.magic.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mari.magic.R;
import com.mari.magic.model.NotificationItem;
import com.mari.magic.ui.notification.NotificationActivity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationHelper {

    private static final String CHANNEL_ID = "anime_channel";
    private static final String GROUP_KEY = "anime_group";

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Anime Updates",
                        NotificationManager.IMPORTANCE_HIGH
                );
                manager.createNotificationChannel(channel);
            }
        }
    }

    // ================= SHOW NOTIFICATION =================
    public static void showNotification(Context context,
                                        String title,
                                        String message,
                                        Intent intent,
                                        @Nullable String imageUrl,
                                        long airingAt,
                                        int episode,
                                        // 🔹 Thêm thông tin chi tiết
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

        createChannel(context);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Log.d("NOTI_DEBUG", "Creating notification: title=" + title + ", episode=" + episode);
        Log.d("NOTI_DEBUG", "Intent class: " + intent.getComponent());
        // 🔹 đây là chỗ dùng nanoTime để tạo notificationId duy nhất
        int notificationId = (title + message + System.nanoTime()).hashCode();

        // 🔹 tạo PendingIntent trỏ NotificationActivity
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        Log.d("NOTI_DEBUG", "PendingIntent created"); // chỉ log là PendingIntent đã tạo xong
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setGroup(GROUP_KEY);

        // ===== Không có ảnh =====
        if (imageUrl == null || imageUrl.isEmpty()) {
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder));
            manager.notify(notificationId, builder.build());

            saveNotification(context, title, message, "", airingAt, episode,
                    format, season, studio, director, duration, rating, views,
                    description, genres, englishTitle, romajiTitle, nativeTitle, trailer);

            increaseBadge(context);
            return;
        }

        // ===== Load ảnh =====
        Glide.with(context.getApplicationContext())
                .asBitmap()
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(new CustomTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap,
                                                @Nullable Transition<? super Bitmap> transition) {

                        builder.setLargeIcon(bitmap)
                                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));

                        manager.notify(notificationId, builder.build());

                        saveNotification(context, title, message, imageUrl, airingAt, episode,
                                format, season, studio, director, duration, rating, views,
                                description, genres, englishTitle, romajiTitle, nativeTitle, trailer);

                        increaseBadge(context);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder));
                        manager.notify(notificationId, builder.build());

                        saveNotification(context, title, message, "", airingAt, episode,
                                format, season, studio, director, duration, rating, views,
                                description, genres, englishTitle, romajiTitle, nativeTitle, trailer);

                        increaseBadge(context);
                    }
                });
    }

    // ================= SAVE =================
    public static void saveNotification(Context context,
                                        String title,
                                        String message,
                                        String imageUrl,
                                        long airingAt,
                                        int episode,
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

        SharedPreferences prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = prefs.getString("notification_list", "[]");

        Type type = new TypeToken<List<NotificationItem>>(){}.getType();
        List<NotificationItem> list = gson.fromJson(json, type);

        if (list == null) list = new ArrayList<>();

        // 🔹 Kiểm tra xem anime đã có trong danh sách notification chưa
        String animeId = title.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        boolean exists = false;
        for (NotificationItem n : list) {
            String nId = n.getTitle().replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
            if (nId.equals(animeId)) {
                exists = true;
                break;
            }
        }

        // 🔹 Chỉ thêm nếu chưa tồn tại
        if (!exists) {
            list.add(0, new NotificationItem(
                    title,
                    message,
                    imageUrl,
                    airingAt,
                    episode,
                    format,
                    season,
                    studio,
                    director,
                    duration,
                    rating,
                    views,
                    description,
                    genres,
                    englishTitle,
                    romajiTitle,
                    nativeTitle,
                    trailer
            ));
        }

        prefs.edit()
                .putString("notification_list", gson.toJson(list))
                .apply();
    }
    public static List<NotificationItem> getNotifications(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = prefs.getString("notification_list", "[]");

        Type type = new TypeToken<List<NotificationItem>>(){}.getType();
        List<NotificationItem> list = gson.fromJson(json, type);

        return list != null ? list : new ArrayList<>();
    }

    // ================= BADGE =================
    public static void resetBadge(Context context) {
        context.getSharedPreferences("app", Context.MODE_PRIVATE)
                .edit().putInt("noti_count", 0).apply();
    }

    public static int getBadge(Context context) {
        return context.getSharedPreferences("app", Context.MODE_PRIVATE)
                .getInt("noti_count", 0);
    }

    public static void increaseBadge(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE);
        int count = prefs.getInt("noti_count", 0);
        prefs.edit().putInt("noti_count", count + 1).apply();
    }

    // ================= FAVORITES =================
    public static void addFavorite(Context context, String animeId) {
        SharedPreferences prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE);
        Set<String> favs = new HashSet<>(prefs.getStringSet("favorites", new HashSet<>()));
        if (!favs.contains(animeId)) {
            favs.add(animeId);
            prefs.edit().putStringSet("favorites", favs).apply();
        }
    }

    public static void removeFavorite(Context context, String animeId) {
        SharedPreferences prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE);
        Set<String> favs = new HashSet<>(prefs.getStringSet("favorites", new HashSet<>()));
        if (favs.contains(animeId)) {
            favs.remove(animeId);
            prefs.edit().putStringSet("favorites", favs).apply();
        }

        // 🔹 Xóa luôn notification liên quan
        removeNotificationForAnime(context, animeId);

    }

    // 🔹 Xóa notification theo animeId hoặc animeTitle
    public static void removeNotificationForAnime(Context context, String animeId){
        List<NotificationItem> list = getNotifications(context);
        if(list == null || list.isEmpty()) return;

        list.removeIf(n -> {
            String nId = n.getTitle().replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
            return nId.equals(animeId);
        });

        Gson gson = new Gson();
        context.getSharedPreferences("app", Context.MODE_PRIVATE)
                .edit()
                .putString("notification_list", gson.toJson(list))
                .apply();
    }
    public static void isFavorite(Context context, String animeId, FavoriteCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onResult(false);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(user.getUid())
                .collection("favorites")
                .document(animeId)
                .get()
                .addOnSuccessListener(doc -> callback.onResult(doc.exists()))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    public interface FavoriteCallback {
        void onResult(boolean isFav);
    }
}