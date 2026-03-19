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
                                        int episode) {

        createChannel(context);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = (title + message + System.currentTimeMillis()).hashCode();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

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

            saveNotification(context, title, message, "", airingAt, episode);
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

                        saveNotification(context, title, message, imageUrl, airingAt, episode);
                        increaseBadge(context);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder));
                        manager.notify(notificationId, builder.build());

                        saveNotification(context, title, message, "", airingAt, episode);
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
                                        int episode) {

        SharedPreferences prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = prefs.getString("notification_list", "[]");

        Type type = new TypeToken<List<NotificationItem>>(){}.getType();
        List<NotificationItem> list = gson.fromJson(json, type);

        if (list == null) list = new ArrayList<>();

        list.add(0, new NotificationItem(title, message, imageUrl, airingAt, episode));

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
    // 🔹 Thêm anime vào favorite, KHÔNG thay đổi thời gian airing
    public static void addFavorite(Context context, String animeId) {
        SharedPreferences prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE);
        Set<String> favs = new HashSet<>(prefs.getStringSet("favorites", new HashSet<>()));
        if (!favs.contains(animeId)) {
            favs.add(animeId);
            prefs.edit().putStringSet("favorites", favs).apply();
        }
    }

    // 🔹 Xóa anime khỏi favorite, KHÔNG thay đổi thời gian airing
    public static void removeFavorite(Context context, String animeId) {
        SharedPreferences prefs = context.getSharedPreferences("app", Context.MODE_PRIVATE);
        Set<String> favs = new HashSet<>(prefs.getStringSet("favorites", new HashSet<>()));
        if (favs.contains(animeId)) {
            favs.remove(animeId);
            prefs.edit().putStringSet("favorites", favs).apply();
        }
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