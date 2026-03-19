package com.mari.magic;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import com.mari.magic.ui.manga.MangaFragment;
import com.mari.magic.adapter.DrawerMenuAdapter;
import com.mari.magic.adapter.GenreAdapter;
import com.mari.magic.model.DrawerMenuItem;
import com.mari.magic.ui.genre.GenreAnimeActivity;
import com.mari.magic.ui.history.HistoryFragment;
import com.mari.magic.ui.home.HomeFragment;
import com.mari.magic.ui.movies.MoviesFragment;
import com.mari.magic.ui.series.TvSeriesFragment;
import com.mari.magic.ui.settings.SettingsFragment;
import com.mari.magic.utils.LocaleManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import java.util.concurrent.TimeUnit;

import com.mari.magic.worker.AnimeWorker;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mari.magic.utils.NotificationHelper;
import com.mari.magic.utils.ThemeManager;
import com.mari.magic.utils.RandomAnimeLoader;
import com.mari.magic.utils.AppSettings;
import com.mari.magic.network.VolleySingleton;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mari.magic.utils.ScheduleHelper;
import com.mari.magic.utils.ScheduleListener;
import com.mari.magic.model.Anime;
import com.mari.magic.adapter.ScheduleAdapter;
import org.json.JSONArray;
import org.json.JSONObject;
import com.mari.magic.ui.component.PaginationView;
import java.util.ArrayList;
import java.util.List;
import com.mari.magic.adapter.SeasonAdapter;
import com.mari.magic.ui.season.SeasonAnimeActivity;
import com.mari.magic.model.Section;
import com.mari.magic.ui.home.SeeAllActivity;
import com.mari.magic.ui.novel.NovelFragment;
import com.mari.magic.ui.top.TopAnimeActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    BottomSheetDialog seasonDialog;

    BottomNavigationView bottomNavigation;
    DrawerLayout drawerLayout;
    Toolbar toolbar;

    RecyclerView menuRecycler;
    DrawerMenuAdapter adapter;
    List<DrawerMenuItem> menuList = new ArrayList<>();
    private TextView badgeText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleManager.setLocale(this, AppSettings.getLanguage(this));
        ThemeManager.applyTheme(this);

        Log.d(TAG,"Theme applied before onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bottomNavigation = findViewById(R.id.bottomNavigation);
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        menuRecycler = findViewById(R.id.menuRecycler);

        setSupportActionBar(toolbar);

        // Popup lần đầu
        if(!AppSettings.isSetupDone(this)){

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                openContentSettings();
            }, 500);

        }

        if(savedInstanceState == null){
            loadFragment(new HomeFragment());
        }

        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                                .setTitle("Thoát ứng dụng")
                                .setMessage("Bạn có muốn thoát khỏi app không?")
                                .setPositiveButton("Thoát",(d,w)->{
                                    finishAffinity();
                                })
                                .setNegativeButton("Hủy",null)
                                .show();
                    }
                });

        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment fragment;

            int id = item.getItemId();

            if(id == R.id.nav_home){
                fragment = new HomeFragment();
            }
            else if(id == R.id.nav_movies){
                fragment = new MoviesFragment();
            }
            else if(id == R.id.nav_tv){
                fragment = new TvSeriesFragment();
            }
            else if(id == R.id.nav_manga){
                MangaFragment mangaFragment = new MangaFragment();

// set chế độ mèo đen
                String filter = AppSettings.getContentFilter(this);
                mangaFragment.setBlackCatMode(filter.equals("18"));

                loadFragment(mangaFragment);
                return true;
            }
            else if(id == R.id.nav_novel){
                loadFragment(new NovelFragment());
                return true;
            }
            else if(id == R.id.nav_settings){
                fragment = new SettingsFragment();
            }
            else{
                return false;
            }

            loadFragment(fragment);
            return true;
        });
        startAnimeWorker();
        setupDrawerMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBadge();
    }
    // Drawer menu
    private void setupDrawerMenu(){

        menuRecycler.setLayoutManager(new LinearLayoutManager(this));

        menuList.add(new DrawerMenuItem(getString(R.string.menu_home),false));
        menuList.add(new DrawerMenuItem(getString(R.string.menu_new),false));
        menuList.add(new DrawerMenuItem(getString(R.string.menu_random),false));
        menuList.add(new DrawerMenuItem(getString(R.string.menu_history),false));
        menuList.add(new DrawerMenuItem(getString(R.string.menu_type),true));
        menuList.add(new DrawerMenuItem(getString(R.string.menu_top),true));
        menuList.add(new DrawerMenuItem(getString(R.string.menu_genre),true));
        menuList.add(new DrawerMenuItem(getString(R.string.menu_season),true));
        menuList.add(new DrawerMenuItem(getString(R.string.menu_schedule),false));

        adapter = new DrawerMenuAdapter(this,menuList);
        menuRecycler.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {

            switch (position){

                case 0:
                    bottomNavigation.setSelectedItemId(R.id.nav_home);
                    break;

                case 1:

                    Intent intent = new Intent(this, SeeAllActivity.class);
                    intent.putExtra("section", Section.CAT_NEW);
                    startActivity(intent);

                    return;

                case 2:
                    RandomAnimeLoader.load(this);
                    break;

                case 3:
                    bottomNavigation.setSelectedItemId(R.id.nav_settings);
                    loadFragment(new HistoryFragment());
                    break;

                case 4:
                    loadAnimeTypes();
                    return;

                case 5:
                    openTopMenu();
                    return;
                case 6:
                    loadGenres();
                    return;

                case 7:
                    openSeasonMenu();
                    return;
                case 8:
                    openScheduleDialog(); // ✅ gọi đúng method mới
                    break;
            }

            drawerLayout.closeDrawer(GravityCompat.END);

        });
    }

    private void loadFragment(Fragment fragment){

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer,fragment)
                .commit();
    }
    private void loadAnimeTypes(){

        String url = "https://graphql.anilist.co";

        try{

            JSONObject body = new JSONObject();
            body.put("query",
                    "query { __type(name:\"MediaFormat\") { enumValues { name } } }"
            );

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,

                    response -> {

                        try{

                            JSONArray array = response
                                    .getJSONObject("data")
                                    .getJSONObject("__type")
                                    .getJSONArray("enumValues");

                            List<String> types = new ArrayList<>();

                            for(int i=0;i<array.length();i++){
                                types.add(array.getJSONObject(i).getString("name"));
                            }

                            openAnimeTypeMenu(types);

                        }catch(Exception e){
                            e.printStackTrace();
                        }

                    },

                    error -> Log.e("API","Type error "+error)
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void openAnimeTypeMenu(List<String> types){

        seasonDialog = new BottomSheetDialog(this);

        View view = getLayoutInflater().inflate(R.layout.bottom_type,null);

        RecyclerView recycler = view.findViewById(R.id.typeRecycler);

        recycler.setLayoutManager(new GridLayoutManager(this,2));

        GenreAdapter adapter = new GenreAdapter(types);

        recycler.setAdapter(adapter);

        adapter.setOnGenreClickListener(type -> {



            Intent intent = new Intent(this, com.mari.magic.ui.type.AnimeTypeActivity.class);
            intent.putExtra("format",type);
            startActivity(intent);

        });

    seasonDialog.setContentView(view);
seasonDialog.show();
    }
    private void openTopMenu(){

        seasonDialog = new BottomSheetDialog(this);

        View view = getLayoutInflater().inflate(R.layout.bottom_top_menu,null);

        RecyclerView recycler = view.findViewById(R.id.topRecycler);

        recycler.setLayoutManager(new GridLayoutManager(this,2));

        List<String> list = new ArrayList<>();

        list.add(getString(R.string.top_today));
        list.add(getString(R.string.top_month));
        list.add(getString(R.string.top_season));
        list.add(getString(R.string.top_year));
        list.add(getString(R.string.top_rating));

        GenreAdapter adapter = new GenreAdapter(list);

        recycler.setAdapter(adapter);

        adapter.setOnGenreClickListener(type -> {



            Intent intent = new Intent(this, TopAnimeActivity.class);
            intent.putExtra("topType", type);

            startActivity(intent);

        });

    seasonDialog.setContentView(view);
seasonDialog.show();
    }
    // Popup filter
    private void openContentSettings(){

        seasonDialog = new BottomSheetDialog(this);

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_content_filter,null);

        // LANGUAGE SWITCH
        MaterialSwitch switchLang =
                view.findViewById(R.id.switchLanguage);

        // FILTER DROPDOWN
        AutoCompleteTextView dropdown =
                view.findViewById(R.id.filterDropdown);

        // LOAD SAVED SETTINGS
        String lang = AppSettings.getLanguage(this);
        String filter = AppSettings.getContentFilter(this);

        if(lang.equals("vi")){
            switchLang.setChecked(true);
        }else{
            switchLang.setChecked(false);
        }

        // DROPDOWN ITEMS
        String[] items = {
                "An toàn",
                "Khơi gợi",
                "Mèo đen"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        items
                );

        dropdown.setAdapter(adapter);

        // SET CURRENT FILTER
        if(filter.equals("16")){
            dropdown.setText("Khơi gợi",false);
        }
        else if(filter.equals("18")){
            dropdown.setText("Mèo đen",false);
        }
        else{
            dropdown.setText("An toàn",false);
        }

        // SAVE SETTINGS
        view.findViewById(R.id.btnSaveSettings)
                .setOnClickListener(v -> {

                    // LANGUAGE
                    if(switchLang.isChecked()){
                        AppSettings.setLanguage(this,"vi");
                    }else{
                        AppSettings.setLanguage(this,"en");
                    }

                    // FILTER
                    String selected = dropdown.getText().toString();

                    if(selected.equals("Khơi gợi")){
                        AppSettings.setContentFilter(this,"16");
                    }
                    else if(selected.equals("Mèo đen")){
                        AppSettings.setContentFilter(this,"18");
                    }
                    else{
                        AppSettings.setContentFilter(this,"all");
                    }

                    AppSettings.setSetupDone(this);


                    recreate();
                });

    seasonDialog.setContentView(view);
seasonDialog.show();
    }
    // Toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.top_menu, menu);

        MenuItem item = menu.findItem(R.id.menu_notification);

        // ✅ KHAI BÁO actionView
        View actionView = item.getActionView();

        // ✅ chống null crash
        if(actionView == null){
            actionView = getLayoutInflater().inflate(R.layout.menu_badge, null);
            item.setActionView(actionView);
        }

        badgeText = actionView.findViewById(R.id.txtBadge);

        updateBadge();

        actionView.setOnClickListener(v -> {
            handleNotificationClick();
        });

        return true;
    }
    private void handleNotificationClick(){

        // 🔴 reset badge
        NotificationHelper.resetBadge(this);

        updateBadge();

        // 🔥 mở màn hình thông báo
        Intent intent = new Intent(this, com.mari.magic.ui.notification.NotificationActivity.class);
        startActivity(intent);
    }
    private void updateBadge(){
        int count = NotificationHelper.getBadge(this);

        if(badgeText == null) return;

        if(count > 0){
            badgeText.setText(count > 9 ? "9+" : String.valueOf(count));
            badgeText.setVisibility(View.VISIBLE);
            badgeText.setSelected(true);
        }else{
            badgeText.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();

        if(id == R.id.menu_nav){
            drawerLayout.openDrawer(GravityCompat.END);
            return true;
        }

        if(id == R.id.menu_filter){
            openContentSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void openSeasonMenu(){

        seasonDialog = new BottomSheetDialog(this);

        View view = getLayoutInflater().inflate(R.layout.bottom_season,null);

        RecyclerView recycler = view.findViewById(R.id.seasonRecycler);

        // ⭐ 4 cột để mỗi hàng có 4 season
        recycler.setLayoutManager(new GridLayoutManager(this,4));

        List<String> seasons = new ArrayList<>();

        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);

        for(int year = currentYear; year >= 1970; year--){

            seasons.add(getString(R.string.season_winter) + " " + year);
            seasons.add(getString(R.string.season_spring) + " " + year);
            seasons.add(getString(R.string.season_summer) + " " + year);
            seasons.add(getString(R.string.season_fall) + " " + year);

        }

        SeasonAdapter adapter = new SeasonAdapter(seasons);

        recycler.setAdapter(adapter);

        adapter.setOnSeasonClickListener(seasonText -> {


            try {

                Log.d("SEASON_CLICK","Clicked: " + seasonText);

                String season;
                int year;

                // lấy year
                year = Integer.parseInt(seasonText.substring(seasonText.length() - 4));

                // xác định season
                String text = seasonText.toLowerCase();

                if(text.contains("winter") || text.contains("đông")){
                    season = "WINTER";
                }
                else if(text.contains("spring") || text.contains("xuân")){
                    season = "SPRING";
                }
                else if(text.contains("summer") || text.contains("hạ")){
                    season = "SUMMER";
                }
                else{
                    season = "FALL";
                }

                Log.d("SEASON_PARSE","season=" + season + " year=" + year);

                Intent intent = new Intent(this, SeasonAnimeActivity.class);
                intent.putExtra("season",season);
                intent.putExtra("year",year);

                startActivity(intent);

            } catch (Exception e) {

                e.printStackTrace();
                Log.e("SEASON_ERROR","Parse error: " + seasonText);

            }

        });
    seasonDialog.setContentView(view);
seasonDialog.show();
    }
    // Load genres
    private void loadGenres(){

        String url = "https://graphql.anilist.co";

        try{

            JSONObject body = new JSONObject();
            body.put("query","query { GenreCollection }");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,

                    response -> {

                        try{

                            JSONArray array = response
                                    .getJSONObject("data")
                                    .getJSONArray("GenreCollection");

                            List<String> genres = new ArrayList<>();

                            for(int i=0;i<array.length();i++){
                                genres.add(array.getString(i));
                            }

                            openGenreMenu(genres);

                        }catch(Exception e){
                            e.printStackTrace();
                        }

                    },

                    error -> Log.e("API","Genre error "+error)
            );

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void openGenreMenu(List<String> genres){

        seasonDialog = new BottomSheetDialog(this);

        View view = getLayoutInflater().inflate(R.layout.bottom_genre,null);

        RecyclerView recycler = view.findViewById(R.id.genreAnimeRecycler);

        recycler.setLayoutManager(new GridLayoutManager(this,3));

        GenreAdapter genreAdapter = new GenreAdapter(genres);

        recycler.setAdapter(genreAdapter);

        genreAdapter.setOnGenreClickListener(genre -> {


            Intent intent = new Intent(this, GenreAnimeActivity.class);
            intent.putExtra("genre",genre);
            startActivity(intent);

        });

    seasonDialog.setContentView(view);
    seasonDialog.show();
    }

    private int currentPage = 1; // page hiện tại
    private List<Anime> fullScheduleList = new ArrayList<>(); // lưu toàn bộ anime đã load

    private void openScheduleDialog() {
        currentPage = 1; // reset
        fullScheduleList.clear();

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_schedule, null);

        RecyclerView recycler = view.findViewById(R.id.scheduleRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        PaginationView paginationView = view.findViewById(R.id.paginationView);

        dialog.setContentView(view);
        dialog.show();

        // click chuyển trang
        paginationView.setOnPageChangeListener(page -> {
            currentPage = page;
            displaySchedulePage(recycler, paginationView);
        });

        // load tất cả anime sắp chiếu và sắp xếp
        ScheduleHelper.loadScheduleAll(this, new ScheduleListener() {
            @Override
            public void onScheduleLoaded(List<Anime> scheduleList, int currentPageApi, int lastPage) {
                fullScheduleList.addAll(scheduleList);
                displaySchedulePage(recycler, paginationView);
            }

            @Override
            public void onScheduleError(String error) {
                Log.e("MainActivity","Schedule load error: "+error);
            }
        });
    }

    // Hiển thị anime theo page (20 anime / page)
    private void displaySchedulePage(RecyclerView recycler, PaginationView paginationView) {

        int pageSize = 20;

        // 🔥 tránh crash list rỗng
        if(fullScheduleList == null || fullScheduleList.isEmpty()){
            recycler.setAdapter(new ScheduleAdapter(new ArrayList<>()));
            paginationView.setPages(1,1);
            return;
        }

        int totalPages = (int) Math.ceil((double) fullScheduleList.size() / pageSize);

        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, fullScheduleList.size());

        // 🔥 đảm bảo không out of bounds
        if(start >= fullScheduleList.size()){
            start = 0;
            end = Math.min(pageSize, fullScheduleList.size());
        }

        List<Anime> pageList = fullScheduleList.subList(start, end);

        ScheduleAdapter adapter = new ScheduleAdapter(pageList);
        recycler.setAdapter(adapter);

        paginationView.setPages(currentPage, totalPages);
    }
    private void startAnimeWorker() {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest =
                new PeriodicWorkRequest.Builder(
                        AnimeWorker.class,
                        15, TimeUnit.MINUTES
                )
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "anime_check",
                ExistingPeriodicWorkPolicy.KEEP, // ✅ giữ cái cũ
                workRequest
        );
    }
}