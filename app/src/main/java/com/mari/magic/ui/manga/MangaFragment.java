package com.mari.magic.ui.manga;

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

public class MangaFragment extends Fragment {

    private static final String TAG = "MANGA_DEBUG";

    RecyclerView recycler;
    HomeSectionAdapter adapter;

    List<Section> sectionList = new ArrayList<>();

    List<Anime> topRatedList = new ArrayList<>();
    List<Anime> trendingList = new ArrayList<>();

    public MangaFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_manga, container, false);

        recycler = view.findViewById(R.id.recyclerManga);

        recycler.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        setupSections();

        adapter = new HomeSectionAdapter(requireContext(), sectionList);
        recycler.setAdapter(adapter);

        loadManga();

        return view;
    }

    // ================= SECTIONS =================

    private void setupSections(){

        sectionList.clear();

        sectionList.add(new Section(Section.TYPE_BANNER));
        sectionList.add(new Section(Section.TYPE_SEARCH));

        sectionList.add(new Section(
                Section.CAT_TOP_MANGA,
                getString(R.string.top_manga_title),
                topRatedList
        ));

        sectionList.add(new Section(
                Section.CAT_TRENDING_MANGA,
                getString(R.string.trending_manga_title),
                trendingList
        ));
    }

    // ================= API =================

    private void loadManga(){

        if(!isAdded()) return;

        String url = "https://graphql.anilist.co";

        String query =
                "query {" +

                        "topRated: Page(page:1,perPage:10){ media(type:MANGA,sort:SCORE_DESC){ " +
                        "id " +
                        "title{romaji english native} " +
                        "format " +
                        "chapters " +
                        "volumes " +
                        "status " +
                        "averageScore " +
                        "description " +
                        "genres " +
                        "isAdult " +
                        "coverImage{large} " +
                        "updatedAt " +

                        "siteUrl " +

                        "externalLinks { site url } " +

                        "trailer { id site } " +

                        "staff(perPage:5){nodes{name{full} primaryOccupations}} " +
                        "} }" +

                        "trending: Page(page:1,perPage:10){ media(type:MANGA,sort:TRENDING_DESC){ " +
                        "id " +
                        "title{romaji english native} " +
                        "format " +
                        "chapters " +
                        "volumes " +
                        "status " +
                        "averageScore " +
                        "description " +
                        "genres " +
                        "isAdult " +
                        "coverImage{large} " +
                        "updatedAt " +

                        "siteUrl " +

                        "externalLinks { site url } " +

                        "trailer { id site } " +

                        "staff(perPage:5){nodes{name{full} primaryOccupations}} " +
                        "} }" +

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
                                            data.getJSONObject("topRated")
                                                    .getJSONArray("media"),
                                            topRatedList
                                    );

                                    parseAnime(
                                            data.getJSONObject("trending")
                                                    .getJSONArray("media"),
                                            trendingList
                                    );

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
                    .getInstance(requireContext())
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

            String filter = AppSettings.getContentFilter(requireContext());

            for(int i=0;i<media.length();i++){

                JSONObject obj = media.getJSONObject(i);

                Anime anime = AnimeParser.parse(obj, requireContext());

                if(anime == null) continue;

                boolean isAdult = anime.isAdult();
                boolean isEcchi = anime.getGenres()!=null &&
                        anime.getGenres().contains("Ecchi");

                if(filter.equals("all")){
                    if(!isAdult && !isEcchi){
                        list.add(anime);
                    }
                }
                else if(filter.equals("16")){
                    if(!isAdult){
                        list.add(anime);
                    }
                }
                else if(filter.equals("18")){
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