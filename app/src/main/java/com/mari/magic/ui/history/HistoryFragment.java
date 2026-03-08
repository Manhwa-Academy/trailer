package com.mari.magic.ui.history;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.mari.magic.R;
import com.mari.magic.adapter.FavoriteAdapter;

import java.util.*;

public class HistoryFragment extends Fragment {

    RecyclerView recyclerView;

    TextView txtHistoryEmpty;
    TextView btnClearHistory;

    FirebaseFirestore db;
    FirebaseAuth auth;

    List<Map<String,Object>> list = new ArrayList<>();
    FavoriteAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_history,
                container,
                false);

        recyclerView = view.findViewById(R.id.recyclerHistory);
        txtHistoryEmpty = view.findViewById(R.id.txtHistoryEmpty);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);

        recyclerView.setLayoutManager(
                new GridLayoutManager(getContext(),2)
        );

        recyclerView.setHasFixedSize(true);

        adapter = new FavoriteAdapter(list,getContext());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadHistory();

        setupClearHistory();

        return view;
    }

    // ===============================
    // LOAD HISTORY
    // ===============================

    private void loadHistory(){

        if(auth.getCurrentUser()==null){
            txtHistoryEmpty.setVisibility(View.VISIBLE);
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("history")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot,error) -> {

                    if(snapshot == null) return;

                    list.clear();

                    for(DocumentSnapshot doc : snapshot){
                        list.add(doc.getData());
                    }

                    adapter.notifyDataSetChanged();

                    if(list.isEmpty()){
                        txtHistoryEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }else{
                        txtHistoryEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }

    // ===============================
    // CLEAR HISTORY
    // ===============================

    private void setupClearHistory(){

        btnClearHistory.setOnClickListener(v -> {

            if(auth.getCurrentUser()==null) return;

            String uid = auth.getCurrentUser().getUid();

            db.collection("users")
                    .document(uid)
                    .collection("history")
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        for(DocumentSnapshot doc : snapshot){
                            doc.getReference().delete();
                        }

                        Toast.makeText(getContext(),
                                "History cleared",
                                Toast.LENGTH_SHORT).show();
                    });
        });
    }
}