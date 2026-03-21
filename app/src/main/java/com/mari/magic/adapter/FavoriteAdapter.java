package com.mari.magic.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mari.magic.R;
import com.mari.magic.model.Anime;
import com.mari.magic.ui.detail.AnimeDetailActivity;
import com.mari.magic.ui.notification.NotificationActivity;
import com.mari.magic.utils.AnimeFirestoreParser;
import com.mari.magic.utils.FavoriteManager;
import com.mari.magic.utils.NotificationHelper;

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

// ---------- TITLE + POSTER + RATING ----------
        holder.txtTitle.setText(anime.getTitle());
        holder.txtRating.setText("⭐ " + String.format("%.1f", anime.getRating()));

        Glide.with(holder.imgPoster)
                .load(anime.getPoster())
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .into(holder.imgPoster);

// ---------- EPISODES ----------
        int episodes = 0;
        Object epObj = map.get("episodes");
        if(epObj instanceof Long) episodes = ((Long) epObj).intValue();
        else if(epObj instanceof Integer) episodes = (Integer) epObj;

// ---------- NEXT EPISODE ----------
        int nextEpisode = 0;
        Object nextObj = map.get("nextEpisode");
        if(nextObj instanceof Long) nextEpisode = ((Long) nextObj).intValue();
        else if(nextObj instanceof Integer) nextEpisode = (Integer) nextObj;
// Cập nhật episodes tự động nếu nextEpisode mới hơn
        if(nextEpisode > 0 && episodes < nextEpisode - 1){
            FavoriteManager.updateEpisodesFromNext((String) map.get("animeId"), nextEpisode);
            episodes = nextEpisode - 1; // cập nhật ngay trong adapter để hiển thị đúng
        }
// ---------- NEXT AIRING ----------
        long nextAiringAt = 0;
        Object airingObj = map.get("nextAiringAt");
        if(airingObj instanceof Long) nextAiringAt = (Long) airingObj;
        else if(airingObj instanceof Integer) nextAiringAt = ((Integer) airingObj).longValue();

// ---------- STATUS ----------
        String status = map.get("status") != null ? (String) map.get("status") : "";

// ---------- TÍNH CURRENT EPISODE CHUẨN ----------
        int currentEpisode;
        if(nextEpisode > 0){
            currentEpisode = nextEpisode - 1;
        } else if(episodes > 0){
            currentEpisode = episodes;
        } else {
            currentEpisode = 0;
        }

// Luôn update episodes để hiển thị mới nhất
        if(nextEpisode > 0 && episodes != nextEpisode - 1){
            episodes = nextEpisode - 1;
            FavoriteManager.updateEpisodesFromNext((String) map.get("animeId"), nextEpisode);
        }

// ---------- TẠO TEXT HIỂN THỊ ----------
        String episodeText;
        if("FINISHED".equalsIgnoreCase(status) && episodes > 0){
            episodeText = "EP " + episodes + " ✓ Completed";
        } else if(nextEpisode > 0){
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            String nextTime = nextAiringAt > 0 ? sdf.format(new java.util.Date(nextAiringAt * 1000)) : "Unknown";
            episodeText = "EP " + currentEpisode + " • Next EP " + nextEpisode + " (" + nextTime + ")";
        } else if(episodes > 0){
            episodeText = "EP " + episodes;
        } else {
            episodeText = "EP ?";
        }

// ---------- SET UI ----------
        holder.txtEpisodes.setText(episodeText);
        if(holder.txtNextEpisode != null){
            holder.txtNextEpisode.setText(episodeText);
        }

// ---------- UPDATED ----------
        long updatedAt = 0;
        Object updObj = map.get("updatedAt");
        if(updObj instanceof Long) updatedAt = (Long) updObj;
        else if(updObj instanceof Integer) updatedAt = ((Integer) updObj).longValue();

        if(holder.txtUpdated != null){
            if(updatedAt > 0){
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                holder.txtUpdated.setText("Updated: " + sdf.format(new java.util.Date(updatedAt * 1000)));
            } else {
                holder.txtUpdated.setText("Updated: Unknown");
            }
        }

// ------- DELETE FAVORITE ----------
        holder.btnDelete.setOnClickListener(v -> {
            String animeId = (String) map.get("animeId");

            // Xóa favorite + xóa notification trong SharedPreferences
            FavoriteManager.removeFavorite(animeId, v.getContext());
            NotificationHelper.removeNotificationForAnime(v.getContext(), animeId);

            // Xóa item khỏi list UI favorite
            int pos = holder.getAdapterPosition();
            if(pos != RecyclerView.NO_POSITION){
                list.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, list.size());
            }

            // 🔹 Thông báo cho NotificationActivity reload list
            if(context instanceof NotificationActivity){
                ((NotificationActivity) context).loadData(); // reload lại RecyclerView
            }
        });

// ---------- OPEN DETAIL ----------
        final int finalEpisodes = episodes;
        final int finalNextEpisode = nextEpisode;
        final long finalNextAiringAt = nextAiringAt;
        final String finalStatus = status;
        final long finalUpdatedAt = updatedAt;

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AnimeDetailActivity.class);

            intent.putExtra("anilistId", anime.getAnilistId());
            intent.putExtra("title", anime.getTitle());
            intent.putExtra("englishTitle", anime.getEnglishTitle());
            intent.putExtra("romajiTitle", anime.getRomajiTitle());
            intent.putExtra("nativeTitle", anime.getNativeTitle());

            intent.putExtra("poster", anime.getPoster());
            intent.putExtra("rating", anime.getRating());
            intent.putExtra("trailer", anime.getTrailer());

            intent.putExtra("description", anime.getDescription());
            intent.putExtra("genres", anime.getGenres());

            intent.putExtra("studio", anime.getStudio());
            intent.putExtra("director", anime.getDirector());
            intent.putExtra("season", anime.getSeason());
            intent.putExtra("format", anime.getFormat());

            intent.putExtra("duration", anime.getDuration());
            intent.putExtra("views", anime.getViews());

            // Gửi dữ liệu mới nhất để AnimeDetail luôn hiển thị EP cập nhật
            intent.putExtra("episodes", finalEpisodes);
            intent.putExtra("nextEpisode", finalNextEpisode);
            intent.putExtra("nextAiringAt", finalNextAiringAt);
            intent.putExtra("status", finalStatus);
            intent.putExtra("updatedAt", finalUpdatedAt);
            intent.putExtra("isAdult", anime.isAdult());

            context.startActivity(intent);
        });

// ---------- DEBUG ----------
        Log.d("FAV_DEBUG",
                "episodes=" + episodes +
                        ", nextEpisode=" + nextEpisode +
                        ", currentEpisode=" + currentEpisode +
                        ", status=" + status
        );
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