package com.mari.magic.ui.notification;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mari.magic.R;
import com.mari.magic.adapter.NotificationAdapter;
import com.mari.magic.model.NotificationItem;
import com.mari.magic.utils.NotificationHelper;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private View txtEmpty;
    private NotificationAdapter adapter;
    private List<NotificationItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recycler = findViewById(R.id.recyclerNotification);
        txtEmpty = findViewById(R.id.txtEmpty);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // reset badge khi mở
        NotificationHelper.resetBadge(this);

        loadData();
        setupSwipe();
    }

    private void loadData() {
        list = NotificationHelper.getNotifications(this);

        if (list == null || list.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
            return;
        }

        // Sắp xếp theo thời gian phát sóng tiếp theo
        Collections.sort(list, (n1, n2) ->
                Long.compare(n1.getNextAiringAt(), n2.getNextAiringAt())
        );

        txtEmpty.setVisibility(View.GONE);
        recycler.setVisibility(View.VISIBLE);

        adapter = new NotificationAdapter(this, list);
        recycler.setAdapter(adapter);
    }

    private void setupSwipe() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                // Xóa notification khỏi list và SharedPreferences
                NotificationItem item = list.get(position);
                list.remove(position);
                adapter.notifyItemRemoved(position);
                removeNotificationFromPrefs(item);

                if(list.isEmpty()){
                    txtEmpty.setVisibility(View.VISIBLE);
                    recycler.setVisibility(View.GONE);
                }
            }

            // 🔹 Reset khi swipe kết thúc
            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setAlpha(1f);
                viewHolder.itemView.setTranslationX(0f);
                viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive){
                    View itemView = viewHolder.itemView;
                    Paint p = new Paint();

                    // nền đỏ
                    p.setColor(Color.parseColor("#f44336"));
                    if(dX > 0){
                        c.drawRect(itemView.getLeft(), itemView.getTop(), dX, itemView.getBottom(), p);
                    } else {
                        c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), p);
                    }

                    // chữ XÓA
                    p.setColor(Color.WHITE);
                    p.setTextSize(40f);
                    p.setTextAlign(Paint.Align.CENTER);
                    float x = dX > 0 ? itemView.getLeft() + 100 : itemView.getRight() - 100;
                    float y = itemView.getTop() + itemView.getHeight()/2 + 15;
                    c.drawText("XÓA", x, y, p);

                    // icon trash
                    Drawable icon = ContextCompat.getDrawable(NotificationActivity.this, R.drawable.ic_delete_white);
                    if(icon != null){
                        int iconMargin = 30;
                        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                        int iconBottom = iconTop + icon.getIntrinsicHeight();

                        if(dX > 0){
                            int iconLeft = itemView.getLeft() + iconMargin;
                            int iconRight = iconLeft + icon.getIntrinsicWidth();
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        } else {
                            int iconRight = itemView.getRight() - iconMargin;
                            int iconLeft = iconRight - icon.getIntrinsicWidth();
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        }
                        icon.draw(c);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recycler);
    }
    private void removeNotificationFromPrefs(NotificationItem item) {
        List<NotificationItem> prefsList = NotificationHelper.getNotifications(this);
        prefsList.removeIf(n -> n.getTitle().equals(item.getTitle())
                && n.getNextAiringAt() == item.getNextAiringAt());
        Gson gson = new Gson();
        getSharedPreferences("app", MODE_PRIVATE)
                .edit()
                .putString("notification_list", gson.toJson(prefsList))
                .apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) adapter.release(); // ngừng handler để tránh leak
    }
}