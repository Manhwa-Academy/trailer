package com.mari.magic.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mari.magic.R;
import com.mari.magic.adapter.HomeSectionAdapter;
import com.mari.magic.model.Anime;
import com.mari.magic.model.Section;
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

    RecyclerView recyclerHome;
    HomeSectionAdapter adapter;

    List<Section> sectionList = new ArrayList<>();

    List<Anime> movieList = new ArrayList<>();
    List<Anime> seriesList = new ArrayList<>();
    List<Anime> ecchiList = new ArrayList<>();
    List<Anime> adultList = new ArrayList<>();

    public HomeFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerHome = view.findViewById(R.id.recyclerHome);

        recyclerHome.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        setupSections();

        adapter = new HomeSectionAdapter(getContext(), sectionList);
        recyclerHome.setAdapter(adapter);

        loadHomeAnime();

        return view;
    }

    // ================= SECTIONS =================

    private void setupSections(){

        sectionList.clear();

        sectionList.add(new Section(Section.TYPE_BANNER));
        sectionList.add(new Section(Section.TYPE_SEARCH));

        sectionList.add(new Section(
                Section.CAT_MOVIES,
                getString(R.string.popular_movies),
                movieList
        ));

        sectionList.add(new Section(
                Section.CAT_SERIES,
                getString(R.string.popular_series),
                seriesList
        ));

        String filter = AppSettings.getContentFilter(getContext());

        if("16".equals(filter)){
            sectionList.add(new Section(
                    Section.CAT_ECCHI,
                    getString(R.string.ecchi_series),
                    ecchiList
            ));
        }

        if("18".equals(filter)){
            sectionList.add(new Section(
                    Section.CAT_ADULT,
                    getString(R.string.adult_series),
                    adultList
            ));
        }
    }

    // ================= API =================

    private void loadHomeAnime(){

        if(!isAdded()) return;

        String url = "https://graphql.anilist.co";
        String query =
                "query {" +

                        "movies: Page(page:1, perPage:20) {" +
                        "  media(type:ANIME, format:MOVIE, sort:POPULARITY_DESC) {" +
                        "   id title { romaji english native }" +
                        "   format season seasonYear duration episodes" +
                        "   status " +
                        "   averageScore description genres isAdult" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +
                        "   updatedAt " +
                        "   staff(perPage:5) {" +
                        "     nodes {" +
                        "       name { full }" +
                        "       primaryOccupations" +
                        "     }" +
                        "   }" +
                        "   trailer { id site }" +
                        "  }" +
                        " }" +

                        "series: Page(page:1, perPage:20) {" +
                        "  media(type:ANIME, format:TV, sort:POPULARITY_DESC) {" +
                        "   id title { romaji english native }" +
                        "   format season seasonYear duration episodes" +
                        "   nextAiringEpisode { episode airingAt }" +
                        "   status " +
                        "   averageScore description genres isAdult" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +
                        "   updatedAt " +
                        "   staff(perPage:5) {" +
                        "     nodes {" +
                        "       name { full }" +
                        "       primaryOccupations" +
                        "     }" +
                        "   }" +
                        "   trailer { id site }" +
                        "  }" +
                        " }" +

                        "ecchi: Page(page:1, perPage:20) {" +
                        "  media(type:ANIME, genre_in:[\"Ecchi\"], sort:POPULARITY_DESC) {" +
                        "   id title { romaji english native }" +
                        "   format season seasonYear duration episodes" +
                        "   nextAiringEpisode { episode airingAt }" +
                        "   status " +
                        "   averageScore description genres isAdult" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +
                        "   updatedAt " +
                        "   staff(perPage:5) {" +
                        "     nodes {" +
                        "       name { full }" +
                        "       primaryOccupations" +
                        "     }" +
                        "   }" +
                        "   trailer { id site }" +
                        "  }" +
                        " }" +

                        "adult: Page(page:1, perPage:20) {" +
                        "  media(type:ANIME, isAdult:true, sort:POPULARITY_DESC) {" +
                        "   id title { romaji english native }" +
                        "   format season seasonYear duration episodes" +
                        "   nextAiringEpisode { episode airingAt }" +
                        "   status " +
                        "   averageScore description genres isAdult" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +
                        "   updatedAt " +
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
                    new JsonObjectRequest(
                            Request.Method.POST,
                            url,
                            body,

                            response -> {

                                if(!isAdded()) return;

                                try{

                                    JSONObject data = response.getJSONObject("data");

                                    parseAnime(
                                            data.getJSONObject("movies")
                                                    .getJSONArray("media"),
                                            movieList
                                    );

                                    parseAnime(
                                            data.getJSONObject("series")
                                                    .getJSONArray("media"),
                                            seriesList
                                    );

                                    String filter =
                                            AppSettings.getContentFilter(getContext());

                                    if("16".equals(filter)){

                                        parseAnime(
                                                data.getJSONObject("ecchi")
                                                        .getJSONArray("media"),
                                                ecchiList
                                        );
                                    }

                                    if("18".equals(filter)){

                                        parseAnime(
                                                data.getJSONObject("adult")
                                                        .getJSONArray("media"),
                                                adultList
                                        );
                                    }

                                    if(adapter != null){
                                        adapter.notifyDataSetChanged();
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

            request.setTag(TAG);

            request.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton
                    .getInstance(getContext())
                    .addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // ================= PARSE =================

    private void parseAnime(JSONArray media, List<Anime> list){

        if(!isAdded()) return;

        list.clear();

        try{

            for(int i=0;i<media.length();i++){

                JSONObject obj = media.getJSONObject(i);

                Anime anime = AnimeParser.parse(obj,getContext());

                if(anime != null){
                    list.add(anime);
                }
            }

        }catch(Exception e){
            Log.e(TAG,"PARSE ERROR",e);
        }
    }

    // ================= CANCEL REQUEST =================

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(getContext()!=null){
            VolleySingleton
                    .getInstance(getContext())
                    .getRequestQueue()
                    .cancelAll(TAG);
        }
    }
}