package com.mari.magic.home;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mari.magic.utils.AnimeFirestoreParser;
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_anime,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){

        Map<String,Object> map = list.get(position);

        Anime anime = AnimeFirestoreParser.parse(map);

        String title = anime.getTitle();
        String poster = anime.getPoster();
        double rating = anime.getRating();

        holder.txtTitle.setText(title);
        holder.txtRating.setText("⭐ " + String.format("%.1f", rating));

        Glide.with(context)
                .load(poster)
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, AnimeDetailActivity.class);

            intent.putExtra("title", anime.getTitle());
            intent.putExtra("poster", anime.getPoster());
            intent.putExtra("rating", anime.getRating());

            intent.putExtra("romajiTitle", anime.getRomajiTitle());
            intent.putExtra("nativeTitle", anime.getNativeTitle());

            intent.putExtra("description", anime.getDescription());
            intent.putExtra("genres", anime.getGenres());
            intent.putExtra("trailer", anime.getTrailer());

            intent.putExtra("studio", anime.getStudio());
            intent.putExtra("director", anime.getDirector());
            intent.putExtra("season", anime.getSeason());
            intent.putExtra("duration", anime.getDuration());
            intent.putExtra("format", anime.getFormat());
            intent.putExtra("views", anime.getViews());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount(){
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imgPoster;
        TextView txtTitle;
        TextView txtRating;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtRating = itemView.findViewById(R.id.txtRating);
        }
    }
}