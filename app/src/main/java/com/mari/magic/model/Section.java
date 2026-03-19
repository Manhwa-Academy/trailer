package com.mari.magic.model;

import java.util.List;

public class Section {

    // ===== LAYOUT TYPES =====
    public static final int TYPE_SEARCH = 0;
    public static final int TYPE_BANNER = 1;
    public static final int TYPE_SECTION = 2;

    // ===== SECTION CATEGORIES =====
    public static final String CAT_NEW = "new";              // ⭐ Anime mới

    public static final String CAT_MOVIES = "movies";
    public static final String CAT_TOP_MOVIES = "top_movies";

    public static final String CAT_SERIES = "series";
    public static final String CAT_TOP_TV = "top_tv";

    public static final String CAT_RELEASING = "releasing";
    public static final String CAT_UPCOMING = "upcoming";

    public static final String CAT_ECCHI = "ecchi";
    public static final String CAT_ADULT = "adult";

    // ===== MANGA =====
    public static final String CAT_TOP_MANGA = "top_manga";
    public static final String CAT_TRENDING_MANGA = "trending_manga";
    public static final String CAT_NEW_MANGA = "new_manga"; // ⭐ Manga mới
    // ===== NOVEL =====
    public static final String CAT_TOP_NOVEL = "top_novel";
    public static final String CAT_TRENDING_NOVEL = "trending_novel";
    public static final String CAT_NEW_NOVEL = "new_novel";
    private int type;
    private String title;
    private String category;
    private List<Anime> animeList;

    // ===== Constructor cho Banner / Search =====
    public Section(int type){
        this.type = type;
    }

    // ===== Constructor cho Anime Section =====
    public Section(String category, String title, List<Anime> list){
        this.type = TYPE_SECTION;
        this.category = category;
        this.title = title;
        this.animeList = list;
    }

    // ===== Getters =====
    public int getType(){
        return type;
    }

    public String getTitle(){
        return title;
    }

    public String getCategory(){
        return category;
    }

    public List<Anime> getAnimeList(){
        return animeList;
    }
}