package com.mari.magic.home;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import com.mari.magic.R;
import com.mari.magic.network.VolleySingleton;
import com.mari.magic.utils.AnimeParser;
import com.mari.magic.utils.AppSettings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String TAG = "HOME_DEBUG";
    ViewPager2 bannerSlider;
    LinearLayout dotsLayout;
    EditText edtSearch;
    ImageView btnSearch;
    ImageView btnClearSearch;
    BannerManager bannerManager;
    RecyclerView recyclerPopularMovies;
    RecyclerView recyclerPopularSeries;
    RecyclerView recyclerAdult;

    TextView txtAdult;
    List<Anime> movieList = new ArrayList<>();
    List<Anime> seriesList = new ArrayList<>();
    List<Anime> adultList = new ArrayList<>();
    RecyclerView recyclerEcchi;
    TextView txtEcchi;

    List<Anime> ecchiList = new ArrayList<>();
    AnimeAdapter ecchiAdapter;
    AnimeAdapter movieAdapter;
    AnimeAdapter seriesAdapter;
    AnimeAdapter adultAdapter;

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
        dotsLayout = view.findViewById(R.id.dotsLayout);
        bannerSlider = view.findViewById(R.id.bannerSlider);

        recyclerPopularMovies = view.findViewById(R.id.recyclerPopularMovies);
        recyclerPopularSeries = view.findViewById(R.id.recyclerPopularSeries);

        recyclerAdult = view.findViewById(R.id.recyclerAdult);
        txtAdult = view.findViewById(R.id.txtAdult);
        recyclerEcchi = view.findViewById(R.id.recyclerEcchi);
        txtEcchi = view.findViewById(R.id.txtEcchi);
        bannerManager = new BannerManager(bannerSlider,dotsLayout);
        bannerManager.setup(getContext());
        recyclerEcchi.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false)
        );

        ecchiAdapter = new AnimeAdapter(getContext(), ecchiList, R.layout.item_anime);
        recyclerEcchi.setAdapter(ecchiAdapter);
        recyclerPopularMovies.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        recyclerPopularSeries.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        recyclerAdult.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        movieAdapter = new AnimeAdapter(getContext(), movieList, R.layout.item_anime);
        seriesAdapter = new AnimeAdapter(getContext(), seriesList, R.layout.item_anime);
        adultAdapter = new AnimeAdapter(getContext(), adultList, R.layout.item_anime);

        recyclerPopularMovies.setAdapter(movieAdapter);
        recyclerPopularSeries.setAdapter(seriesAdapter);
        recyclerAdult.setAdapter(adultAdapter);

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


        checkFilterUI();

        loadHomeAnime();

        return view;
    }

    // ================= FILTER UI =================

    private void checkFilterUI(){

        String filter = AppSettings.getContentFilter(getContext());

        if(filter.equals("18")){

            recyclerPopularMovies.setVisibility(View.GONE);
            recyclerPopularSeries.setVisibility(View.GONE);

            txtAdult.setVisibility(View.VISIBLE);
            recyclerAdult.setVisibility(View.VISIBLE);

        }else{

            recyclerPopularMovies.setVisibility(View.VISIBLE);
            recyclerPopularSeries.setVisibility(View.VISIBLE);

            txtAdult.setVisibility(View.GONE);
            recyclerAdult.setVisibility(View.GONE);
        }

        if(filter.equals("16")){

            txtEcchi.setVisibility(View.VISIBLE);
            recyclerEcchi.setVisibility(View.VISIBLE);

        }else{

            txtEcchi.setVisibility(View.GONE);
            recyclerEcchi.setVisibility(View.GONE);
        }
    }

    // ================= GRAPHQL =================

    private void loadHomeAnime(){

        String url = "https://graphql.anilist.co";

        String query =
                "query {" +

                        " movies: Page(page:1, perPage:20) {" +
                        "  media(type:ANIME, format:MOVIE, sort:POPULARITY_DESC) {" +
                        "   id title { romaji english native }" +
                        "   format season seasonYear duration" +
                        "   averageScore description genres isAdult" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +

                        // ⭐ THÊM STAFF
                        "   staff(perPage:5) {" +
                        "     nodes {" +
                        "       name { full }" +
                        "       primaryOccupations" +
                        "     }" +
                        "   }" +

                        "   trailer { id site }" +
                        "  }" +
                        " }" +

                        " series: Page(page:1, perPage:20) {" +
                        "  media(type:ANIME, format:TV, sort:POPULARITY_DESC) {" +
                        "   id title { romaji english native }" +
                        "   format season seasonYear duration" +
                        "   averageScore description genres isAdult" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +

                        // ⭐ THÊM STAFF
                        "   staff(perPage:5) {" +
                        "     nodes {" +
                        "       name { full }" +
                        "       primaryOccupations" +
                        "     }" +
                        "   }" +

                        "   trailer { id site }" +
                        "  }" +
                        " }" +

                        // ⭐ ECCHI (16+)
                        " ecchi: Page(page:1, perPage:20) {" +
                        "  media(type:ANIME, genre_in:[\"Ecchi\"], sort:POPULARITY_DESC) {" +
                        "   id title { romaji english native }" +
                        "   format season seasonYear duration" +
                        "   averageScore description genres isAdult" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +

                        // ⭐ THÊM STAFF
                        "   staff(perPage:5) {" +
                        "     nodes {" +
                        "       name { full }" +
                        "       primaryOccupations" +
                        "     }" +
                        "   }" +

                        "   trailer { id site }" +
                        "  }" +
                        " }" +

                        // ⭐ ADULT (18+)
                        " adult: Page(page:1, perPage:20) {" +
                        "  media(type:ANIME, isAdult:true, sort:POPULARITY_DESC) {" +
                        "   id title { romaji english native }" +
                        "   format season seasonYear duration" +
                        "   averageScore description genres isAdult" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +

                        // ⭐ THÊM STAFF
                        "   staff(perPage:5) {" +
                        "     nodes {" +
                        "       name { full }" +
                        "       primaryOccupations" +
                        "     }" +
                        "   }" +

                        "   trailer { id site }" +
                        "  }" +
                        " }" +

                        "}";
        try{

            JSONObject body = new JSONObject();
            body.put("query",query);

            JsonObjectRequest request =
                    new JsonObjectRequest(Request.Method.POST,url,body,

                            response -> {

                                try{

                                    JSONObject data = response.getJSONObject("data");

                                    parseAnime(
                                            data.getJSONObject("movies")
                                                    .getJSONArray("media"),
                                            movieList,
                                            movieAdapter
                                    );

                                    parseAnime(
                                            data.getJSONObject("series")
                                                    .getJSONArray("media"),
                                            seriesList,
                                            seriesAdapter
                                    );

                                    String filter = AppSettings.getContentFilter(getContext());

                                    if(filter.equals("16")){

                                        parseAnime(
                                                data.getJSONObject("ecchi").getJSONArray("media"),
                                                ecchiList,
                                                ecchiAdapter
                                        );

                                    }
                                    if(filter.equals("18")){

                                        parseAnime(
                                                data.getJSONObject("adult")
                                                        .getJSONArray("media"),
                                                adultList,
                                                adultAdapter
                                        );
                                    }

                                }catch(Exception e){
                                    Log.e(TAG,"JSON ERROR",e);
                                }

                            },

                            error -> Log.e(TAG,"API ERROR "+error)

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

            VolleySingleton
                    .getInstance(requireContext())
                    .addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // ================= PARSE =================

    private void parseAnime(JSONArray media, List<Anime> list, AnimeAdapter adapter){

        list.clear();

        try{

            for(int i=0;i<media.length();i++){

                JSONObject obj = media.getJSONObject(i);

                Anime anime = AnimeParser.parse(obj, getContext());

                if(anime != null){
                    list.add(anime);
                }

            }

            adapter.notifyDataSetChanged();

        }catch(Exception e){
            Log.e(TAG,"PARSE ERROR",e);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(bannerManager != null){
            bannerManager.stop();
        }
    }
}