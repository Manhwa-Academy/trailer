package com.mari.magic.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.text.TextWatcher;
import android.text.Editable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mari.magic.utils.HistoryManager;
import com.mari.magic.utils.AppSettings;
import com.mari.magic.adapter.SearchHistoryAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.mari.magic.R;
import com.mari.magic.adapter.AnimeAdapter;
import com.mari.magic.model.Anime;
import com.mari.magic.utils.GridSpacingItemDecoration;
import com.mari.magic.utils.AnimeParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SEARCH_DEBUG";

    RecyclerView recyclerSearch;
    RecyclerView recyclerHistory;

    TextView txtTitle;

    EditText edtSearch;
    ImageView btnSearch, btnClearSearch;

    List<Anime> searchList = new ArrayList<>();
    AnimeAdapter searchAdapter;

    List<String> historyList = new ArrayList<>();
    SearchHistoryAdapter historyAdapter;

    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        recyclerSearch = findViewById(R.id.recyclerSearch);
        recyclerHistory = findViewById(R.id.recyclerHistory);

        txtTitle = findViewById(R.id.txtTitle);

        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        GridLayoutManager layoutManager = new GridLayoutManager(this,3);
        recyclerSearch.setLayoutManager(layoutManager);

        recyclerSearch.addItemDecoration(new GridSpacingItemDecoration(3,20,true));

        searchAdapter = new AnimeAdapter(this, searchList, R.layout.item_anime_search);
        recyclerSearch.setAdapter(searchAdapter);

        // HISTORY LIST
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        loadSearchHistory();

        requestQueue = Volley.newRequestQueue(this);

        // ================= CLEAR BUTTON =================

        edtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s){}
        });

        btnClearSearch.setOnClickListener(v -> {

            edtSearch.setText("");
            txtTitle.setText(R.string.search_result);

            searchList.clear();
            searchAdapter.notifyDataSetChanged();

            loadSearchHistory();

            recyclerHistory.setVisibility(View.VISIBLE);

        });

        // ================= SEARCH BUTTON =================

        btnSearch.setOnClickListener(v -> doSearch());

        // ================= ENTER SEARCH =================

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {

            if(actionId == EditorInfo.IME_ACTION_SEARCH){

                doSearch();
                return true;
            }

            return false;
        });

        // ================= SEARCH FROM HOME =================

        String keyword = getIntent().getStringExtra("keyword");

        if(keyword != null && !keyword.isEmpty()){

            edtSearch.setText(keyword);
            doSearch();
        }
    }

    // ================= LOAD HISTORY =================

    private void loadSearchHistory(){

        historyList = HistoryManager.getSearchHistory(this);
        Log.d("HISTORY_TEST", historyList.toString());   // 👈 thêm dòng này
        historyAdapter = new SearchHistoryAdapter(historyList, keyword -> {

            edtSearch.setText(keyword);
            doSearch();

        });

        recyclerHistory.setAdapter(historyAdapter);
    }

    // ================= SEARCH FUNCTION =================

    private void doSearch(){

        String keyword = edtSearch.getText().toString().trim();

        if(keyword.isEmpty()) return;

        if(AppSettings.isSearchHistoryEnabled(this)){
            HistoryManager.saveSearch(this, keyword);
            Log.d("SEARCH_HISTORY","Saved: " + keyword);

            loadSearchHistory();
            recyclerHistory.setVisibility(View.VISIBLE);
        }

        edtSearch.setText("");

        searchList.clear();
        searchAdapter.notifyDataSetChanged();

        txtTitle.setText(getString(R.string.search_result, keyword));

        searchAnime(keyword);
    }
    // ================= API SEARCH =================

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
                        "   updatedAt " +
                        "   isAdult" +
                        "   format" +
                        "   season" +
                        "   seasonYear" +
                        "   duration" +
                        "   episodes" +
                        "   nextAiringEpisode { episode airingAt }" +
                        "   status " +
                        "   studios { nodes { name } }" +
                        "   staff(perPage:5) { nodes { name { full } primaryOccupations } }" +
                        "   trailer { id site }" +
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

                                        Anime anime = AnimeParser.parse(obj, this);

                                        if(anime == null) continue;

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

                                        txtTitle.setText(getString(R.string.search_no_result, keyword));
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
}