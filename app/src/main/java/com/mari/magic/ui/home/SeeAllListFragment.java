package com.mari.magic.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mari.magic.R;
import com.mari.magic.adapter.AnimeAdapter;
import com.mari.magic.model.Anime;
import com.mari.magic.model.Section;
import com.mari.magic.network.VolleySingleton;
import com.mari.magic.utils.AnimeParser;
import com.mari.magic.utils.AppSettings;
import com.mari.magic.utils.GridSpacingItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SeeAllListFragment extends Fragment {

    private static final String TAG = "SEE_ALL_FRAGMENT";

    RecyclerView recycler;
    AnimeAdapter adapter;

    List<Anime> list = new ArrayList<>();

    String section;

    public SeeAllListFragment(){}

    public static SeeAllListFragment newInstance(String section){

        SeeAllListFragment fragment = new SeeAllListFragment();

        Bundle args = new Bundle();
        args.putString("section", section);

        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_see_all_list, container, false);

        recycler = view.findViewById(R.id.recyclerSeeAllList);

        recycler.setLayoutManager(new GridLayoutManager(getContext(),3));
        recycler.addItemDecoration(new GridSpacingItemDecoration(3,20,true));

        adapter = new AnimeAdapter(getContext(), list, R.layout.item_anime_genre);

        recycler.setAdapter(adapter);

        if(getArguments() != null){
            section = getArguments().getString("section", Section.CAT_MOVIES);
        }

        loadAnime();

        return view;
    }

    private void loadAnime(){

        String contentFilter = AppSettings.getContentFilter(getContext());
        String url = "https://graphql.anilist.co";

        boolean safeMode = contentFilter.equals("all");

        try{

            String filter = "";

            switch(section){

                case Section.CAT_NEW:

                    if(safeMode){
                        filter = "status:RELEASING, episodes_greater:0, isAdult:false, genre_not_in:[\"Ecchi\",\"Hentai\"], sort:UPDATED_AT_DESC";
                    }else{
                        filter = "status:RELEASING, episodes_greater:0, sort:UPDATED_AT_DESC";
                    }

                    break;

                case Section.CAT_MOVIES:

                    if(safeMode){
                        filter = "format:MOVIE, isAdult:false, genre_not_in:[\"Ecchi\",\"Hentai\"], sort:POPULARITY_DESC";
                    }else{
                        filter = "format:MOVIE, sort:POPULARITY_DESC";
                    }

                    break;

                case Section.CAT_TOP_MOVIES:

                    if(safeMode){
                        filter = "format:MOVIE, isAdult:false, genre_not_in:[\"Ecchi\",\"Hentai\"], sort:SCORE_DESC";
                    }else{
                        filter = "format:MOVIE, sort:SCORE_DESC";
                    }

                    break;

                case Section.CAT_SERIES:

                    if(safeMode){
                        filter = "format:TV, isAdult:false, genre_not_in:[\"Ecchi\",\"Hentai\"], sort:POPULARITY_DESC";
                    }else{
                        filter = "format:TV, sort:POPULARITY_DESC";
                    }

                    break;

                case Section.CAT_TOP_TV:

                    if(safeMode){
                        filter = "format:TV, isAdult:false, genre_not_in:[\"Ecchi\",\"Hentai\"], sort:SCORE_DESC";
                    }else{
                        filter = "format:TV, sort:SCORE_DESC";
                    }

                    break;

                case Section.CAT_RELEASING:

                    if(safeMode){
                        filter = "format:TV, status:RELEASING, isAdult:false, genre_not_in:[\"Ecchi\",\"Hentai\"], sort:POPULARITY_DESC";
                    }else{
                        filter = "format:TV, status:RELEASING, sort:POPULARITY_DESC";
                    }

                    break;

                case Section.CAT_UPCOMING:

                    if(safeMode){
                        filter = "status:NOT_YET_RELEASED, isAdult:false, genre_not_in:[\"Ecchi\",\"Hentai\"], sort:POPULARITY_DESC";
                    }else{
                        filter = "status:NOT_YET_RELEASED, sort:POPULARITY_DESC";
                    }

                    break;

                case Section.CAT_ECCHI:

                    if(!contentFilter.equals("all")){
                        filter = "genre_in:[\"Ecchi\"], sort:POPULARITY_DESC";
                    }else{
                        filter = "id:-1";
                    }

                    break;

                case Section.CAT_ADULT:

                    if(contentFilter.equals("18")){
                        filter = "isAdult:true, sort:POPULARITY_DESC";
                    }else{
                        filter = "id:-1";
                    }

                    break;
            }

            String query =
                    "query {" +
                            " Page(page:1, perPage:30) {" +
                            "  media(type:ANIME,"+filter+") {" +

                            "   id " +
                            "   title { romaji english native } " +
                            "   description(asHtml:false) " +
                            "   format " +
                            "   season " +
                            "   seasonYear " +
                            "   duration " +
                            "   updatedAt " +
                            "   episodes " +
                            "   nextAiringEpisode { episode  airingAt} " +
                            "   status " +
                            "   averageScore " +
                            "   genres " +
                            "   coverImage { large extraLarge } " +
                            "   trailer { id site thumbnail } " +
                            "   studios(isMain:true){ nodes{ name } } " +
                            "   staff(perPage:10){ nodes{ name{full} primaryOccupations } } " +

                            "  }" +
                            " }" +
                            "}";

            JSONObject body = new JSONObject();
            body.put("query",query);

            JsonObjectRequest request =
                    new JsonObjectRequest(
                            Request.Method.POST,
                            url,
                            body,

                            response -> {

                                try{

                                    JSONArray media = response
                                            .getJSONObject("data")
                                            .getJSONObject("Page")
                                            .getJSONArray("media");

                                    list.clear();

                                    for(int i=0;i<media.length();i++){

                                        JSONObject obj = media.getJSONObject(i);
                                        Anime anime = AnimeParser.parse(obj,getContext());

                                        if(anime == null) continue;

                                        // chỉ tab "Anime mới" mới lọc episode
                                        if(section.equals(Section.CAT_NEW)){
                                            if(anime.getNextEpisode() > 0){
                                                list.add(anime);
                                            }
                                        }else{
                                            list.add(anime);
                                        }


                                    }

                                    adapter.notifyDataSetChanged();

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
}