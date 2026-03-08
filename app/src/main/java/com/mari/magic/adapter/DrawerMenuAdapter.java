package com.mari.magic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mari.magic.R;
import com.mari.magic.model.DrawerMenuItem;

import java.util.List;

public class DrawerMenuAdapter extends RecyclerView.Adapter<DrawerMenuAdapter.ViewHolder> {

    Context context;
    List<DrawerMenuItem> list;

    OnItemClickListener listener;

    public interface OnItemClickListener{
        void onClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    public DrawerMenuAdapter(Context context, List<DrawerMenuItem> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_drawer_menu,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){

        DrawerMenuItem item = list.get(position);

        holder.title.setText(item.title);

        if(item.expandable){

            holder.arrow.setImageResource(R.drawable.ic_arrow_down);
            holder.arrow.setVisibility(View.VISIBLE);

            if(item.expanded){
                holder.arrow.setRotation(180f);
            }else{
                holder.arrow.setRotation(0f);
            }

        }else{

            holder.arrow.setImageResource(R.drawable.ic_arrow_right);
            holder.arrow.setRotation(0f);
            holder.arrow.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();

            if(pos == RecyclerView.NO_POSITION) return;

            DrawerMenuItem clickedItem = list.get(pos);

            if(clickedItem.expandable){

                clickedItem.expanded = !clickedItem.expanded;

                if(clickedItem.expanded){

                    holder.arrow.animate()
                            .rotation(180f)
                            .setDuration(200)
                            .start();

                }else{

                    holder.arrow.animate()
                            .rotation(0f)
                            .setDuration(200)
                            .start();
                }

                notifyItemChanged(pos);
            }

            if(listener != null){
                listener.onClick(pos);
            }

        });

    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView title;
        ImageView arrow;

        public ViewHolder(View itemView){
            super(itemView);

            title = itemView.findViewById(R.id.menuTitle);
            arrow = itemView.findViewById(R.id.menuArrow);
        }
    }
}