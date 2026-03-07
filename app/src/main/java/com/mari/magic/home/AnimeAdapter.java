package com.mari.magic.home;

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

        View view = LayoutInflater.from(context)
                .inflate(layoutId, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){

        Anime anime = list.get(position);

        String title = getBestTitle(anime);

        holder.title.setText(title);

        // rating đã là 0–10
        double rating = anime.getRating();
        holder.rating.setText("⭐ " + String.format("%.1f", rating));

        // Load GIF loading trước
        Glide.with(context)
                .asGif()
                .load(R.drawable.no_money)
                .into(holder.poster);

        // Delay rồi load poster anime
        holder.poster.postDelayed(() -> {

            Glide.with(context)
                    .load(anime.getPoster())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade())
                    .into(holder.poster);

        }, 600);

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

        intent.putExtra("isAdult", anime.isAdult());

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

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            poster = itemView.findViewById(R.id.imgPoster);
            title = itemView.findViewById(R.id.txtTitle);
            rating = itemView.findViewById(R.id.txtRating);
        }
    }
}