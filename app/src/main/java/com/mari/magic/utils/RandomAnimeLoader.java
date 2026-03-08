package com.mari.magic.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mari.magic.home.Anime;
import com.mari.magic.home.AnimeDetailActivity;
import com.mari.magic.network.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

public class RandomAnimeLoader {

    private static final String TAG = "RandomAnime";
    private static final int MAX_TRY = 5;

    public static void load(Context context){
        load(context,0);
    }

    private static void load(Context context, int attempt){

        if(attempt >= MAX_TRY){
            Log.e(TAG,"Random failed after "+MAX_TRY+" tries");
            return;
        }

        int randomPage = 1 + (int)(Math.random() * 200);

        String url = "https://graphql.anilist.co";

        try{

            JSONObject body = new JSONObject();

            String query =
                    "query ($page: Int) {" +
                            " Page(page:$page, perPage:1) {" +
                            "  media(type:ANIME, sort:POPULARITY_DESC) {" +
                            "   id title { romaji english native } " +
                            "   coverImage { large } " +
                            "   averageScore description genres " +
                            "   format season seasonYear duration " +
                            "   popularity trailer { id site } " +
                            "   studios { nodes { name } } " +
                            "   staff(perPage:5) { nodes { name { full } primaryOccupations } } " +
                            "   isAdult " +
                            "  }" +
                            " }" +
                            "}";

            JSONObject variables = new JSONObject();
            variables.put("page", randomPage);

            body.put("query", query);
            body.put("variables", variables);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,

                    response -> {

                        try{

                            JSONArray mediaArray = response
                                    .getJSONObject("data")
                                    .getJSONObject("Page")
                                    .getJSONArray("media");

                            if(mediaArray.length() == 0){
                                load(context, attempt+1);
                                return;
                            }

                            JSONObject media = mediaArray.getJSONObject(0);

                            Anime anime = AnimeParser.parse(media, context);

                            if(anime == null){
                                load(context, attempt+1);
                                return;
                            }

                            Intent intent = new Intent(context, AnimeDetailActivity.class);

                            intent.putExtra("id", anime.getId());
                            intent.putExtra("title", anime.getTitle());
                            intent.putExtra("poster", anime.getPoster());
                            intent.putExtra("rating", anime.getRating());
                            intent.putExtra("description", anime.getDescription());
                            intent.putExtra("genres", anime.getGenres());
                            intent.putExtra("season", anime.getSeason());
                            intent.putExtra("duration", anime.getDuration());
                            intent.putExtra("format", anime.getFormat());
                            intent.putExtra("studio", anime.getStudio());
                            intent.putExtra("director", anime.getDirector());
                            intent.putExtra("trailer", anime.getTrailer());
                            intent.putExtra("romajiTitle", anime.getRomajiTitle());
                            intent.putExtra("englishTitle", anime.getEnglishTitle());
                            intent.putExtra("nativeTitle", anime.getNativeTitle());

                            context.startActivity(intent);

                        }catch(Exception e){
                            Log.e(TAG,"Parse error",e);
                        }

                    },

                    error -> Log.e(TAG,"Volley error "+error)
            );

            VolleySingleton.getInstance(context).addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}