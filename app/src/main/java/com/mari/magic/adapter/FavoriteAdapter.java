package com.mari.magic.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mari.magic.R;
import com.mari.magic.model.Anime;
import com.mari.magic.ui.detail.AnimeDetailActivity;
import com.mari.magic.utils.AnimeFirestoreParser;
import com.mari.magic.utils.FavoriteManager;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private final List<Map<String,Object>> list;
    private final Context context;

    public FavoriteAdapter(List<Map<String,Object>> list, Context context){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_anime,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){

        Map<String,Object> map = list.get(position);
        Anime anime = AnimeFirestoreParser.parse(map);

        // ---------------- Title, Poster, Rating ----------------
        holder.txtTitle.setText(anime.getTitle());
        holder.txtRating.setText("⭐ " + String.format("%.1f", anime.getRating()));
        Glide.with(holder.imgPoster)
                .load(anime.getPoster())
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .into(holder.imgPoster);

        // ---------------- Episodes ----------------
        Object epObj = map.get("episodes");
        int episodes = 0;
        if(epObj instanceof Long) episodes = ((Long) epObj).intValue();
        else if(epObj instanceof Integer) episodes = (Integer) epObj;

        // ---------------- Status ----------------
        String status = map.get("status") != null ? (String) map.get("status") : "";

        String episodeText = "Episodes: " + (episodes > 0 ? episodes : "?");
        if("FINISHED".equalsIgnoreCase(status) && episodes > 0){
            episodeText += " ✓ Completed";
        }
        if(holder.txtEpisodes != null) holder.txtEpisodes.setText(episodeText);

        // ---------------- Next Episode ----------------
        int nextEpisode = 0;
        Object nextEpObj = map.get("nextAiringEpisode");
        if(nextEpObj instanceof Map){
            Map<String,Object> nextMap = (Map<String,Object>) nextEpObj;
            Object ep = nextMap.get("episode");
            if(ep instanceof Long) nextEpisode = ((Long) ep).intValue();
            else if(ep instanceof Integer) nextEpisode = (Integer) ep;
        }

        if(holder.txtNextEpisode != null){
            String nextEpText;
            if("FINISHED".equalsIgnoreCase(status) && episodes > 0){
                nextEpText = "EP " + episodes + " ✓ Completed";
            } else {
                nextEpText = "EP " + (nextEpisode > 0 ? nextEpisode - 1 : "?");
                if(nextEpisode > 0){
                    nextEpText += " • Next EP " + nextEpisode;
                }
            }
            holder.txtNextEpisode.setText(nextEpText);
        }

        // ---------------- UpdatedAt ----------------
        Object updObj = map.get("updatedAt");
        long updatedAt = 0;
        if(updObj instanceof Long) updatedAt = (Long) updObj;
        else if(updObj instanceof Integer) updatedAt = ((Integer) updObj).longValue();

        if(holder.txtUpdated != null){
            if(updatedAt > 0){
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                holder.txtUpdated.setText("Updated: " + sdf.format(new java.util.Date(updatedAt*1000)));
            } else {
                holder.txtUpdated.setText("Updated: Unknown");
            }
        }

        // ⚡ Final copies để lambda
        final int finalEpisodes = episodes;
        final int finalNextEpisode = nextEpisode;
        final String finalStatus = status;
        final long finalUpdatedAt = updatedAt;

        // ---------------- DELETE FAVORITE ----------------
        holder.btnDelete.setOnClickListener(v -> {
            String animeId = (String) map.get("animeId");
            FavoriteManager.removeFavorite(animeId);
            int pos = holder.getAdapterPosition();
            if(pos != RecyclerView.NO_POSITION){
                list.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, list.size());
            }
        });

        // ---------------- OPEN DETAIL ----------------
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AnimeDetailActivity.class);
            intent.putExtra("animeId", anime.getId());
            intent.putExtra("title", anime.getTitle());
            intent.putExtra("englishTitle", anime.getEnglishTitle());
            intent.putExtra("romajiTitle", anime.getRomajiTitle());
            intent.putExtra("nativeTitle", anime.getNativeTitle());
            intent.putExtra("poster", anime.getPoster());
            intent.putExtra("rating", anime.getRating());
            intent.putExtra("trailer", anime.getTrailer());
            intent.putExtra("description", anime.getDescription() != null ? anime.getDescription() : "");
            intent.putExtra("genres", anime.getGenres() != null ? anime.getGenres() : "");
            intent.putExtra("studio", anime.getStudio() != null ? anime.getStudio() : "Unknown");
            intent.putExtra("director", anime.getDirector() != null ? anime.getDirector() : "Unknown");
            intent.putExtra("season", anime.getSeason() != null ? anime.getSeason() : "Unknown");
            intent.putExtra("duration", anime.getDuration());
            intent.putExtra("format", anime.getFormat() != null ? anime.getFormat() : "Unknown");
            intent.putExtra("views", anime.getViews());

            // ⚡ dùng final variables
            intent.putExtra("episodes", finalEpisodes);
            intent.putExtra("nextEpisode", finalNextEpisode);
            intent.putExtra("status", finalStatus);
            intent.putExtra("updatedAt", finalUpdatedAt);

            intent.putExtra("nextAiringAt", anime.getNextAiringAt());
            intent.putExtra("isAdult", anime.isAdult());

            context.startActivity(intent);
        });

        // ---------------- LOG API ----------------
        Log.d("API_DEBUG", "episodes=" + map.get("episodes")
                + ", nextEpisode=" + map.get("nextAiringEpisode")
                + ", status=" + map.get("status")
                + ", updatedAt=" + map.get("updatedAt"));
    }

    @Override
    public int getItemCount(){
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPoster;
        ImageView btnDelete;
        TextView txtTitle;
        TextView txtRating;
        TextView txtEpisodes;
        TextView txtUpdated;
        TextView txtNextEpisode;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtRating = itemView.findViewById(R.id.txtRating);
            txtEpisodes = itemView.findViewById(R.id.txtEpisodes);
            txtUpdated = itemView.findViewById(R.id.txtUpdated);
            txtNextEpisode = itemView.findViewById(R.id.txtNextEpisode);
        }
    }
}