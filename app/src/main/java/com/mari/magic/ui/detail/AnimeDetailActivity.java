package com.mari.magic.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.*;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mari.magic.MainActivity;
import com.mari.magic.model.Anime;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.nl.translate.*;
import com.mari.magic.utils.NotificationHelper;
import com.mari.magic.utils.NovelResolver;
import com.mari.magic.R;
import com.mari.magic.utils.YoutubeService;
import com.mari.magic.utils.FavoriteManager;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.mari.magic.utils.HistoryManager;
import com.mari.magic.utils.TrailerHelper;
import com.mari.magic.utils.MangaDexResolver;
import com.mari.magic.network.VolleySingleton;
public class AnimeDetailActivity extends AppCompatActivity {

    ImageView imgBanner, btnFavorite;
    TextView txtChapters;
    int chapters, volumes;
    Button btnRead;
    String author;
    TextView txtVolumes;
    TextView txtAuthor;
    TextView txtTitle, txtRating, txtDesc, txtGenres, txtFavorite;
    TextView txtNextEpisode;
    TextView txtStudio, txtDirector, txtSeason, txtDuration, txtFormat, txtViews, txtEpisodes;
    TextView txtUpdated, txtTrailerDate;

    Button btnTrailer;
    TextView btnEnglish, btnVietnamese;

    FirebaseAuth auth;
    FirebaseFirestore db;
    Translator translator;

    String title, englishTitle, romajiTitle, nativeTitle;
    String poster, trailer, desc, genres;

    String studio, director, season, format;

    int duration, episodes;
    private boolean isFavorite = false;
    long views, updatedAt;

    double rating;

    boolean isAdult;

    String animeId;

    String translatedDesc = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_detail);

        // ================= VIEW =================

        imgBanner = findViewById(R.id.imgBanner);
        btnFavorite = findViewById(R.id.btnFavorite);

        txtTitle = findViewById(R.id.txtTitle);
        txtRating = findViewById(R.id.txtRating);
        txtDesc = findViewById(R.id.txtDesc);
        txtGenres = findViewById(R.id.txtGenres);
        txtFavorite = findViewById(R.id.txtFavorite);
        txtChapters = findViewById(R.id.txtChapters);
        txtVolumes = findViewById(R.id.txtVolumes);
        txtAuthor = findViewById(R.id.txtAuthor);
        txtStudio = findViewById(R.id.txtStudio);
        txtDirector = findViewById(R.id.txtDirector);
        txtSeason = findViewById(R.id.txtSeason);
        txtDuration = findViewById(R.id.txtDuration);
        txtFormat = findViewById(R.id.txtFormat);
        txtViews = findViewById(R.id.txtViews);
        txtEpisodes = findViewById(R.id.txtEpisodes);
        txtNextEpisode = findViewById(R.id.txtNextEpisode);
        txtUpdated = findViewById(R.id.txtUpdated);
        txtTrailerDate = findViewById(R.id.txtTrailerDate);

        btnRead = findViewById(R.id.btnRead);
        btnTrailer = findViewById(R.id.btnTrailer);

        btnEnglish = findViewById(R.id.btnEnglish);
        btnVietnamese = findViewById(R.id.btnVietnamese);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ================= RECEIVE DATA =================

        title = getIntent().getStringExtra("title");
        englishTitle = getIntent().getStringExtra("englishTitle");
        romajiTitle = getIntent().getStringExtra("romajiTitle");
        nativeTitle = getIntent().getStringExtra("nativeTitle");

        poster = getIntent().getStringExtra("poster");
        trailer = getIntent().getStringExtra("trailer");

        rating = getIntent().getDoubleExtra("rating", 0);

        desc = getIntent().getStringExtra("description");
        genres = getIntent().getStringExtra("genres");

        studio = getIntent().getStringExtra("studio");
        director = getIntent().getStringExtra("director");
        season = getIntent().getStringExtra("season");

        duration = getIntent().getIntExtra("duration", 0);
        episodes = getIntent().getIntExtra("episodes", 0);
        chapters = getIntent().getIntExtra("chapters",0);
        volumes = getIntent().getIntExtra("volumes",0);
        author = getIntent().getStringExtra("author");
        format = getIntent().getStringExtra("format");
        String mangaDex = getIntent().getStringExtra("mangadex");
        String mal = getIntent().getStringExtra("mal");

        Log.d("READ_DEBUG","MangaDex=" + mangaDex);
        Log.d("READ_DEBUG","MAL=" + mal);
        boolean isManga =
                format != null &&
                        (format.equalsIgnoreCase("MANGA")
                                || format.equalsIgnoreCase("NOVEL")
                                || format.equalsIgnoreCase("ONE_SHOT"));
        isAdult = getIntent().getBooleanExtra("isAdult", false);
        boolean hasTrailer =
                (trailer != null && !trailer.isEmpty()) || isAdult;
