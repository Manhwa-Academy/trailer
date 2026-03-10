package com.mari.magic.model;

public class Banner {

    private String id;
    private String romajiTitle;
    private String nativeTitle;

    private String imageUrl;
    private String title;
    private String trailerUrl;

    private String studio;
    private String director;
    private String season;
    private String format;
    private String status;
    private String genres;

    private int duration;
    private int episodes;

    private long updatedAt;

    private double rating;
    private String description;

    private int nextEpisode;
    private long nextAiringAt;   // ⭐ đổi tên ở đây

    public Banner() {
    }

    public Banner(
            String id,
            String imageUrl,
            String title,
            String trailerUrl,
            String studio,
            String director,
            String season,
            int duration,
            String format,
            int episodes,
            String status,
            long updatedAt
    ) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.title = title;
        this.trailerUrl = trailerUrl;
        this.studio = studio;
        this.director = director;
        this.season = season;
        this.duration = duration;
        this.format = format;
        this.episodes = episodes;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getRomajiTitle() {
        return romajiTitle;
    }

    public String getNativeTitle() {
        return nativeTitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public String getStudio() {
        return studio;
    }

    public String getDirector() {
        return director;
    }

    public String getSeason() {
        return season;
    }

    public String getFormat() {
        return format;
    }

    public String getStatus() {
        return status;
    }

    public String getGenres() {
        return genres;
    }

    public int getDuration() {
        return duration;
    }

    public int getEpisodes() {
        return episodes;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public double getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public int getNextEpisode() {
        return nextEpisode;
    }

    public long getNextAiringAt() {   // ⭐ getter mới
        return nextAiringAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRomajiTitle(String romajiTitle) {
        this.romajiTitle = romajiTitle;
    }

    public void setNativeTitle(String nativeTitle) {
        this.nativeTitle = nativeTitle;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public void setStudio(String studio) {
        this.studio = studio;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setEpisodes(int episodes) {
        this.episodes = episodes;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNextEpisode(int nextEpisode) {
        this.nextEpisode = nextEpisode;
    }

    public void setNextAiringAt(long nextAiringAt) {   // ⭐ setter mới
        this.nextAiringAt = nextAiringAt;
    }
}