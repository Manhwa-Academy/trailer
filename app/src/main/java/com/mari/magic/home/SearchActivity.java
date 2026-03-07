package com.mari.magic.home;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.text.TextWatcher;
import android.text.Editable;
import com.mari.magic.utils.TextUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mari.magic.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SEARCH_DEBUG";

    RecyclerView recyclerSearch;
    TextView txtTitle;

    EditText edtSearch;
    ImageView btnSearch, btnClearSearch;

    List<Anime> searchList = new ArrayList<>();
    AnimeAdapter searchAdapter;

    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        recyclerSearch = findViewById(R.id.recyclerSearch);
        txtTitle = findViewById(R.id.txtTitle);

        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        recyclerSearch.setLayoutManager(layoutManager);

        recyclerSearch.setPadding(16,16,16,16);
        recyclerSearch.setClipToPadding(false);

        recyclerSearch.addItemDecoration(new GridSpacingItemDecoration(3,20,true));

        searchAdapter = new AnimeAdapter(this, searchList, R.layout.item_anime_search);
        recyclerSearch.setAdapter(searchAdapter);

        requestQueue = Volley.newRequestQueue(this);

        // ===== CLEAR BUTTON =====
        edtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> edtSearch.setText(""));

        // ===== SEARCH BUTTON =====
        btnSearch.setOnClickListener(v -> {

            String keyword = edtSearch.getText().toString().trim();

            if(!keyword.isEmpty()){
                txtTitle.setText("Search result for: " + keyword);
                searchAnime(keyword);
            }
        });

        // ===== KEYWORD FROM HOME =====
        String keyword = getIntent().getStringExtra("keyword");

        if(keyword != null){
            edtSearch.setText(keyword);
            txtTitle.setText("Search result for: " + keyword);
            searchAnime(keyword);
        }
    }

    private void searchAnime(String keyword){

        String url = "https://graphql.anilist.co";

        String query =
                "query ($search: String) {" +
                        " Page(page:1, perPage:30) {" +
                        "  media(search:$search, type:ANIME) {" +
                        "   title { romaji english native }" +
                        "   coverImage { large }" +
                        "   averageScore" +
                        "   description" +
                        "   genres" +
                        "   isAdult" +
                        "   format" +
                        "   season" +
                        "   duration" +
                        "   studios { nodes { name } }" +
                        "   staff(perPage:10) { nodes { name { full } } }" +
                        "   trailer { id }" +
                        "  }" +
                        " }" +
                        "}";

        try{

            JSONObject body = new JSONObject();
            body.put("query", query);

            JSONObject variables = new JSONObject();
            variables.put("search", keyword);

            body.put("variables", variables);

            JsonObjectRequest request =
                    new JsonObjectRequest(Request.Method.POST, url, body,

                            response -> {

                                try{

                                    JSONArray media = response
                                            .getJSONObject("data")
                                            .getJSONObject("Page")
                                            .getJSONArray("media");

                                    searchList.clear();

                                    String key = keyword.toLowerCase();

                                    for(int i=0;i<media.length();i++){

                                        JSONObject anime = media.getJSONObject(i);

                                        JSONObject titleObj = anime.getJSONObject("title");
                                        String romaji = TextUtils.clean(titleObj.optString("romaji"));
                                        String english = TextUtils.clean(titleObj.optString("english"));
                                        String nativeTitle = TextUtils.clean(titleObj.optString("native"));
                                        String format = anime.optString("format","");
                                        String season = anime.optString("season","");
                                        int duration = anime.optInt("duration",0);
                                        String poster = anime
                                                .getJSONObject("coverImage")
                                                .optString("large","");

                                        double rating = anime.optDouble("averageScore",0);

                                        String description = anime.optString("description","");

                                        boolean isAdult = anime.optBoolean("isAdult",false);
                                        String studio = "";

                                        JSONObject studiosObj = anime.optJSONObject("studios");

                                        if(studiosObj != null){

                                            JSONArray nodes = studiosObj.optJSONArray("nodes");

                                            if(nodes != null && nodes.length() > 0){
                                                studio = nodes.getJSONObject(0).optString("name","");
                                            }
                                        }
                                        String director = "";

                                        JSONObject staffObj = anime.optJSONObject("staff");

                                        if(staffObj != null){

                                            JSONArray nodes = staffObj.optJSONArray("nodes");

                                            if(nodes != null && nodes.length() > 0){
                                                director = nodes.getJSONObject(0)
                                                        .getJSONObject("name")
                                                        .optString("full","");
                                            }
                                        }
                                        // ===== GENRES =====
                                        String genres = "";
                                        JSONArray genresArray = anime.optJSONArray("genres");

                                        if(genresArray != null){

                                            for(int g=0; g<genresArray.length(); g++){

                                                genres += genresArray.optString(g);

                                                if(g < genresArray.length()-1)
                                                    genres += ", ";
                                            }
                                        }

                                        String trailer = "";

                                        if(anime.has("trailer") && !anime.isNull("trailer")){
                                            trailer = anime
                                                    .getJSONObject("trailer")
                                                    .optString("id","");
                                        }

                                        // ===== SEARCH MATCH =====
                                        if(romaji.toLowerCase().contains(key) ||
                                                english.toLowerCase().contains(key) ||
                                                nativeTitle.toLowerCase().contains(key)){

                                            // ⭐ fallback title
                                            String title;

                                            if(english != null && !english.equals("null") && !english.isEmpty())
                                                title = english;

                                            else if(romaji != null && !romaji.equals("null") && !romaji.isEmpty())
                                                title = romaji;

                                            else if(nativeTitle != null && !nativeTitle.equals("null") && !nativeTitle.isEmpty())
                                                title = nativeTitle;

                                            else
                                                title = "Unknown";

                                            Anime animeObj =
                                                    new Anime(title, poster, trailer, rating);

                                            animeObj.setAdult(isAdult);
                                            animeObj.setDescription(description);
                                            animeObj.setGenres(genres);
                                            animeObj.setFormat(format);
                                            animeObj.setSeason(season);
                                            animeObj.setDuration(duration);
                                            animeObj.setStudio(studio);
                                            animeObj.setDirector(director);
                                            animeObj.setEnglishTitle(english);
                                            animeObj.setRomajiTitle(romaji);
                                            animeObj.setNativeTitle(nativeTitle);

                                            searchList.add(animeObj);
                                        }
                                    }

                                    if(searchList.isEmpty()){
                                        txtTitle.setText("No result found for: " + keyword);
                                    }

                                    searchAdapter.notifyDataSetChanged();

                                }catch(Exception e){
                                    Log.e(TAG,"JSON ERROR",e);
                                }

                            },

                            error -> Log.e(TAG,"SEARCH ERROR",error)
                    );

            requestQueue.add(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // ===== GRID SPACING =====

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent,
                                   RecyclerView.State state) {

            int position = parent.getChildAdapterPosition(view);
            int column = position % spanCount;

            if (includeEdge) {

                outRect.left = spacing - column * spacing / spanCount;
                outRect.right = (column + 1) * spacing / spanCount;

                if (position < spanCount) {
                    outRect.top = spacing;
                }

                outRect.bottom = spacing;

            } else {

                outRect.left = column * spacing / spanCount;
                outRect.right = spacing - (column + 1) * spacing / spanCount;

                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }
}