// ===== DEBUG LOG =====
        Log.d("TRAILER_DEBUG","title=" + title);
        Log.d("TRAILER_DEBUG","trailer=" + trailer);
        Log.d("TRAILER_DEBUG","isAdult=" + isAdult);
        Log.d("TRAILER_DEBUG","hasTrailer=" + hasTrailer);
// ===== HIỂN THỊ BUTTON =====

        if(isManga){

            btnRead.setVisibility(View.VISIBLE);

            if(hasTrailer){
                btnTrailer.setVisibility(View.VISIBLE);
            }else{
                btnTrailer.setVisibility(View.GONE);
            }

        }else{

            btnRead.setVisibility(View.GONE);

            if(hasTrailer){
                btnTrailer.setVisibility(View.VISIBLE);
            }else{
                btnTrailer.setVisibility(View.GONE);
            }
        }
        btnRead.setOnClickListener(v -> {
            String searchTitle = title != null ? title : romajiTitle;
            int nextEpisode = 0;
            long nextAiring = 0;
            String status = "";
            // 🔹 Lưu history + tăng mangaRead/streak
            HistoryManager.saveHistory(
                    this,
                    animeId,
                    title,
                    poster,
                    rating,
                    trailer,
                    studio,
                    director,
                    season,
                    duration,
                    format,
                    romajiTitle,
                    nativeTitle,
                    desc,
                    genres,
                    episodes,
                    nextEpisode,
                    nextAiring,
                    status,
                    isAdult,
                    updatedAt,
                    views
            );

            // Mở truyện
            if(format.equalsIgnoreCase("MANGA") || format.equalsIgnoreCase("ONE_SHOT")){
                MangaDexResolver.openMangaDex(this, searchTitle);
            } else if(format.equalsIgnoreCase("NOVEL")){
                NovelResolver.openNovel(this, searchTitle);
            }
        });

        if(format != null){

            if(format.equalsIgnoreCase("MANGA") ||
                    format.equalsIgnoreCase("NOVEL") ||
                    format.equalsIgnoreCase("ONE_SHOT")){

                txtSeason.setVisibility(View.GONE);
                txtDuration.setVisibility(View.GONE);
                txtStudio.setVisibility(View.GONE);
                txtDirector.setVisibility(View.GONE);
                txtEpisodes.setVisibility(View.GONE);
                txtNextEpisode.setVisibility(View.GONE);

                // ✔ chỉ ẩn nếu không có trailer
                if(trailer != null && !trailer.isEmpty()){
                    btnTrailer.setVisibility(View.VISIBLE);
                }else{
                    btnTrailer.setVisibility(View.GONE);
                }


                // show manga info
                txtChapters.setVisibility(TextView.VISIBLE);
                txtVolumes.setVisibility(TextView.VISIBLE);
                txtAuthor.setVisibility(TextView.VISIBLE);

            }
            else{

                // ===== ANIME MODE =====

                txtSeason.setVisibility(TextView.VISIBLE);
                txtDuration.setVisibility(TextView.VISIBLE);
                txtStudio.setVisibility(TextView.VISIBLE);
                txtDirector.setVisibility(TextView.VISIBLE);
                txtEpisodes.setVisibility(TextView.VISIBLE);
                txtNextEpisode.setVisibility(TextView.VISIBLE);
                txtViews.setVisibility(TextView.VISIBLE);
                btnTrailer.setVisibility(Button.VISIBLE);

                // hide manga info
                txtChapters.setVisibility(TextView.GONE);
                txtVolumes.setVisibility(TextView.GONE);
                txtAuthor.setVisibility(TextView.GONE);
            }
        }
        views = getIntent().getLongExtra("views", 0);
        updatedAt = getIntent().getLongExtra("updatedAt", 0);



        if (title == null) title = "Unknown";

        animeId = title
                .replaceAll("[^a-zA-Z0-9]", "_")
                .toLowerCase();
        Log.d("FAV_CHECK", "animeId = " + animeId);

        // 🔥 GỌI API TẠI ĐÂY
        int anilistId = getIntent().getIntExtra("anilistId", 0);

        if(anilistId != 0){
            loadAnimeDetailFromApi(anilistId);
        }
        // ================= TRANSLATOR =================

        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.VIETNAMESE)
                        .build();

        translator = Translation.getClient(options);

        // ================= TITLE =================

        String displayTitle = title;

        if (nativeTitle != null && !nativeTitle.isEmpty())
            displayTitle += "\n" + nativeTitle;

        if (romajiTitle != null && !romajiTitle.isEmpty())
            displayTitle += " (" + romajiTitle + ")";

        txtTitle.setText(displayTitle);

        // ================= RATING =================

        txtRating.setText("⭐ " + String.format("%.1f", rating));

        if (genres != null)
            txtGenres.setText(genres);

        if (desc != null)
            txtDesc.setText(Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY));

        // ================= INFO =================
        txtStudio.setText("Studio: " + studio);
        txtDirector.setText("Director: " + director);
        txtSeason.setText("Season: " + season);
        txtDuration.setText("Duration: " + duration + " min");

        txtChapters.setText(chapters > 0 ? "Chapters: " + chapters : "Chapters: ?");
        txtVolumes.setText(volumes > 0 ? "Volumes: " + volumes : "Volumes: ?");
        txtAuthor.setText(author != null && !author.isEmpty() ? "Author: " + author : "Author: Unknown");

