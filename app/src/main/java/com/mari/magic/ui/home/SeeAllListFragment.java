package com.mari.magic.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

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
import com.mari.magic.ui.component.PaginationView;
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

    PaginationView paginationBottom;
    ProgressBar loading;

    List<Anime> list = new ArrayList<>();

    String section;

    int currentPage = 1;
    int totalPages = 1;

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
        paginationBottom = view.findViewById(R.id.paginationBottom);
        loading = view.findViewById(R.id.loading);

        recycler.setLayoutManager(new GridLayoutManager(getContext(),3));
        recycler.addItemDecoration(new GridSpacingItemDecoration(3,20,true));

        adapter = new AnimeAdapter(getContext(), list, R.layout.item_anime_genre);
        recycler.setAdapter(adapter);

        if(getArguments() != null){
            section = getArguments().getString("section", Section.CAT_MOVIES);
        }

        paginationBottom.setOnPageChangeListener(page -> {

            currentPage = page;

            loadAnime();

            recycler.scrollToPosition(0);
        });

        loadAnime();

        return view;
    }

    private void loadAnime(){

        loading.setVisibility(View.VISIBLE);

        String contentFilter = AppSettings.getContentFilter(getContext());
        String url = "https://graphql.anilist.co";

        boolean safeMode = contentFilter.equals("all"); // an toàn
        boolean ecchiMode = contentFilter.equals("16"); // khơi gợi
        boolean adultMode = contentFilter.equals("18"); // mèo đen

        try{

            String filter = "";
            String mediaType = "ANIME";
// ===== base filter =====
            switch(section){

                case Section.CAT_NEW:
                    filter = "status:RELEASING, episodes_greater:0";
                    filter += ", sort:UPDATED_AT_DESC";
                    break;

                case Section.CAT_MOVIES:
                    filter = "format:MOVIE";
                    filter += ", sort:POPULARITY_DESC";
                    break;

                case Section.CAT_SERIES:
                    filter = "format:TV";
                    filter += ", sort:POPULARITY_DESC";
                    break;

                case Section.CAT_TOP_TV:
                    filter = "format:TV";
                    filter += ", sort:SCORE_DESC";
                    break;

                case Section.CAT_RELEASING:
                    filter = "format:TV, status:RELEASING";
                    filter += ", sort:POPULARITY_DESC";
                    break;

                case Section.CAT_UPCOMING:
                    filter = "status:NOT_YET_RELEASED";
                    filter += ", sort:POPULARITY_DESC";
                    break;
                case Section.CAT_TOP_MANGA:

                    mediaType = "MANGA";
                    filter = "sort:SCORE_DESC";

                    break;

                case Section.CAT_TRENDING_MANGA:

                    mediaType = "MANGA";
                    filter = "sort:TRENDING_DESC";

                    break;
                case Section.CAT_TOP_NOVEL:
                    mediaType = "MANGA";
                    filter = "format:NOVEL, sort:SCORE_DESC";
                    break;

                case Section.CAT_TRENDING_NOVEL:
                    mediaType = "MANGA";
                    filter = "format:NOVEL, sort:TRENDING_DESC";
                    break;

                case Section.CAT_NEW_NOVEL:
                    mediaType = "MANGA";
                    filter = "format:NOVEL, sort:START_DATE_DESC";
                    break;
                case Section.CAT_ECCHI:

                    filter = "genre:\"Ecchi\"";
                    filter += ", sort:POPULARITY_DESC";

                    if(safeMode){
                        filter += ", isAdult:false";
                    }

                    break;
                case Section.CAT_ADULT:

                    filter = "genre:\"Hentai\"";
                    filter += ", sort:POPULARITY_DESC";

                    break;
            }

// ===== content filter =====
            if(safeMode){

                filter += ", isAdult:false, genre_not_in:[\"Ecchi\",\"Hentai\"]";

            }
            else if(ecchiMode){

                filter += ", isAdult:false, genre_not_in:[\"Hentai\"]";

            }
// adultMode → không thêm filter
            String query =
                    "query {" +
                            " Page(page:"+currentPage+", perPage:30) {" +
                            " pageInfo { currentPage hasNextPage } " +
                            " media(type:"+mediaType+","+filter+") {" +

                            " id " +
                            " title { romaji english native } " +
                            " description(asHtml:false) " +
                            " format " +
                            " season " +
                            " seasonYear " +
                            " duration " +
                            " updatedAt " +
                            " episodes " +
                            " chapters " +
                            " volumes " +
                            " nextAiringEpisode { episode airingAt } " +
                            " status " +
                            " averageScore " +
                            " genres " +
                            " coverImage { large extraLarge } " +
                            " trailer { id site thumbnail } " +
                            " studios(isMain:true){ nodes{ name } } " +
                            " staff(perPage:10){edges{role node{name{full}}}} " +

                            " }" +
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

                                    JSONObject page = response
                                            .getJSONObject("data")
                                            .getJSONObject("Page");

                                    JSONArray media = page.getJSONArray("media");

                                    JSONObject pageInfo = page.getJSONObject("pageInfo");

                                    boolean hasNext = pageInfo.getBoolean("hasNextPage");

                                    if(hasNext){
                                        totalPages = currentPage + 1;
                                    }else{
                                        totalPages = currentPage;
                                    }

                                    list.clear();

                                    for(int i=0;i<media.length();i++){

                                        JSONObject obj = media.getJSONObject(i);
                                        Anime anime = AnimeParser.parse(obj,getContext());

                                        if(anime != null){
                                            list.add(anime);
                                        }
                                    }

                                    adapter.notifyDataSetChanged();

                                    paginationBottom.setPages(currentPage,totalPages);

                                }catch(Exception e){
                                    Log.e(TAG,"JSON ERROR",e);
                                }

                                loading.setVisibility(View.GONE);
                            },

                            error -> {

                                Log.e(TAG,"API ERROR "+error);
                                loading.setVisibility(View.GONE);

                            }
                    );

            VolleySingleton
                    .getInstance(requireContext())
                    .addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}