package com.mari.magic.utils;

import android.content.Context;

import com.mari.magic.model.Anime;

import org.json.JSONArray;
import org.json.JSONObject;

public class AnimeParser {

    public static Anime parse(JSONObject obj, Context ctx){

        try{

            boolean isAdult = obj.optBoolean("isAdult", false);
            JSONArray genresArray = obj.optJSONArray("genres");

            String filter = AppSettings.getContentFilter(ctx);

            // ===== FILTER LOGIC =====

            if(filter.equals("all")){
                if(isAdult) return null;
            }
            else if(filter.equals("16")){
                if(isAdult) return null;
            }

            // ===== CREATE OBJECT =====

            Anime anime = new Anime();

            anime.setId(obj.optInt("id"));
            anime.setAdult(isAdult);

            // ===== TITLE =====

            JSONObject titleObj = obj.optJSONObject("title");

            if(titleObj != null){

                String romaji = TextUtils.clean(titleObj.optString("romaji"));
                String english = TextUtils.clean(titleObj.optString("english"));
                String nativeTitle = TextUtils.clean(titleObj.optString("native"));

                anime.setRomajiTitle(romaji);
                anime.setEnglishTitle(english);
                anime.setNativeTitle(nativeTitle);

                anime.setTitle(
                        TextUtils.bestTitle(english, romaji, nativeTitle)
                );
            }

            // ===== POSTER =====

            JSONObject cover = obj.optJSONObject("coverImage");

            if(cover != null)
                anime.setPoster(cover.optString("large",""));

            // ===== RATING =====

            anime.setRating(obj.optDouble("averageScore",0));

            // ===== POPULARITY / VIEWS =====

            anime.setViews(obj.optLong("popularity",0));

            // ===== DESCRIPTION =====

            String desc = obj.optString("description","");
            desc = desc.replaceAll("<[^>]*>","");
            anime.setDescription(desc);

            // ===== GENRES =====

            StringBuilder genres = new StringBuilder();

            if(genresArray != null){

                for(int i=0;i<genresArray.length();i++){

                    genres.append(genresArray.optString(i));

                    if(i < genresArray.length()-1)
                        genres.append(", ");
                }
            }

            anime.setGenres(genres.toString());

            // ===== FORMAT =====

            anime.setFormat(obj.optString("format","Unknown"));

            // ===== YEAR =====

            anime.setYear(obj.optInt("seasonYear",0));

            // ===== SEASON =====

            String season = obj.optString("season","");
            int year = obj.optInt("seasonYear",0);

            if(!season.isEmpty())
                season = season + " " + year;

            anime.setSeason(season);

            // ===== DURATION =====

            anime.setDuration(obj.optInt("duration",0));
            anime.setStatus(obj.optString("status",""));
            // ===== EPISODES =====
            anime.setUpdatedAt(obj.optLong("updatedAt",0));
            // ===== EPISODES =====

            int episodes = obj.optInt("episodes",0);

            JSONObject next = obj.optJSONObject("nextAiringEpisode");

            if(next != null){

                int nextEpisode = next.optInt("episode",0);
                long airingAt = next.optLong("airingAt",0);

                anime.setNextEpisode(nextEpisode);
                anime.setNextAiringAt(airingAt);

                // đảm bảo episodes luôn đúng
                if(nextEpisode > 1){

                    int airedEpisodes = nextEpisode - 1;

                    if(episodes == 0 || episodes < airedEpisodes){
                        episodes = airedEpisodes;
                    }

                }
            }

            anime.setEpisodes(episodes);

            // ===== TRAILER =====

            if(obj.has("trailer") && !obj.isNull("trailer")){

                JSONObject trailer = obj.getJSONObject("trailer");

                if(trailer.optString("site").equalsIgnoreCase("youtube")){
                    anime.setTrailer(trailer.optString("id"));
                }
            }

            // ===== STUDIO =====

            JSONObject studios = obj.optJSONObject("studios");

            if(studios != null){

                JSONArray nodes = studios.optJSONArray("nodes");

                if(nodes != null && nodes.length() > 0){

                    anime.setStudio(
                            nodes.getJSONObject(0)
                                    .optString("name","Unknown")
                    );
                }
            }

            // ===== DIRECTOR =====

            JSONObject staff = obj.optJSONObject("staff");

            if(staff != null){

                JSONArray nodes = staff.optJSONArray("nodes");

                if(nodes != null){

                    for(int i=0;i<nodes.length();i++){

                        JSONObject person = nodes.getJSONObject(i);

                        JSONArray jobs = person.optJSONArray("primaryOccupations");

                        if(jobs != null){

                            for(int j=0;j<jobs.length();j++){

                                if(jobs.getString(j).equalsIgnoreCase("Director")){

                                    anime.setDirector(
                                            person.getJSONObject("name")
                                                    .optString("full","Unknown")
                                    );

                                    break;
                                }
                            }
                        }
                    }
                }
            }

            return anime;

        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }
}