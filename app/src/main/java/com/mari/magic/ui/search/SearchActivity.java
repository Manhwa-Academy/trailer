package com.mari.magic.ui.search;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.text.TextWatcher;
import android.text.Editable;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mari.magic.ui.base.BaseFragment;
import com.mari.magic.utils.HistoryManager;
import com.mari.magic.utils.AppSettings;
import com.mari.magic.adapter.SearchHistoryAdapter;
import com.mari.magic.ui.component.PaginationView;
import com.mari.magic.R;
import com.mari.magic.adapter.AnimeAdapter;
import com.mari.magic.model.Anime;
import com.mari.magic.utils.GridSpacingItemDecoration;
import com.mari.magic.utils.AnimeParser;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.RequestQueue;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SEARCH_DEBUG";

    RecyclerView recyclerSearch, recyclerHistory;
    PaginationView paginationView;
    TextView txtTitle;
    EditText edtSearch;
    ImageView btnSearch, btnClearSearch;
    Spinner spinnerType;

    List<Anime> searchList = new ArrayList<>();
    AnimeAdapter searchAdapter;

    List<String> historyList = new ArrayList<>();
    SearchHistoryAdapter historyAdapter;

    RequestQueue requestQueue;

    int currentPage = 1;
    int totalPages = 1;
    String currentKeyword = "";
    String currentType = "ANIME";

    Handler searchHandler = new Handler(Looper.getMainLooper());
    Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        recyclerSearch = findViewById(R.id.recyclerSearch);
        recyclerHistory = findViewById(R.id.recyclerHistory);
        paginationView = findViewById(R.id.paginationBottom);
        txtTitle = findViewById(R.id.txtTitle);
        edtSearch = findViewById(R.id.edtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        spinnerType = findViewById(R.id.spinnerType); // spinner chọn type
        applyBackground();
        // spinner Type setup
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"ANIME", "MANGA", "NOVEL"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
        spinnerType.setSelection(0); // default ANIME
        spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                currentType = parent.getItemAtPosition(position).toString();
                if(!currentKeyword.isEmpty()) searchAnime(currentKeyword, currentPage, currentType);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        recyclerSearch.setLayoutManager(new GridLayoutManager(this,3));
        recyclerSearch.addItemDecoration(new GridSpacingItemDecoration(3,20,true));
        searchAdapter = new AnimeAdapter(this, searchList, R.layout.item_anime_search);
        recyclerSearch.setAdapter(searchAdapter);

        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        loadSearchHistory();

        requestQueue = Volley.newRequestQueue(this);

        paginationView.setOnPageChangeListener(page -> {
            currentPage = page;
            recyclerSearch.scrollToPosition(0);
            searchAnime(currentKeyword, currentPage, currentType);
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length()>0 ? View.VISIBLE : View.GONE);
                searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> {
                    String keyword = s.toString().trim();
                    if(keyword.length() < 2) return;

                    currentKeyword = keyword;
                    currentPage = 1;
                    recyclerHistory.setVisibility(View.GONE);
                    txtTitle.setText(getString(R.string.search_result, keyword));
                    searchAnime(keyword, currentPage, currentType);
                };
                searchHandler.postDelayed(searchRunnable,400);
            }
        });

        btnClearSearch.setOnClickListener(v -> {
            edtSearch.setText("");
            txtTitle.setText(R.string.search_result);
            searchList.clear();
            searchAdapter.notifyDataSetChanged();
            loadSearchHistory();
            recyclerHistory.setVisibility(View.VISIBLE);
        });

        btnSearch.setOnClickListener(v -> doSearch());
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                doSearch();
                return true;
            }
            return false;
        });

        String keyword = getIntent().getStringExtra("keyword");
        if(keyword != null && !keyword.isEmpty()){
            edtSearch.setText(keyword);
            doSearch();
        }
    }

    private void loadSearchHistory(){
        historyList = HistoryManager.getSearchHistory(this);
        historyAdapter = new SearchHistoryAdapter(historyList, keyword -> {
            edtSearch.setText(keyword);
            doSearch();
        });
        recyclerHistory.setAdapter(historyAdapter);
    }
    private void applyBackground() {
        View root = findViewById(R.id.rootLayout); // ✅ đúng layout của bạn

        if (root == null) return;

        String bg = AppSettings.getBackground(this);

        switch (bg){
            case "anh1":
                root.setBackgroundResource(R.drawable.anh1);
                break;
            case "anh2":
                root.setBackgroundResource(R.drawable.anh2);
                break;
            case "anh3":
                root.setBackgroundResource(R.drawable.anh3);
                break;
            case "anh4":
                root.setBackgroundResource(R.drawable.anh4);
                break;
            default:
                root.setBackgroundColor(Color.BLACK);
                break;
        }
    }
    private void doSearch(){
        String keyword = edtSearch.getText().toString().trim();
        if(keyword.isEmpty()) return;

        currentKeyword = keyword;
        currentPage = 1;

        if(AppSettings.isSearchHistoryEnabled(this)){
            HistoryManager.saveSearch(this, keyword);
        }

        edtSearch.setText("");
        searchList.clear();
        searchAdapter.notifyDataSetChanged();
        txtTitle.setText(getString(R.string.search_result, keyword));

        searchAnime(keyword, currentPage, currentType);
    }

    private void searchAnime(String keyword, int page, String type){
        String url = "https://graphql.anilist.co";
        String query =
                "query ($search:String,$page:Int,$type:MediaType) {" +
                        " Page(page:$page, perPage:18) {" +
                        "   pageInfo { currentPage lastPage total }" +
                        "   media(search:$search, type:$type) {" +
                        "     id" +
                        "     type" +
                        "     title { romaji english native }" +
                        "     coverImage { large }" +
                        "     averageScore" +
                        "     description" +
                        "     genres" +
                        "     updatedAt" +
                        "     isAdult" +
                        "     format" +
                        "     season" +
                        "     seasonYear" +
                        "     duration" +
                        "     episodes" +
                        "     chapters" +
                        "     volumes" +
                        "     nextAiringEpisode { episode airingAt }" +
                        "     status" +
                        "     studios { nodes { name } }" +
                        "     staff(perPage:5){ edges{ role node{ name{ full } } } } " +
                        "     trailer { id site }" +
                        "   }" +
                        " }" +
                        "}";

        try {
            JSONObject body = new JSONObject();
            body.put("query", query);

            JSONObject variables = new JSONObject();
            variables.put("search", keyword);
            variables.put("page", page);
            variables.put("type", type);
            body.put("variables", variables);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        try {
                            JSONObject pageInfo = response
                                    .getJSONObject("data")
                                    .getJSONObject("Page")
                                    .getJSONObject("pageInfo");

                            currentPage = pageInfo.getInt("currentPage");
                            totalPages = pageInfo.getInt("lastPage");

                            paginationView.setPages(currentPage, totalPages);

                            JSONArray media = response
                                    .getJSONObject("data")
                                    .getJSONObject("Page")
                                    .getJSONArray("media");

                            searchList.clear();
                            String key = keyword.toLowerCase();

                            for (int i = 0; i < media.length(); i++) {
                                JSONObject obj = media.getJSONObject(i);
                                Anime anime = AnimeParser.parse(obj, this);
                                if (anime == null) continue;

                                // Nếu muốn đánh dấu/ưu tiên keyword trong title
                                String titleToCheck = anime.getTitle() != null ? anime.getTitle().toLowerCase() : "";
                                String keyLower = keyword.toLowerCase();

                                if(titleToCheck.equals(keyLower)){
                                    // Nếu title hoàn toàn trùng với keyword -> ưu tiên đầu list
                                    searchList.add(0, anime);
                                } else if(titleToCheck.contains(keyLower)){
                                    // Nếu title chứa keyword -> thêm đầu list nhưng sau exact match
                                    int firstIndex = 0;
                                    while(firstIndex < searchList.size()){
                                        String t = searchList.get(firstIndex).getTitle().toLowerCase();
                                        if(t.equals(keyLower)) break;
                                        firstIndex++;
                                    }
                                    searchList.add(firstIndex, anime);
                                } else {
                                    // Kết quả khác -> thêm cuối list
                                    searchList.add(anime);
                                }
                            }

                            if(searchList.isEmpty()){
                                txtTitle.setText(getString(R.string.search_no_result, keyword));
                            }

                            searchAdapter.notifyDataSetChanged();

                        } catch (Exception e){
                            Log.e(TAG, "JSON ERROR", e);
                        }
                    },
                    error -> Log.e(TAG, "SEARCH ERROR", error)
            );

            requestQueue.add(request);

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}