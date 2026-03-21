package com.mari.magic.utils;

import android.content.Context;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mari.magic.network.VolleySingleton;
import com.mari.magic.R;

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
                                       TextView txtDate) {

        if(trailerId == null || trailerId.isEmpty()){
            txtDate.setText(ctx.getString(R.string.trailer_label) + ": " + ctx.getString(R.string.unknown_label));
            txtViews.setText(ctx.getString(R.string.views_label) + ": " + ctx.getString(R.string.unknown_label));
            return;
        }

        String url =
                "https://www.googleapis.com/youtube/v3/videos?id="
                        + trailerId +
                        "&part=statistics,snippet&key=AIzaSyBrm4ovRR5rbFQkK4DtjsfKf8bToom0IF4";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray items = response.getJSONArray("items");

                        if(items.length() == 0){
                            txtDate.setText(ctx.getString(R.string.trailer_label) + ": " + ctx.getString(R.string.unknown_label));
                            txtViews.setText(ctx.getString(R.string.views_label) + ": " + ctx.getString(R.string.unknown_label));
                            return;
                        }

                        JSONObject video = items.getJSONObject(0);

                        long views = video.getJSONObject("statistics").optLong("viewCount", 0);

                        txtViews.setText(
                                ctx.getString(R.string.views_label) + ": " +
                                        NumberFormat.getInstance(Locale.getDefault()).format(views)
                        );

                        String published = video.getJSONObject("snippet").getString("publishedAt");

                        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                        Date date = apiFormat.parse(published);

                        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

                        txtDate.setText(
                                ctx.getString(R.string.trailer_label) + ": " + displayFormat.format(date)
                        );

                    } catch(Exception e){
                        txtDate.setText(ctx.getString(R.string.trailer_label) + ": " + ctx.getString(R.string.unknown_label));
                        txtViews.setText(ctx.getString(R.string.views_label) + ": " + ctx.getString(R.string.unknown_label));
                    }

                },
                error -> {
                    txtDate.setText(ctx.getString(R.string.trailer_label) + ": " + ctx.getString(R.string.unknown_label));
                    txtViews.setText(ctx.getString(R.string.views_label) + ": " + ctx.getString(R.string.unknown_label));
                }
        );

        VolleySingleton.getInstance(ctx).addToRequestQueue(request);
    }
}