package com.mari.magic.utils;

import android.content.Context;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mari.magic.network.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class YoutubeService {

    public static void loadTrailerInfo(Context ctx,
                                       String trailerId,
                                       TextView txtViews,
                                       TextView txtDate){

        if(trailerId == null || trailerId.isEmpty()){
            txtDate.setText("Trailer: Unknown");
            return;
        }

        String url =
                "https://www.googleapis.com/youtube/v3/videos?id="
                        + trailerId +
                        "&part=statistics,snippet&key=AIzaSyBrm4ovRR5rbFQkK4DtjsfKf8bToom0IF4";

        JsonObjectRequest request =
                new JsonObjectRequest(Request.Method.GET, url, null,

                        response -> {

                            try{

                                JSONArray items = response.getJSONArray("items");

                                if(items.length() == 0){
                                    txtDate.setText("Trailer: Unknown");
                                    return;
                                }

                                JSONObject video = items.getJSONObject(0);

                                long views = video
                                        .getJSONObject("statistics")
                                        .optLong("viewCount",0);

                                txtViews.setText(
                                        "Views: " +
                                                NumberFormat.getInstance().format(views)
                                );

                                String published =
                                        video.getJSONObject("snippet")
                                                .getString("publishedAt");

                                SimpleDateFormat api =
                                        new SimpleDateFormat(
                                                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                                                Locale.US);

                                Date date = api.parse(published);

                                SimpleDateFormat display =
                                        new SimpleDateFormat(
                                                "HH:mm dd/MM/yyyy",
                                                Locale.getDefault());

                                txtDate.setText(
                                        "Trailer Released: " +
                                                display.format(date)
                                );

                            }catch(Exception e){
                                txtDate.setText("Trailer: Unknown");
                            }

                        },

                        error -> txtDate.setText("Trailer: Unknown")
                );

        VolleySingleton.getInstance(ctx).addToRequestQueue(request);
    }
}