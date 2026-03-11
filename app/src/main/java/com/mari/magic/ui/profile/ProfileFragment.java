package com.mari.magic.ui.profile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.mari.magic.R;
import com.mari.magic.adapter.AvatarAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private String[] tabTitles;

    private ShapeableImageView imgAvatar;
    private ImageView btnEditAvatar;
    private TextView txtUsername;
    private TextView txtUserId;

    private String currentUsername;
    private int selectedAvatar = -1;

    private static final int REQUEST_PICK_IMAGE = 100;

    public ProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_tabs, container, false);

        imgAvatar = view.findViewById(R.id.imgAvatar);
        btnEditAvatar = view.findViewById(R.id.btnEditAvatar);
        txtUsername = view.findViewById(R.id.txtUsername);
        txtUserId = view.findViewById(R.id.txtUserId);

        viewPager = view.findViewById(R.id.viewPagerProfile);
        tabLayout = view.findViewById(R.id.tabLayoutProfile);

        viewPager.setAdapter(new ProfilePagerAdapter(getActivity()));

        tabTitles = new String[]{
                getString(R.string.tab_overview),
                getString(R.string.tab_edit),
                getString(R.string.tab_security)
        };
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();

        loadUserHeader();

        btnEditAvatar.setOnClickListener(v -> showAvatarDialog());

        return view;
    }

    private void loadUserHeader() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentUsername = doc.getString("username");
                        if (currentUsername == null) currentUsername = "@user";

                        txtUsername.setText(user.getDisplayName());
                        txtUserId.setText(currentUsername);

                        if (doc.contains("avatarUrl") && doc.getString("avatarUrl") != null) {
                            Glide.with(this).load(doc.getString("avatarUrl")).into(imgAvatar);
                        } else if (doc.contains("avatarResId") && doc.getLong("avatarResId") != null) {
                            int resId = doc.getLong("avatarResId").intValue();
                            imgAvatar.setImageResource(resId);
                        }
                    }
                });
    }

    private void showAvatarDialog() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_avatar);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerViewAvatars);
        Button btnSelect = dialog.findViewById(R.id.btnSelectAvatar);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));

        List<Integer> avatars = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            int resId = getResources().getIdentifier("avatar" + i, "drawable", requireContext().getPackageName());
            avatars.add(resId);
        }

        AvatarAdapter adapter = new AvatarAdapter(avatars, resId -> {
            selectedAvatar = resId;
            btnSelect.setEnabled(true);
        });
        recyclerView.setAdapter(adapter);

        btnSelect.setOnClickListener(v -> {
            if (selectedAvatar != -1) {
                imgAvatar.setImageResource(selectedAvatar);
                saveAvatarResId(selectedAvatar);
                Toast.makeText(getContext(), "Chọn avatar thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Hãy chọn avatar trước!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void saveAvatarResId(int resId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .update(
                        "avatarResId", resId,
                        "avatarUrl", null
                )
                .addOnSuccessListener(aVoid -> android.util.Log.d("ProfileFragment", "Avatar updated successfully in Firestore"))
                .addOnFailureListener(e -> android.util.Log.e("ProfileFragment", "Failed to update avatar", e));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null){
            Uri imageUri = data.getData();
            if(imageUri != null){
                imgAvatar.setImageURI(imageUri);
                saveAvatarToFirestore(imageUri);
            }
        }
    }

    private void saveAvatarToFirestore(Uri imageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || imageUri == null) return;

        try {
            // 1. Lấy bitmap từ URI
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);

            // 2. Resize bitmap nếu quá lớn (max 500x500)
            int maxWidth = 500;
            int maxHeight = 500;
            float ratio = Math.min(
                    (float) maxWidth / bitmap.getWidth(),
                    (float) maxHeight / bitmap.getHeight());
            int width = Math.round(ratio * bitmap.getWidth());
            int height = Math.round(ratio * bitmap.getHeight());
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

            // 3. Compress bitmap xuống JPEG 80%
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] data = baos.toByteArray();

            // 4. Tạo StorageReference chuẩn
            StorageReference storageRef = FirebaseStorage.getInstance()
                    .getReference()  // root của storage
                    .child("avatars/" + user.getUid() + ".jpg"); // Firebase tự tạo folder khi upload

            // 5. Upload ảnh
            storageRef.putBytes(data)
                    .addOnSuccessListener(taskSnapshot ->
                            storageRef.getDownloadUrl()
                                    .addOnSuccessListener(uri -> {
                                        // 6. Lưu URL vào Firestore
                                        FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(user.getUid())
                                                .update(
                                                        "avatarUrl", uri.toString(),
                                                        "avatarResId", null
                                                )
                                                .addOnSuccessListener(aVoid ->
                                                        android.util.Log.d("ProfileFragment", "Avatar uploaded & saved: " + uri.toString())
                                                )
                                                .addOnFailureListener(e ->
                                                        android.util.Log.e("ProfileFragment", "Failed to save avatar URL", e)
                                                );
                                    })
                                    .addOnFailureListener(e ->
                                            android.util.Log.e("ProfileFragment", "Failed to get download URL", e)
                                    )
                    )
                    .addOnFailureListener(e ->
                            android.util.Log.e("ProfileFragment", "Failed to upload avatar", e)
                    );

        } catch (IOException e) {
            e.printStackTrace();
            android.util.Log.e("ProfileFragment", "Failed to process image", e);
        }
    }
    public void updateHeader(String displayName){
        if(txtUsername != null) txtUsername.setText(displayName);
        if(txtUserId != null && currentUsername != null) txtUserId.setText(currentUsername);
    }

    public void updateUsername(String username){
        currentUsername = username;
        if(txtUserId != null) txtUserId.setText(currentUsername);
    }
}