package com.mari.magic.ui.top;

import android.graphics.Color;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TopAnimeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    PaginationView paginationView;
    TextView txtTopTitle;

    String topType;

    List<Anime> animeList = new ArrayList<>();
    AnimeAdapter adapter;

    int currentPage = 1;
    int totalPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_anime);

        recyclerView = findViewById(R.id.recyclerTopAnime);
        paginationView = findViewById(R.id.paginationBottom);
        txtTopTitle = findViewById(R.id.txtTopTitle);

        recyclerView.setLayoutManager(new GridLayoutManager(this,3));

        adapter = new AnimeAdapter(this,animeList,R.layout.item_anime_search);
        recyclerView.setAdapter(adapter);

        topType = getIntent().getStringExtra("topType");

        if(topType == null)
            topType = "Top Today";

        txtTopTitle.setText(topType);

        paginationView.setOnPageChangeListener(page -> {

            currentPage = page;

            recyclerView.scrollToPosition(0);

            loadTopAnime();
        });

        loadTopAnime();
        applyBackground();
    }

    private void loadTopAnime(){

        String url = "https://graphql.anilist.co";

        String filter = AppSettings.getContentFilter(this);

        String adultFilter = "";

        // SAFE
        if(filter.equals("all")){
            adultFilter = ", isAdult:false";
        }

        // 16+
        else if(filter.equals("16")){
            adultFilter = "";
        }

        // 18+
        else if(filter.equals("18")){
            adultFilter = "";
        }

        String sort = "TRENDING_DESC";
        String extraFilter = "";

        // ===== TOP RATING =====
        if(topType.equals("Top Rating")){
            sort = "SCORE_DESC";
            extraFilter = ", status:FINISHED";
        }

        // ===== TOP MONTH =====
        else if(topType.equals("Top Month")){
            sort = "POPULARITY_DESC";
        }

        // ===== TOP YEAR =====
        else if(topType.equals("Top Year")){

            int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);

            sort = "SCORE_DESC";
            extraFilter = ", seasonYear:"+year;
        }

        // ===== TOP SEASON =====
        else if(topType.equals("Top Season")){

            int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);

            sort = "SCORE_DESC";
            extraFilter = ", season:SPRING, seasonYear:"+year;
        }

        String query =
                "query {" +
                        " Page(page:"+currentPage+", perPage:20){" +

                        " pageInfo { currentPage lastPage total } " +

                        " media(type:ANIME"+extraFilter+", sort:"+sort+ adultFilter +"){" +

                        " id title { romaji english native }" +
                        " format season seasonYear duration episodes" +
                        " status averageScore description genres isAdult" +
                        " coverImage { large }" +
                        " nextAiringEpisode { episode airingAt }" +
                        " studios { nodes { name } }" +

                        // 🔥 FIX CHÍNH Ở ĐÂY
                        " staff(perPage:10){ edges{ role node{ name{ full } } } }" +

                        " trailer { id site }" +
                        " updatedAt" +

                        " }}}";

        try{

            JSONObject body = new JSONObject();
            body.put("query",query);

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

                            paginationView.setPages(currentPage,totalPages);

                            JSONArray media = response
                                    .getJSONObject("data")
                                    .getJSONObject("Page")
                                    .getJSONArray("media");

                            animeList.clear();

                            for(int i=0;i<media.length();i++){

                                JSONObject obj = media.getJSONObject(i);

                                Anime anime = AnimeParser.parse(obj,this);

                                if(anime != null){
                                    animeList.add(anime);
                                }
                            }

                            adapter.notifyDataSetChanged();

                        }catch(Exception e){
                            e.printStackTrace();
                        }

                    },

                    error -> error.printStackTrace()
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void applyBackground() {
        View root = findViewById(R.id.rootLayout);
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
}