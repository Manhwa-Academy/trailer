package com.mari.magic.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mari.magic.R;
import com.mari.magic.model.Anime;
import com.mari.magic.ui.detail.AnimeDetailActivity;

import java.util.List;

public class RandomAnimeAdapter extends RecyclerView.Adapter<RandomAnimeAdapter.ViewHolder> {

    private final Context context;
    private final List<Anime> animeList;

    public RandomAnimeAdapter(Context context, List<Anime> animeList){
        this.context = context;
        this.animeList = animeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.item_anime, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        Anime anime = animeList.get(position);
        if(anime == null) return;

        // Poster
        Glide.with(context)
                .load(anime.getPoster() != null ? anime.getPoster() : "")
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .into(holder.imgPoster);

        // Title
        holder.txtTitle.setText(anime.getTitle() != null ? anime.getTitle() : "Unknown");

        // Rating badge
        if(holder.txtRating != null){
            double rating = anime.getRating();
            if(rating > 0){
                holder.txtRating.setVisibility(View.VISIBLE);
                holder.txtRating.setText("⭐ " + String.format("%.1f", rating));
            } else {
                holder.txtRating.setVisibility(View.GONE);
            }
        }

        // EP badge
        if(holder.txtEpisode != null){
            int nextEp = anime.getNextEpisode();
            int eps = anime.getEpisodes();
            if(nextEp > 0){
                holder.txtEpisode.setVisibility(View.VISIBLE);
                holder.txtEpisode.setText("EP " + (nextEp-1));
            } else if(eps > 0){
                holder.txtEpisode.setVisibility(View.VISIBLE);
                holder.txtEpisode.setText("EP " + eps);
            } else {
                holder.txtEpisode.setVisibility(View.GONE);
            }
        }

        // Click → mở Detail đầy đủ dữ liệu
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AnimeDetailActivity.class);

            intent.putExtra("title", anime.getTitle());
            intent.putExtra("englishTitle", anime.getEnglishTitle());
            intent.putExtra("romajiTitle", anime.getRomajiTitle());
            intent.putExtra("nativeTitle", anime.getNativeTitle());
            intent.putExtra("poster", anime.getPoster());
            intent.putExtra("format", anime.getFormat());
            intent.putExtra("episodes", anime.getEpisodes());

            // 🔹 Quan trọng: truyền nextEpisode và thời gian phát tập tiếp theo
            intent.putExtra("nextEpisode", anime.getNextEpisode());
            intent.putExtra("nextAiringAt", anime.getNextAiringAt());

            intent.putExtra("trailer", anime.getTrailer());
            intent.putExtra("season", anime.getSeason());
            intent.putExtra("duration", anime.getDuration());
            intent.putExtra("rating", anime.getRating());
            intent.putExtra("views", anime.getViews());
            intent.putExtra("description", anime.getDescription());
            intent.putExtra("genres", anime.getGenres());
            intent.putExtra("studio", anime.getStudio());
            intent.putExtra("director", anime.getDirector());
            intent.putExtra("author", anime.getAuthor());
            intent.putExtra("updatedAt", anime.getUpdatedAt());
            intent.putExtra("isAdult", anime.isAdult());
            intent.putExtra("status", anime.getStatus());
            intent.putExtra("chapters", anime.getChapters());
            intent.putExtra("volumes", anime.getVolumes());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount(){
        return animeList != null ? animeList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imgPoster;
        TextView txtTitle;
        TextView txtRating;
        TextView txtEpisode;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtEpisode = itemView.findViewById(R.id.txtEpisode);
        }
    }
}