package com.mari.magic.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mari.magic.R;

public class ProfileFragment extends Fragment {

    ImageView imgAvatar;
    TextView txtName, txtEmail;

    FirebaseAuth auth;
    FirebaseFirestore db;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgAvatar = view.findViewById(R.id.imgAvatar);
        txtName = view.findViewById(R.id.txtName);
        txtEmail = view.findViewById(R.id.txtEmail);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUser();

        return view;
    }

    private void loadUser(){

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if(document.exists()){

                        String name = document.getString("name");
                        String email = document.getString("email");
                        String avatar = document.getString("avatar");

                        txtName.setText(name);
                        txtEmail.setText(email);

                        Glide.with(getContext())
                                .load(avatar)
                                .circleCrop()   // avatar tròn
                                .into(imgAvatar);
                    }

                });

    }
}