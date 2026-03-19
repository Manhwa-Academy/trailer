package com.mari.magic.adapter;

import android.content.Context;
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

        // Hiển thị tên anime
        holder.txtTitle.setText(item.getTitle());

        // Số tập
        String epText = item.getEpisode() > 0 ? "EP " + item.getEpisode() + " • " : "";

        // Thời gian hiện tại và airingAt thực tế
        long now = System.currentTimeMillis() / 1000L;
        long airingAt = item.getNextAiringAt(); // dùng giá trị chuẩn từ API / Firestore
        long diff = airingAt - now;

        String countdownText;

        if(airingAt <= 0){
            countdownText = "Không có lịch ⏳";
        } else if(diff > 0){
            // Tập chưa phát, hiển thị countdown
            long days = diff / 86400;
            long hours = (diff % 86400) / 3600;
            long minutes = (diff % 3600) / 60;
            long seconds = diff % 60;

            StringBuilder countdown = new StringBuilder();
            if(days > 0) countdown.append(days).append(" ngày ");
            countdown.append(String.format("%02d:%02d:%02d ⏳", hours, minutes, seconds));

            countdownText = countdown.toString();
        } else {
            // Tập đã phát, tính chính xác
            long passedSeconds = Math.abs(diff);
            long minutes = passedSeconds / 60;
            long hours = minutes / 60;

            if(passedSeconds < 60){
                countdownText = "Vừa phát xong 🔥"; // < 1 phút
            } else if(minutes < 60){
                countdownText = "Đã phát " + minutes + " phút trước 🔥";
            } else if(hours < 24){
                countdownText = "Đã phát " + hours + " giờ trước 🔥";
            } else {
                long days = hours / 24;
                countdownText = "Đã phát " + days + " ngày trước 🔥";
            }
        }

        // Hiển thị countdown + số tập
        holder.txtCountdown.setText(epText + countdownText);

        // Load poster
        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.imgPoster);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imgPoster;
        TextView txtTitle, txtCountdown;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtCountdown = itemView.findViewById(R.id.txtCountdown);
        }
    }

    public void release(){
        handler.removeCallbacks(updateRunnable);
    }
}