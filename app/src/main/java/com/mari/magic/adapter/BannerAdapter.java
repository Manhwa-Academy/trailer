package com.mari.magic.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mari.magic.R;
import com.mari.magic.model.Banner;
import com.mari.magic.ui.detail.AnimeDetailActivity;
import com.mari.magic.utils.HistoryManager;
import com.mari.magic.utils.TrailerHelper;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final Context context;
    private final List<Banner> bannerList;

    public BannerAdapter(Context context, List<Banner> bannerList){
        this.context = context;
        this.bannerList = bannerList;
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder{

        ImageView bannerImage;

        public BannerViewHolder(@NonNull View itemView){
            super(itemView);
            bannerImage = itemView.findViewById(R.id.bannerImage);
        }
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner,parent,false);

        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder,int position){

        final Banner banner = bannerList.get(position);

        Glide.with(holder.bannerImage)
                .load(banner.getImageUrl())
                .placeholder(R.drawable.no_money)
                .error(R.drawable.no_money)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.bannerImage);

        holder.bannerImage.setOnClickListener(v -> {

            int adapterPosition = holder.getAdapterPosition();
            if(adapterPosition == RecyclerView.NO_POSITION) return;

            // ---------- SAVE HISTORY ----------
            HistoryManager.saveBannerHistory(
                    context,
                    banner.getId(),
                    banner.getTitle(),
                    banner.getImageUrl(),
                    banner.getTrailerUrl(),
                    banner.getStudio(),
                    banner.getDirector(),
                    banner.getSeason(),
                    banner.getDuration(),
                    banner.getFormat(),
                    banner.getEpisodes(),
                    banner.getStatus(),
                    banner.getUpdatedAt(),
                    banner.getRating(),
                    banner.getDescription(),
                    banner.getGenres(),
                    banner.getNextEpisode(),
                    banner.getNextAiringAt(),
                    banner.getRomajiTitle(),
                    banner.getNativeTitle()
            );

            // ---------- OPEN DETAIL ----------
            Intent intent = new Intent(context, AnimeDetailActivity.class);

            intent.putExtra("title", banner.getTitle());
            intent.putExtra("romajiTitle", banner.getRomajiTitle());
            intent.putExtra("nativeTitle", banner.getNativeTitle());

            intent.putExtra("poster", banner.getImageUrl());
            intent.putExtra("rating", banner.getRating());
            intent.putExtra("trailer", banner.getTrailerUrl());

            intent.putExtra("description", banner.getDescription());
            intent.putExtra("genres", banner.getGenres());

            intent.putExtra("studio", banner.getStudio());
            intent.putExtra("director", banner.getDirector());
            intent.putExtra("season", banner.getSeason());

            intent.putExtra("duration", banner.getDuration());
            intent.putExtra("episodes", banner.getEpisodes());
            intent.putExtra("status", banner.getStatus());

            intent.putExtra("nextEpisode", banner.getNextEpisode());
            intent.putExtra("nextAiringAt", banner.getNextAiringAt());

            intent.putExtra("updatedAt", banner.getUpdatedAt());

            context.startActivity(intent);

            // ---------- OPEN TRAILER ----------
            TrailerHelper.openTrailer(
                    context,
                    banner.getTitle(),
                    banner.getTrailerUrl(),
                    false
            );

        });
    }

    @Override
    public int getItemCount(){
        return bannerList != null ? bannerList.size() : 0;
    }
}