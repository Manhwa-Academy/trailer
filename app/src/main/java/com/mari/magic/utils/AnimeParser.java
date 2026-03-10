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

            String genresText = "";

            if(genresArray != null){
                for(int i=0;i<genresArray.length();i++){
                    genresText += genresArray.optString(i) + ",";
                }
            }

            // SAFE
            if(filter.equals("all")){
                if(isAdult || genresText.contains("Ecchi"))
                    return null;
            }

            // 16+
            else if(filter.equals("16")){
                if(isAdult)
                    return null;
            }

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

            // ===== POPULARITY =====

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

            // ===== YEAR + SEASON =====

            int year = obj.optInt("seasonYear",0);
            String season = obj.optString("season","");

            anime.setYear(year);

            if(!season.isEmpty())
                anime.setSeason(season + " " + year);

            // ===== DURATION =====

            anime.setDuration(obj.optInt("duration",0));

            // ===== STATUS =====

            anime.setStatus(obj.optString("status",""));

            // ===== UPDATED =====

            anime.setUpdatedAt(obj.optLong("updatedAt",0));

            // ===== EPISODES =====

            int episodes = obj.optInt("episodes",0);

            JSONObject next = obj.optJSONObject("nextAiringEpisode");

            if(next != null){

                int nextEpisode = next.optInt("episode",0);
                long airingAt = next.optLong("airingAt",0);

                anime.setNextEpisode(nextEpisode);
                anime.setNextAiringAt(airingAt);

                if(nextEpisode > 1){

                    int airedEpisodes = nextEpisode - 1;

                    if(episodes == 0 || episodes < airedEpisodes){
                        episodes = airedEpisodes;
                    }
                }
            }

            anime.setEpisodes(episodes);

            // ===== MANGA DATA =====

            anime.setChapters(obj.optInt("chapters",0));
            anime.setVolumes(obj.optInt("volumes",0));

            // ===== TRAILER =====

            if(obj.has("trailer") && !obj.isNull("trailer")){

                JSONObject trailer = obj.getJSONObject("trailer");

                if(trailer.optString("site").equalsIgnoreCase("youtube")){
                    anime.setTrailer(trailer.optString("id"));
                }
            }

            // ===== STUDIOS =====

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

            // ===== STAFF (AUTHOR + DIRECTOR) =====

            JSONObject staff = obj.optJSONObject("staff");

            if(staff != null){

                JSONArray nodes = staff.optJSONArray("nodes");

                if(nodes != null){

                    for(int i=0;i<nodes.length();i++){

                        JSONObject person = nodes.getJSONObject(i);
                        JSONArray jobs = person.optJSONArray("primaryOccupations");

                        if(jobs == null) continue;

                        for(int j=0;j<jobs.length();j++){

                            String job = jobs.getString(j).toLowerCase();

                            if(job.contains("director")){

                                anime.setDirector(
                                        person.getJSONObject("name")
                                                .optString("full","Unknown")
                                );
                            }

                            if(job.contains("author") ||
                                    job.contains("story") ||
                                    job.contains("writer")){

                                anime.setAuthor(
                                        person.getJSONObject("name")
                                                .optString("full","Unknown")
                                );
                            }
                        }
                    }
                }
            }
            JSONArray links = obj.optJSONArray("externalLinks");

            if(links != null && links.length() > 0){

                for(int i = 0; i < links.length(); i++){

                    JSONObject link = links.optJSONObject(i);

                    if(link == null) continue;

                    String site = link.optString("site","");
                    String url = link.optString("url","");

                    if(site.equalsIgnoreCase("MangaDex")){
                        anime.setMangaDexUrl(url);
                    }

                    if(site.equalsIgnoreCase("MyAnimeList")){
                        anime.setMalUrl(url);
                    }

                    // ⭐ fallback link
                    if(anime.getMangaDexUrl() == null && anime.getMalUrl() == null){
                        anime.setMalUrl(url);
                    }
                }
            }
            return anime;
// ===== EXTERNAL LINKS (MangaDex / MAL) =====


        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

}
