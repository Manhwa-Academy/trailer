package com.mari.magic.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.widget.*;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.mlkit.nl.translate.*;
import com.mari.magic.R;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mari.magic.network.VolleySingleton;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AnimeDetailActivity extends AppCompatActivity {

    ImageView imgBanner, btnFavorite;

    TextView txtTitle, txtRating, txtDesc, txtGenres, txtFavorite;

    TextView txtStudio, txtDirector, txtSeason, txtDuration, txtFormat, txtViews;

    Button btnTrailer;
    TextView btnEnglish, btnVietnamese;

    FirebaseAuth auth;
    FirebaseFirestore db;

    Translator translator;

    String title, englishTitle, romajiTitle, nativeTitle;
    String poster, trailer, desc, genres;

    String studio, director, season, format;
    int duration;
    long views;

    double rating;
    boolean isAdult;

    boolean isFavorite = false;
    String animeId;

    String translatedDesc = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_detail);

        // ===== VIEW =====

        imgBanner = findViewById(R.id.imgBanner);
        btnFavorite = findViewById(R.id.btnFavorite);

        txtTitle = findViewById(R.id.txtTitle);
        txtRating = findViewById(R.id.txtRating);
        txtDesc = findViewById(R.id.txtDesc);
        txtGenres = findViewById(R.id.txtGenres);
        txtFavorite = findViewById(R.id.txtFavorite);

        txtStudio = findViewById(R.id.txtStudio);
        txtDirector = findViewById(R.id.txtDirector);
        txtSeason = findViewById(R.id.txtSeason);
        txtDuration = findViewById(R.id.txtDuration);
        txtFormat = findViewById(R.id.txtFormat);
        txtViews = findViewById(R.id.txtViews);

        btnTrailer = findViewById(R.id.btnTrailer);
        btnEnglish = findViewById(R.id.btnEnglish);
        btnVietnamese = findViewById(R.id.btnVietnamese);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ===== RECEIVE DATA =====

        title = getIntent().getStringExtra("title");
        englishTitle = getIntent().getStringExtra("englishTitle");
        romajiTitle = getIntent().getStringExtra("romajiTitle");
        nativeTitle = getIntent().getStringExtra("nativeTitle");

        poster = getIntent().getStringExtra("poster");
        trailer = getIntent().getStringExtra("trailer");
        rating = getIntent().getDoubleExtra("rating",0);

        desc = getIntent().getStringExtra("description");
        genres = getIntent().getStringExtra("genres");

        studio = getIntent().getStringExtra("studio");
        director = getIntent().getStringExtra("director");
        season = getIntent().getStringExtra("season");
        duration = getIntent().getIntExtra("duration",0);
        format = getIntent().getStringExtra("format");
        views = getIntent().getLongExtra("views",0);

        isAdult = getIntent().getBooleanExtra("isAdult", false);

        if(title == null) title = "Unknown";

        animeId = title.replace("/","_");

        // ===== TRANSLATOR =====

        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.VIETNAMESE)
                        .build();

        translator = Translation.getClient(options);

        // ===== TITLE =====

        String displayTitle = title;

        if(nativeTitle != null && !nativeTitle.isEmpty())
            displayTitle += "\n" + nativeTitle;

        if(romajiTitle != null && !romajiTitle.isEmpty())
            displayTitle += " (" + romajiTitle + ")";

        txtTitle.setText(displayTitle);

        // ===== RATING =====
        double score = rating;
        txtRating.setText("⭐ " + String.format("%.1f", score));

        if(genres != null)
            txtGenres.setText(genres);

        if(desc != null)
            txtDesc.setText(Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY));

        // ===== EXTRA INFO =====

        if(studio != null)
            txtStudio.setText("Studio: " + studio);

        if(director != null)
            txtDirector.setText("Director: " + director);

        if(season != null)
            txtSeason.setText("Season: " + season);

        txtDuration.setText("Duration: " + duration + " min");

        if(format != null)
            txtFormat.setText("Type: " + format);

        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        txtViews.setText("Views: " + nf.format(views));
        loadYoutubeViews(trailer);
        // ===== POSTER =====

        Glide.with(this)
                .load(poster)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .into(imgBanner);

        txtFavorite.setText("Lưu vào danh sách yêu thích");

        checkFavorite();

        // ===== TRAILER =====

        btnTrailer.setOnClickListener(v -> {

            saveHistory();
            openTrailer();
        });

        // ===== LANGUAGE =====

        btnEnglish.setOnClickListener(v -> {

            if(desc != null)
                txtDesc.setText(Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY));
        });

        btnVietnamese.setOnClickListener(v -> {

            if(!translatedDesc.isEmpty()){
                txtDesc.setText(translatedDesc);
                return;
            }

            translateToVietnamese(desc);
        });

        // ===== FAVORITE =====

        btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    // ===============================
    // CHECK FAVORITE
    // ===============================

    private void checkFavorite(){

        if(auth.getCurrentUser()==null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(animeId)
                .get()
                .addOnSuccessListener(doc -> {

                    if(doc.exists()){

                        isFavorite = true;
                        btnFavorite.setImageResource(R.drawable.ic_favorite);
                        txtFavorite.setText(getString(R.string.favorite_remove));
                    }else{

                        isFavorite = false;
                        btnFavorite.setImageResource(R.drawable.ic_favorite_border);
                        txtFavorite.setText(getString(R.string.favorite_add));
                    }
                });
    }

    // ===============================
    // TOGGLE FAVORITE
    // ===============================

    private void toggleFavorite(){

        if(auth.getCurrentUser()==null){

            Toast.makeText(this,"Please login first",Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        DocumentReference ref = db.collection("users")
                .document(uid)
                .collection("favorites")
                .document(animeId);

        if(isFavorite){

            ref.delete();

            btnFavorite.setImageResource(R.drawable.ic_favorite_border);
            txtFavorite.setText("Lưu vào danh sách yêu thích");

            isFavorite = false;

        }else{

            Map<String,Object> data = new HashMap<>();

            data.put("animeId", animeId);
            data.put("title", title);
            data.put("poster", poster);
            data.put("rating", rating);
            data.put("trailer", trailer);

            data.put("studio", studio);
            data.put("director", director);
            data.put("season", season);
            data.put("duration", duration);
            data.put("format", format);

            data.put("romajiTitle", romajiTitle);
            data.put("nativeTitle", nativeTitle);
            data.put("description", desc);
            data.put("genres", genres);

            data.put("time", System.currentTimeMillis());

            ref.set(data);

            btnFavorite.setImageResource(R.drawable.ic_favorite);
            txtFavorite.setText("Xóa khỏi danh sách yêu thích");

            isFavorite = true;
        }
    }

    // ===============================
    // HISTORY
    // ===============================

    private void saveHistory(){

        if(auth.getCurrentUser()==null){
            Log.e("HISTORY_DEBUG","User not logged in");
            return;
        }

        if(animeId == null){
            Log.e("HISTORY_DEBUG","animeId null");
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        Log.d("HISTORY_DEBUG","Saving history for "+animeId);

        Map<String,Object> data = new HashMap<>();

        data.put("animeId", animeId);
        data.put("title", title);
        data.put("poster", poster);
        data.put("rating", rating);
        data.put("trailer", trailer);

        data.put("studio", studio != null ? studio : "Unknown");
        data.put("director", director != null ? director : "Unknown");
        data.put("season", season != null ? season : "Unknown");
        data.put("duration", duration);
        data.put("format", format != null ? format : "Unknown");

        data.put("romajiTitle", romajiTitle);
        data.put("nativeTitle", nativeTitle);
        data.put("description", desc);
        data.put("genres", genres);

        data.put("time", System.currentTimeMillis());

        db.collection("users")
                .document(uid)
                .collection("history")
                .document(animeId)
                .set(data)
                .addOnSuccessListener(unused ->
                        Log.d("HISTORY_DEBUG","History saved successfully"))
                .addOnFailureListener(e ->
                        Log.e("HISTORY_DEBUG","History save failed",e));
    }

    // ===============================
    // TRAILER
    // ===============================

    private void openTrailer(){

        // nếu là anime 18+
        if(isAdult){

            String cleanTitle = title.toLowerCase()
                    .replaceAll("[^a-z0-9 ]", "")
                    .trim();

            String episode = "1"; // mặc định tập 1
            String baseTitle = cleanTitle;

            String[] parts = cleanTitle.split(" ");

            // nếu từ cuối là số -> đó là episode
            if(parts.length > 1 && parts[parts.length - 1].matches("\\d+")){
                episode = parts[parts.length - 1];
                baseTitle = cleanTitle.substring(0, cleanTitle.lastIndexOf(" ")).trim();
            }

            String slug = title.toLowerCase()

                    // bỏ dấu - trong từ (yaru-ki -> yaruki)
                    .replace("-", "")

                    // bỏ ký tự đặc biệt
                    .replaceAll("[^a-z0-9\\s]", "")

                    // space -> -
                    .replaceAll("\\s+", "-")

                    // nhiều - -> 1 -
                    .replaceAll("-+", "-")

                    // bỏ - đầu cuối
                    .replaceAll("^-|-$", "");

            String url = "https://hentaiz.dog/browse?q=" + slug + "-" + episode;

            Intent browserIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
            );

            startActivity(browserIntent);
            return; // ⭐ rất quan trọng
        }

        // anime bình thường -> mở trailer youtube
        if(trailer != null && !trailer.isEmpty()){

            try{

                startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("vnd.youtube:" + trailer)
                ));

            }catch(Exception e){

                startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=" + trailer)
                ));
            }

        }else{

            String query = Uri.encode(title + " trailer anime");

            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/results?search_query=" + query)
            ));
        }
    }
