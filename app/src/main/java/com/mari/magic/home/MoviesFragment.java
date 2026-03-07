package com.mari.magic.home;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import android.content.Intent;
import com.mari.magic.utils.TextUtils;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.mari.magic.utils.AnimeParser;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mari.magic.R;
import com.mari.magic.network.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoviesFragment extends Fragment {

    private static final String TAG = "MOVIES_DEBUG";

    ViewPager2 bannerSlider;
    LinearLayout dotsLayout;

    RecyclerView recyclerTopRated;
    RecyclerView recyclerPopular;
    RecyclerView recyclerUpcoming;

    EditText edtSearch;
    ImageView btnSearch;
    ImageView btnClearSearch;

    List<Banner> bannerList;

    List<Anime> topRatedList = new ArrayList<>();
    List<Anime> popularList = new ArrayList<>();
    List<Anime> upcomingList = new ArrayList<>();

    AnimeAdapter topRatedAdapter;
    AnimeAdapter popularAdapter;
    AnimeAdapter upcomingAdapter;

    Handler handler = new Handler();
    Runnable runnable;

    public MoviesFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_movies, container, false);

        edtSearch = view.findViewById(R.id.edtSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);

        bannerSlider = view.findViewById(R.id.bannerSlider);
        dotsLayout = view.findViewById(R.id.dotsLayout);

        recyclerTopRated = view.findViewById(R.id.recyclerTopRated);
        recyclerPopular = view.findViewById(R.id.recyclerPopular);
        recyclerUpcoming = view.findViewById(R.id.recyclerUpcoming);

        recyclerTopRated.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        recyclerPopular.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        recyclerUpcoming.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        topRatedAdapter = new AnimeAdapter(getContext(), topRatedList, R.layout.item_anime);
        popularAdapter = new AnimeAdapter(getContext(), popularList, R.layout.item_anime);
        upcomingAdapter = new AnimeAdapter(getContext(), upcomingList, R.layout.item_anime);

        recyclerTopRated.setAdapter(topRatedAdapter);
        recyclerPopular.setAdapter(popularAdapter);
        recyclerUpcoming.setAdapter(upcomingAdapter);

        setupSearch();
        setupBanner();

        if(topRatedList.isEmpty()){
            loadAllMovies();
        }

        return view;
    }

    // ================= SEARCH =================

    private void setupSearch(){

        edtSearch.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s,int start,int count,int after){}

            public void onTextChanged(CharSequence s,int start,int before,int count){

                if(s.length()>0)
                    btnClearSearch.setVisibility(View.VISIBLE);
                else
                    btnClearSearch.setVisibility(View.GONE);
            }

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

    private void loadAllMovies(){

        String url = "https://graphql.anilist.co";
        String query =
                "query {" +

                        " topRated: Page(page:1, perPage:10) {" +
                        " media(type:ANIME, format:MOVIE, sort:SCORE_DESC) {" +
                        " id " +
                        " title { romaji english native }" +
                        " format" +
                        " season" +
                        " seasonYear" +
                        " duration" +
                        " averageScore" +
                        " description(asHtml:false)" +
                        " genres" +
                        " isAdult " + // ⭐ thêm
                        " coverImage { large }" +
                        " studios { nodes { name } }" +
                        " staff(perPage:5) { nodes { name { full } primaryOccupations } }" +
                        " trailer { id site } } }" +

                        " popular: Page(page:1, perPage:10) {" +
                        " media(type:ANIME, format:MOVIE, sort:POPULARITY_DESC) {" +
                        " id " +
                        " title { romaji english native }" +
                        " format" +
                        " season" +
                        " seasonYear" +
                        " duration" +
                        " averageScore" +
                        " description(asHtml:false)" +
                        " genres" +
                        " isAdult " + // ⭐ thêm
                        " coverImage { large }" +
                        " studios { nodes { name } }" +
                        " staff(perPage:5) { nodes { name { full } primaryOccupations } }" +
                        " trailer { id site } } }" +

                        " upcoming: Page(page:1, perPage:10) {" +
                        " media(type:ANIME, format:MOVIE, status:NOT_YET_RELEASED, sort:POPULARITY_DESC) {" +
                        " id " +
                        " title { romaji english native }" +
                        " format" +
                        " season" +
                        " seasonYear" +
                        " duration" +
                        " averageScore" +
                        " description(asHtml:false)" +
                        " genres" +
                        " isAdult " + // ⭐ thêm
                        " coverImage { large }" +
                        " studios { nodes { name } }" +
                        " staff(perPage:5) { nodes { name { full } primaryOccupations } }" +
                        " trailer { id site } } }" +

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
                                            data.getJSONObject("topRated").getJSONArray("media"),
                                            topRatedList,
                                            topRatedAdapter
                                    );

                                    parseAnime(
                                            data.getJSONObject("popular").getJSONArray("media"),
                                            popularList,
                                            popularAdapter
                                    );

                                    parseAnime(
                                            data.getJSONObject("upcoming").getJSONArray("media"),
                                            upcomingList,
                                            upcomingAdapter
                                    );

                                }catch(Exception e){
                                    Log.e(TAG,"JSON ERROR",e);
                                }

                            },

                            error -> Log.e(TAG,"API ERROR", error)
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
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            request.setShouldCache(true);

            VolleySingleton
                    .getInstance(requireContext())
                    .addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // ================= PARSE =================

    private void parseAnime(JSONArray media, List<Anime> list, AnimeAdapter adapter) throws Exception{

        list.clear();

        for(int i=0;i<media.length();i++){

            JSONObject obj = media.getJSONObject(i);

            Anime anime = AnimeParser.parse(obj);

            list.add(anime);
        }

        adapter.notifyDataSetChanged();
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

            if(current==bannerList.size()-1)
                bannerSlider.setCurrentItem(0);
            else
                bannerSlider.setCurrentItem(current+1);

            handler.postDelayed(runnable,4000);
        };

        handler.postDelayed(runnable,4000);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();

        if(handler!=null && runnable!=null)
            handler.removeCallbacks(runnable);
    }
}