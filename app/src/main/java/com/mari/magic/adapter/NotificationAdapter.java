package com.mari.magic.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mari.magic.R;
import com.mari.magic.model.NotificationItem;
import com.mari.magic.ui.detail.AnimeDetailActivity;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final Context context;
    private final List<NotificationItem> list;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateRunnable;

    public NotificationAdapter(Context context, List<NotificationItem> list){
        this.context = context;
        this.list = list;

        // cập nhật countdown từng giây
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateRunnable);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = list.get(position);

        holder.txtTitle.setText(item.getTitle());

        long now = System.currentTimeMillis() / 1000L;
        long nextAiringAt = item.getNextAiringAt(); // UNIX timestamp tập tiếp theo
        int nextEp = item.getEpisode();             // Tập sắp phát
        int currentEp = nextEp - 1;                // Tập vừa phát

        // --- Tính thời gian EP vừa phát ---
        long prevAiringAt = nextAiringAt - 7 * 86400; // giả sử 1 tuần giữa các tập

        long diffSeconds = now - prevAiringAt;
        String prevTimeStr;

        if(diffSeconds < 60){
            prevTimeStr = "vừa phát";
        } else if(diffSeconds < 3600){
            long minutes = diffSeconds / 60;
            prevTimeStr = minutes + " phút trước";
        } else if(diffSeconds < 86400){
            long hours = diffSeconds / 3600;
            prevTimeStr = hours + " giờ trước";
        } else {
            long days = diffSeconds / 86400;
            prevTimeStr = days + " ngày trước";
        }

        // --- Countdown cho EP tiếp theo ---
        long diffNext = nextAiringAt - now;
        String countdownStr;
        if(diffNext > 0){
            long days = diffNext / 86400;
            long hours = (diffNext % 86400) / 3600;
            long minutes = (diffNext % 3600) / 60;
            long seconds = diffNext % 60;
            countdownStr = (days > 0 ? days + "d " : "") +
                    String.format("%02d:%02d:%02d ⏳", hours, minutes, seconds);
        } else {
            countdownStr = "Đang phát";
        }

        // --- Set Text ---
        holder.txtCountdown.setText("EP " + currentEp + " đã phát: " + prevTimeStr);
        holder.txtNextEp.setText("Next EP " + nextEp + " • " + countdownStr);

        // --- Load poster ---
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgPoster);

        // --- Click chuyển Activity ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AnimeDetailActivity.class);

            intent.putExtra("title", item.getTitle());
            intent.putExtra("poster", item.getImageUrl());
            intent.putExtra("episodes", item.getEpisode());
            intent.putExtra("nextEpisode", nextEp);
            intent.putExtra("nextAiringAt", nextAiringAt);
            intent.putExtra("status", "RELEASING");
            intent.putExtra("isAdult", false);

            intent.putExtra("format", item.getFormat());
            intent.putExtra("season", item.getSeason());
            intent.putExtra("studio", item.getStudio());
            intent.putExtra("director", item.getDirector());
            intent.putExtra("duration", item.getDuration());
            intent.putExtra("rating", item.getRating());
            intent.putExtra("views", item.getViews());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("genres", item.getGenres());
            intent.putExtra("englishTitle", item.getEnglishTitle());
            intent.putExtra("romajiTitle", item.getRomajiTitle());
            intent.putExtra("nativeTitle", item.getNativeTitle());
            intent.putExtra("trailer", item.getTrailer());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtTitle, txtCountdown, txtNextEp; // <-- thêm txtNextEp

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtCountdown = itemView.findViewById(R.id.txtCountdown);
            txtNextEp = itemView.findViewById(R.id.txtNextEp); // <-- khai báo ở đây
        }
    }

    public void release(){
        handler.removeCallbacks(updateRunnable);
    }
}