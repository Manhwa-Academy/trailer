package com.mari.magic.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.net.URLEncoder;

public class NovelResolver {

    public static void openNovel(Context ctx, String title){

        try{

            if(title == null || title.isEmpty()){
                title = "novel";
            }

            String query = URLEncoder.encode(title, "UTF-8");

            // ⭐ URL search đúng của NovelUpdates
            String url =
                    "https://www.novelupdates.com/series-finder/?sf=1&sh=" + query;

            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

        }
        catch(Exception e){

            Toast.makeText(ctx,"Cannot open novel reader",Toast.LENGTH_SHORT).show();

        }
    }
}