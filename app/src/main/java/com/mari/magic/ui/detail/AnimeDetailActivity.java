package com.mari.magic.ui.detail;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.*;
import androidx.browser.customtabs.CustomTabsIntent;

import com.android.volley.DefaultRetryPolicy;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mari.magic.MainActivity;
import com.mari.magic.adapter.RandomAnimeAdapter;
import com.mari.magic.model.Anime;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Button;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.nl.translate.*;
import com.mari.magic.ui.notification.NotificationActivity;
import com.mari.magic.utils.AnimeParser;
import com.mari.magic.utils.AppSettings;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
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
    // RecyclerView + Adapter + List
    private RecyclerView recyclerRandomAnime;
    private RandomAnimeAdapter randomAdapter;
    private final List<Anime> randomAnimeList = new ArrayList<>();
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
        applyBackground();
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
        recyclerRandomAnime = findViewById(R.id.recyclerRandomAnime);
        recyclerRandomAnime.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        randomAdapter = new RandomAnimeAdapter(this, randomAnimeList); // <-- gán vào field
        recyclerRandomAnime.setAdapter(randomAdapter);
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
        randomAnimeList.clear();
        randomAdapter.notifyDataSetChanged(); // này chỉ để reset view cũ trước khi load API
        loadRandomAnimeFromApi(); // 🔹 gọi API AniList
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
        // Gán nút Watch
        Button btnWatch = findViewById(R.id.btnWatch);

// Hiển thị nút Watch nếu anime là TV/Anime, bất kể có trailer hay không
        if(title != null && format != null && !format.equalsIgnoreCase("MANGA")) {
            btnWatch.setVisibility(View.VISIBLE);
        } else {
            btnWatch.setVisibility(View.GONE);
        }

// Click mở AnimeVietSub (tìm kiếm Romaji + Native + English)
        btnWatch.setOnClickListener(v -> {

            List<String> searchNames = new ArrayList<>();

            if (romajiTitle != null && !romajiTitle.isEmpty()) searchNames.add(romajiTitle);
            if (nativeTitle != null && !nativeTitle.isEmpty()) searchNames.add(nativeTitle);
            if (englishTitle != null && !englishTitle.isEmpty()) searchNames.add(englishTitle);

            // Ghép tất cả tên thành 1 chuỗi, chuyển thành keyword
            StringBuilder sb = new StringBuilder();
            for (String name : searchNames) {
                String clean = name.toLowerCase()
                        .replaceAll("season|part|movie|final", "")
                        .replaceAll("[^a-z0-9\\s]", "")
                        .trim()
                        .replaceAll("\\s+", "+");
                sb.append(clean).append("+");
            }

            String keyword = sb.toString();
            if (keyword.endsWith("+")) keyword = keyword.substring(0, keyword.length() - 1);

            String url = "https://animevietsub.mx/tim-kiem/" + keyword + "/";

            Log.d("ANIMEVIE_SUB", "Search URL: " + url);

            CustomTabsIntent customTabsIntent =
                    new CustomTabsIntent.Builder().build();

            customTabsIntent.launchUrl(this, Uri.parse(url));
        });
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
        txtStudio.setText(getString(R.string.studio_label) + ": " + studio);
        txtDirector.setText(getString(R.string.director_label) + ": " + director);
        txtSeason.setText(getString(R.string.season_label) + ": " + season);
        txtDuration.setText(getString(R.string.duration_label) + ": " + duration + " phút");

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
        txtEpisodes.setText(episodes > 0
                ? getString(R.string.episodes_label) + ": " + episodes
                : getString(R.string.episodes_label) + ": " + getString(R.string.unknown_label));

// Hiển thị nextEpisode
        if ("FINISHED".equalsIgnoreCase(status)) {
            txtNextEpisode.setText(
                    getString(R.string.episodes_label) + " " + episodes + " " + getString(R.string.completed_label)
            );
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

                        txtNextEpisode.setText(String.format(
                                getString(R.string.next_episode_countdown),
                                getString(R.string.episodes_label),      // "Tập" / "Episodes"
                                currentEpisode,
                                getString(R.string.next_episode_label),  // "Tập tiếp theo" / "Next EP"
                                nextEpisode,
                                days,
                                hours,
                                minutes,
                                sec
                        ));
                    }

                    @Override
                    public void onFinish() {
                        txtNextEpisode.setText(
                                getString(R.string.episodes_label) + " " + nextEpisode + " • " + getString(R.string.airing_now)
                        );
                    }
                }.start();
            } else {
                txtNextEpisode.setText(
                        getString(R.string.episodes_label) + " " + currentEpisode +
                                " • " + getString(R.string.next_episode_label) + " " + nextEpisode
                );
            }
        } else if (nextEpisode > 0) {
            txtNextEpisode.setText(getString(R.string.episodes_label) + " " + currentEpisode);
        } else {
            txtNextEpisode.setText(getString(R.string.episodes_label) + " " + getString(R.string.unknown_label));
        }
// Type + Views
// Type
        txtFormat.setText(getString(R.string.type_label) + ": " + (format != null ? format : getString(R.string.unknown_label)));

// Views
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        txtViews.setText(getString(R.string.views_label) + ": " + nf.format(views));

