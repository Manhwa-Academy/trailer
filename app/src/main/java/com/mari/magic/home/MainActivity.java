package com.mari.magic.home;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.RadioGroup;
import com.mari.magic.utils.LocaleManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.mari.magic.R;
import com.mari.magic.utils.ThemeManager;
import com.mari.magic.utils.RandomAnimeLoader;
import com.mari.magic.utils.AppSettings;
import com.mari.magic.network.VolleySingleton;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    BottomNavigationView bottomNavigation;
    DrawerLayout drawerLayout;
    Toolbar toolbar;

    RecyclerView menuRecycler;
    DrawerMenuAdapter adapter;
    List<DrawerMenuItem> menuList = new ArrayList<>();

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

            new Handler().postDelayed(() -> {
                openContentSettings();
            },500);

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
                                    System.exit(0);
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
            else if(id == R.id.nav_settings){
                fragment = new SettingsFragment();
            }
            else{
                return false;
            }

            loadFragment(fragment);
            return true;
        });

        setupDrawerMenu();
    }

    // Drawer menu
    private void setupDrawerMenu(){

        menuRecycler.setLayoutManager(new LinearLayoutManager(this));

        menuList.add(new DrawerMenuItem("TRANG CHỦ",false));
        menuList.add(new DrawerMenuItem("ANIME MỚI",false));
        menuList.add(new DrawerMenuItem("XEM ANIME NGẪU NHIÊN",false));
        menuList.add(new DrawerMenuItem("LỊCH SỬ XEM",false));
        menuList.add(new DrawerMenuItem("DẠNG ANIME",true));
        menuList.add(new DrawerMenuItem("TOP ANIME",true));
        menuList.add(new DrawerMenuItem("THỂ LOẠI",true));
        menuList.add(new DrawerMenuItem("SEASON",true));

        adapter = new DrawerMenuAdapter(this,menuList);
        menuRecycler.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> {

            String title = menuList.get(position).title;

            switch (title){

                case "TRANG CHỦ":
                    loadFragment(new HomeFragment());
                    break;

                case "ANIME MỚI":
                    loadFragment(new MoviesFragment());
                    break;

                case "XEM ANIME NGẪU NHIÊN":
                    RandomAnimeLoader.load(this);
                    break;

                case "LỊCH SỬ XEM":

                    bottomNavigation.setSelectedItemId(R.id.nav_settings);
                    loadFragment(new HistoryFragment());

                    break;

                case "THỂ LOẠI":
                    loadGenres();
                    return;
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

    // Popup filter
    private void openContentSettings(){

        BottomSheetDialog dialog = new BottomSheetDialog(this);

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
                "Nguy hiểm"
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
            dropdown.setText("Nguy hiểm",false);
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
                    else if(selected.equals("Nguy hiểm")){
                        AppSettings.setContentFilter(this,"18");
                    }
                    else{
                        AppSettings.setContentFilter(this,"all");
                    }

                    AppSettings.setSetupDone(this);

                    dialog.dismiss();
                    recreate();
                });

        dialog.setContentView(view);
        dialog.show();
    }
    // Toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.top_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if(item.getItemId() == R.id.menu_nav){
            drawerLayout.openDrawer(GravityCompat.END);
            return true;
        }

        if(item.getItemId() == R.id.menu_filter){
            openContentSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        BottomSheetDialog dialog = new BottomSheetDialog(this);

        View view = getLayoutInflater().inflate(R.layout.bottom_genre,null);

        RecyclerView recycler = view.findViewById(R.id.genreAnimeRecycler);

        recycler.setLayoutManager(new GridLayoutManager(this,3));

        GenreAdapter genreAdapter = new GenreAdapter(genres);

        recycler.setAdapter(genreAdapter);

        genreAdapter.setOnGenreClickListener(genre -> {

            dialog.dismiss();

            Intent intent = new Intent(this, GenreAnimeActivity.class);
            intent.putExtra("genre",genre);
            startActivity(intent);

        });

        dialog.setContentView(view);
        dialog.show();
    }
}