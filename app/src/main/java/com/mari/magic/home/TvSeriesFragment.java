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

    List<Banner> bannerList;

    List<Anime> topRatedList = new ArrayList<>();
    List<Anime> popularList = new ArrayList<>();
    List<Anime> releasingList = new ArrayList<>();

    AnimeAdapter topRatedAdapter;
    AnimeAdapter popularAdapter;
    AnimeAdapter releasingAdapter;

    Handler handler = new Handler();
    Runnable runnable;

    public TvSeriesFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tv, container, false);

        edtSearch = view.findViewById(R.id.edtSearch);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);

        bannerSlider = view.findViewById(R.id.bannerSlider);
        dotsLayout = view.findViewById(R.id.dotsLayout);

        recyclerTopRatedSeries = view.findViewById(R.id.recyclerTopRatedSeries);
        recyclerPopularSeries = view.findViewById(R.id.recyclerPopularSeries);
        recyclerReleasingSeries = view.findViewById(R.id.recyclerUpcomingSeries);

        recyclerTopRatedSeries.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        recyclerPopularSeries.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        recyclerReleasingSeries.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL,false));

        topRatedAdapter = new AnimeAdapter(getContext(), topRatedList, R.layout.item_anime);
        popularAdapter = new AnimeAdapter(getContext(), popularList, R.layout.item_anime);
        releasingAdapter = new AnimeAdapter(getContext(), releasingList, R.layout.item_anime);

        recyclerTopRatedSeries.setAdapter(topRatedAdapter);
        recyclerPopularSeries.setAdapter(popularAdapter);
        recyclerReleasingSeries.setAdapter(releasingAdapter);

        setupSearch();
        setupBanner();

        if(topRatedList.isEmpty()){
            loadAllSeries();
        }

        return view;
    }

    // ================= SEARCH =================

    private void setupSearch(){

        edtSearch.addTextChangedListener(new TextWatcher(){

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

    // ================= GRAPHQL API =================

    private void loadAllSeries(){

        String url = "https://graphql.anilist.co";

        String query =
                "query {" +

                        " topRated: Page(page:1, perPage:10) {" +
                        " media(type:ANIME, format:TV, sort:SCORE_DESC, status:FINISHED) {" +
                        " title { romaji english native }" +
                        " format" +
                        " season" +
                        " seasonYear" +
                        " duration" +
                        " coverImage { large }" +
                        " averageScore" +
                        " description(asHtml:false)" +
                        " genres" +
                        " studios { nodes { name } }" +
                        " staff(perPage:5) { nodes { name { full } primaryOccupations } }" +
                        " trailer { id site }" +
                        " } }" +

                        " popular: Page(page:1, perPage:10) {" +
                        " media(type:ANIME, format:TV, sort:POPULARITY_DESC) {" +
                        " title { romaji english native }" +
                        " format" +
                        " season" +
                        " seasonYear" +
                        " duration" +
                        " coverImage { large }" +
                        " averageScore" +
                        " description(asHtml:false)" +
                        " genres" +
                        " studios { nodes { name } }" +
                        " staff(perPage:5) { nodes { name { full } primaryOccupations } }" +
                        " trailer { id site }" +
                        " } }" +

                        " releasing: Page(page:1, perPage:10) {" +
                        " media(type:ANIME, format:TV, status:RELEASING, sort:POPULARITY_DESC) {" +
                        " title { romaji english native }" +
                        " format" +
                        " season" +
                        " seasonYear" +
                        " duration" +
                        " coverImage { large }" +
                        " averageScore" +
                        " description(asHtml:false)" +
                        " genres" +
                        " studios { nodes { name } }" +
                        " staff(perPage:5) { nodes { name { full } primaryOccupations } }" +
                        " trailer { id site }" +
                        " } }" +

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
                                            data.getJSONObject("topRated")
                                                    .getJSONArray("media"),
                                            topRatedList,
                                            topRatedAdapter
                                    );

                                    parseAnime(
                                            data.getJSONObject("popular")
                                                    .getJSONArray("media"),
                                            popularList,
                                            popularAdapter
                                    );

                                    parseAnime(
                                            data.getJSONObject("releasing")
                                                    .getJSONArray("media"),
                                            releasingList,
                                            releasingAdapter
                                    );

                                }catch(Exception e){
                                    Log.e(TAG,"JSON ERROR",e);
                                }

                            },

                            error -> {

                                if(error.networkResponse != null){

                                    int status = error.networkResponse.statusCode;

                                    String bodyError =
                                            new String(error.networkResponse.data);

                                    Log.e(TAG,"STATUS: "+status);
                                    Log.e(TAG,"BODY: "+bodyError);
                                }

                                Log.e(TAG,"API ERROR",error);
                            }

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

    private void parseAnime(JSONArray media,List<Anime> list,AnimeAdapter adapter) throws Exception{

        list.clear();

        for(int i=0;i<media.length();i++){

            JSONObject anime = media.getJSONObject(i);

            JSONObject titleObj = anime.getJSONObject("title");

            String romaji = TextUtils.clean(titleObj.optString("romaji"));
            String english = TextUtils.clean(titleObj.optString("english"));
            String nativeTitle = TextUtils.clean(titleObj.optString("native"));

            String title = TextUtils.bestTitle(english, romaji, nativeTitle);

            String poster = anime.getJSONObject("coverImage")
                    .optString("large","");

            double rating = anime.optDouble("averageScore",0);

            String description = anime.optString("description","");
// ===== FORMAT =====
            String format = anime.optString("format","");

// ===== SEASON =====
            String season = anime.optString("season","");
            int seasonYear = anime.optInt("seasonYear",0);

            if(!season.isEmpty())
                season = season + " " + seasonYear;

// ===== DURATION =====
            int duration = anime.optInt("duration",0);

// ===== STUDIO =====
            String studio = "";

            JSONObject studios = anime.optJSONObject("studios");

            if(studios != null){

                JSONArray nodes = studios.optJSONArray("nodes");

                if(nodes != null && nodes.length() > 0){

                    studio = nodes.getJSONObject(0)
                            .optString("name","");
                }
            }

// ===== DIRECTOR =====
            String director = "";

            JSONObject staff = anime.optJSONObject("staff");

            if(staff != null){

                JSONArray nodes = staff.optJSONArray("nodes");

                if(nodes != null){

                    for(int s=0;s<nodes.length();s++){

                        JSONObject person = nodes.getJSONObject(s);

                        JSONArray jobs = person.optJSONArray("primaryOccupations");

                        if(jobs != null){

                            for(int j=0;j<jobs.length();j++){

                                if(jobs.getString(j).equalsIgnoreCase("Director")){

                                    director = person
                                            .getJSONObject("name")
                                            .optString("full","");

                                    break;
                                }
                            }
                        }
                    }
                }
            }
            String trailer="";

            if(anime.has("trailer") && !anime.isNull("trailer")){

                JSONObject t = anime.getJSONObject("trailer");

                if("youtube".equalsIgnoreCase(t.optString("site","")))
                    trailer = t.optString("id","");
            }

            Anime animeObj = new Anime(title,poster,trailer,rating);

            animeObj.setFormat(format);
            animeObj.setSeason(season);
            animeObj.setDuration(duration);
            animeObj.setStudio(studio);
            animeObj.setDirector(director);
            animeObj.setDescription(description);
            animeObj.setEnglishTitle(english);
            animeObj.setRomajiTitle(romaji);
            animeObj.setNativeTitle(nativeTitle);
            list.add(animeObj);
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

            if(current == bannerList.size()-1)
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