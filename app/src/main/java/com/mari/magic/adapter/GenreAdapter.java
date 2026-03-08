package com.mari.magic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mari.magic.R;

import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder>{

    List<String> list;
    OnGenreClickListener listener;

    public interface OnGenreClickListener{
        void onClick(String genre);
    }

    public void setOnGenreClickListener(OnGenreClickListener listener){
        this.listener = listener;
    }

    public GenreAdapter(List<String> list){
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_genre,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position){

        String genre = list.get(position);

        holder.text.setText(genre);

        holder.itemView.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();

            if(pos == RecyclerView.NO_POSITION) return;

            if(listener != null){
                listener.onClick(list.get(pos));
            }

        });
    }

    @Override
    public int getItemCount(){
        return list != null ? list.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView text;

        public ViewHolder(View itemView){
            super(itemView);

            text = itemView.findViewById(R.id.genreText);
        }
    }
}