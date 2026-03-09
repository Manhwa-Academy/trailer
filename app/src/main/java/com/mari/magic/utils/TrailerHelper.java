package com.mari.magic.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class TrailerHelper {

    public static void openTrailer(
            Context context,
            String title,
            String trailer,
            boolean isAdult
    ){

        // ===== 18+ =====
        if(isAdult){

            String cleanTitle = title.toLowerCase()
                    .replaceAll("[^a-z0-9 ]", "")
                    .trim();

            String episode = "1";

            String[] parts = cleanTitle.split(" ");

            if(parts.length > 1 && parts[parts.length - 1].matches("\\d+")){
                episode = parts[parts.length - 1];
            }

            String slug = title.toLowerCase()
                    .replace("-", "")
                    .replaceAll("[^a-z0-9\\s]", "")
                    .replaceAll("\\s+", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");

            String url = "https://hentaiz.dog/browse?q=" + slug + "-" + episode;

            context.startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
            ));

            return;
        }

        // ===== YOUTUBE TRAILER =====

        if(trailer != null && !trailer.isEmpty()){

            try{

                context.startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("vnd.youtube:" + trailer)
                ));

            }catch(Exception e){

                context.startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=" + trailer)
                ));
            }

        }else{

            String query = Uri.encode(title + " trailer anime");

            context.startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/results?search_query=" + query)
            ));
        }
    }
}