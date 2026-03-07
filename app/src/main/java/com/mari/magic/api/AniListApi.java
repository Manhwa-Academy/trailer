package com.mari.magic.api;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class AniListApi {

    private static final String URL = "https://graphql.anilist.co";

    public static void getTrendingAnime(Context context,
                                        com.android.volley.Response.Listener<JSONObject> listener,
                                        com.android.volley.Response.ErrorListener errorListener) {

        RequestQueue queue = Volley.newRequestQueue(context);

        try {

            String query =
                    "query {" +
                            " Page(page:1, perPage:20) {" +
                            "  media(type:ANIME, sort:TRENDING_DESC) {" +
                            "   title { romaji }" +
                            "   coverImage { large }" +
                            "   averageScore" +
                            "   trailer { id site }" +
                            "  }" +
                            " }" +
                            "}";

            JSONObject body = new JSONObject();
            body.put("query", query);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    URL,
                    body,
                    listener,
                    errorListener
            );

            queue.add(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}