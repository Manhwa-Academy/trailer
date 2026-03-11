package com.mari.magic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mari.magic.R;

import java.util.List;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.ViewHolder> {

    private final List<Integer> avatars;
    private final OnAvatarClickListener listener;
    private int selectedAvatar = -1; // lưu avatar được chọn

    public interface OnAvatarClickListener {
        void onAvatarClick(int avatarResId);
    }

    public AvatarAdapter(List<Integer> avatars, OnAvatarClickListener listener) {
        this.avatars = avatars;
        this.listener = listener;
    }

    public void setSelectedAvatar(int resId) {
        selectedAvatar = resId;
        notifyDataSetChanged();
    }

    public int getSelectedAvatar() {
        return selectedAvatar;
    }

    @NonNull
    @Override
    public AvatarAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_avatar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarAdapter.ViewHolder holder, int position) {
        int resId = avatars.get(position);
        holder.imgAvatar.setImageResource(resId);

        // highlight avatar đã chọn
        if(resId == selectedAvatar){
            holder.imgAvatar.setAlpha(0.5f);
        } else {
            holder.imgAvatar.setAlpha(1f);
        }

        holder.imgAvatar.setOnClickListener(v -> {
            if(listener != null){
                listener.onAvatarClick(resId);
            }
            setSelectedAvatar(resId); // cập nhật highlight
        });
    }

    @Override
    public int getItemCount() {
        return avatars.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatarItem);
        }
    }
}