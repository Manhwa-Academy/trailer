package com.mari.magic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mari.magic.R;
import com.mari.magic.utils.HistoryManager;

import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder>{

    List<String> list;
    OnClick listener;

    public interface OnClick{
        void onClick(String keyword);
    }

    public SearchHistoryAdapter(List<String> list, OnClick listener){
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_history,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){

        String keyword = list.get(position);

        holder.txtKeyword.setText(keyword);

        // click để search lại
        holder.itemView.setOnClickListener(v -> {
            if(listener != null){
                listener.onClick(keyword);
            }
        });

        // nút xoá history
        holder.btnDelete.setOnClickListener(v -> {

            HistoryManager.removeSearch(
                    v.getContext(),
                    keyword
            );

            list.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, list.size());

        });
    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView txtKeyword;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            txtKeyword = itemView.findViewById(R.id.txtKeyword);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}