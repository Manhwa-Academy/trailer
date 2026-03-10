package com.mari.magic.ui.type;

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
import com.mari.magic.utils.AppSettings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AnimeTypeActivity extends AppCompatActivity {

    private static final String TAG = "AnimeType";

    RecyclerView recycler;
    AnimeAdapter adapter;
    List<Anime> animeList = new ArrayList<>();

    String format;
    TextView typeTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_type);

        recycler = findViewById(R.id.typeAnimeRecycler);
        typeTitle = findViewById(R.id.typeTitle);

        recycler.setLayoutManager(new GridLayoutManager(this,3));

        adapter = new AnimeAdapter(
                this,
                animeList,
                R.layout.item_anime_type
        );

        recycler.setAdapter(adapter);

        // nhận format
        format = getIntent().getStringExtra("format");

        if(format == null || format.isEmpty()){
            format = "TV";
        }

        // title
        typeTitle.setText(getString(R.string.anime_type) + " • " + format);
        setTitle(format + " Anime");

        loadAnime();
    }

    private void loadAnime(){

        String url = "https://graphql.anilist.co";

        try{

            // xác định media type
            String mediaType = "ANIME";

            if(format.equals("MANGA") ||
                    format.equals("NOVEL") ||
                    format.equals("ONE_SHOT")){
                mediaType = "MANGA";
            }

            JSONObject variables = new JSONObject();
            variables.put("format", format);
            variables.put("type", mediaType);

            JSONObject body = new JSONObject();

            body.put("query",
                    "query ($format: MediaFormat,$type: MediaType) {" +
                            " Page(page:1,perPage:30) {" +
                            " media(type:$type,format:$format) {" +

                            " id " +
                            " title { romaji english native } " +
                            " coverImage { large } " +
                            " averageScore " +
                            " description(asHtml:false) " +
                            " genres " +
                            " format " +
                            " season " +
                            " seasonYear " +
                            " duration " +
                            " episodes " +
                            " chapters " +
                            " volumes " +
                            " updatedAt " +
                            " status " +
                            " isAdult " +

                            " trailer { id site } " +

                            " studios(isMain:true){ nodes{ name } } " +
                            " staff(perPage:20){ nodes{ name{full} primaryOccupations } } " +

                            " nextAiringEpisode { episode airingAt } " +

                            " externalLinks { site url } " +   // ⭐ MangaDex / MAL ở đây

                            "}}}");


            body.put("variables", variables);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,

                    response -> {

                        try{

                            JSONArray array = response
                                    .getJSONObject("data")
                                    .getJSONObject("Page")
                                    .getJSONArray("media");

                            animeList.clear();

                            String filter = AppSettings.getContentFilter(this);

                            for(int i = 0; i < array.length(); i++){

                                JSONObject obj = array.getJSONObject(i);
                                JSONArray links = obj.optJSONArray("externalLinks");
                                Log.d("API_DEBUG","links=" + links);
                                Anime anime = AnimeParser.parse(obj, this);

                                if(anime == null) continue;

                                boolean isAdult = anime.isAdult();
                                boolean isEcchi = anime.getGenres() != null &&
                                        anime.getGenres().contains("Ecchi");

                                // SAFE
                                if(filter.equals("all")){
                                    if(!isAdult && !isEcchi){
                                        animeList.add(anime);
                                    }
                                }

                                // 16+
                                else if(filter.equals("16")){
                                    if(!isAdult){
                                        animeList.add(anime);
                                    }
                                }

                                // 18+
                                else if(filter.equals("18")){
                                    animeList.add(anime);
                                }

                            }

                            adapter.notifyDataSetChanged();

                        }catch(Exception e){
                            e.printStackTrace();
                        }

                    },

                    error -> Log.e(TAG,"API error " + error)
            );

            VolleySingleton
                    .getInstance(this)
                    .addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}