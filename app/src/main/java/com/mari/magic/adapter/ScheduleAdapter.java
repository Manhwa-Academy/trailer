package com.mari.magic.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mari.magic.R;
import com.mari.magic.model.Anime;
import com.mari.magic.ui.detail.AnimeDetailActivity;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<Anime> list;

    public ScheduleAdapter(List<Anime> list){
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        Anime anime = list.get(position);
        holder.title.setText(anime.getTitle() + " - Tập " + anime.getNextEpisode());

        String dateText = "Unknown";
        if(anime.getNextAiringAt() > 0){
            Date date = new Date(anime.getNextAiringAt() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd/MM HH:mm", Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            dateText = sdf.format(date);
        }

        holder.airingTime.setText(dateText);

        // Load poster
        Picasso.get().load(anime.getPoster()).into(holder.cover);

        // ================= CLICK OPEN DETAIL =================
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, AnimeDetailActivity.class);

            intent.putExtra("title", anime.getTitle());
            intent.putExtra("romajiTitle", anime.getRomajiTitle());
            intent.putExtra("englishTitle", anime.getEnglishTitle());
            intent.putExtra("nativeTitle", anime.getNativeTitle()); // ❌ thiếu dòng này
            intent.putExtra("poster", anime.getPoster());
            intent.putExtra("trailer", anime.getTrailer());
            intent.putExtra("rating", anime.getRating());
            intent.putExtra("description", anime.getDescription());
            intent.putExtra("genres", anime.getGenres());
            intent.putExtra("studio", anime.getStudio());
            intent.putExtra("director", anime.getDirector());
            intent.putExtra("season", anime.getSeason());
            intent.putExtra("format", anime.getFormat());
            intent.putExtra("duration", anime.getDuration());
            intent.putExtra("episodes", anime.getEpisodes());
            intent.putExtra("nextEpisode", anime.getNextEpisode());
            intent.putExtra("nextAiringAt", anime.getNextAiringAt());
            intent.putExtra("status", anime.getStatus());
            intent.putExtra("isAdult", anime.isAdult());
            intent.putExtra("views", anime.getViews());
            intent.putExtra("updatedAt", anime.getUpdatedAt());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title;
        TextView airingTime;
        public ViewHolder(View itemView){
            super(itemView);
            cover = itemView.findViewById(R.id.scheduleCover);
            title = itemView.findViewById(R.id.scheduleTitle);
            airingTime = itemView.findViewById(R.id.scheduleTime);
        }
    }
}