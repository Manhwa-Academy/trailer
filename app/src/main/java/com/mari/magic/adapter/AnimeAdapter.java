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
import com.mari.magic.R;
import com.mari.magic.model.Anime;
import com.mari.magic.ui.detail.AnimeDetailActivity;

import java.util.List;

public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.ViewHolder>{

    Context context;
    List<Anime> list;
    int layoutId;

    public AnimeAdapter(Context context, List<Anime> list, int layoutId){
        this.context = context;
        this.list = list;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){

        Anime anime = list.get(position);

        String title = getBestTitle(anime);
        holder.title.setText(title);

        // ⭐ Rating
        double rating = anime.getRating();
        holder.rating.setText("⭐ " + String.format("%.1f", rating));

        // 📺 Episode logic
        int episodes = anime.getEpisodes();
        int nextEpisode = anime.getNextEpisode();

        if(holder.episode != null){

            if(nextEpisode > 0){

                int currentEpisode = nextEpisode - 1;
                holder.episode.setVisibility(View.VISIBLE);
                holder.episode.setText("EP " + currentEpisode);

            }
            else if(episodes > 0){

                holder.episode.setVisibility(View.VISIBLE);
                holder.episode.setText("EP " + episodes);

            }
            else{

                holder.episode.setVisibility(View.GONE);
            }
        }

        // 🖼 Load poster (safe Glide)
        // loading gif
        Glide.with(holder.poster)
                .asGif()
                .load(R.drawable.no_money)
                .into(holder.poster);

// load poster sau 500ms
        holder.poster.postDelayed(() -> {

            Glide.with(holder.poster)
                    .load(anime.getPoster())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade())
                    .into(holder.poster);

        },500);

        holder.itemView.setOnClickListener(v -> openDetail(anime,title));
    }

    private void openDetail(Anime anime,String title){

        Intent intent = new Intent(context, AnimeDetailActivity.class);

        intent.putExtra("animeId", anime.getId());
        intent.putExtra("title", title);

        intent.putExtra("englishTitle", anime.getEnglishTitle());
        intent.putExtra("romajiTitle", anime.getRomajiTitle());
        intent.putExtra("nativeTitle", anime.getNativeTitle());

        intent.putExtra("poster", anime.getPoster());
        intent.putExtra("rating", anime.getRating());
        intent.putExtra("trailer", anime.getTrailer());

        intent.putExtra("description",
                anime.getDescription() != null ? anime.getDescription() : "");

        intent.putExtra("genres",
                anime.getGenres() != null ? anime.getGenres() : "");

        intent.putExtra("studio",
                anime.getStudio() != null ? anime.getStudio() : "Unknown");

        intent.putExtra("director",
                anime.getDirector() != null ? anime.getDirector() : "Unknown");

        intent.putExtra("season",
                anime.getSeason() != null ? anime.getSeason() : "Unknown");

        intent.putExtra("duration", anime.getDuration());

        intent.putExtra("format",
                anime.getFormat() != null ? anime.getFormat() : "Unknown");

        intent.putExtra("views", anime.getViews());
        intent.putExtra("updatedAt", anime.getUpdatedAt());

        intent.putExtra("isAdult", anime.isAdult());
        intent.putExtra("episodes", anime.getEpisodes());
        intent.putExtra("nextEpisode", anime.getNextEpisode());
        intent.putExtra("nextAiringAt", anime.getNextAiringAt());
        intent.putExtra("status", anime.getStatus());

        context.startActivity(intent);
    }

    @Override
    public int getItemCount(){
        return list != null ? list.size() : 0;
    }

    private String getBestTitle(Anime anime){

        if(anime.getEnglishTitle() != null && !anime.getEnglishTitle().isEmpty())
            return anime.getEnglishTitle();

        if(anime.getRomajiTitle() != null && !anime.getRomajiTitle().isEmpty())
            return anime.getRomajiTitle();

        if(anime.getNativeTitle() != null && !anime.getNativeTitle().isEmpty())
            return anime.getNativeTitle();

        if(anime.getTitle() != null && !anime.getTitle().isEmpty())
            return anime.getTitle();

        return "Unknown";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView poster;
        TextView title;
        TextView rating;
        TextView episode;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            poster = itemView.findViewById(R.id.imgPoster);
            title = itemView.findViewById(R.id.txtTitle);
            rating = itemView.findViewById(R.id.txtRating);
            episode = itemView.findViewById(R.id.txtEpisode);
        }
    }
}

