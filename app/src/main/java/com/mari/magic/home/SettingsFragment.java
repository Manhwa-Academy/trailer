package com.mari.magic.home;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.firebase.auth.FirebaseAuth;
import com.mari.magic.R;
import com.mari.magic.auth.LoginActivity;
import com.mari.magic.utils.ThemeManager;

import java.io.File;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private CardView btnFavorites;
    private CardView btnHistory;
    private CardView btnLogout;
    private CardView btnClearCache;
    private CardView btnTheme;

    private TextView txtTheme;
    private ImageView iconTheme;

    private SharedPreferences pref;

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

            pref = requireContext().getSharedPreferences("settings",0);

            updateThemeUI();

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
    // FAVORITE ANIME
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
    // WATCH HISTORY
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
    // THEME TOGGLE
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

            txtTheme.setText("Light Mode");
            iconTheme.setImageResource(R.drawable.ic_lightmode);

        }else{

            txtTheme.setText("Dark Mode");
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
                    "Logged out successfully",
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(),
                    com.mari.magic.auth.WelcomeActivity.class);

            // ⭐ Xóa toàn bộ stack
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

                File dir = requireActivity().getCacheDir();

                long size = getDirSize(dir);

                deleteDir(dir);

                double mb = size / (1024.0 * 1024.0);

                Toast.makeText(getContext(),
                        "Cleared "+String.format("%.2f",mb)+" MB cache",
                        Toast.LENGTH_LONG).show();

            }catch(Exception e){

                Toast.makeText(getContext(),
                        "Error clearing cache",
                        Toast.LENGTH_SHORT).show();
            }

        });
    }

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

    private boolean deleteDir(File dir){

        if(dir!=null && dir.isDirectory()){

            String[] children = dir.list();

            if(children!=null){

                for(String child : children){

                    boolean success = deleteDir(new File(dir,child));

                    if(!success) return false;
                }
            }
        }

        return dir!=null && dir.delete();
    }
}