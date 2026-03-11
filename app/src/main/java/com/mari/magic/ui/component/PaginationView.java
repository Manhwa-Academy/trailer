package com.mari.magic.ui.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mari.magic.R;

public class PaginationView extends LinearLayout {

    private TextView txtPageInfo;
    private LinearLayout pageContainer;

    private int currentPage = 1;
    private int totalPages = 1;

    public interface OnPageChangeListener{
        void onPageChange(int page);
    }

    private OnPageChangeListener listener;

    public PaginationView(Context context, AttributeSet attrs){
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_pagination,this,true);

        txtPageInfo = findViewById(R.id.txtPageInfo);
        pageContainer = findViewById(R.id.pageContainer);
    }

    public void setOnPageChangeListener(OnPageChangeListener l){
        listener = l;
    }

    public void setPages(int current, int total){

        currentPage = current;
        totalPages = total;

        txtPageInfo.setText("Trang " + current + " của " + total);

        pageContainer.removeAllViews();

        int start = Math.max(1,current - 2);
        int end = Math.min(total,start + 4);

        for(int i=start;i<=end;i++){

            TextView btn = new TextView(getContext());

            btn.setText(String.valueOf(i));
            btn.setPadding(24,16,24,16);
            btn.setBackgroundResource(
                    i == current ?
                            R.drawable.page_selected :
                            R.drawable.page_normal
            );

            int page = i;

            btn.setOnClickListener(v -> {
                if(listener != null){
                    listener.onPageChange(page);
                }
            });

            pageContainer.addView(btn);
        }
    }
}