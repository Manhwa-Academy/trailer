package com.mari.magic.banner;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mari.magic.R;
import com.mari.magic.adapter.BannerAdapter;
import com.mari.magic.model.Banner;
import com.mari.magic.network.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BannerManager {

    private ViewPager2 bannerSlider;
    private LinearLayout dotsLayout;

    private List<Banner> bannerList = new ArrayList<>();

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    public BannerManager(ViewPager2 slider, LinearLayout dots){
        this.bannerSlider = slider;
        this.dotsLayout = dots;
    }

    public void setup(Context context){

        String url = "https://graphql.anilist.co";

        try{

            JSONObject body = new JSONObject();

            body.put("query",
                    "query {" +
                            " Page(page:1, perPage:5) {" +
                            "  media(type:ANIME, sort:TRENDING_DESC) {" +
                            "   id " +
                            "   title { romaji english native } " +
                            "   bannerImage " +
                            "   coverImage { extraLarge } " +
                            "   format " +
                            "   season " +
                            "   seasonYear " +
                            "   duration " +
                            "   episodes " +
                            "   status " +
                            "   updatedAt " +
                            "   averageScore " +
                            "   genres " +
                            "   description(asHtml:false) " +
                            "   nextAiringEpisode { episode airingAt } " +
                            "   studios { nodes { name } } " +
                            "   staff(perPage:5) { nodes { name { full } primaryOccupations } } " +
                            "   trailer { id site } " +
                            "  }" +
                            " }" +
                            "}");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,

                    response -> {

                        try{

                            JSONArray media = response
                                    .getJSONObject("data")
                                    .getJSONObject("Page")
                                    .getJSONArray("media");

                            bannerList.clear();

                            for(int i=0;i<media.length();i++){

                                JSONObject obj = media.getJSONObject(i);

                                String id = String.valueOf(obj.optInt("id"));

                                JSONObject titleObj = obj.getJSONObject("title");

                                String english = titleObj.optString("english","");
                                String romaji = titleObj.optString("romaji","");
                                String nativeTitle = titleObj.optString("native","");

                                String title = !english.isEmpty() ? english :
                                        !romaji.isEmpty() ? romaji : nativeTitle;


                                String image = obj.optString("bannerImage");

                                if(image == null || image.isEmpty()){
                                    image = obj.getJSONObject("coverImage")
                                            .optString("extraLarge");
                                }

                                String format = obj.optString("format");
                                String season = obj.optString("season") + " " + obj.optInt("seasonYear");

                                int duration = obj.optInt("duration");
                                int episodes = obj.optInt("episodes");

                                String status = obj.optString("status");

                                long updatedAt = obj.optLong("updatedAt");
                                double rating = obj.optDouble("averageScore",0) / 10.0;
                                String description = obj.optString("description","");
                                JSONArray genresArr = obj.optJSONArray("genres");

                                String genres = "";

                                if(genresArr != null){

                                    StringBuilder g = new StringBuilder();

                                    for(int j=0;j<genresArr.length();j++){

                                        if(j > 0) g.append(", ");

                                        g.append(genresArr.optString(j));
                                    }

                                    genres = g.toString();
                                }
                                int nextEpisode = 0;
                                long nextEpisodeTime = 0;

                                JSONObject next = obj.optJSONObject("nextAiringEpisode");

                                if(next != null){
                                    nextEpisode = next.optInt("episode");
                                    nextEpisodeTime = next.optLong("airingAt");

                                    if(episodes == 0){
                                        episodes = nextEpisode - 1;
                                    }
                                }
                                else if("RELEASING".equalsIgnoreCase(status) && episodes > 0){
                                    nextEpisode = episodes + 1;
                                }
                                // studio
                                String studio = "Unknown";
                                JSONObject studios = obj.optJSONObject("studios");
                                if(studios != null){
                                    JSONArray nodes = studios.optJSONArray("nodes");
                                    if(nodes != null && nodes.length() > 0){
                                        studio = nodes.getJSONObject(0).optString("name","Unknown");
                                    }
                                }

                                // director
                                String director = "Unknown";
                                JSONObject staff = obj.optJSONObject("staff");
                                if(staff != null){
                                    JSONArray nodes = staff.optJSONArray("nodes");
                                    if(nodes != null){
                                        for(int j=0;j<nodes.length();j++){

                                            JSONObject person = nodes.getJSONObject(j);
                                            JSONArray jobs = person.optJSONArray("primaryOccupations");

                                            if(jobs != null){
                                                for(int k=0;k<jobs.length();k++){

                                                    if(jobs.getString(k).equalsIgnoreCase("Director")){
                                                        director = person.getJSONObject("name")
                                                                .optString("full","Unknown");
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                String trailer = "";

                                JSONObject trailerObj = obj.optJSONObject("trailer");

                                if(trailerObj != null &&
                                        trailerObj.optString("site")
                                                .equalsIgnoreCase("youtube")){

                                    trailer = trailerObj.optString("id");
                                }

                                Banner banner = new Banner(
                                        id,
                                        image,
                                        title,
                                        trailer,
                                        studio,
                                        director,
                                        season,
                                        duration,
                                        format,
                                        episodes,
                                        status,
                                        updatedAt
                                );

                                banner.setRating(rating);
                                banner.setDescription(description);
                                banner.setGenres(genres);
                                banner.setNextEpisode(nextEpisode);
                                banner.setRomajiTitle(romaji);
                                banner.setNativeTitle(nativeTitle);
                                banner.setNextAiringAt(nextEpisodeTime);
                                bannerList.add(banner);
                            }

                            BannerAdapter adapter =
                                    new BannerAdapter(context,bannerList);

                            bannerSlider.setAdapter(adapter);

                            setupDots(context);

                            if(bannerList.size() > 1)
                                autoSlide();

                        }catch(Exception e){
                            e.printStackTrace();
                        }

                    },

                    error -> error.printStackTrace()
            );

            VolleySingleton.getInstance(context)
                    .addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void setupDots(Context context){

        dotsLayout.removeAllViews();

        if(bannerList.isEmpty()) return;

        ImageView[] dots = new ImageView[bannerList.size()];

        for(int i=0;i<dots.length;i++){

            dots[i] = new ImageView(context);
            dots[i].setImageResource(R.drawable.dot_inactive);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(20,20);

            params.setMargins(8,0,8,0);

            dotsLayout.addView(dots[i],params);
        }

        dots[0].setImageResource(R.drawable.dot_active);

        bannerSlider.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback(){

                    @Override
                    public void onPageSelected(int position){

                        for(int i=0;i<dots.length;i++){

                            dots[i].setImageResource(
                                    i == position ?
                                            R.drawable.dot_active :
                                            R.drawable.dot_inactive
                            );
                        }
                    }
                });
    }

    private void autoSlide(){

        runnable = () -> {

            int current = bannerSlider.getCurrentItem();

            if(current == bannerList.size()-1)
                bannerSlider.setCurrentItem(0);
            else
                bannerSlider.setCurrentItem(current+1);

            handler.postDelayed(runnable,4000);
        };

        handler.postDelayed(runnable,4000);
    }

    public void stop(){

        if(handler != null && runnable != null){
            handler.removeCallbacks(runnable);
        }
    }
}