// Lấy episodes và nextEpisode từ intent
        int episodes = getIntent().getIntExtra("episodes", 0);
        int nextEpisode = getIntent().getIntExtra("nextEpisode", 0);
        long nextAiring = getIntent().getLongExtra("nextAiringAt", 0);
        String status = getIntent().getStringExtra("status");

// Tính currentEpisode chuẩn
        int currentEpisode;
        if(nextEpisode > 0){
            currentEpisode = nextEpisode - 1; // chính xác luôn
        } else if(episodes > 0){
            currentEpisode = episodes;
        } else {
            currentEpisode = 0;
        }

// Hiển thị episodes
        txtEpisodes.setText(episodes > 0 ? "Episodes: " + episodes : "Episodes: ?");

// Hiển thị nextEpisode
        if ("FINISHED".equalsIgnoreCase(status)) {
            txtNextEpisode.setText("EP " + episodes + " ✓ Completed");
        } else if (nextEpisode > 0 && nextAiring > 0) {
            long now = System.currentTimeMillis() / 1000;
            long diff = nextAiring - now;

            if (diff > 0) {
                new android.os.CountDownTimer(diff * 1000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long seconds = millisUntilFinished / 1000;
                        long days = seconds / 86400;
                        long hours = (seconds % 86400) / 3600;
                        long minutes = (seconds % 3600) / 60;
                        long sec = seconds % 60;

                        txtNextEpisode.setText(
                                "EP " + currentEpisode +
                                        " • Next EP " + nextEpisode +
                                        " in " +
                                        days + "d " +
                                        hours + "h " +
                                        minutes + "m " +
                                        sec + "s"
                        );
                    }

                    @Override
                    public void onFinish() {
                        txtNextEpisode.setText("Episode " + nextEpisode + " • Airing now");
                    }
                }.start();
            } else {
                txtNextEpisode.setText("EP " + currentEpisode + " • Next EP " + nextEpisode);
            }
        } else if (nextEpisode > 0) {
            txtNextEpisode.setText("EP " + currentEpisode);
        } else {
            txtNextEpisode.setText("Episode ?");
        }

