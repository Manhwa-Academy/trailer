package com.mari.magic.ui.favorite;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

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

import java.util.*;

public class FavoriteFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView txtEmpty;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private List<Map<String, Object>> list = new ArrayList<>();
    private FavoriteAdapter adapter;
    private ListenerRegistration listener;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        recyclerView = view.findViewById(R.id.recyclerFavorites);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        adapter = new FavoriteAdapter(list, getContext());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadFavoritesRealtime();
        setupSwipeActions();

        return view;
    }

    // ================= LOAD FAVORITES REALTIME =================
    private void loadFavoritesRealtime() {
        if (auth.getCurrentUser() == null) {
            txtEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        listener = db.collection("users")
                .document(uid)
                .collection("favorites")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    list.clear();

                    if (error != null) {
                        txtEmpty.setText("Lỗi tải dữ liệu!");
                        txtEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        return;
                    }

                    if (snapshot != null && !snapshot.isEmpty()) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Map<String, Object> data = doc.getData();
                            if (data != null) {

                                // 🔹 CẬP NHẬT EPHIỆN TẠI dựa vào nextEpisode
                                int episodes = 0;
                                int nextEpisode = 0;

                                Object epObj = data.get("episodes");
                                Object nextObj = data.get("nextEpisode");

                                if(epObj instanceof Long) episodes = ((Long) epObj).intValue();
                                else if(epObj instanceof Integer) episodes = (Integer) epObj;

                                if(nextObj instanceof Long) nextEpisode = ((Long) nextObj).intValue();
                                else if(nextObj instanceof Integer) nextEpisode = (Integer) nextObj;

                                if(nextEpisode > 0 && episodes < nextEpisode - 1){
                                    data.put("episodes", nextEpisode - 1);
                                    // optional: update local cache
                                    com.mari.magic.utils.FavoriteManager.updateEpisodesFromNext((String)data.get("animeId"), nextEpisode);
                                }

                                data.put("docId", doc.getId());
                                list.add(data);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    txtEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }

    // ================= SWIPE ACTIONS =================
    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if(position == RecyclerView.NO_POSITION) return;

                Map<String,Object> item = list.get(position);

                if(direction == ItemTouchHelper.RIGHT){
                    Intent intent = new Intent(getContext(), AnimeDetailActivity.class);
                    for(String key : item.keySet()){
                        Object value = item.get(key);
                        if(value instanceof Number) {
                            if(value instanceof Double)
                                intent.putExtra(key, ((Number)value).doubleValue());
                            else
                                intent.putExtra(key, ((Number)value).longValue());
                        } else if(value instanceof Boolean){
                            intent.putExtra(key, (Boolean)value);
                        } else if(value instanceof String){
                            intent.putExtra(key, (String)value);
                        }
                    }
                    startActivity(intent);
                    adapter.notifyItemChanged(position);
                } else if(direction == ItemTouchHelper.LEFT){
                    String docId = (String) item.get("docId");
                    if(auth.getCurrentUser() != null && docId != null){
                        String uid = auth.getCurrentUser().getUid();
                        db.collection("users")
                                .document(uid)
                                .collection("favorites")
                                .document(docId)
                                .delete();
                        list.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    View itemView = viewHolder.itemView;
                    Paint paint = new Paint();
                    Paint textPaint = new Paint();
                    textPaint.setColor(Color.WHITE);
                    textPaint.setTextSize(40f);
                    textPaint.setAntiAlias(true);

                    float textY = itemView.getTop() + itemView.getHeight()/2f + 15;

                    if(dX > 0){
                        paint.setColor(Color.parseColor("#4CAF50"));
                        c.drawRect(itemView.getLeft(), itemView.getTop(), itemView.getLeft()+dX, itemView.getBottom(), paint);
                        c.drawText("Detail", itemView.getLeft()+50, textY, textPaint);
                    } else if(dX < 0){
                        paint.setColor(Color.parseColor("#f44336"));
                        c.drawRect(itemView.getRight()+dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), paint);
                        c.drawText("Delete", itemView.getRight()-250, textY, textPaint);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(listener != null) listener.remove();
    }
}