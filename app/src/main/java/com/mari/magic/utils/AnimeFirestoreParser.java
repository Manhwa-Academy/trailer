package com.mari.magic.utils;

import com.mari.magic.model.Anime;

import java.util.Map;

public class AnimeFirestoreParser {

    public static Anime parse(Map<String,Object> map){

        Anime anime = new Anime();

        if(map == null) return anime;

        // ===== BASIC =====
        anime.setTitle((String) map.getOrDefault("title",""));
        anime.setPoster((String) map.getOrDefault("poster",""));
        anime.setTrailer((String) map.getOrDefault("trailer",""));

        anime.setRomajiTitle((String) map.getOrDefault("romajiTitle",""));
        anime.setEnglishTitle((String) map.getOrDefault("englishTitle",""));
        anime.setNativeTitle((String) map.getOrDefault("nativeTitle",""));

        anime.setDescription((String) map.getOrDefault("description",""));
        anime.setGenres((String) map.getOrDefault("genres",""));

        anime.setStudio((String) map.getOrDefault("studio",""));
        anime.setDirector((String) map.getOrDefault("director",""));
        anime.setSeason((String) map.getOrDefault("season",""));
        anime.setFormat((String) map.getOrDefault("format",""));

        anime.setStatus((String) map.getOrDefault("status",""));

        // ===== NUMBER SAFE =====

        if(map.get("rating") instanceof Number)
            anime.setRating(((Number) map.get("rating")).doubleValue());

        if(map.get("duration") instanceof Number)
            anime.setDuration(((Number) map.get("duration")).intValue());

        if(map.get("views") instanceof Number)
            anime.setViews(((Number) map.get("views")).longValue());

        if(map.get("updatedAt") instanceof Number)
            anime.setUpdatedAt(((Number) map.get("updatedAt")).longValue());

        if(map.get("episodes") instanceof Number)
            anime.setEpisodes(((Number) map.get("episodes")).intValue());

        if(map.get("nextEpisode") instanceof Number)
            anime.setNextEpisode(((Number) map.get("nextEpisode")).intValue());

        if(map.get("nextAiringAt") instanceof Number)
            anime.setNextAiringAt(((Number) map.get("nextAiringAt")).longValue());

        // ===== 🔥 ANILIST ID (QUAN TRỌNG NHẤT) =====
        if(map.get("anilistId") instanceof Number){
            anime.setAnilistId(((Number) map.get("anilistId")).intValue());
        }

        // ===== LEGACY ID =====
        Object id = map.get("animeId");

        if(id instanceof Number){
            anime.setId(((Number) id).intValue());
        }
        else if(id instanceof String){
            try{
                anime.setId(Integer.parseInt((String) id));
            }catch(Exception ignored){}
        }

        // ===== ADULT =====
        if(map.get("isAdult") instanceof Boolean)
            anime.setAdult((Boolean) map.get("isAdult"));

        return anime;
    }
}