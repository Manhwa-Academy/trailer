package com.mari.magic.ui.type;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AnimeTypeActivity extends AppCompatActivity {

    private static final String TAG = "AnimeType";

    RecyclerView recycler;
    AnimeAdapter adapter;
    List<Anime> animeList = new ArrayList<>();

    PaginationView paginationView;

    String format;
    TextView typeTitle;

    int currentPage = 1;
    int totalPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_type);
        applyBackground();
        recycler = findViewById(R.id.typeAnimeRecycler);
        typeTitle = findViewById(R.id.typeTitle);
        paginationView = findViewById(R.id.paginationBottom);

        recycler.setLayoutManager(new GridLayoutManager(this,3));

        adapter = new AnimeAdapter(
                this,
                animeList,
                R.layout.item_anime_type
        );

        recycler.setAdapter(adapter);

        format = getIntent().getStringExtra("format");

        if(format == null || format.isEmpty()){
            format = "TV";
        }

        typeTitle.setText(getString(R.string.anime_type) + " • " + format);
        setTitle(format + " Anime");

        paginationView.setOnPageChangeListener(page -> {

            currentPage = page;
            loadAnime(currentPage);

        });

        loadAnime(currentPage);
    }

    private void loadAnime(int page){

        String url = "https://graphql.anilist.co";

        try{

            String mediaType = "ANIME";

            if(format.equals("MANGA") ||
                    format.equals("NOVEL") ||
                    format.equals("ONE_SHOT")){
                mediaType = "MANGA";
            }

            JSONObject variables = new JSONObject();
            variables.put("format", format);
            variables.put("type", mediaType);
            variables.put("page", page);

            JSONObject body = new JSONObject();

            body.put("query",
                    "query ($format: MediaFormat,$type: MediaType,$page:Int) {" +
                            " Page(page:$page,perPage:30) {" +

                            " pageInfo { currentPage lastPage total } " +

                            " media(type:$type,format:$format, sort:TRENDING_DESC) {" +

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
                            "staff(perPage:20){ edges{ role node{ name{ full } } } } " +

                            " nextAiringEpisode { episode airingAt } " +
                            " siteUrl " +
                            " externalLinks { site url } " +

                            "}}}");

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

                            paginationView.setPages(currentPage,totalPages);

                            JSONArray array = response
                                    .getJSONObject("data")
                                    .getJSONObject("Page")
                                    .getJSONArray("media");

                            animeList.clear();

                            String filter = AppSettings.getContentFilter(this);

                            for(int i = 0; i < array.length(); i++){

                                JSONObject obj = array.getJSONObject(i);

                                Anime anime = AnimeParser.parse(obj, this);

                                if(anime == null) continue;

                                boolean isAdult = anime.isAdult();
                                boolean isEcchi = anime.getGenres() != null &&
                                        anime.getGenres().contains("Ecchi");

                                if(filter.equals("all")){
                                    if(!isAdult && !isEcchi){
                                        animeList.add(anime);
                                    }
                                }

                                else if(filter.equals("16")){
                                    if(!isAdult){
                                        animeList.add(anime);
                                    }
                                }

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