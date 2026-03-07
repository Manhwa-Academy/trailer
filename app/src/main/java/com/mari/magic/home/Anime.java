package com.mari.magic.home;

public class Anime {

    private int id;

    // ===== TITLE =====
    private String title;
    private String englishTitle;
    private String romajiTitle;
    private String nativeTitle;
    private String vietnameseTitle;

    // ===== MEDIA =====
    private String poster;
    private String trailer;
    private String videoUrl;

    // ===== INFO =====
    private double rating;
    private int year;
    private String description;
    private String genres;

    // ===== EXTRA INFO =====
    private String studio;
    private String director;
    private String season;
    private int duration;
    private String format;
    private long views;

    // ===== FLAGS =====
    private boolean favorite;
    private boolean isAdult;

    public Anime(){}

    public Anime(String title, String poster, String trailer, double rating){
        this.title = title;
        this.poster = poster;
        this.trailer = trailer;
        this.rating = rating;
    }

    // ================= GETTER =================

    public int getId() {
        return id;
    }

    public String getTitle() {

        if(title != null && !title.isEmpty()) return title;
        if(englishTitle != null && !englishTitle.isEmpty()) return englishTitle;
        if(romajiTitle != null && !romajiTitle.isEmpty()) return romajiTitle;
        if(nativeTitle != null && !nativeTitle.isEmpty()) return nativeTitle;

        return "Unknown";
    }

    public String getEnglishTitle() {
        return englishTitle != null ? englishTitle : "";
    }

    public String getRomajiTitle() {
        return romajiTitle != null ? romajiTitle : "";
    }

    public String getNativeTitle() {
        return nativeTitle != null ? nativeTitle : "";
    }

    public String getVietnameseTitle() {
        return vietnameseTitle != null ? vietnameseTitle : "";
    }

    public String getPoster() {
        return poster != null ? poster : "";
    }

    public String getTrailer() {
        return trailer != null ? trailer : "";
    }

    public String getVideoUrl() {
        return videoUrl != null ? videoUrl : "";
    }

    public double getRating() {
        return rating;
    }

    public int getYear() {
        return year;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public String getGenres() {
        return genres != null ? genres : "";
    }

    public String getStudio() {
        return studio != null ? studio : "Unknown";
    }

    public String getDirector() {
        return director != null ? director : "Unknown";
    }

    public String getSeason() {
        return season != null ? season : "Unknown";
    }

    public int getDuration() {
        return duration;
    }

    public String getFormat() {
        return format != null ? format : "Unknown";
    }

    public long getViews() {
        return views;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public boolean isAdult() {
        return isAdult;
    }

    // ================= SETTER =================

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setEnglishTitle(String englishTitle) {
        this.englishTitle = englishTitle;
    }

    public void setRomajiTitle(String romajiTitle) {
        this.romajiTitle = romajiTitle;
    }

    public void setNativeTitle(String nativeTitle) {
        this.nativeTitle = nativeTitle;
    }

    public void setVietnameseTitle(String vietnameseTitle) {
        this.vietnameseTitle = vietnameseTitle;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setRating(double rating) {

        // AniList trả averageScore 0–100
        if(rating > 10){
            this.rating = rating / 10.0;
        }else{
            this.rating = rating;
        }
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGenres(String genres) {
        this.genres = genres;
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

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void setAdult(boolean adult) {
        isAdult = adult;
    }

    @Override
    public String toString() {
        return "Anime{" +
                "id=" + id +
                ", title='" + getTitle() + '\'' +
                ", rating=" + rating +
                ", year=" + year +
                ", format='" + format + '\'' +
                ", studio='" + studio + '\'' +
                ", director='" + director + '\'' +
                '}';
    }
}