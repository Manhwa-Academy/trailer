package com.mari.magic.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mari.magic.network.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MangaDexResolver {

    private static final String TAG = "MangaDexResolver";

    /**
     * Tìm MangaDex link từ title và mở trực tiếp trang truyện
     * @param context Context để startActivity
     * @param title Title từ AniList (romaji / english)
     */
    public static void openMangaDex(Context context, String title) {
        if(title == null || title.isEmpty()){
            Toast.makeText(context,"Cannot find manga title",Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString());

            String url = "https://api.mangadex.org/manga?title=" + encodedTitle + "&limit=5";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    response -> {
                        try {
                            JSONArray data = response.getJSONArray("data");
                            if(data.length() == 0){
                                Toast.makeText(context,"Manga not found on MangaDex",Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Chọn manga đầu tiên (title khớp nhất)
                            JSONObject first = data.getJSONObject(0);
                            String id = first.getString("id");

                            // Lấy slug để mở link chuẩn
                            JSONObject attributes = first.getJSONObject("attributes");
                            String slug = attributes.optString("slug","");

                            String mangaUrl = "https://mangadex.org/title/" + id;
                            if(!slug.isEmpty()){
                                mangaUrl += "/" + slug;
                            }

                            // Mở link bằng intent
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mangaUrl));
                            context.startActivity(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context,"Error parsing MangaDex response",Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e(TAG,"MangaDex API error: "+error);
                        Toast.makeText(context,"Cannot reach MangaDex",Toast.LENGTH_SHORT).show();
                    }
            );

            request.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(context).addToRequestQueue(request);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context,"Cannot open MangaDex",Toast.LENGTH_SHORT).show();
        }
    }
}