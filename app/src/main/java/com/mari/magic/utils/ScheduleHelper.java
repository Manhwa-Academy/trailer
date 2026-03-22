package com.mari.magic.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mari.magic.model.Anime;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.mari.magic.network.VolleySingleton;

public class ScheduleHelper {

    private static final String TAG = "ScheduleHelper";
    private static final String API_URL = "https://graphql.anilist.co";

    public static void loadScheduleAll(Context context, ScheduleListener listener){
        List<Anime> allAnime = new ArrayList<>();
        loadPage(context, 1, allAnime, listener);
    }

    private static void loadPage(Context context, int page, List<Anime> allAnime, ScheduleListener listener){
        try{
            JSONObject body = new JSONObject();
            body.put("query",
                    "query ($page:Int){ Page(page:$page, perPage:18){ " +
                            "pageInfo{ currentPage lastPage } " +
                            "media(status:RELEASING, type:ANIME){ " +

                            // 🎯 ID + TITLE
                            "id title{ romaji english native } coverImage{ large } " +

                            // 🎯 RATING
                            "averageScore " +

                            // 🎯 AIRING
                            "nextAiringEpisode{ episode airingAt } " +

                            // 🎯 INFO
                            "isAdult genres episodes duration format season seasonYear " +

                            // 🎯 STUDIO
                            "studios{ nodes{ name } } " +

                            // 🎯 DIRECTOR (FIX CHUẨN)
                            "staff(perPage:10){ edges{ role node{ name{ full } } } } " +

                            // 🎯 EXTRA
                            "description updatedAt popularity trailer{ site id } " +

                            "} } }"
            );

            JSONObject variables = new JSONObject();
            variables.put("page", page);
            body.put("variables", variables);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    API_URL,
                    body,
                    response -> {
                        try{
                            JSONObject pageObj = response.getJSONObject("data").getJSONObject("Page");
                            JSONArray array = pageObj.getJSONArray("media");

                            Calendar today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                            today.set(Calendar.HOUR_OF_DAY,0);
                            today.set(Calendar.MINUTE,0);
                            today.set(Calendar.SECOND,0);
                            today.set(Calendar.MILLISECOND,0);
                            long todayTimestamp = today.getTimeInMillis()/1000L;

                            for(int i=0;i<array.length();i++){
                                JSONObject obj = array.getJSONObject(i);
                                if(!obj.has("nextAiringEpisode") || obj.isNull("nextAiringEpisode")) continue;
                                JSONObject nextEp = obj.getJSONObject("nextAiringEpisode");
                                long airingAt = nextEp.getLong("airingAt");
                                if(airingAt < todayTimestamp) continue;

                                Anime anime = AnimeParser.parse(obj, context);

                                if(anime != null){
                                    anime.setNextEpisode(nextEp.optInt("episode",0));
                                    anime.setNextAiringAt(airingAt);

                                    // Lấy views từ popularity
                                    anime.setViews(obj.optLong("popularity",0));

                                    // Lấy trailer
                                    if(obj.has("trailer") && !obj.isNull("trailer")){
                                        JSONObject trailer = obj.getJSONObject("trailer");
                                        if(trailer.optString("site","").equalsIgnoreCase("youtube"))
                                            anime.setTrailer(trailer.optString("id",""));
                                    }

                                    allAnime.add(anime);
                                }
                            }

                            int currentPage = pageObj.getJSONObject("pageInfo").getInt("currentPage");
                            int lastPage = pageObj.getJSONObject("pageInfo").getInt("lastPage");

                            if(currentPage < lastPage){
                                loadPage(context, currentPage+1, allAnime, listener);
                            } else {
                                allAnime.sort((a,b)->Long.compare(a.getNextAiringAt(), b.getNextAiringAt()));
                                listener.onScheduleLoaded(allAnime, 1, (int)Math.ceil(allAnime.size()/20.0));
                            }

                        }catch(Exception e){
                            e.printStackTrace();
                            listener.onScheduleError(e.getMessage());
                        }
                    },
                    error -> listener.onScheduleError(error.toString())
            );

            VolleySingleton.getInstance(context).addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
            listener.onScheduleError(e.getMessage());
        }
    }
}