// ===============================
// YOUTUBE VIEWS
// ===============================

    private void loadYoutubeViews(String trailerId){

        Log.d("YT_DEBUG","Received trailerId = " + trailerId);

        if(trailerId == null || trailerId.isEmpty()){
            Log.e("YT_DEBUG","Trailer ID NULL");
            return;
        }

        String url =
                "https://www.googleapis.com/youtube/v3/videos?id="
                        + trailerId +
                        "&part=statistics&key=AIzaSyBrm4ovRR5rbFQkK4DtjsfKf8bToom0IF4";

        Log.d("YT_DEBUG","API URL = " + url);

        JsonObjectRequest request =
                new JsonObjectRequest(Request.Method.GET, url, null,

                        response -> {

                            Log.d("YT_DEBUG","API RESPONSE = " + response.toString());

                            try{

                                JSONArray items =
                                        response.getJSONArray("items");

                                Log.d("YT_DEBUG","Items size = " + items.length());

                                if(items.length() > 0){

                                    JSONObject stats =
                                            items.getJSONObject(0)
                                                    .getJSONObject("statistics");

                                    long viewCount = Long.parseLong(stats.getString("viewCount")); ;

                                    Log.d("YT_DEBUG","ViewCount = " + viewCount);

                                    txtViews.setText(
                                            "Views: " +
                                                    NumberFormat.getInstance().format(viewCount)
                                    );
                                }
                                else{
                                    Log.e("YT_DEBUG","No items returned from YouTube API");
                                }

                            }catch(Exception e){
                                Log.e("YT_DEBUG","JSON ERROR", e);
                            }

                        },

                        error -> {
                            Log.e("YT_DEBUG","API ERROR", error);
                        }
                );

        VolleySingleton
                .getInstance(this)
                .addToRequestQueue(request);
    }
    // ===============================
    // TRANSLATE
    // ===============================

    private void translateToVietnamese(String text){

        if(text == null || text.isEmpty()) return;

        String clean = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();

        txtDesc.setText("Đang dịch...");

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> {

                    translator.translate(clean)
                            .addOnSuccessListener(translated -> {

                                translatedDesc = translated;
                                txtDesc.setText(translated);
                            });
                });
    }
}