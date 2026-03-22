package com.mari.magic.ui.notification;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.mari.magic.R;
import com.mari.magic.adapter.NotificationAdapter;
import com.mari.magic.model.NotificationItem;
import com.mari.magic.utils.AppSettings;
import com.mari.magic.utils.NotificationHelper;

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
        applyBackground();
        // 🔹 Log để biết activity được mở
        Log.d("NOTI_DEBUG", "NotificationActivity opened");

        recycler = findViewById(R.id.recyclerNotification);
        txtEmpty = findViewById(R.id.txtEmpty);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // 🔹 Log trước khi reset badge
        int oldBadge = NotificationHelper.getBadge(this);
        Log.d("NOTI_DEBUG", "Current badge before reset: " + oldBadge);

        NotificationHelper.resetBadge(this);

        // 🔹 Log sau khi reset badge
        int newBadge = NotificationHelper.getBadge(this);
        Log.d("NOTI_DEBUG", "Badge after reset: " + newBadge);

        // 🔹 Log dữ liệu notification đang load
        List<NotificationItem> notifications = NotificationHelper.getNotifications(this);
        Log.d("NOTI_DEBUG", "Notifications loaded: " + notifications.size());
        for (NotificationItem item : notifications) {
            Log.d("NOTI_DEBUG", "Item: title=" + item.getTitle() + ", episode=" + item.getEpisode()
                    + ", nextAiringAt=" + item.getNextAiringAt()
                    + ", poster=" + item.getImageUrl());
        }

        loadData();
        setupSwipe();
    }

    // 🔹 Load lại dữ liệu và refresh RecyclerView
    public void loadData() {
        list = NotificationHelper.getNotifications(this);

        if (list == null || list.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
            return;
        }

        Collections.sort(list, (n1, n2) -> Long.compare(n1.getNextAiringAt(), n2.getNextAiringAt()));

        txtEmpty.setVisibility(View.GONE);
        recycler.setVisibility(View.VISIBLE);

        if(adapter == null){
            adapter = new NotificationAdapter(this, list);
            recycler.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }
    private void applyBackground() {
        View root = findViewById(R.id.rootLayout);
        if (root == null) return;

        String bg = AppSettings.getBackground(this);

        switch (bg) {
            case "anh1":
                root.setBackgroundResource(R.drawable.anh1);
                break;
            case "anh2":
                root.setBackgroundResource(R.drawable.anh2);
                break;
            case "anh3":
                root.setBackgroundResource(R.drawable.anh3);
                break;
            case "anh4":
                root.setBackgroundResource(R.drawable.anh4);
                break;
            case "anh5":
                root.setBackgroundResource(R.drawable.anh5);
                break;
            // ✅ bỏ default → nếu bg không phải anh1~anh5 thì giữ nguyên background
        }
    }
    private void setupSwipe() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                // 🔹 check position an toàn
                if (position < 0 || position >= list.size()) return;

                NotificationItem item = list.get(position);

                // 🔹 remove item khỏi prefs trước
                List<NotificationItem> prefsList = NotificationHelper.getNotifications(NotificationActivity.this);
                prefsList.removeIf(n -> n.getTitle().equals(item.getTitle())
                        && n.getNextAiringAt() == item.getNextAiringAt());

                Gson gson = new Gson();
                getSharedPreferences("app", MODE_PRIVATE)
                        .edit()
                        .putString("notification_list", gson.toJson(prefsList))
                        .apply();

                // 🔹 remove item khỏi list và notify adapter
                list.remove(position);
                adapter.notifyItemRemoved(position);

                // 🔹 show empty view nếu list trống
                if(list.isEmpty()){
                    txtEmpty.setVisibility(View.VISIBLE);
                    recycler.setVisibility(View.GONE);
                }
            }

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
                    p.setColor(Color.parseColor("#f44336"));
                    if(dX > 0) c.drawRect(itemView.getLeft(), itemView.getTop(), dX, itemView.getBottom(), p);
                    else c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), p);

                    p.setColor(Color.WHITE);
                    p.setTextSize(40f);
                    p.setTextAlign(Paint.Align.CENTER);
                    float x = dX > 0 ? itemView.getLeft() + 100 : itemView.getRight() - 100;
                    float y = itemView.getTop() + itemView.getHeight()/2 + 15;
                    c.drawText("XÓA", x, y, p);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) adapter.release();
    }
}