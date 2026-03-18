package com.mari.magic.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.*;
import com.mari.magic.model.Anime;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.nl.translate.*;
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

        animeId = title.replace("/", "_");

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
        if(chapters > 0)
            txtChapters.setText("Chapters: " + chapters);
        else
            txtChapters.setText("Chapters: ?");

        if(volumes > 0)
            txtVolumes.setText("Volumes: " + volumes);
        else
            txtVolumes.setText("Volumes: ?");

        if(author != null && !author.isEmpty())
            txtAuthor.setText("Author: " + author);
        else
            txtAuthor.setText("Author: Unknown");
        if (episodes > 0) {
            txtEpisodes.setText("Episodes: " + episodes);
        } else {
            txtEpisodes.setText("Episodes: ?");
        }

        int nextEpisode = getIntent().getIntExtra("nextEpisode",0);
        long nextAiring = getIntent().getLongExtra("nextAiringAt",0);
        String status = getIntent().getStringExtra("status");
        if("FINISHED".equalsIgnoreCase(status)){

            txtNextEpisode.setText("EP " + episodes + " ✓ Completed");

        }
        else if(nextEpisode > 0 && nextAiring > 0){

            int currentEpisode = nextEpisode - 1;

            long now = System.currentTimeMillis() / 1000;
            long diff = nextAiring - now;

            if(diff > 0){

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

            }

        }
        else if(nextEpisode > 0){

            txtNextEpisode.setText("EP " + (nextEpisode - 1));

        }
        else{

            txtNextEpisode.setText("Episode ?");
        }

        txtFormat.setText("Type: " + format);


        if("FINISHED".equalsIgnoreCase(status)){
            txtEpisodes.setText("Episodes: " + episodes + " ✓ Completed");
        }
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        txtViews.setText("Views: " + nf.format(views));

        // ================= UPDATED =================

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
// Tạo favoriteId cố định dựa trên API ID hoặc title + romaji
        String safeTitle = title != null ? title.replace("/", "_") : "";
        String safeRomaji = romajiTitle != null ? romajiTitle.replace("/", "_") : "";

        String favoriteId = (safeTitle + "_" + safeRomaji)
                .replaceAll("[^a-zA-Z0-9_]", "_"); // 🔥 lọc sạch toàn bộ ký tự nguy hiểm
// Kiểm tra trạng thái favorite
        FavoriteManager.checkFavorite(favoriteId, btnFavorite, txtFavorite);

        btnFavorite.setOnClickListener(v -> {
            // Tạo Anime object đầy đủ từ dữ liệu API
            Anime anime = new Anime();
            anime.setId(favoriteId.hashCode());      // ID cố định
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
            anime.setEpisodes(episodes);             // ✅ Lấy từ API
            anime.setNextEpisode(nextEpisode);
            anime.setNextAiringAt(nextAiring);
            anime.setStatus(status);
            anime.setAdult(isAdult);
            anime.setViews(views);
            anime.setUpdatedAt(updatedAt);           // ✅ Lấy từ API

            // 🔹 LOG kiểm tra dữ liệu trước khi lưu vào Firestore
            Log.d("FAV_DEBUG", "Before toggleFavorite -> episodes=" + anime.getEpisodes() + ", updatedAt=" + anime.getUpdatedAt());

            // Gọi toggle favorite với favoriteId cố định
            FavoriteManager.toggleFavorite(this, anime, btnFavorite, txtFavorite, favoriteId);
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