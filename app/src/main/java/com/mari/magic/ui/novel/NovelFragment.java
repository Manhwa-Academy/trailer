package com.mari.magic.ui.novel;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mari.magic.R;
import com.mari.magic.adapter.HomeSectionAdapter;
import com.mari.magic.model.Anime;
import com.mari.magic.model.Section;
import com.mari.magic.network.VolleySingleton;
import com.mari.magic.ui.base.BaseFragment;
import com.mari.magic.utils.AnimeParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NovelFragment extends BaseFragment {

    private static final String TAG = "NOVEL_DEBUG";

    RecyclerView recycler;

    HomeSectionAdapter adapter;

    List<Section> sectionList = new ArrayList<>();

    List<Anime> trendingList = new ArrayList<>();
    List<Anime> topList = new ArrayList<>();
    List<Anime> newList = new ArrayList<>();

    public NovelFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_novel,container,false);

        recycler = view.findViewById(R.id.recyclerNovel);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HomeSectionAdapter(getContext(),sectionList);

        recycler.setAdapter(adapter);

        loadNovel();

        return view;
    }

    // ================= API =================

    private void loadNovel(){

        String url = "https://graphql.anilist.co";

        String query =
                "query {" +

                        " trending: Page(page:1,perPage:10) {" +
                        " media(type:MANGA,format:NOVEL,sort:TRENDING_DESC) {" +

                        " id " +
                        " title{romaji english native} " +
                        " format " +
                        " chapters " +
                        " volumes " +
                        " status " +
                        " averageScore " +
                        " description " +
                        " genres " +
                        " isAdult " +
                        " coverImage{large} " +
                        " updatedAt " +

                        " trailer{ id site } " +
                        " siteUrl " +

                        " staff(perPage:10){edges{role node{name{full}}}} " +

                        " }}"+


                        " top: Page(page:1,perPage:10) {" +
                        " media(type:MANGA,format:NOVEL,sort:SCORE_DESC) {" +

                        " id title{romaji english native} format chapters volumes status " +
                        " averageScore description genres isAdult coverImage{large} updatedAt " +

                        " trailer{ id site } " +
                        " siteUrl " +

                        " staff(perPage:10){edges{role node{name{full}}}} " +

                        " }}"+


                        " newest: Page(page:1,perPage:10) {" +
                        " media(type:MANGA,format:NOVEL,sort:START_DATE_DESC) {" +

                        " id title{romaji english native} format chapters volumes status " +
                        " averageScore description genres isAdult coverImage{large} updatedAt " +

                        " trailer{ id site } " +
                        " siteUrl " +

                        " staff(perPage:10){edges{role node{name{full}}}} " +

                        " }}"+

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

                                try{

                                    JSONObject data = response.getJSONObject("data");

                                    parseList(
                                            data.getJSONObject("trending")
                                                    .getJSONArray("media"),
                                            trendingList
                                    );

                                    parseList(
                                            data.getJSONObject("top")
                                                    .getJSONArray("media"),
                                            topList
                                    );

                                    parseList(
                                            data.getJSONObject("newest")
                                                    .getJSONArray("media"),
                                            newList
                                    );

                                    buildSections();

                                }catch(Exception e){
                                    Log.e(TAG,"JSON ERROR",e);
                                }

                            },

                            error -> Log.e(TAG,"API ERROR "+error)
                    );

            VolleySingleton
                    .getInstance(requireContext())
                    .addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // ================= BUILD UI =================

    private void buildSections(){

        sectionList.clear();

        // ⭐ thêm search
        sectionList.add(new Section(Section.TYPE_SEARCH));

        // ⭐ thêm banner
        sectionList.add(new Section(Section.TYPE_BANNER));

        sectionList.add(new Section(
                "trending_novel",
                getString(R.string.trending_novel_title),
                trendingList
        ));

        sectionList.add(new Section(
                "top_novel",
                getString(R.string.top_novel_title),
                topList
        ));

        sectionList.add(new Section(
                "new_novel",
                getString(R.string.new_novel_title),
                newList
        ));

        adapter.notifyDataSetChanged();
    }

    // ================= PARSER =================

    private void parseList(JSONArray media, List<Anime> list){

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
}