package com.mari.magic.ui.home;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mari.magic.R;
import com.mari.magic.adapter.SeeAllPagerAdapter;
import com.mari.magic.model.Section;
import com.mari.magic.utils.AppSettings;

import java.util.ArrayList;
import java.util.List;

public class SeeAllActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager;
    TextView title;

    List<String> sections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all);

        title = findViewById(R.id.txtTitle);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        title.setText(getString(R.string.app_name));

        String filter = AppSettings.getContentFilter(this);
        boolean safeMode = filter.equals("all");
        // ===== TAB LIST =====

        sections.add(Section.CAT_NEW);        // ⭐ Anime mới
        sections.add(Section.CAT_MOVIES);
        sections.add(Section.CAT_SERIES);
        sections.add(Section.CAT_TOP_TV);
        sections.add(Section.CAT_RELEASING);
        sections.add(Section.CAT_UPCOMING);

        if("16".equals(filter) || "18".equals(filter)){
            sections.add(Section.CAT_ECCHI);
        }

        if("18".equals(filter)){
            sections.add(Section.CAT_ADULT);
        }

        SeeAllPagerAdapter adapter = new SeeAllPagerAdapter(this, sections);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {

            String section = sections.get(position);

            switch(section){

                case Section.CAT_NEW:
                    tab.setText(getString(R.string.menu_new));
                    break;

                case Section.CAT_MOVIES:
                    tab.setText(getString(R.string.tab_movies));
                    break;

                case Section.CAT_SERIES:
                    tab.setText(getString(R.string.tab_series));
                    break;

                case Section.CAT_TOP_TV:
                    tab.setText(getString(R.string.tab_top_tv));
                    break;

                case Section.CAT_RELEASING:
                    tab.setText(getString(R.string.tab_releasing));
                    break;

                case Section.CAT_UPCOMING:
                    tab.setText(getString(R.string.tab_upcoming));
                    break;

                case Section.CAT_ECCHI:
                    tab.setText(getString(R.string.tab_ecchi));
                    break;

                case Section.CAT_ADULT:
                    tab.setText(getString(R.string.tab_adult));
                    break;
            }

        }).attach();

        // ===== mở đúng tab =====

        String openSection = getIntent().getStringExtra("section");

        int startTab = 0;

        if(openSection != null){

            for(int i = 0; i < sections.size(); i++){

                if(sections.get(i).equals(openSection)){
                    startTab = i;
                    break;
                }

            }

        }

        viewPager.setCurrentItem(startTab,false);
    }
}