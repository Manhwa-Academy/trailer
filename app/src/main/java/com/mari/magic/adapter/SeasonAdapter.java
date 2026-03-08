package com.mari.magic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mari.magic.R;

import java.util.List;

public class SeasonAdapter extends RecyclerView.Adapter<SeasonAdapter.ViewHolder>{

    List<String> list;
    OnSeasonClickListener listener;

    public SeasonAdapter(List<String> list){
        this.list = list;
    }

    public interface OnSeasonClickListener{
        void onClick(String season);
    }

    public void setOnSeasonClickListener(OnSeasonClickListener l){
        this.listener = l;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_season,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){

        String season = list.get(position);

        String text = season
                .replace("WINTER", holder.itemView.getContext().getString(R.string.season_winter))
                .replace("SPRING", holder.itemView.getContext().getString(R.string.season_spring))
                .replace("SUMMER", holder.itemView.getContext().getString(R.string.season_summer))
                .replace("FALL", holder.itemView.getContext().getString(R.string.season_fall));

        String[] parts = text.split(" ");
        holder.txtSeason.setText(parts[0] + "\n" + parts[1]);

        holder.itemView.setOnClickListener(v -> {

            if(listener != null){
                listener.onClick(season);
            }

        });

    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    private String getSeasonVN(String season){

        if(season == null) return "";

        return season
                .replace("WINTER","Đông")
                .replace("SPRING","Xuân")
                .replace("SUMMER","Hạ")
                .replace("FALL","Thu");
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView txtSeason;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            txtSeason = itemView.findViewById(R.id.txtSeason);
        }

    }
}