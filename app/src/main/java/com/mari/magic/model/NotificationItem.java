package com.mari.magic.model;

public class NotificationItem {

    private String title;
    private String message;
    private String imageUrl;
    private long nextAiringAt;
    private int episode;
    // 🔥 constructor đầy đủ
    public NotificationItem(String title, String message, String imageUrl, long nextAiringAt, int episode){
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.nextAiringAt = nextAiringAt;
        this.episode = episode;
    }

    // 🔥 constructor cũ (fallback)
    public NotificationItem(String title, String message, String imageUrl){
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
    }

    // 🔥 bắt buộc cho Gson
    public NotificationItem(){}

    // ===== GETTER =====
    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getNextAiringAt() {
        return nextAiringAt;
    }
    public int getEpisode() {
        return episode;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }
    // ===== SETTER =====
    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setNextAiringAt(long nextAiringAt) {
        this.nextAiringAt = nextAiringAt;
    }
}