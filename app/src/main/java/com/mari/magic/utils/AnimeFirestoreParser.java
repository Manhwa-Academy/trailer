package com.mari.magic.utils;

import com.mari.magic.model.Anime;

import java.util.Map;

public class AnimeFirestoreParser {

    public static Anime parse(Map<String,Object> map){

        Anime anime = new Anime();

        if(map == null) return anime;

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

        // ===== NUMBER SAFE PARSE =====

        if(map.get("rating") instanceof Number)
            anime.setRating(((Number) map.get("rating")).doubleValue());

        if(map.get("duration") instanceof Number)
            anime.setDuration(((Number) map.get("duration")).intValue());

        if(map.get("views") instanceof Number)
            anime.setViews(((Number) map.get("views")).longValue());

        // ===== ANIME ID SAFE =====

        Object id = map.get("animeId");

        if(id instanceof Number){
            anime.setId(((Number) id).intValue());
        }
        else if(id instanceof String){
            try{
                anime.setId(Integer.parseInt((String) id));
            }catch(Exception ignored){}
        }

        // ===== ADULT FLAG =====

        if(map.get("isAdult") instanceof Boolean)
            anime.setAdult((Boolean) map.get("isAdult"));

        return anime;
    }
}