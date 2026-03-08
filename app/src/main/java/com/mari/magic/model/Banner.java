package com.mari.magic.model;

public class Banner {

    private String imageUrl;
    private String title;
    private String trailerUrl;

    public Banner() {
        // constructor rỗng (Firebase / API cần)
    }

    public Banner(String imageUrl, String title, String trailerUrl) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.trailerUrl = trailerUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }
}