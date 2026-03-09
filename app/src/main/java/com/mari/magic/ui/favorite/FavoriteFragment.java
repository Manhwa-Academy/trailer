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

        loadFavorites();
        setupSwipeActions();

        return view;
    }

    private void loadFavorites() {
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
                    if (error != null) {
                        txtEmpty.setText("Lỗi tải dữ liệu!");
                        txtEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        return;
                    }

                    list.clear();
                    if (snapshot != null && !snapshot.isEmpty()) {
                        for (DocumentSnapshot doc : snapshot) {
                            Map<String, Object> data = new HashMap<>(doc.getData());
                            data.put("docId", doc.getId()); // để xóa
                            list.add(data);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    txtEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                });
    }

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
                Map<String,Object> item = list.get(position);

                if(direction == ItemTouchHelper.RIGHT) {
                    // Mở Detail + gửi tất cả data
                    Intent intent = new Intent(getContext(), AnimeDetailActivity.class);

                    intent.putExtra("animeId", (String)item.get("animeId"));
                    intent.putExtra("title", (String)item.get("title"));
                    intent.putExtra("englishTitle", (String)item.get("englishTitle"));
                    intent.putExtra("romajiTitle", (String)item.get("romajiTitle"));
                    intent.putExtra("nativeTitle", (String)item.get("nativeTitle"));

                    intent.putExtra("poster", (String)item.get("poster"));
                    intent.putExtra("rating", item.get("rating") != null ? ((Number)item.get("rating")).doubleValue() : 0.0);
                    intent.putExtra("trailer", (String)item.get("trailer"));

                    intent.putExtra("description", (String)item.get("description"));
                    intent.putExtra("genres", (String)item.get("genres"));

                    intent.putExtra("studio", (String)item.get("studio"));
                    intent.putExtra("director", (String)item.get("director"));
                    intent.putExtra("season", (String)item.get("season"));
                    intent.putExtra("format", (String)item.get("format"));

                    intent.putExtra("duration", item.get("duration") != null ? ((Number)item.get("duration")).intValue() : 0);
                    intent.putExtra("episodes", item.get("episodes") != null ? ((Number)item.get("episodes")).intValue() : 0);

                    intent.putExtra("status", (String)item.get("status"));
                    intent.putExtra("nextEpisode", item.get("nextEpisode") != null ? ((Number)item.get("nextEpisode")).intValue() : 0);
                    intent.putExtra("nextAiringAt", item.get("nextAiringAt") != null ? ((Number)item.get("nextAiringAt")).longValue() : 0);
                    intent.putExtra("isAdult", item.get("isAdult") != null && (Boolean)item.get("isAdult"));
                    intent.putExtra("views", item.get("views") != null ? ((Number)item.get("views")).longValue() : 0L);
                    intent.putExtra("updatedAt", item.get("updatedAt") != null ? ((Number)item.get("updatedAt")).longValue() : 0L);

                    startActivity(intent);

                    adapter.notifyItemChanged(position); // reset item
                } else if(direction == ItemTouchHelper.LEFT) {
                    // Xóa favorite
                    String uid = auth.getCurrentUser().getUid();
                    String docId = (String)item.get("docId");
                    db.collection("users").document(uid).collection("favorites").document(docId).delete();
                    list.remove(position);
                    adapter.notifyItemRemoved(position);
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

                    float textY = itemView.getTop() + itemView.getHeight() / 2f + 15;

                    if (dX > 0) {
                        paint.setColor(Color.parseColor("#4CAF50")); // xanh → mở detail
                        c.drawRect(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom(), paint);
                        c.drawText("Detail", itemView.getLeft() + 50, textY, textPaint);
                    } else if (dX < 0) {
                        paint.setColor(Color.parseColor("#f44336")); // đỏ → xóa
                        c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), paint);
                        c.drawText("Delete", itemView.getRight() - 250, textY, textPaint);
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
        if (listener != null) {
            listener.remove();
        }
    }
}