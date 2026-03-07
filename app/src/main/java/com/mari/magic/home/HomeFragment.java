package com.mari.magic.home;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import com.mari.magic.network.VolleySingleton;
import android.content.Intent;
import com.mari.magic.utils.TextUtils;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mari.magic.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String TAG = "HOME_DEBUG";

    ViewPager2 bannerSlider;

    EditText edtSearch;
    ImageView btnSearch;
    ImageView btnClearSearch;

    RecyclerView recyclerPopularMovies;
    RecyclerView recyclerPopularSeries;

    List<Banner> bannerList;

    List<Anime> movieList = new ArrayList<>();
    List<Anime> seriesList = new ArrayList<>();

    AnimeAdapter movieAdapter;
    AnimeAdapter seriesAdapter;


    Handler handler = new Handler();
    Runnable runnable;

    public HomeFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        edtSearch = view.findViewById(R.id.edtSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);

        bannerSlider = view.findViewById(R.id.bannerSlider);

        recyclerPopularMovies = view.findViewById(R.id.recyclerPopularMovies);
        recyclerPopularSeries = view.findViewById(R.id.recyclerPopularSeries);

        recyclerPopularMovies.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        recyclerPopularSeries.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        movieAdapter = new AnimeAdapter(getContext(), movieList, R.layout.item_anime);
        seriesAdapter = new AnimeAdapter(getContext(), seriesList, R.layout.item_anime);

        recyclerPopularMovies.setAdapter(movieAdapter);
        recyclerPopularSeries.setAdapter(seriesAdapter);

        btnSearch.setOnClickListener(v -> {

            String keyword = edtSearch.getText().toString().trim();

            if(!keyword.isEmpty()){

                edtSearch.setText("");
                edtSearch.clearFocus();

                Intent intent = new Intent(getContext(), SearchActivity.class);
                intent.putExtra("keyword", keyword);
                startActivity(intent);
            }
        });

        btnClearSearch.setOnClickListener(v -> edtSearch.setText(""));

        setupBanner();

        loadHomeAnime();

        return view;
    }

    // ================= GRAPHQL API =================

    private void loadHomeAnime(){

        String url = "https://graphql.anilist.co";

        String query =
                "query {" +
                        " movies: Page(page:1, perPage:20) {" +
                        "  media(type:ANIME, format:MOVIE, sort:POPULARITY_DESC) {" +
                        "   title { romaji english native }"+
                        "   format" +
                        "   season" +
                        "   seasonYear" +
                        "   duration" +
                        "   averageScore" +
                        "   description" +
                        "   genres" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +
                        "   staff(perPage:5) { nodes { name { full } primaryOccupations } }" +
                        "   trailer { id site }" +
                        "  }" +
                        " }" +
                        " series: Page(page:1, perPage:20) {" +
                        "  media(type:ANIME, format:TV, sort:POPULARITY_DESC) {" +
                        "   title { romaji english native }"+
                        "   format" +
                        "   season" +
                        "   seasonYear" +
                        "   duration" +
                        "   averageScore" +
                        "   description" +
                        "   genres" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +
                        "   staff(perPage:5) { nodes { name { full } primaryOccupations } }" +
                        "   trailer { id site }" +
                        "  }" +
                        " }" +
                        "}";
        try{

            JSONObject body = new JSONObject();
            body.put("query",query);

            Log.d(TAG,"QUERY: "+query);

            JsonObjectRequest request =
                    new JsonObjectRequest(Request.Method.POST,url,body,

                            response -> {

                                try{

                                    JSONObject data = response.getJSONObject("data");

                                    parseAnime(data.getJSONObject("movies")
                                                    .getJSONArray("media"),
                                            movieList,
                                            movieAdapter);

                                    parseAnime(data.getJSONObject("series")
                                                    .getJSONArray("media"),
                                            seriesList,
                                            seriesAdapter);

                                }catch(Exception e){
                                    Log.e(TAG,"JSON PARSE ERROR",e);
                                }

                            },

                            error -> {

                                if(error.networkResponse != null){

                                    int status = error.networkResponse.statusCode;

                                    String responseBody =
                                            new String(error.networkResponse.data);

                                    Log.e(TAG,"STATUS: "+status);
                                    Log.e(TAG,"BODY: "+responseBody);

                                    // Retry if rate limit
                                    if(status == 429){

                                        Log.e(TAG,"Retry after 3s");

                                        new Handler().postDelayed(
                                                this::loadHomeAnime,
                                                3000
                                        );
                                    }
                                }

                                Log.e(TAG,"API ERROR", error);
                            }
                    ){

                        @Override
                        public Map<String,String> getHeaders(){

                            Map<String,String> headers = new HashMap<>();

                            headers.put("Content-Type","application/json");
                            headers.put("Accept","application/json");
                            headers.put("User-Agent","MagicAnimeApp");

                            return headers;
                        }
                    };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            request.setShouldCache(true);

            VolleySingleton
                    .getInstance(requireContext())
                    .addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // ================= PARSE =================

    private void parseAnime(JSONArray media,List<Anime> list,AnimeAdapter adapter){

        list.clear();

        try{

            for(int i=0;i<media.length();i++){

                JSONObject anime = media.getJSONObject(i);

                JSONObject titleObj = anime.getJSONObject("title");

                String romaji = TextUtils.clean(titleObj.optString("romaji"));
                String english = TextUtils.clean(titleObj.optString("english"));
                String nativeTitle = TextUtils.clean(titleObj.optString("native"));

                String title = TextUtils.bestTitle(english, romaji, nativeTitle);

                String poster = anime
                        .getJSONObject("coverImage")
                        .optString("large","");

                double rating = anime.optDouble("averageScore",0);

                String description = anime.optString("description","");

                // ===== GENRES =====

                String genres = "";

                JSONArray genresArray = anime.optJSONArray("genres");

                if(genresArray!=null){

                    for(int g=0;g<genresArray.length();g++){

                        genres += genresArray.optString(g);

                        if(g < genresArray.length()-1)
                            genres += ", ";
                    }
                }

                // ===== TRAILER =====

                String trailer = "";

                if(anime.has("trailer") && !anime.isNull("trailer")){

                    JSONObject trailerObj = anime.getJSONObject("trailer");

                    String site = trailerObj.optString("site","");
                    String id = trailerObj.optString("id","");

                    if(site.equalsIgnoreCase("youtube"))
                        trailer = id;
                }

                // ===== FORMAT =====

                String format = anime.optString("format","Unknown");

                // ===== SEASON =====

                String season = anime.optString("season","");

                int seasonYear = anime.optInt("seasonYear",0);

                if(!season.isEmpty())
                    season = season + " " + seasonYear;

                // ===== DURATION =====

                int duration = anime.optInt("duration",0);

                // ===== STUDIO =====

                String studio = "";

                JSONObject studios = anime.optJSONObject("studios");

                if(studios != null){

                    JSONArray nodes = studios.optJSONArray("nodes");

                    if(nodes != null && nodes.length() > 0){

                        studio = nodes.getJSONObject(0)
                                .optString("name","");
                    }
                }

                // ===== DIRECTOR =====

                String director = "";

                JSONObject staff = anime.optJSONObject("staff");

                if(staff != null){

                    JSONArray nodes = staff.optJSONArray("nodes");

                    if(nodes != null){

                        for(int s=0;s<nodes.length();s++){

                            JSONObject person = nodes.getJSONObject(s);

                            JSONArray jobs = person.optJSONArray("primaryOccupations");

                            if(jobs != null){

                                for(int j=0;j<jobs.length();j++){

                                    if(jobs.getString(j).equalsIgnoreCase("Director")){

                                        director = person
                                                .getJSONObject("name")
                                                .optString("full","");

                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                // ===== CREATE OBJECT =====

                Anime animeObj = new Anime(title,poster,trailer,rating);

                animeObj.setEnglishTitle(english);
                animeObj.setRomajiTitle(romaji);
                animeObj.setNativeTitle(nativeTitle);

                animeObj.setDescription(description);
                animeObj.setGenres(genres);

                animeObj.setFormat(format);
                animeObj.setSeason(season);
                animeObj.setDuration(duration);
                animeObj.setStudio(studio);
                animeObj.setDirector(director);

                list.add(animeObj);
            }

            adapter.notifyDataSetChanged();

        }catch(Exception e){
            Log.e(TAG,"PARSE ERROR",e);
        }
    }

    // ================= BANNER =================

    private void setupBanner(){

        bannerList = new ArrayList<>();

        bannerList.add(new Banner(
                "https://cdn.myanimelist.net/images/anime/10/47347.jpg",
                "Attack on Titan",
                "https://youtu.be/MGRm4IzK1SQ"));

        bannerList.add(new Banner(
                "https://cdn.myanimelist.net/images/anime/1286/99889.jpg",
                "Demon Slayer",
                "https://youtu.be/VQGCKyvzIM4"));

        bannerList.add(new Banner(
                "https://cdn.myanimelist.net/images/anime/1171/109222.jpg",
                "Jujutsu Kaisen",
                "https://youtu.be/pkKu9hLT-t8"));

        BannerAdapter adapter = new BannerAdapter(getContext(),bannerList);
        bannerSlider.setAdapter(adapter);

        autoSlideBanner();
    }

    private void autoSlideBanner(){

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(handler!=null && runnable!=null)
            handler.removeCallbacks(runnable);
    }
}