package com.mari.magic.ui.settings;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.bumptech.glide.Glide;

import com.mari.magic.R;
import com.mari.magic.BuildConfig;
import com.mari.magic.network.VolleySingleton;
import com.mari.magic.ui.base.BaseFragment;
import com.mari.magic.ui.favorite.FavoriteFragment;
import com.mari.magic.ui.history.HistoryFragment;
import com.mari.magic.ui.profile.ProfileFragment;
import com.mari.magic.utils.ThemeManager;
import com.mari.magic.utils.AppSettings;

import java.io.File;

public class SettingsFragment extends BaseFragment {

    private static final String TAG = "SettingsFragment";

    private CardView btnProfile;
    private CardView btnFavorites;
    private CardView btnHistory;
    private CardView btnLogout;
    private CardView btnClearCache;
    private CardView btnTheme;

    private SwitchMaterial switchSearchHistory;

    private TextView txtTheme;
    private TextView txtVersion;

    private ImageView iconTheme;

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        try {
            // =============== INIT BUTTONS =================
            btnProfile = view.findViewById(R.id.btnProfile);
            btnFavorites = view.findViewById(R.id.btnFavorites);
            btnHistory = view.findViewById(R.id.btnHistory);
            btnLogout = view.findViewById(R.id.btnLogout);
            btnClearCache = view.findViewById(R.id.btnClearCache);
            btnTheme = view.findViewById(R.id.btnTheme);

            txtTheme = view.findViewById(R.id.txtTheme);
            txtVersion = view.findViewById(R.id.txtVersion);
            iconTheme = view.findViewById(R.id.iconTheme);

            switchSearchHistory = view.findViewById(R.id.switchSearchHistory);

            // ===== VERSION =====
            txtVersion.setText(
                    getString(R.string.app_version, BuildConfig.VERSION_NAME)
            );

            updateThemeUI();
            applyBackgroundToRoot(view); // <<< truyền view vào

            // =============== SETUP =================
            setupProfile();
            setupFavorites();
            setupHistory();
            setupSearchHistoryToggle();
            setupThemeToggle();
            setupBackgroundToggle(view); // <<< truyền view fragmentRoot vào
            setupLogout();
            setupClearCache();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing SettingsFragment", e);
            Toast.makeText(getContext(),
                    "Settings error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        return view;
    }

    // ==============================
    // OPEN PROFILE
    // ==============================
    private void setupProfile() {

        if (btnProfile == null) return;

        btnProfile.setOnClickListener(v ->
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new ProfileFragment())
                        .addToBackStack(null)
                        .commit()
        );
    }

