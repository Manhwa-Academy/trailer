package com.mari.magic.ui.genre;

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

public class GenreAnimeActivity extends AppCompatActivity {

    TextView txtNoResult;
    RecyclerView recycler;
    AnimeAdapter adapter;
    PaginationView paginationView;

    List<Anime> list = new ArrayList<>();

    String genre;

    int currentPage = 1;
    int totalPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_anime);
        applyBackground();
        recycler = findViewById(R.id.genreAnimeRecycler);
        txtNoResult = findViewById(R.id.txtNoResult);
        paginationView = findViewById(R.id.paginationBottom);

        recycler.setLayoutManager(new GridLayoutManager(this,3));
        recycler.addItemDecoration(new GridSpacingItemDecoration(3,20,true));

        TextView title = findViewById(R.id.txtGenreTitle);

        genre = getIntent().getStringExtra("genre");

        title.setText(getString(R.string.genre_title_format, genre));

        adapter = new AnimeAdapter(
                this,
                list,
                R.layout.item_anime_genre
        );

        recycler.setAdapter(adapter);

        if(genre == null){
            Log.e("GENRE","Genre is null");
            finish();
            return;
        }

        paginationView.setOnPageChangeListener(page -> {

            currentPage = page;

            recycler.scrollToPosition(0);

            loadAnimeByGenre(genre,currentPage);

        });

        loadAnimeByGenre(genre,currentPage);
    }

    private void loadAnimeByGenre(String genre,int page){

        String url = "https://graphql.anilist.co";

        try{

            JSONObject variables = new JSONObject();
            variables.put("genre",genre);
            variables.put("page",page);

            JSONObject body = new JSONObject();

            body.put("query",
                    "query ($genre: String,$page:Int) {" +
                            " Page(page:$page, perPage:18) {" +

                            " pageInfo { currentPage lastPage total } " +

                            " media(genre_in:[$genre], type:ANIME, sort:TRENDING_DESC) {" +

                            " id " +
                            " title { romaji english native } " +
                            " coverImage { large } " +
                            " averageScore " +
                            " description(asHtml:false) " +
                            " genres " +
                            " format " +
                            " updatedAt " +
                            " season " +
                            " seasonYear " +
                            " duration " +
                            " episodes " +
                            " nextAiringEpisode { episode airingAt } " +
                            " status " +
                            " isAdult " +
                            " studios { nodes { name } } " +
                            " staff(perPage:10) {edges{role node{name{full}}}} " +
                            " trailer { id site } " +

                            " }}}");

            body.put("variables",variables);

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

                            JSONArray array = response
                                    .getJSONObject("data")
                                    .getJSONObject("Page")
                                    .getJSONArray("media");

                            list.clear();

                            for(int i=0;i<array.length();i++){

                                JSONObject obj = array.getJSONObject(i);

                                Anime anime = AnimeParser.parse(obj,this);

                                if(anime != null){
                                    list.add(anime);
                                }

                            }

                            adapter.notifyDataSetChanged();

                            if(list.isEmpty()){

                                txtNoResult.setText(R.string.genre_empty);
                                txtNoResult.setVisibility(View.VISIBLE);

                            }else{

                                txtNoResult.setVisibility(View.GONE);

                            }

                        }catch(Exception e){
                            e.printStackTrace();
                        }

                    },

                    error -> Log.e("API","Genre anime error "+error)
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