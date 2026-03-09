package com.mari.magic.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.bumptech.glide.Glide;

import com.mari.magic.R;
import com.mari.magic.network.VolleySingleton;
import com.mari.magic.ui.favorite.FavoriteFragment;
import com.mari.magic.ui.history.HistoryFragment;
import com.mari.magic.utils.ThemeManager;
import com.mari.magic.utils.AppSettings;

import java.io.File;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private CardView btnFavorites;
    private CardView btnHistory;
    private CardView btnLogout;
    private CardView btnClearCache;
    private CardView btnTheme;

    private SwitchMaterial switchSearchHistory;

    private TextView txtTheme;
    private ImageView iconTheme;

    public SettingsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        try{

            btnFavorites = view.findViewById(R.id.btnFavorites);
            btnHistory = view.findViewById(R.id.btnHistory);
            btnLogout = view.findViewById(R.id.btnLogout);
            btnClearCache = view.findViewById(R.id.btnClearCache);
            btnTheme = view.findViewById(R.id.btnTheme);

            txtTheme = view.findViewById(R.id.txtTheme);
            iconTheme = view.findViewById(R.id.iconTheme);

            switchSearchHistory = view.findViewById(R.id.switchSearchHistory);

            updateThemeUI();

            setupSearchHistoryToggle();
            setupThemeToggle();
            setupLogout();
            setupClearCache();
            setupFavorites();
            setupHistory();

        }catch (Exception e){

            Log.e(TAG,"Error initializing SettingsFragment",e);

            Toast.makeText(getContext(),
                    "Settings error: "+e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        return view;
    }

    // =============================
    // FAVORITES
    // =============================

    private void setupFavorites(){

        if(btnFavorites == null) return;

        btnFavorites.setOnClickListener(v -> {

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new FavoriteFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    // =============================
    // HISTORY
    // =============================

    private void setupHistory(){

        if(btnHistory == null) return;

        btnHistory.setOnClickListener(v -> {

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new HistoryFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    // =============================
    // SEARCH HISTORY
    // =============================

    private void setupSearchHistoryToggle(){

        if(switchSearchHistory == null) return;

        boolean enabled = AppSettings.isSearchHistoryEnabled(requireContext());

        switchSearchHistory.setChecked(enabled);

        switchSearchHistory.setOnCheckedChangeListener((buttonView, isChecked) -> {

            AppSettings.setSearchHistoryEnabled(requireContext(), isChecked);

            if(isChecked){

                Toast.makeText(
                        getContext(),
                        "Search history enabled",
                        Toast.LENGTH_SHORT
                ).show();

            }else{

                Toast.makeText(
                        getContext(),
                        "Search history disabled",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // =============================
    // THEME
    // =============================

    private void setupThemeToggle(){

        if(btnTheme == null) return;

        btnTheme.setOnClickListener(v -> {

            int mode = ThemeManager.getTheme(requireContext());

            if(mode == AppCompatDelegate.MODE_NIGHT_YES){

                ThemeManager.saveTheme(
                        requireContext(),
                        AppCompatDelegate.MODE_NIGHT_NO
                );

            }else{

                ThemeManager.saveTheme(
                        requireContext(),
                        AppCompatDelegate.MODE_NIGHT_YES
                );
            }

            updateThemeUI();

            if(getActivity()!=null){
                getActivity().recreate();
            }
        });
    }

    private void updateThemeUI(){

        int mode = ThemeManager.getTheme(requireContext());

        if(mode == AppCompatDelegate.MODE_NIGHT_YES){

            txtTheme.setText(getString(R.string.theme_light));
            iconTheme.setImageResource(R.drawable.ic_lightmode);

        }else{

            txtTheme.setText(getString(R.string.theme_dark));
            iconTheme.setImageResource(R.drawable.ic_darkmode);
        }
    }

    // =============================
    // LOGOUT
    // =============================

    private void setupLogout(){

        if(btnLogout == null) return;

        btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            Toast.makeText(getContext(),
                    getString(R.string.logout_success),
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(
                    getActivity(),
                    com.mari.magic.auth.WelcomeActivity.class
            );

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            if(getActivity()!=null){
                getActivity().finish();
            }
        });
    }

    // =============================
    // CLEAR CACHE
    // =============================

    private void setupClearCache(){

        if(btnClearCache == null) return;

        btnClearCache.setOnClickListener(v -> {

            try{

                File cacheDir = requireContext().getCacheDir();

                long size = getDirSize(cacheDir);

                double mb = size / (1024.0 * 1024.0);

                new Thread(() -> {
                    Glide.get(requireContext()).clearDiskCache();
                }).start();

                Glide.get(requireContext()).clearMemory();

                VolleySingleton.getInstance(requireContext())
                        .getRequestQueue()
                        .getCache()
                        .clear();

                String sizeText = String.format("%.2f", mb);

                Toast.makeText(
                        getContext(),
                        getString(R.string.cache_cleared, sizeText),
                        Toast.LENGTH_LONG
                ).show();

                Log.d("CACHE","Cleared " + sizeText + " MB");

            }catch(Exception e){

                Log.e("CACHE","Clear cache error",e);

                Toast.makeText(
                        getContext(),
                        getString(R.string.cache_error),
                        Toast.LENGTH_SHORT
                ).show();
            }

        });
    }

    // =============================
    // CACHE SIZE
    // =============================

    private long getDirSize(File dir){

        long size = 0;

        if(dir!=null && dir.isDirectory()){

            File[] files = dir.listFiles();

            if(files!=null){

                for(File file : files){

                    if(file.isFile()){
                        size += file.length();
                    }else{
                        size += getDirSize(file);
                    }
                }
            }
        }

        return size;
    }
}