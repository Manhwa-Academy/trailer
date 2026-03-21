package com.mari.magic.model;

public class NotificationItem {

    private String title;
    private String message;
    private String imageUrl;
    private long nextAiringAt;
    private int episode;

    // 🔹 Thêm các trường chi tiết
    private String format;
    private String season;
    private String studio;
    private String director;
    private int duration;
    private double rating;
    private long views;
    private String description;
    private String genres;
    private String englishTitle;
    private String romajiTitle;
    private String nativeTitle;
    private String trailer;

    // ================= CONSTRUCTORS =================

    public NotificationItem(String title, String message, String imageUrl, long nextAiringAt, int episode){
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.nextAiringAt = nextAiringAt;
        this.episode = episode;
    }

    // ✅ Constructor đầy đủ
    public NotificationItem(String title, String message, String imageUrl, long nextAiringAt, int episode,
                            String format, String season, String studio, String director, int duration,
                            double rating, long views, String description, String genres,
                            String englishTitle, String romajiTitle, String nativeTitle, String trailer) {
        this.title = title;
        this.message = message;
        this.imageUrl = imageUrl;
        this.nextAiringAt = nextAiringAt;
        this.episode = episode;
        this.format = format;
        this.season = season;
        this.studio = studio;
        this.director = director;
        this.duration = duration;
        this.rating = rating;
        this.views = views;
        this.description = description;
        this.genres = genres;
        this.englishTitle = englishTitle;
        this.romajiTitle = romajiTitle;
        this.nativeTitle = nativeTitle;
        this.trailer = trailer;
    }

    // 🔹 Gson cần
    public NotificationItem(){}

    // ================= GETTER =================
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getImageUrl() { return imageUrl; }
    public long getNextAiringAt() { return nextAiringAt; }
    public int getEpisode() { return episode; }

    public String getFormat() { return format; }
    public String getSeason() { return season; }
    public String getStudio() { return studio; }
    public String getDirector() { return director; }
    public int getDuration() { return duration; }
    public double getRating() { return rating; }
    public long getViews() { return views; }
    public String getDescription() { return description; }
    public String getGenres() { return genres; }
    public String getEnglishTitle() { return englishTitle; }
    public String getRomajiTitle() { return romajiTitle; }
    public String getNativeTitle() { return nativeTitle; }
    public String getTrailer() { return trailer; }

    // ================= SETTER =================
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setNextAiringAt(long nextAiringAt) { this.nextAiringAt = nextAiringAt; }
    public void setEpisode(int episode) { this.episode = episode; }

    public void setFormat(String format) { this.format = format; }
    public void setSeason(String season) { this.season = season; }
    public void setStudio(String studio) { this.studio = studio; }
    public void setDirector(String director) { this.director = director; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setRating(double rating) { this.rating = rating; }
    public void setViews(long views) { this.views = views; }
    public void setDescription(String description) { this.description = description; }
    public void setGenres(String genres) { this.genres = genres; }
    public void setEnglishTitle(String englishTitle) { this.englishTitle = englishTitle; }
    public void setRomajiTitle(String romajiTitle) { this.romajiTitle = romajiTitle; }
    public void setNativeTitle(String nativeTitle) { this.nativeTitle = nativeTitle; }
    public void setTrailer(String trailer) { this.trailer = trailer; }
}