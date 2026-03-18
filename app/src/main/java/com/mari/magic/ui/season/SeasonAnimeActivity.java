package com.mari.magic.ui.season;

import android.os.Bundle;
import android.util.Log;
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
import com.mari.magic.utils.AnimeParser;
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

    List<Anime> list = new ArrayList<>();

    String season;
    int year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_anime);

        recycler = findViewById(R.id.genreAnimeRecycler);
        title = findViewById(R.id.txtGenreTitle);

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

        Log.d(TAG,"Received season=" + season + " year=" + year);

        title.setText(getSeasonVN(season) + " " + year);

        loadSeasonAnime();
    }

    private void loadSeasonAnime(){

        String url = "https://graphql.anilist.co";

        try{

            Log.d(TAG,"Loading anime for season=" + season + " year=" + year);

            JSONObject body = new JSONObject();

            body.put("query",
                    "query ($season: MediaSeason,$year: Int) {" +
                            " Page(page:1,perPage:30) {" +
                            " media(type:ANIME,season:$season,seasonYear:$year,isAdult:false) {" +
                            " id " +
                            " title { romaji english native } " +
                            " coverImage { large } " +
                            " averageScore " +
                            " description(asHtml:false) " +
                            " genres " +
                            " format " +
                            " season " +
                            " updatedAt " +
                            " seasonYear " +
                            " duration " +
                            " episodes " +
                            "nextAiringEpisode { episode airingAt } " +
                            "status " +
                            " trailer { id site } " +
                            " studios(isMain:true){ nodes{ name } } " +
                            "staff(perPage:20){ edges{ role node{ name{ full } } } } " +
                            " }}}"
            );

            JSONObject variables = new JSONObject();
            variables.put("season",season);
            variables.put("year",year);

            body.put("variables",variables);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,

                    response -> {

                        Log.d(TAG,"API Response received");

                        try{

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

                            Log.d(TAG,"Anime loaded: " + list.size());

                            adapter.notifyDataSetChanged();

                        }catch(Exception e){
                            Log.e(TAG,"Parse error",e);
                        }

                    },

                    error -> Log.e(TAG,"API Error: " + error)
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        }catch(Exception e){
            Log.e(TAG,"Query build error",e);
        }
    }

    private String getSeasonVN(String season){

        switch(season){

            case "WINTER":
                return getString(R.string.season_winter);

            case "SPRING":
                return getString(R.string.season_spring);

            case "SUMMER":
                return getString(R.string.season_summer);

            case "FALL":
                return getString(R.string.season_fall);

            default:
                return season;
        }
    }
}