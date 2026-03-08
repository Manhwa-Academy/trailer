package com.mari.magic.ui.favorite;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.mari.magic.R;
import com.mari.magic.adapter.FavoriteAdapter;

import java.util.*;

public class FavoriteFragment extends Fragment {

    RecyclerView recyclerView;
    TextView txtEmpty;

    FirebaseFirestore db;
    FirebaseAuth auth;

    List<Map<String,Object>> list = new ArrayList<>();
    FavoriteAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_favorite,
                container,
                false);

        recyclerView = view.findViewById(R.id.recyclerFavorites);
        txtEmpty = view.findViewById(R.id.txtEmpty);

        recyclerView.setLayoutManager(
                new GridLayoutManager(getContext(),2)
        );

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);

        adapter = new FavoriteAdapter(list,getContext());
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadFavorites();

        return view;
    }

    private void loadFavorites(){

        if(auth.getCurrentUser()==null){
            txtEmpty.setVisibility(View.VISIBLE);
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("favorites")
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {

                    if(snapshot == null) return;

                    list.clear();

                    for(DocumentSnapshot doc : snapshot){
                        list.add(doc.getData());
                    }

                    adapter.notifyDataSetChanged();

                    // ⭐ HIỂN THỊ EMPTY TEXT
                    if(list.isEmpty()){
                        txtEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }else{
                        txtEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
    }
}