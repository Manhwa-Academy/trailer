package com.mari.magic.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.text.TextWatcher;
import android.text.Editable;
import android.content.Intent;

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

public class TvSeriesFragment extends Fragment {

    private static final String TAG = "TV_DEBUG";
    ViewPager2 bannerSlider;
    LinearLayout dotsLayout;
    EditText edtSearch;
    ImageView btnSearch;
    ImageView btnClearSearch;

    RecyclerView recyclerTopRatedSeries;
    RecyclerView recyclerPopularSeries;
    RecyclerView recyclerReleasingSeries;
    RecyclerView recyclerAdultSeries;
    TextView txtAdultSeries;
    List<Anime> topRatedList = new ArrayList<>();
    List<Anime> popularList = new ArrayList<>();
    List<Anime> releasingList = new ArrayList<>();
    List<Anime> adultList = new ArrayList<>();
    RecyclerView recyclerEcchiSeries;
    TextView txtEcchiSeries;
    List<Anime> ecchiList = new ArrayList<>();
    AnimeAdapter ecchiAdapter;
    AnimeAdapter topRatedAdapter;
    AnimeAdapter popularAdapter;
    AnimeAdapter releasingAdapter;
    AnimeAdapter adultAdapter;
    BannerManager bannerManager;

    public TvSeriesFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_tv, container, false);

        edtSearch = view.findViewById(R.id.edtSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);

        bannerSlider = view.findViewById(R.id.bannerSlider);
        dotsLayout = view.findViewById(R.id.dotsLayout);

        recyclerTopRatedSeries = view.findViewById(R.id.recyclerTopRatedSeries);
        recyclerPopularSeries = view.findViewById(R.id.recyclerPopularSeries);
        recyclerReleasingSeries = view.findViewById(R.id.recyclerUpcomingSeries);
        bannerManager = new BannerManager(bannerSlider,dotsLayout);
        bannerManager.setup(getContext());
        recyclerAdultSeries = view.findViewById(R.id.recyclerAdultSeries);
        txtAdultSeries = view.findViewById(R.id.txtAdultSeries);

        recyclerTopRatedSeries.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
        recyclerPopularSeries.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
        recyclerReleasingSeries.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));
        recyclerAdultSeries.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.HORIZONTAL,false));

        topRatedAdapter = new AnimeAdapter(getContext(),topRatedList,R.layout.item_anime);
        popularAdapter = new AnimeAdapter(getContext(),popularList,R.layout.item_anime);
        releasingAdapter = new AnimeAdapter(getContext(),releasingList,R.layout.item_anime);
        adultAdapter = new AnimeAdapter(getContext(),adultList,R.layout.item_anime);
        recyclerEcchiSeries = view.findViewById(R.id.recyclerEcchiSeries);
        txtEcchiSeries = view.findViewById(R.id.txtEcchiSeries);
        recyclerEcchiSeries.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        ecchiAdapter = new AnimeAdapter(getContext(), ecchiList, R.layout.item_anime);

        recyclerEcchiSeries.setAdapter(ecchiAdapter);
        recyclerTopRatedSeries.setAdapter(topRatedAdapter);
        recyclerPopularSeries.setAdapter(popularAdapter);
        recyclerReleasingSeries.setAdapter(releasingAdapter);
        recyclerAdultSeries.setAdapter(adultAdapter);

        setupSearch();

        checkFilterUI();

        loadAllSeries();

        return view;
    }

    // ================= FILTER =================

    private void checkFilterUI(){

        String filter = AppSettings.getContentFilter(getContext());

        if(filter.equals("18")){

            recyclerTopRatedSeries.setVisibility(View.GONE);
            recyclerPopularSeries.setVisibility(View.GONE);
            recyclerReleasingSeries.setVisibility(View.GONE);

            txtAdultSeries.setVisibility(View.VISIBLE);
            recyclerAdultSeries.setVisibility(View.VISIBLE);

        }    else if(filter.equals("16")){

            // 16+ hiển thị tất cả + ecchi
            recyclerTopRatedSeries.setVisibility(View.VISIBLE);
            recyclerPopularSeries.setVisibility(View.VISIBLE);
            recyclerReleasingSeries.setVisibility(View.VISIBLE);

            txtEcchiSeries.setVisibility(View.VISIBLE);
            recyclerEcchiSeries.setVisibility(View.VISIBLE);

            txtAdultSeries.setVisibility(View.GONE);
            recyclerAdultSeries.setVisibility(View.GONE);

        }
        else{

            txtEcchiSeries.setVisibility(View.GONE);
            recyclerEcchiSeries.setVisibility(View.GONE);

            txtAdultSeries.setVisibility(View.GONE);
            recyclerAdultSeries.setVisibility(View.GONE);
        }
    }

    // ================= SEARCH =================

    private void setupSearch(){

        edtSearch.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){

                if(s.length() > 0)
                    btnClearSearch.setVisibility(View.VISIBLE);
                else
                    btnClearSearch.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s){}
        });

        btnClearSearch.setOnClickListener(v -> edtSearch.setText(""));

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
    }
    // ================= API =================

    private void loadAllSeries(){

        String url="https://graphql.anilist.co";

        String query =
                "query {" +

                        "topRated: Page(page:1,perPage:10){" +
                        "media(type:ANIME,format:TV,sort:SCORE_DESC){" +
                        "id title{romaji english native} format season seasonYear duration " +
                        "averageScore description(asHtml:false) genres isAdult " +
                        "coverImage{large} studios{nodes{name}} " +

                        "staff(perPage:5){nodes{name{full} primaryOccupations}} " +

                        "trailer{id site}" +
                        "}}"+

                        "popular: Page(page:1,perPage:10){" +
                        "media(type:ANIME,format:TV,sort:POPULARITY_DESC){" +
                        "id title{romaji english native} format season seasonYear duration " +
                        "averageScore description(asHtml:false) genres isAdult " +
                        "coverImage{large} studios{nodes{name}} " +

                        "staff(perPage:5){nodes{name{full} primaryOccupations}} " +

                        "trailer{id site}" +
                        "}}"+

                        "releasing: Page(page:1,perPage:10){" +
                        "media(type:ANIME,format:TV,status:RELEASING,sort:POPULARITY_DESC){" +
                        "id title{romaji english native} format season seasonYear duration " +
                        "averageScore description(asHtml:false) genres isAdult " +
                        "coverImage{large} studios{nodes{name}} " +

                        "staff(perPage:5){nodes{name{full} primaryOccupations}} " +

                        "trailer{id site}" +
                        "}}"+

                        "ecchi: Page(page:1,perPage:10){" +
                        "media(type:ANIME,format:TV,genre_in:[\"Ecchi\"],sort:POPULARITY_DESC){" +
                        "id title{romaji english native} format season seasonYear duration " +
                        "averageScore description(asHtml:false) genres isAdult " +
                        "coverImage{large} studios{nodes{name}} " +

                        "staff(perPage:5){nodes{name{full} primaryOccupations}} " +

                        "trailer{id site}" +
                        "}}"+

                        "adult: Page(page:1,perPage:10){" +
                        "media(type:ANIME,format:TV,sort:POPULARITY_DESC,isAdult:true){" +
                        "id title{romaji english native} format season seasonYear duration " +
                        "averageScore description(asHtml:false) genres isAdult " +
                        "coverImage{large} studios{nodes{name}} " +

                        "staff(perPage:5){nodes{name{full} primaryOccupations}} " +

                        "trailer{id site}" +
                        "}}}";

        try{

            JSONObject body=new JSONObject();
            body.put("query",query);

            JsonObjectRequest request=new JsonObjectRequest(
                    Request.Method.POST,url,body,

                    response->{

                        try{

                            JSONObject data=response.getJSONObject("data");

                            parseAnime(data.getJSONObject("topRated").getJSONArray("media"),topRatedList,topRatedAdapter);
                            parseAnime(data.getJSONObject("popular").getJSONArray("media"),popularList,popularAdapter);
                            parseAnime(data.getJSONObject("releasing").getJSONArray("media"),releasingList,releasingAdapter);
                            // ⭐ LẤY FILTER
                            String filter = AppSettings.getContentFilter(getContext());

                            // ⭐ 16+
                            if(filter.equals("16")){

                                parseAnime(
                                        data.getJSONObject("ecchi").getJSONArray("media"),
                                        ecchiList,
                                        ecchiAdapter
                                );
                            }

                            // ⭐ 18+
                            if(filter.equals("18")){

                                parseAnime(
                                        data.getJSONObject("adult").getJSONArray("media"),
                                        adultList,
                                        adultAdapter
                                );
                            }

                        }catch(Exception e){
                            Log.e(TAG,"JSON ERROR",e);
                        }

                    },

                    error->Log.e(TAG,"API ERROR",error)
            ){

                @Override
                public Map<String,String> getHeaders(){

                    Map<String,String> headers=new HashMap<>();

                    headers.put("Content-Type","application/json");
                    headers.put("Accept","application/json");
                    headers.put("User-Agent","MagicAnimeApp");

                    return headers;
                }
            };

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

    private void parseAnime(JSONArray media,List<Anime> list,AnimeAdapter adapter) throws Exception{

        list.clear();

        for(int i=0;i<media.length();i++){

            JSONObject obj=media.getJSONObject(i);

            Anime anime=AnimeParser.parse(obj,getContext());

            if(anime!=null)
                list.add(anime);
        }

        adapter.notifyDataSetChanged();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(bannerManager != null){
            bannerManager.stop();
        }
    }
}