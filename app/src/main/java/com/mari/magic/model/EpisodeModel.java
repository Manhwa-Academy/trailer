package com.mari.magic.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class EpisodeModel {

    private int episode;       // Tập hiện tại
    private int nextEpisode;   // Tập tiếp theo
    private long airingAt;     // Thời gian phát tiếp theo (epoch)

    @ServerTimestamp
    private Date updatedAt;    // Firestore tự set khi update

    // 🔥 Constructor rỗng bắt buộc cho Firebase
    public EpisodeModel() {}

    // Constructor đầy đủ
    public EpisodeModel(int episode, int nextEpisode, long airingAt) {
        this.episode = episode;
        this.nextEpisode = nextEpisode;
        this.airingAt = airingAt;
    }

    // ---------- GETTERS ----------
    public int getEpisode() {
        return episode;
    }

    public int getNextEpisode() {
        return nextEpisode;
    }

    public long getAiringAt() {
        return airingAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    // ---------- SETTERS ----------
    public void setEpisode(int episode) {
        this.episode = episode;
    }

    public void setNextEpisode(int nextEpisode) {
        this.nextEpisode = nextEpisode;
    }

    public void setAiringAt(long airingAt) {
        this.airingAt = airingAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}