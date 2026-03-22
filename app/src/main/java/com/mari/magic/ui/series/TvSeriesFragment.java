package com.mari.magic.ui.series;

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
import com.mari.magic.ui.base.BaseFragment;
import com.mari.magic.utils.AnimeParser;
import com.mari.magic.utils.AppSettings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TvSeriesFragment extends BaseFragment {

    private static final String TAG = "TV_DEBUG";

    RecyclerView recyclerTv;
    HomeSectionAdapter adapter;

    List<Section> sectionList = new ArrayList<>();

    List<Anime> topRatedList = new ArrayList<>();
    List<Anime> popularList = new ArrayList<>();
    List<Anime> releasingList = new ArrayList<>();
    List<Anime> ecchiList = new ArrayList<>();
    List<Anime> adultList = new ArrayList<>();

    public TvSeriesFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_tv, container, false);

        recyclerTv = view.findViewById(R.id.recyclerTv);

        recyclerTv.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        setupSections();

        adapter = new HomeSectionAdapter(requireContext(), sectionList);
        recyclerTv.setAdapter(adapter);

        loadAllSeries();

        return view;
    }

    // ================= SECTIONS =================

    private void setupSections(){

        sectionList.clear();

        sectionList.add(new Section(Section.TYPE_BANNER));
        sectionList.add(new Section(Section.TYPE_SEARCH));

        sectionList.add(new Section(
                Section.CAT_TOP_TV,
                getString(R.string.top_rated_series),
                topRatedList
        ));

        sectionList.add(new Section(
                Section.CAT_SERIES,
                getString(R.string.popular_series),
                popularList
        ));

        sectionList.add(new Section(
                Section.CAT_RELEASING,
                getString(R.string.releasing_series),
                releasingList
        ));

        String filter = AppSettings.getContentFilter(requireContext());

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

    private void loadAllSeries(){

        if(!isAdded()) return;

        String url = "https://graphql.anilist.co";
        String query = "query {" +
                "topRated: Page(page:1,perPage:10){ media(type:ANIME,format:TV,sort:SCORE_DESC){ id title{romaji english native} format season seasonYear duration episodes nextAiringEpisode{episode airingAt} status averageScore description genres isAdult coverImage{large} studios{nodes{name}} updatedAt staff(perPage:10){edges{role node{name{full}}}} trailer{id site} } }" +
                "popular: Page(page:1,perPage:10){ media(type:ANIME,format:TV,sort:POPULARITY_DESC){ id title{romaji english native} format season seasonYear duration episodes nextAiringEpisode{episode airingAt} status averageScore description genres isAdult coverImage{large} studios{nodes{name}} updatedAt staff(perPage:10){edges{role node{name{full}}}} trailer{id site} } }" +
                "releasing: Page(page:1,perPage:10){ media(type:ANIME,format:TV,status:RELEASING,sort:POPULARITY_DESC){ id title{romaji english native} format season seasonYear duration episodes nextAiringEpisode{episode airingAt} status averageScore description genres isAdult coverImage{large} studios{nodes{name}} updatedAt staff(perPage:10){edges{role node{name{full}}}} trailer{id site} } }" +
                "ecchi: Page(page:1,perPage:10){ media(type:ANIME,format:TV,genre_in:[\"Ecchi\"],sort:POPULARITY_DESC){ id title{romaji english native} format season seasonYear duration episodes nextAiringEpisode{episode airingAt} status averageScore description genres isAdult coverImage{large} studios{nodes{name}} updatedAt staff(perPage:10){edges{role node{name{full}}}} trailer{id site} } }" +
                "adult: Page(page:1,perPage:10){ media(type:ANIME,format:TV,isAdult:true,sort:POPULARITY_DESC){ id title{romaji english native} format season seasonYear duration episodes nextAiringEpisode{episode airingAt} status averageScore description genres isAdult coverImage{large} studios{nodes{name}} updatedAt staff(perPage:10){edges{role node{name{full}}}} trailer{id site} } }" +
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

                                    parseAnime(data.getJSONObject("topRated").getJSONArray("media"), topRatedList);
                                    parseAnime(data.getJSONObject("popular").getJSONArray("media"), popularList);
                                    parseAnime(data.getJSONObject("releasing").getJSONArray("media"), releasingList);

                                    String filter = AppSettings.getContentFilter(requireContext());

                                    if("16".equals(filter)){
                                        parseAnime(data.getJSONObject("ecchi").getJSONArray("media"), ecchiList);
                                    }

                                    if("18".equals(filter)){
                                        parseAnime(data.getJSONObject("adult").getJSONArray("media"), adultList);
                                    }

                                    if(adapter != null){
                                        adapter.notifyDataSetChanged();
                                    }

                                }catch(Exception e){
                                    Log.e(TAG,"JSON ERROR",e);
                                }

                            },

                            error -> Log.e(TAG,"API ERROR",error)
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

    private void parseAnime(JSONArray media, List<Anime> list) throws Exception{

        if(!isAdded()) return;

        list.clear();

        for(int i=0;i<media.length();i++){

            JSONObject obj = media.getJSONObject(i);

            Anime anime = AnimeParser.parse(obj, requireContext());

            if(anime != null){
                list.add(anime);
            }
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

