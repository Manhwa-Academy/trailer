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
import com.mari.magic.utils.AnimeParser;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;
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
                        "   id" +
                        "   title { romaji english native }" +
                        "   coverImage { large }" +
                        "   averageScore" +
                        "   description" +
                        "   genres" +
                        "   isAdult" +
                        "   format" +
                        "   season" +
                        "   seasonYear" +
                        "   duration" +
                        "   studios { nodes { name } }" +
                        "   staff(perPage:5) { nodes { name { full } primaryOccupations } }" +
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

                                        JSONObject obj = media.getJSONObject(i);

                                        Anime anime = AnimeParser.parse(obj);

                                        String romaji = anime.getRomajiTitle() != null ? anime.getRomajiTitle() : "";
                                        String english = anime.getEnglishTitle() != null ? anime.getEnglishTitle() : "";
                                        String nativeTitle = anime.getNativeTitle() != null ? anime.getNativeTitle() : "";

                                        if(romaji.toLowerCase().contains(key) ||
                                                english.toLowerCase().contains(key) ||
                                                nativeTitle.toLowerCase().contains(key)){

                                            searchList.add(anime);
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