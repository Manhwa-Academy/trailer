package com.mari.magic.home;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mari.magic.R;

import java.util.List;
import java.util.Map;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    List<Map<String,Object>> list;
    Context context;

    public FavoriteAdapter(List<Map<String,Object>> list, Context context){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_anime, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Map<String,Object> anime = list.get(position);

        String title = (String) anime.get("title");
        String poster = (String) anime.get("poster");
        String romaji = (String) anime.get("romajiTitle");
        String nativeTitle = (String) anime.get("nativeTitle");
        String desc = (String) anime.get("description");
        String genres = (String) anime.get("genres");
        String trailer = (String) anime.get("trailer");

        String studio = (String) anime.get("studio");
        String director = (String) anime.get("director");
        String season = (String) anime.get("season");
        String format = (String) anime.get("format");

        // duration
        Number durationNum = (Number) anime.get("duration");
        int duration = durationNum != null ? durationNum.intValue() : 0;

        // views
        Number viewsNum = (Number) anime.get("views");
        long views = viewsNum != null ? viewsNum.longValue() : 0;

        // rating
        Number ratingNum = (Number) anime.get("rating");
        double rating = 0;

        if(ratingNum != null){
            rating = ratingNum.doubleValue() / 10.0;
        }

        holder.txtRating.setText("⭐ " + String.format("%.1f", rating));
        holder.txtRating.setVisibility(View.VISIBLE);

        holder.txtTitle.setText(title);

        Glide.with(context)
                .load(poster)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .into(holder.imgPoster);

        double finalRating = rating;

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, AnimeDetailActivity.class);

            intent.putExtra("title", title);
            intent.putExtra("poster", poster);
            intent.putExtra("rating", finalRating);

            intent.putExtra("romajiTitle", romaji);
            intent.putExtra("nativeTitle", nativeTitle);

            intent.putExtra("description", desc);
            intent.putExtra("genres", genres);
            intent.putExtra("trailer", trailer);

            intent.putExtra("studio", studio);
            intent.putExtra("director", director);
            intent.putExtra("season", season);
            intent.putExtra("duration", duration);
            intent.putExtra("format", format);
            intent.putExtra("views", views);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imgPoster;
        TextView txtTitle;
        TextView txtRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtRating = itemView.findViewById(R.id.txtRating);
        }
    }
}