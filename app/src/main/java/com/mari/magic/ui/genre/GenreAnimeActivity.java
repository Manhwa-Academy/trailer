package com.mari.magic.ui.genre;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mari.magic.R;
import com.mari.magic.adapter.AnimeAdapter;
import com.mari.magic.model.Anime;
import com.mari.magic.network.VolleySingleton;
import com.mari.magic.utils.AnimeParser;
import org.json.JSONArray;
import org.json.JSONObject;
import com.mari.magic.utils.GridSpacingItemDecoration;
import java.util.ArrayList;
import java.util.List;
import android.view.View;
public class GenreAnimeActivity extends AppCompatActivity {
    TextView txtNoResult;
    RecyclerView recycler;
    AnimeAdapter adapter;
    List<Anime> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_anime);

        recycler = findViewById(R.id.genreAnimeRecycler);
        txtNoResult = findViewById(R.id.txtNoResult);
        recycler.setLayoutManager(new GridLayoutManager(this,3));
        recycler.addItemDecoration(new GridSpacingItemDecoration(3,20,true));
        TextView title = findViewById(R.id.txtGenreTitle);

        String genre = getIntent().getStringExtra("genre");
        title.setText(getString(R.string.genre_title_format, genre));
        // FIX 1: AnimeAdapter cần layoutId
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

        loadAnimeByGenre(genre);
    }

    private void loadAnimeByGenre(String genre){

        String url = "https://graphql.anilist.co";

        try{

            JSONObject body = new JSONObject();

            body.put("query",
                    "query ($genre: String) {" +
                            " Page(page:1, perPage:20) {" +
                            " media(genre_in:[$genre], type:ANIME) {" +
                            " id " +
                            " title { romaji english native } " +
                            " coverImage { large } " +
                            " averageScore " +
                            " description(asHtml:false) " +
                            " genres " +
                            " format " +
                            "   updatedAt " +
                            " season " +
                            " seasonYear " +
                            " duration " +
                            "   episodes" +
                            "   nextAiringEpisode { episode  airingAt} " +
                            "status " +
                            " isAdult " +   // ⭐ thêm dòng này
                            " studios { nodes { name } } " +
                            " staff(perPage:5) { nodes { name { full } primaryOccupations } } " +
                            " trailer { id site } " +
                            " }}}");

            JSONObject variables = new JSONObject();
            variables.put("genre",genre);

            body.put("variables",variables);

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

                            list.clear();

                            for(int i=0;i<array.length();i++){

                                JSONObject obj = array.getJSONObject(i);
                                Anime anime = AnimeParser.parse(obj, this);

                                if(anime != null){
                                    list.add(anime);
                                }
                            }

                            adapter.notifyDataSetChanged();
                            // ⭐ kiểm tra sau khi load dữ liệu
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
}