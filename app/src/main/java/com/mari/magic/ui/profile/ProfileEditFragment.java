package com.mari.magic.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.mari.magic.R;

import java.util.HashMap;
import java.util.Map;

public class ProfileEditFragment extends Fragment {

    private static final String TAG = "ProfileEditFragment";

    private EditText edtEmail, edtUsername, edtDisplayName, edtBio;
    private Button btnSaveProfile;
    private TextView txtUsernameInfo;

    private FirebaseUser user;
    private FirebaseFirestore firestore;

    private static final int USERNAME_CHANGE_DAYS = 90;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        edtEmail = view.findViewById(R.id.edtEmail);
        edtUsername = view.findViewById(R.id.edtUsername);
        edtDisplayName = view.findViewById(R.id.edtDisplayName);
        edtBio = view.findViewById(R.id.edtBio);
        txtUsernameInfo = view.findViewById(R.id.txtUsernameInfo);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);

        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if(user != null){
            edtEmail.setText(user.getEmail()); // read-only
            Log.d(TAG, "User email loaded: " + user.getEmail());

            firestore.collection("users").document(user.getUid())
                    .get().addOnSuccessListener(documentSnapshot -> {
                        if(documentSnapshot.exists()){
                            String username = documentSnapshot.getString("username");
                            String displayName = documentSnapshot.getString("displayName");
                            String bio = documentSnapshot.getString("bio");
                            Long lastChange = documentSnapshot.getLong("usernameLastChanged");

                            // Luôn hiển thị @ ở ô input
                            // Chỉ dùng @user nếu Firestore hoàn toàn không có username
                            if(username == null || username.isEmpty()){
                                username = "@user";
                            } else if(!username.startsWith("@")){
                                username = "@" + username; // thêm @ nếu thiếu
                            }

                            edtUsername.setText(username);
                            edtDisplayName.setText(displayName != null ? displayName : "");
                            edtBio.setText(bio != null ? bio : "");

                            // Xử lý disable username nếu chưa đủ 90 ngày
                            boolean canChangeUsername = lastChange == null ||
                                    (System.currentTimeMillis() - lastChange) / (1000L * 60 * 60 * 24) >= USERNAME_CHANGE_DAYS;

                            if(canChangeUsername){
                                edtUsername.setEnabled(true);
                                txtUsernameInfo.setText("");
                            } else {
                                edtUsername.setEnabled(false);
                                long daysLeft = USERNAME_CHANGE_DAYS -
                                        (System.currentTimeMillis() - lastChange) / (1000L * 60 * 60 * 24);
                                txtUsernameInfo.setText(getString(R.string.username_info, daysLeft));
                            }

                            edtUsername.setTag(lastChange);

                            Log.d(TAG, "Loaded from Firestore -> username: " + username +
                                    ", displayName: " + displayName + ", bio: " + bio +
                                    ", lastChange: " + lastChange + ", canChangeUsername: " + canChangeUsername);
                        } else {
                            Log.w(TAG, "Firestore document not exists");
                        }
                    }).addOnFailureListener(e -> Log.e(TAG, "Firestore load error", e));
        }

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        return view;
    }

    private void saveProfile() {
        String inputUsername = edtUsername.getText().toString().trim();
        String displayNameInput = edtDisplayName.getText().toString().trim();
        String bioInput = edtBio.getText().toString().trim();

        if(TextUtils.isEmpty(displayNameInput)){
            Toast.makeText(getContext(), getString(R.string.error_displayname_empty), Toast.LENGTH_SHORT).show();
            Log.w(TAG, "DisplayName empty");
            return;
        }

        firestore.collection("users").document(user.getUid())
                .get().addOnSuccessListener(doc -> {
                    if(doc.exists()){
                        String oldUsername = doc.getString("username"); // username thực từ Firestore
                        Long lastChange = doc.getLong("usernameLastChanged");

                        boolean canChangeUsername = lastChange == null ||
                                (System.currentTimeMillis() - lastChange) / (1000L*60*60*24) >= USERNAME_CHANGE_DAYS;

                        String finalUsername = canChangeUsername ? inputUsername : oldUsername;

                        Map<String,Object> data = new HashMap<>();
                        data.put("displayName", displayNameInput);
                        data.put("bio", bioInput);
                        data.put("username", finalUsername);
                        if(canChangeUsername) data.put("usernameLastChanged", System.currentTimeMillis());

                        final String usernameForHeader = finalUsername;
                        final String displayNameForHeader = displayNameInput;

                        // Lưu Firestore
                        firestore.collection("users").document(user.getUid())
                                .set(data, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Firestore save success: " + data.toString());

                                    // Cập nhật FirebaseUser displayName
                                    user.updateProfile(new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(displayNameInput)
                                                    .build())
                                            .addOnCompleteListener(task -> {
                                                if(task.isSuccessful()){
                                                    Fragment parent = getParentFragment();
                                                    if(parent instanceof ProfileFragment){
                                                        // Tên hiển thị update ngay
                                                        ((ProfileFragment) parent).updateHeader(displayNameForHeader);
                                                        // Username luôn lấy đúng từ Firestore
                                                        ((ProfileFragment) parent).updateUsername(usernameForHeader);
                                                        Log.d(TAG, "Header updated -> displayName: " + displayNameForHeader
                                                                + ", username: " + usernameForHeader);
                                                    }
                                                }
                                            });

                                    Toast.makeText(getContext(), getString(R.string.profile_save_success), Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Firestore save failed", e);
                                    Toast.makeText(getContext(), getString(R.string.profile_save_fail), Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Log.w(TAG, "Firestore document not found");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore load failed", e));
    }
}