// UpdatedAt
        if (updatedAt > 0) {
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            String time = sdf.format(new java.util.Date(updatedAt * 1000));
            txtUpdated.setText(getString(R.string.updated_label) + ": " + time);
        } else {
            txtUpdated.setText(getString(R.string.updated_label) + ": " + getString(R.string.unknown_label));
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

                    // 🔹 Gọi showNotification đầy đủ
                    NotificationHelper.showNotification(
                            this,
                            title,
                            "Bạn đã thêm vào yêu thích 🔥",
                            new Intent(this, NotificationActivity.class),
                            poster != null ? poster : "",
                            nextAiring > 0 ? nextAiring : System.currentTimeMillis() / 1000,
                            nextEpisode,
                            format,
                            season,
                            studio,
                            director,
                            duration,
                            rating,
                            views,
                            desc,
                            genres,
                            englishTitle,
                            romajiTitle,
                            nativeTitle,
                            trailer
                    );

                    // Subscribe topic FCM để nhận notification tập mới
                    FirebaseMessaging.getInstance().subscribeToTopic("anime_" + safeId);

                    Log.d("FAV_LOCAL","Added favorite & notified: " + safeId);

                } else {
                    // Xóa khỏi favorite
                    NotificationHelper.removeFavorite(this, safeId);

                    // 🔹 Xóa notification liên quan luôn
                    NotificationHelper.removeNotificationForAnime(this, safeId);

                    FirebaseMessaging.getInstance().unsubscribeFromTopic("anime_" + safeId);

                    Log.d("FAV_LOCAL","Removed favorite & notification: " + safeId);
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

        String query =
                "query ($id: Int) {" +
                        "  Media(id: $id, type: ANIME) {" +
                        "    episodes" +
                        "    nextAiringEpisode {" +
                        "      episode" +
                        "      airingAt" +
                        "    }" +
                        "    status" +
                        "  }" +
                        "}";

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

        long now = System.currentTimeMillis() / 1000L;
        int currentEpisode;
        int displayNextEp;

        if("FINISHED".equalsIgnoreCase(status)){
            txtEpisodes.setText("Episodes: " + episodes + " ✓ Completed");
            txtNextEpisode.setText("EP " + episodes);
            return;
        }

        if(nextEp > 0 && nextAiring > now){
            // Tập sắp phát → current = tập trước, next = tập sắp phát
            currentEpisode = nextEp - 1;
            displayNextEp = nextEp;

            long diff = nextAiring - now;
            long days = diff / 86400;
            long hours = (diff % 86400) / 3600;
            long minutes = (diff % 3600) / 60;
            long seconds = diff % 60;

            String countdown = (days > 0 ? days + "d " : "") +
                    String.format("%02d:%02d:%02d ⏳", hours, minutes, seconds);

            txtNextEpisode.setText("Next EP " + displayNextEp + " • " + countdown);
        } else if(nextEp > 0){
            // EP đã phát
            currentEpisode = nextEp - 1;
            displayNextEp = nextEp;

            txtNextEpisode.setText("EP " + currentEpisode + " • Next EP " + displayNextEp);
        } else if(episodes > 0){
            currentEpisode = episodes;
            txtNextEpisode.setText("EP " + currentEpisode);
        } else {
            currentEpisode = 0;
            txtNextEpisode.setText("EP ?");
        }

        txtEpisodes.setText("Episodes: " + (episodes > 0 ? episodes : "?"));
    }
    private void loadRandomAnimeFromApi() {

        String url = "https://graphql.anilist.co";

        String query = "query { " +
                "randomAnime: Page(page:1, perPage:20) { " +
                "  media(type:ANIME, sort:TRENDING_DESC) { " +
                "    id title { romaji english native } " +
                "    format season seasonYear duration episodes " +
                "    status averageScore description genres isAdult " +
                "    coverImage { large } " +
                "    nextAiringEpisode { episode airingAt } " +
                "    studios { nodes { name } } " +
                "    updatedAt " +
                "    staff(perPage:10) { edges { role node { name { full } } } } " +
                "    trailer { id site } " +
                "  } " +
                "} " +
                "}";

        try {
            JSONObject body = new JSONObject();
            body.put("query", query);
            body.put("variables", new JSONObject());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        try {
                            JSONArray mediaArray = response.getJSONObject("data")
                                    .getJSONObject("randomAnime")
                                    .getJSONArray("media");

                            randomAnimeList.clear();

                            for (int i = 0; i < mediaArray.length(); i++) {
                                JSONObject obj = mediaArray.getJSONObject(i);
                                Anime anime = AnimeParser.parse(obj, this); // <-- 'this' là Activity context
                                if (anime != null) randomAnimeList.add(anime);
                            }

                            // Shuffle + giới hạn 5 anime
                            Collections.shuffle(randomAnimeList);
                            if (randomAnimeList.size() > 5) {
                                randomAnimeList.subList(5, randomAnimeList.size()).clear();
                            }

                            randomAdapter.notifyDataSetChanged();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("RANDOM_API", "JSON ERROR " + e.getMessage());
                        }
                    },
                    error -> Log.e("RANDOM_API", "Volley error: " + error)
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept", "application/json");
                    headers.put("User-Agent", "MagicAnimeApp");
                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (Exception e) {
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