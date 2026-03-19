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

            int id = obj.optInt("id");

            anime.setId(id);
            anime.setAnilistId(id);
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

                if(nextEpisode > 0){

                    int airedEpisodes = Math.max(nextEpisode - 1, 0);

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

            JSONObject staff = obj.optJSONObject("staff");

// ✅ set default trước (quan trọng nhất)
            anime.setDirector("Unknown");
            anime.setAuthor("Unknown");

            if (staff != null) {

                JSONArray edges = staff.optJSONArray("edges");

                if (edges != null) {

                    for (int i = 0; i < edges.length(); i++) {

                        JSONObject edge = edges.getJSONObject(i);

                        String role = edge.optString("role", "").toLowerCase();

                        JSONObject node = edge.optJSONObject("node");
                        if (node == null) continue;

                        String name = node.getJSONObject("name")
                                .optString("full", "Unknown");

                        // 🎯 Director (Anime)
                        if (role.contains("director")) {
                            anime.setDirector(name);
                            break; // lấy cái đầu tiên là đủ
                        }

                        // 🎯 Author (Manga/Novel)
                        if (role.contains("original") ||
                                role.contains("story") ||
                                role.contains("writer")) {
                            anime.setAuthor(name);
                        }
                    }
                }
            }
            // ===== READ LINK =====

            // ===== EXTERNAL LINKS =====

            JSONArray links = obj.optJSONArray("externalLinks");

            String mangaDex = null;
            String mal = null;
            String fallback = null;

            if(links != null){

                for(int i = 0; i < links.length(); i++){

                    JSONObject link = links.optJSONObject(i);
                    if(link == null) continue;

                    String site = link.optString("site","");
                    String url = link.optString("url","");

                    if(url == null || url.isEmpty()) continue;

                    if(fallback == null)
                        fallback = url;

                    if(site.equalsIgnoreCase("MangaDex")){
                        mangaDex = url;
                    }

                    if(site.equalsIgnoreCase("MyAnimeList")){
                        mal = url;
                    }
                }
            }

// AniList page
            String siteUrl = obj.optString("siteUrl","");

// ưu tiên MangaDex -> MAL -> AniList -> fallback

            if(mangaDex != null){
                anime.setMalUrl(mangaDex);
            }
            else if(mal != null){
                anime.setMalUrl(mal);
            }
            else if(siteUrl != null && !siteUrl.isEmpty()){
                anime.setMalUrl(siteUrl);
            }
            else{
                anime.setMalUrl(fallback);
            }
            return anime;
// ===== EXTERNAL LINKS (MangaDex / MAL) =====


        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

}