    // ==============================
    // FAVORITES
    // ==============================
    private void setupFavorites() {

        if (btnFavorites == null) return;

        btnFavorites.setOnClickListener(v ->
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new FavoriteFragment())
                        .addToBackStack(null)
                        .commit()
        );
    }

    // ==============================
    // HISTORY
    // ==============================
    private void setupHistory() {

        if (btnHistory == null) return;

        btnHistory.setOnClickListener(v ->
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new HistoryFragment())
                        .addToBackStack(null)
                        .commit()
        );
    }

    // ==============================
    // SEARCH HISTORY TOGGLE
    // ==============================
    private void setupSearchHistoryToggle() {

        if (switchSearchHistory == null) return;

        boolean enabled = AppSettings.isSearchHistoryEnabled(requireContext());
        switchSearchHistory.setChecked(enabled);

        switchSearchHistory.setOnCheckedChangeListener((buttonView, isChecked) -> {

            AppSettings.setSearchHistoryEnabled(requireContext(), isChecked);

            String msg = isChecked ?
                    getString(R.string.search_history_enabled) :
                    getString(R.string.search_history_disabled);

            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    // ==============================
    // THEME TOGGLE
    // ==============================
    private void setupThemeToggle() {

        if (btnTheme == null) return;

        btnTheme.setOnClickListener(v -> {

            int mode = ThemeManager.getTheme(requireContext());

            if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
                ThemeManager.saveTheme(requireContext(), AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                ThemeManager.saveTheme(requireContext(), AppCompatDelegate.MODE_NIGHT_YES);
            }

            updateThemeUI();

            if (getActivity() != null) {
                getActivity().recreate();
            }
        });
    }

    private void updateThemeUI() {

        int mode = ThemeManager.getTheme(requireContext());

        if (mode == AppCompatDelegate.MODE_NIGHT_YES) {

            txtTheme.setText(getString(R.string.theme_light));
            iconTheme.setImageResource(R.drawable.ic_lightmode);

        } else {

            txtTheme.setText(getString(R.string.theme_dark));
            iconTheme.setImageResource(R.drawable.ic_darkmode);
        }
    }

    // ==============================
    // LOGOUT
    // ==============================
    private void setupLogout() {

        if (btnLogout == null) return;

        btnLogout.setOnClickListener(v -> {

            FirebaseAuth.getInstance().signOut();

            Toast.makeText(getContext(),
                    getString(R.string.logout_success),
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(),
                    com.mari.magic.auth.WelcomeActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    // ==============================
    // CLEAR CACHE
    // ==============================
    private void setupClearCache() {

        if (btnClearCache == null) return;

        btnClearCache.setOnClickListener(v -> {

            try {

                File cacheDir = requireContext().getCacheDir();
                long size = getDirSize(cacheDir);

                double mb = size / (1024.0 * 1024.0);

                new Thread(() ->
                        Glide.get(requireContext()).clearDiskCache()
                ).start();

                Glide.get(requireContext()).clearMemory();

                VolleySingleton
                        .getInstance(requireContext())
                        .getRequestQueue()
                        .getCache()
                        .clear();
                String sizeText = String.format(java.util.Locale.US, "%.2f", mb);

                Toast.makeText(getContext(),
                        getString(R.string.cache_cleared, sizeText),
                        Toast.LENGTH_LONG).show();

                Log.d("CACHE", "Cleared " + sizeText + " MB");

            } catch (Exception e) {

                Log.e("CACHE", "Clear cache error", e);

                Toast.makeText(getContext(),
                        getString(R.string.cache_error),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupBackgroundToggle(View fragmentRoot) {
        CardView btnBackground = fragmentRoot.findViewById(R.id.btnBackground);
        if (btnBackground == null) return;

        btnBackground.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_background, null);

            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Chọn hình nền")
                    .setView(dialogView)
                    .setNegativeButton("Hủy", null)
                    .create();

            ImageView bg1 = dialogView.findViewById(R.id.bg1);
            ImageView bg2 = dialogView.findViewById(R.id.bg2);
            ImageView bg3 = dialogView.findViewById(R.id.bg3);
            ImageView bg4 = dialogView.findViewById(R.id.bg4);
            ImageView bg5 = dialogView.findViewById(R.id.bg5);

            View.OnClickListener listener = click -> {
                String selected = "default";
                if(click.getId() == R.id.bg1) selected = "anh1";
                else if(click.getId() == R.id.bg2) selected = "anh2";
                else if(click.getId() == R.id.bg3) selected = "anh3";
                else if(click.getId() == R.id.bg4) selected = "anh4";
                else if(click.getId() == R.id.bg5) selected = "anh5";

                // ✅ Lưu background mới
                AppSettings.setBackground(requireContext(), selected);

                Toast.makeText(getContext(), "Đã chọn hình nền: " + selected, Toast.LENGTH_SHORT).show();

                dialog.dismiss();

                // ✅ Apply ngay cho fragment hiện tại
                applyBackgroundToRoot(fragmentRoot);

                // 🔥 Recreate activity để HomeFragment & các fragment khác cập nhật background
                if(getActivity() != null){
                    getActivity().recreate();
                }
            };

            bg1.setOnClickListener(listener);
            bg2.setOnClickListener(listener);
            bg3.setOnClickListener(listener);
            bg4.setOnClickListener(listener);
            bg5.setOnClickListener(listener); // ✅ nhớ set listener cho ảnh 5

            dialog.show();
        });
    }

    // Áp dụng hình nền cho root layout fragment
    private void applyBackgroundToRoot(View root){
        if(root == null) return;

        String bg = AppSettings.getBackground(requireContext());

        switch(bg){
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
            // ✅ bỏ default → nếu bg không phải anh1~anh5, giữ nguyên background
        }
    }
    // ==============================
    // HELPER: GET DIR SIZE
    // ==============================
    private long getDirSize(File dir) {

        long size = 0;

        if (dir != null && dir.isDirectory()) {

            File[] files = dir.listFiles();

            if (files != null) {

                for (File file : files) {

                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += getDirSize(file);
                    }
                }
            }
        }

        return size;
    }
}