// Type + Views
        txtFormat.setText("Type: " + format);
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        txtViews.setText("Views: " + nf.format(views));

// UpdatedAt
        if (updatedAt > 0) {
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            String time = sdf.format(new java.util.Date(updatedAt * 1000));
            txtUpdated.setText("Updated: " + time);
        } else {
            txtUpdated.setText("Updated: Unknown");
        }

        // ================= POSTER =================

        Glide.with(this)
                .load(poster)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .into(imgBanner);

        // ================= YOUTUBE =================

        if(trailer != null && !trailer.isEmpty()){

            txtViews.setVisibility(View.VISIBLE);
            txtTrailerDate.setVisibility(View.VISIBLE);
            btnTrailer.setVisibility(View.VISIBLE);

            YoutubeService.loadTrailerInfo(
                    this,
                    trailer,
                    txtViews,
                    txtTrailerDate
            );

        }else{

            txtTrailerDate.setVisibility(View.GONE);

            if(!isAdult){
                btnTrailer.setVisibility(View.GONE);
            }else{
                btnTrailer.setVisibility(View.VISIBLE);
            }
        }
        // ================= FAVORITE =================

// ✅ LUÔN dùng 1 ID duy nhất
        String safeId = animeId.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();

// 🔥 Check trạng thái từ Firestore/LOCAL
        FavoriteManager.checkFavorite(safeId, btnFavorite, txtFavorite, fav -> {
            isFavorite = fav;
            Log.d("FAV_STATE", "Init state = " + isFavorite);
        });

// 👉 CLICK
        // 👉 CLICK FAVORITE
        btnFavorite.setOnClickListener(v -> {

            Anime anime = new Anime();
            anime.setId(safeId.hashCode());
            anime.setTitle(title);
            anime.setEnglishTitle(englishTitle);
            anime.setRomajiTitle(romajiTitle);
            anime.setNativeTitle(nativeTitle);
            anime.setPoster(poster);
            anime.setRating(rating);
            anime.setTrailer(trailer);
            anime.setDescription(desc);
            anime.setGenres(genres);
            anime.setStudio(studio);
            anime.setDirector(director);
            anime.setSeason(season);
            anime.setFormat(format);
            anime.setDuration(duration);
            anime.setEpisodes(episodes);
            anime.setNextEpisode(nextEpisode);
            anime.setNextAiringAt(nextAiring);
            anime.setStatus(status);
            anime.setAdult(isAdult);
            anime.setViews(views);
            anime.setUpdatedAt(updatedAt);

            // 🔹 Toggle favorite dùng FavoriteManager
            FavoriteManager.toggleFavorite(this, anime, btnFavorite, txtFavorite, safeId, fav -> {
                isFavorite = fav;

                if (isFavorite) {
                    // Thêm vào local favorite
                    NotificationHelper.addFavorite(this, safeId);

                    // 🔥 Hiển thị notification ngay lập tức
                    // Sửa thành
                    long realAiringAt = getIntent().getLongExtra("nextAiringAt", 0); // lấy từ intent hoặc DB
                    NotificationHelper.showNotification(
                            this,
                            title,
                            "Bạn đã thêm vào yêu thích 🔥",
                            new Intent(this, MainActivity.class),
                            poster != null ? poster : "",
                            realAiringAt > 0 ? realAiringAt : System.currentTimeMillis() / 1000, // ✅ dùng thời gian thật
                            getIntent().getIntExtra("nextEpisode", 0)
                    );

                    // Subscribe topic FCM để nhận notification tập mới
                    FirebaseMessaging.getInstance().subscribeToTopic("anime_" + safeId);

                    Log.d("FAV_LOCAL","Added favorite & notified: " + safeId);

                } else {
                    // Xóa khỏi favorite
                    NotificationHelper.removeFavorite(this, safeId);
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("anime_" + safeId);

                    Log.d("FAV_LOCAL","Removed favorite: " + safeId);
                }
            });
        });
