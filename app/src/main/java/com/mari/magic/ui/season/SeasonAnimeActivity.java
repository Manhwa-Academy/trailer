package com.mari.magic.ui.season;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mari.magic.R;
import com.mari.magic.adapter.AnimeAdapter;
import com.mari.magic.model.Anime;
import com.mari.magic.network.VolleySingleton;
import com.mari.magic.ui.component.PaginationView;
import com.mari.magic.utils.AnimeParser;
import com.mari.magic.utils.AppSettings;
import com.mari.magic.utils.GridSpacingItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SeasonAnimeActivity extends AppCompatActivity {

    private static final String TAG = "SeasonAnime";

    RecyclerView recycler;
    AnimeAdapter adapter;
    TextView title;
    PaginationView paginationView;

    List<Anime> list = new ArrayList<>();

    String season;
    int year;
    int currentPage = 1;
    int totalPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_anime); // dùng layout có PaginationView
        applyBackground();

        recycler = findViewById(R.id.genreAnimeRecycler);
        title = findViewById(R.id.txtGenreTitle);
        paginationView = findViewById(R.id.paginationBottom);

        recycler.setLayoutManager(new GridLayoutManager(this,3));
        recycler.addItemDecoration(new GridSpacingItemDecoration(3,20,true));

        adapter = new AnimeAdapter(this,list,R.layout.item_anime_genre);
        recycler.setAdapter(adapter);

        // nhận dữ liệu từ Intent
        season = getIntent().getStringExtra("season");
        year = getIntent().getIntExtra("year",2024);

        if(season == null){
            season = "WINTER";
        }

        title.setText(getSeasonVN(season) + " " + year);

        paginationView.setOnPageChangeListener(page -> {
            currentPage = page;
            recycler.scrollToPosition(0);
            loadSeasonAnime(currentPage);
        });

        // load lần đầu
        loadSeasonAnime(currentPage);
    }

    private void loadSeasonAnime(int page){
        String url = "https://graphql.anilist.co";

        try{
            JSONObject variables = new JSONObject();
            variables.put("season", season);
            variables.put("year", year);
            variables.put("page", page);
            variables.put("perPage", 18); // số anime mỗi trang

            JSONObject body = new JSONObject();
            body.put("query",
                    "query ($season: MediaSeason,$year: Int,$page:Int,$perPage:Int) {" +
                            " Page(page:$page, perPage:$perPage) {" +
                            " pageInfo { currentPage lastPage total } " +
                            " media(type:ANIME,season:$season,seasonYear:$year,isAdult:false) {" +
                            " id title { romaji english native } coverImage { large } averageScore }}}"
            );
            body.put("variables", variables);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        try{
                            JSONObject pageInfo = response
                                    .getJSONObject("data")
                                    .getJSONObject("Page")
                                    .getJSONObject("pageInfo");

                            currentPage = pageInfo.getInt("currentPage");
                            totalPages = pageInfo.getInt("lastPage");

                            paginationView.setPages(currentPage, totalPages);

                            JSONArray array = response
                                    .getJSONObject("data")
                                    .getJSONObject("Page")
                                    .getJSONArray("media");

                            list.clear();
                            for(int i=0;i<array.length();i++){
                                Anime anime = AnimeParser.parse(array.getJSONObject(i), this);
                                if(anime != null) list.add(anime);
                            }

                            adapter.notifyDataSetChanged();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    },
                    error -> Log.e(TAG,"API Error: "+error)
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private String getSeasonVN(String season){
        switch(season){
            case "WINTER": return getString(R.string.season_winter);
            case "SPRING": return getString(R.string.season_spring);
            case "SUMMER": return getString(R.string.season_summer);
            case "FALL": return getString(R.string.season_fall);
            default: return season;
        }
    }

    private void applyBackground() {
        View root = findViewById(R.id.rootLayout);
        if (root == null) return;

        String bg = AppSettings.getBackground(this);

        switch (bg) {
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
            case "anh5":
                root.setBackgroundResource(R.drawable.anh5);
                break;
            // ✅ bỏ default → nếu bg không phải anh1~anh5 thì giữ nguyên background
        }
    }
}