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
import com.mari.magic.R;
import com.mari.magic.ui.detail.AnimeTrailerActivity;
import com.mari.magic.model.Banner;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    Context context;
    List<Banner> bannerList;

    public BannerAdapter(Context context, List<Banner> bannerList) {
        this.context = context;
        this.bannerList = bannerList;
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {

        ImageView bannerImage;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);

            bannerImage = itemView.findViewById(R.id.bannerImage);
        }
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_banner, parent, false);

        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {

        Banner banner = bannerList.get(position);

        Glide.with(context)
                .load(banner.getImageUrl())
                .into(holder.bannerImage);

        // Click banner → mở trailer
        holder.bannerImage.setOnClickListener(v -> {

            Intent intent = new Intent(context, AnimeTrailerActivity.class);
            intent.putExtra("trailer", banner.getTrailerUrl());
            context.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }
}