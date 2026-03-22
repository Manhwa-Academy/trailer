package com.mari.magic.ui.history;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mari.magic.ui.base.BaseFragment;
import com.mari.magic.utils.TrailerHelper;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.mari.magic.R;
import com.mari.magic.adapter.FavoriteAdapter;
import com.mari.magic.ui.detail.AnimeDetailActivity;
import com.mari.magic.utils.HistoryManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private TextView txtHistoryEmpty;
    private TextView btnClearHistory;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Map<String, Object>> list = new ArrayList<>();
    private FavoriteAdapter adapter;
    private ListenerRegistration listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recyclerHistory);
        txtHistoryEmpty = view.findViewById(R.id.txtHistoryEmpty);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new FavoriteAdapter(list, getContext());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadHistory();
        setupClearHistory();
        setupSwipeActions();

        return view;
    }

    // ===============================
    // LOAD HISTORY
    // ===============================
    private void loadHistory() {
        if (auth.getCurrentUser() == null) {
            txtHistoryEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        listener = db.collection("users")
                .document(uid)
                .collection("history")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        error.printStackTrace();
                        return;
                    }
                    if (snapshot == null) return;

                    list.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            list.add(data);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (list.isEmpty()) {
                        txtHistoryEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        txtHistoryEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }

    // ===============================
    // CLEAR HISTORY
    // ===============================
    private void setupClearHistory() {
        btnClearHistory.setOnClickListener(v -> {
            if (auth.getCurrentUser() == null) return;

            HistoryManager.clearWatchHistory(getContext());
            Toast.makeText(getContext(), "History cleared", Toast.LENGTH_SHORT).show();
        });
    }

    // ===============================
    // SWIPE ACTIONS
    // ===============================
    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false; // không hỗ trợ drag & drop
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Map<String,Object> item = list.get(position);

                if (direction == ItemTouchHelper.RIGHT) {

                    // Xóa khỏi lịch sử
                    String animeId = (String) item.get("animeId");
                    HistoryManager.removeHistory(animeId);

                    list.remove(position);
                    adapter.notifyItemRemoved(position);

                } else if (direction == ItemTouchHelper.LEFT) {

                    // Mở trailer
                    TrailerHelper.openTrailer(
                            getContext(),
                            (String) item.get("title"),
                            (String) item.get("trailer"),
                            item.get("isAdult") != null && (Boolean) item.get("isAdult")
                    );

                    adapter.notifyItemChanged(position); // reset item
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();
                    Paint textPaint = new Paint();
                    textPaint.setColor(Color.WHITE);
                    textPaint.setTextSize(40f);
                    textPaint.setAntiAlias(true);

                    float textY = itemView.getTop() + itemView.getHeight() / 2f + 15; // căn giữa theo chiều dọc

                    if (dX > 0) {
                        // Kéo sang phải → màu đỏ (Delete)
                        paint.setColor(Color.parseColor("#f44336"));
                        c.drawRect(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom(), paint);

                        c.drawText("Delete", itemView.getLeft() + 50, textY, textPaint);

                    } else if (dX < 0) {
                        // Kéo sang trái → màu xanh (Continue / mở trailer)
                        paint.setColor(Color.parseColor("#4CAF50"));
                        c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), paint);

                        c.drawText("Continue", itemView.getRight() - 250, textY, textPaint);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) {
            listener.remove();
        }
    }
}