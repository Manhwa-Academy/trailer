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

public class MoviesFragment extends Fragment {
    private static final String TAG = "MOVIES_DEBUG";
    ViewPager2 bannerSlider;
    LinearLayout dotsLayout;
    RecyclerView recyclerTopRated;
    RecyclerView recyclerPopular;
    RecyclerView recyclerUpcoming;
    RecyclerView recyclerAdultMovies;
    TextView txtAdultMovies;
    EditText edtSearch;
    ImageView btnSearch;
    ImageView btnClearSearch;
    List<Anime> topRatedList = new ArrayList<>();
    List<Anime> popularList = new ArrayList<>();
    List<Anime> upcomingList = new ArrayList<>();
    List<Anime> adultMoviesList = new ArrayList<>();
    AnimeAdapter topRatedAdapter;
    AnimeAdapter popularAdapter;
    AnimeAdapter upcomingAdapter;
    AnimeAdapter adultMoviesAdapter;
    RecyclerView recyclerEcchiMovies;
    TextView txtEcchiMovies;
    List<Anime> ecchiMoviesList = new ArrayList<>();
    AnimeAdapter ecchiMoviesAdapter;
    BannerManager bannerManager;

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

        bannerManager = new BannerManager(bannerSlider, dotsLayout);
        bannerManager.setup(getContext());

        recyclerTopRated = view.findViewById(R.id.recyclerTopRated);
        recyclerPopular = view.findViewById(R.id.recyclerPopular);
        recyclerUpcoming = view.findViewById(R.id.recyclerUpcoming);
        recyclerEcchiMovies = view.findViewById(R.id.recyclerEcchiMovies);
        txtEcchiMovies = view.findViewById(R.id.txtEcchiMovies);

        recyclerEcchiMovies.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false)
        );

        ecchiMoviesAdapter = new AnimeAdapter(getContext(), ecchiMoviesList, R.layout.item_anime);

        recyclerEcchiMovies.setAdapter(ecchiMoviesAdapter);
        recyclerAdultMovies = view.findViewById(R.id.recyclerAdultMovies);
        txtAdultMovies = view.findViewById(R.id.txtAdultMovies);

        recyclerTopRated.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        recyclerPopular.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        recyclerUpcoming.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        recyclerAdultMovies.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        topRatedAdapter = new AnimeAdapter(getContext(), topRatedList, R.layout.item_anime);
        popularAdapter = new AnimeAdapter(getContext(), popularList, R.layout.item_anime);
        upcomingAdapter = new AnimeAdapter(getContext(), upcomingList, R.layout.item_anime);
        adultMoviesAdapter = new AnimeAdapter(getContext(), adultMoviesList, R.layout.item_anime);

        recyclerTopRated.setAdapter(topRatedAdapter);
        recyclerPopular.setAdapter(popularAdapter);
        recyclerUpcoming.setAdapter(upcomingAdapter);
        recyclerAdultMovies.setAdapter(adultMoviesAdapter);

        setupSearch();

        checkFilterUI();

        if(topRatedList.isEmpty()){
            loadAllMovies();
        }

        return view;
    }

    // ================= FILTER UI =================

    private void checkFilterUI(){

        String filter = AppSettings.getContentFilter(getContext());

        if(filter.equals("18")){

            recyclerTopRated.setVisibility(View.GONE);
            recyclerPopular.setVisibility(View.GONE);
            recyclerUpcoming.setVisibility(View.GONE);
            recyclerEcchiMovies.setVisibility(View.GONE);

            txtAdultMovies.setVisibility(View.VISIBLE);
            recyclerAdultMovies.setVisibility(View.VISIBLE);

        }
        else if(filter.equals("16")){

            txtEcchiMovies.setVisibility(View.VISIBLE);
            recyclerEcchiMovies.setVisibility(View.VISIBLE);

            txtAdultMovies.setVisibility(View.GONE);
            recyclerAdultMovies.setVisibility(View.GONE);

        }
        else{

            txtEcchiMovies.setVisibility(View.GONE);
            recyclerEcchiMovies.setVisibility(View.GONE);

            txtAdultMovies.setVisibility(View.GONE);
            recyclerAdultMovies.setVisibility(View.GONE);
        }
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
                        "  media(type:ANIME, format:MOVIE, sort:SCORE_DESC) {" +
                        "   id title { romaji english native }" +
                        "   format season seasonYear duration" +
                        "   averageScore description(asHtml:false) genres isAdult" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +

                        "   staff(perPage:5) {" +
                        "     nodes {" +
                        "       name { full }" +
                        "       primaryOccupations" +
                        "     }" +
                        "   }" +

                        "   trailer { id site }" +
                        "  }" +
                        " }" +

                        " popular: Page(page:1, perPage:10) {" +
                        "  media(type:ANIME, format:MOVIE, sort:POPULARITY_DESC) {" +
                        "   id title { romaji english native }" +
                        "   format season seasonYear duration" +
                        "   averageScore description(asHtml:false) genres isAdult" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +

                        "   staff(perPage:5) {" +
                        "     nodes {" +
                        "       name { full }" +
                        "       primaryOccupations" +
                        "     }" +
                        "   }" +

                        "   trailer { id site }" +
                        "  }" +
                        " }" +

                        " upcoming: Page(page:1, perPage:10) {" +
                        "  media(type:ANIME, format:MOVIE, status:NOT_YET_RELEASED, sort:POPULARITY_DESC) {" +
                        "   id title { romaji english native }" +
                        "   format season seasonYear duration" +
                        "   averageScore description(asHtml:false) genres isAdult" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +

                        "   staff(perPage:5) {" +
                        "     nodes {" +
                        "       name { full }" +
                        "       primaryOccupations" +
                        "     }" +
                        "   }" +

                        "   trailer { id site }" +
                        "  }" +
                        " }" +

                        " ecchi: Page(page:1, perPage:10) {" +
                        "  media(type:ANIME, format:MOVIE, genre_in:[\"Ecchi\"], sort:POPULARITY_DESC) {" +
                        "   id title { romaji english native }" +
                        "   format season seasonYear duration" +
                        "   averageScore description(asHtml:false) genres isAdult" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +

                        "   staff(perPage:5) {" +
                        "     nodes {" +
                        "       name { full }" +
                        "       primaryOccupations" +
                        "     }" +
                        "   }" +

                        "   trailer { id site }" +
                        "  }" +
                        " }" +

                        " adult: Page(page:1, perPage:10) {" +
                        "  media(type:ANIME, format:MOVIE, isAdult:true, sort:POPULARITY_DESC) {" +
                        "   id title { romaji english native }" +
                        "   format season seasonYear duration" +
                        "   averageScore description(asHtml:false) genres isAdult" +
                        "   coverImage { large }" +
                        "   studios { nodes { name } }" +

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

                                    String filter = AppSettings.getContentFilter(getContext());
                                    if(filter.equals("16")){

                                        parseAnime(
                                                data.getJSONObject("ecchi").getJSONArray("media"),
                                                ecchiMoviesList,
                                                ecchiMoviesAdapter
                                        );

                                    }
                                    if(filter.equals("18")){

                                        parseAnime(
                                                data.getJSONObject("adult").getJSONArray("media"),
                                                adultMoviesList,
                                                adultMoviesAdapter
                                        );
                                    }

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

            Anime anime = AnimeParser.parse(obj, getContext());

            if(anime != null){
                list.add(anime);
            }
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