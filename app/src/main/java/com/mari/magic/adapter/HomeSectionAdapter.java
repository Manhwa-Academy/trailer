package com.mari.magic.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.mari.magic.R;
import com.mari.magic.banner.BannerManager;
import com.mari.magic.model.Section;
import com.mari.magic.ui.home.SeeAllActivity;
import com.mari.magic.ui.search.SearchActivity;

import java.util.List;
import com.mari.magic.ui.manga.MangaFragment;
public class HomeSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    Context context;
    List<Section> sectionList;

    public HomeSectionAdapter(Context ctx, List<Section> list){
        context = ctx;
        sectionList = list;
    }

    @Override
    public int getItemViewType(int position){
        return sectionList.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        if(viewType == Section.TYPE_SEARCH){

            View v = LayoutInflater.from(context)
                    .inflate(R.layout.item_search_bar,parent,false);

            return new SearchHolder(v);
        }

        if(viewType == Section.TYPE_BANNER){

            View v = LayoutInflater.from(context)
                    .inflate(R.layout.item_banner_slider,parent,false);

            return new BannerHolder(v);
        }

        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_anime_section,parent,false);

        return new SectionHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){

        Section section = sectionList.get(position);

        // ===== BANNER =====
        if(holder instanceof BannerHolder){

            BannerHolder h = (BannerHolder) holder;

            BannerManager manager =
                    new BannerManager(h.banner, h.dots);

            manager.setup(context);
        }

        // ===== SEARCH BAR =====
        else if(holder instanceof SearchHolder){

            SearchHolder h = (SearchHolder) holder;

            // Click search bar -> mở SearchActivity
            View.OnClickListener openSearch = v -> {
                Intent intent = new Intent(context, SearchActivity.class);
                context.startActivity(intent);
            };

            h.search.setOnClickListener(openSearch);
            h.itemView.setOnClickListener(openSearch);

            // Hiển thị icon clear khi có text
            h.search.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after){}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count){

                    if(s.length() > 0){
                        h.clear.setVisibility(View.VISIBLE);
                    }else{
                        h.clear.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s){}
            });

            h.clear.setOnClickListener(v -> h.search.setText(""));

            h.search.setOnEditorActionListener((v, actionId, event) -> {

                if(actionId == EditorInfo.IME_ACTION_SEARCH){

                    String keyword = h.search.getText().toString().trim();

                    if(!keyword.isEmpty()){

                        Intent intent = new Intent(context, SearchActivity.class);
                        intent.putExtra("keyword", keyword);

                        h.search.setText("");
                        h.clear.setVisibility(View.GONE);

                        context.startActivity(intent);
                    }
                }

                return true;
            });
        }

        // ===== ANIME SECTION =====
        else if(holder instanceof SectionHolder){

            SectionHolder h = (SectionHolder) holder;

            h.title.setText(section.getTitle());

            h.seeAll.setOnClickListener(v -> {

                Intent intent = new Intent(context, SeeAllActivity.class);
                intent.putExtra("section", section.getCategory());
                context.startActivity(intent);
            });

            AnimeAdapter adapter =
                    new AnimeAdapter(context, section.getAnimeList(), R.layout.item_anime);

            h.recycler.setLayoutManager(
                    new LinearLayoutManager(context,RecyclerView.HORIZONTAL,false));

            h.recycler.setAdapter(adapter);
        }
    }

    @Override
    public int getItemCount(){
        return sectionList.size();
    }

    // ================= SEARCH HOLDER =================

    static class SearchHolder extends RecyclerView.ViewHolder{

        EditText search;
        ImageView clear;

        SearchHolder(View v){
            super(v);

            search = v.findViewById(R.id.edtSearch);
            clear = v.findViewById(R.id.btnClearSearch);

            // Không mở bàn phím ở Home
            search.setFocusable(false);
            search.setFocusableInTouchMode(false);
            search.setCursorVisible(false);
        }
    }

    // ================= BANNER HOLDER =================

    static class BannerHolder extends RecyclerView.ViewHolder{

        ViewPager2 banner;
        LinearLayout dots;

        BannerHolder(View v){
            super(v);

            banner = v.findViewById(R.id.bannerSlider);
            dots = v.findViewById(R.id.dotsLayout);
        }
    }

    // ================= SECTION HOLDER =================

    static class SectionHolder extends RecyclerView.ViewHolder{

        TextView title;
        TextView seeAll;
        RecyclerView recycler;

        SectionHolder(View v){
            super(v);

            title = v.findViewById(R.id.txtTitle);
            seeAll = v.findViewById(R.id.txtSeeAll);
            recycler = v.findViewById(R.id.recyclerSection);
        }
    }
}