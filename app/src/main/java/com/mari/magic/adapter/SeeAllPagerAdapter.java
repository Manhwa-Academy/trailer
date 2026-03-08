package com.mari.magic.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.mari.magic.ui.home.SeeAllListFragment;

import java.util.List;

public class SeeAllPagerAdapter extends FragmentStateAdapter {

    private final List<String> sections;

    public SeeAllPagerAdapter(@NonNull FragmentActivity fa, List<String> sections){
        super(fa);
        this.sections = sections;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position){
        return SeeAllListFragment.newInstance(sections.get(position));
    }

    @Override
    public int getItemCount(){
        return sections.size();
    }
}