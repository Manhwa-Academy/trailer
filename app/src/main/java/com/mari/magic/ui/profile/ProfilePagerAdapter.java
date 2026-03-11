package com.mari.magic.ui.profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    public ProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new ProfileStatsFragment(); // Tổng quan
            case 1: return new ProfileEditFragment();  // Chỉnh sửa
            case 2: return new ProfileSecurityFragment(); // Bảo mật
            default: return new ProfileStatsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // 3 tab
    }
}