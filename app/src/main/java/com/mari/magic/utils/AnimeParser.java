package com.mari.magic.utils;

import com.mari.magic.home.Anime;

import org.json.JSONArray;
import org.json.JSONObject;

public class AnimeParser {

    public static Anime parse(JSONObject obj){

        Anime anime = new Anime();

        try{

            // ===== ID =====
            anime.setId(obj.optInt("id"));

            // ===== ADULT =====
            anime.setAdult(obj.optBoolean("isAdult", false));

            // ===== TITLE =====
            JSONObject titleObj = obj.optJSONObject("title");

            if(titleObj != null){

                String romaji = TextUtils.clean(titleObj.optString("romaji"));
                String english = TextUtils.clean(titleObj.optString("english"));
                String nativeTitle = TextUtils.clean(titleObj.optString("native"));

                anime.setRomajiTitle(romaji);
                anime.setEnglishTitle(english);
                anime.setNativeTitle(nativeTitle);

                String bestTitle =
                        TextUtils.bestTitle(english, romaji, nativeTitle);

                anime.setTitle(bestTitle);
            }

            // ===== POSTER =====
            JSONObject cover = obj.optJSONObject("coverImage");

            if(cover != null)
                anime.setPoster(cover.optString("large",""));

            // ===== RATING =====
            double score = obj.optDouble("averageScore",0);
            anime.setRating(score);

            // ===== DESCRIPTION =====
            String desc = obj.optString("description","");
            desc = desc.replaceAll("<[^>]*>", ""); // remove HTML
            anime.setDescription(desc);

            // ===== GENRES =====
            JSONArray genresArray = obj.optJSONArray("genres");

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

            // ===== TRAILER =====
            if(obj.has("trailer") && !obj.isNull("trailer")){

                JSONObject trailer = obj.getJSONObject("trailer");

                String site = trailer.optString("site","");
                String id = trailer.optString("id","");

                if(site.equalsIgnoreCase("youtube"))
                    anime.setTrailer(id);
            }

            // ===== STUDIO =====
            JSONObject studios = obj.optJSONObject("studios");

            if(studios != null){

                JSONArray nodes = studios.optJSONArray("nodes");

                if(nodes != null && nodes.length() > 0){

                    anime.setStudio(
                            nodes.getJSONObject(0)
                                    .optString("name","")
                    );
                }
            }

            // ===== DIRECTOR =====
            JSONObject staff = obj.optJSONObject("staff");

            if(staff != null){

                JSONArray nodes = staff.optJSONArray("nodes");

                if(nodes != null){

                    for(int s=0;s<nodes.length();s++){

                        JSONObject person = nodes.getJSONObject(s);

                        JSONArray jobs = person.optJSONArray("primaryOccupations");

                        if(jobs != null){

                            for(int j=0;j<jobs.length();j++){

                                if(jobs.getString(j)
                                        .equalsIgnoreCase("Director")){

                                    anime.setDirector(
                                            person.getJSONObject("name")
                                                    .optString("full","")
                                    );

                                    break;
                                }
                            }
                        }
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return anime;
    }
}