// ================= TRAILER =================
// Gọi saveHistory đầy đủ 22 tham số, lấy chính xác từ API
        btnTrailer.setOnClickListener(v -> {
            // 🔹 LOG kiểm tra dữ liệu trước khi lưu history
            Log.d("HISTORY_DEBUG", "saveHistory -> episodes=" + episodes + ", updatedAt=" + updatedAt);

            HistoryManager.saveHistory(
                    this,
                    animeId,        // animeId
                    title,
                    poster,
                    rating,
                    trailer,
                    studio,
                    director,
                    season,
                    duration,
                    format,
                    romajiTitle,
                    nativeTitle,
                    desc,
                    genres,
                    episodes,       // ✅ từ API
                    nextEpisode,
                    nextAiring,
                    status,
                    isAdult,
                    updatedAt,      // ✅ từ API
                    views
            );

            TrailerHelper.openTrailer(this, title, trailer, isAdult);
        });
        // ================= LANGUAGE =================

        btnEnglish.setOnClickListener(v -> {

            if (desc != null)
                txtDesc.setText(Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY));
        });

        btnVietnamese.setOnClickListener(v -> {

            if (!translatedDesc.isEmpty()) {

                txtDesc.setText(translatedDesc);
                return;
            }

            translateToVietnamese(desc);
        });
    }
    private void loadAnimeDetailFromApi(int id){

        String url = "https://graphql.anilist.co";

        String query = "{ \"query\": \"query ($id: Int) { Media(id: $id, type: ANIME) { " +
                "episodes nextAiringEpisode { episode airingAt } status } }\", " +
                "\"variables\": { \"id\": " + id + " } }";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    try {

                        JSONObject media = response
                                .getJSONObject("data")
                                .getJSONObject("Media");

                        int episodes = media.optInt("episodes", 0);
                        String status = media.optString("status", "");

                        int nextEp = 0;
                        long nextAiring = 0;

                        if(media.has("nextAiringEpisode") && !media.isNull("nextAiringEpisode")){
                            JSONObject airing = media.getJSONObject("nextAiringEpisode");
                            nextEp = airing.optInt("episode", 0);
                            nextAiring = airing.optLong("airingAt", 0);
                        }

                        // 🔥 UPDATE UI NGAY
                        updateEpisodeUI(episodes, nextEp, nextAiring, status);

                    } catch (Exception e){
                        e.printStackTrace();
                    }
                },
                error -> Log.e("API_ERROR", error.toString())
        ){

            @Override
            public byte[] getBody(){
                try{
                    return query.getBytes("utf-8");
                }catch (Exception e){
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }
    private void updateEpisodeUI(int episodes, int nextEp, long nextAiring, String status){

        if("FINISHED".equalsIgnoreCase(status)){
            txtEpisodes.setText("Episodes: " + episodes + " ✓ Completed");
            txtNextEpisode.setText("EP " + episodes);
            return;
        }

        if(nextEp > 0){
            int current = nextEp - 1;
            txtNextEpisode.setText("EP " + current + " • Next EP " + nextEp);
        }else if(episodes > 0){
            txtNextEpisode.setText("EP " + episodes);
        }else{
            txtNextEpisode.setText("EP ?");
        }

        txtEpisodes.setText("Episodes: " + (episodes > 0 ? episodes : "?"));
    }
    // ================= TRANSLATE =================

    private void translateToVietnamese(String text) {

        if (text == null || text.isEmpty()) return;

        String clean = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();

        txtDesc.setText("Đang dịch...");

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused ->

                        translator.translate(clean)
                                .addOnSuccessListener(translated -> {

                                    translatedDesc = translated;
                                    txtDesc.setText(translated);
                                })
                );
    }

}