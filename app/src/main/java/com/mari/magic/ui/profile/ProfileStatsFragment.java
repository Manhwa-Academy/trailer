package com.mari.magic.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mari.magic.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileStatsFragment extends Fragment {

    private TextView txtStreak, txtMoviesWatched, txtMangaRead, txtAccountCreated;

    private FirebaseUser user;
    private FirebaseFirestore firestore;

    public ProfileStatsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_profile_stats, container, false);

        txtStreak = view.findViewById(R.id.txtStreak);
        txtMoviesWatched = view.findViewById(R.id.txtMoviesWatched);
        txtMangaRead = view.findViewById(R.id.txtMangaRead);
        txtAccountCreated = view.findViewById(R.id.txtAccountCreated);

        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        if(user != null){
            // Ngày tạo tài khoản
            Date creationDate = new Date(user.getMetadata().getCreationTimestamp());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            txtAccountCreated.setText(getString(R.string.account_created) + sdf.format(creationDate));

            // Lấy thống kê từ Firestore
            refreshStats();
        }

        return view;
    }

    public void refreshStats() {
        if(user == null) return;

        // Step 1: Đếm số video/manga hôm nay
        firestore.collection("users")
                .document(user.getUid())
                .collection("history")
                .get().addOnSuccessListener(snapshot -> {

                    int moviesToday = 0;
                    int mangaToday = 0;
                    final boolean[] hasActivityToday = {false}; // dùng mảng để lambda

                    SimpleDateFormat sdfDay = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                    String today = sdfDay.format(new Date());

                    for(QueryDocumentSnapshot doc : snapshot){
                        Object timeObj = doc.get("time");
                        Object formatObj = doc.get("format");

                        if(timeObj == null || formatObj == null) continue;

                        long time = ((Number) timeObj).longValue();
                        String format = formatObj.toString();

                        String day = sdfDay.format(new Date(time));

                        if(today.equals(day)){
                            hasActivityToday[0] = true; // ghi nhận có activity hôm nay
                            if(format.equalsIgnoreCase("MANGA") || format.equalsIgnoreCase("NOVEL") || format.equalsIgnoreCase("ONE_SHOT")){
                                mangaToday++;
                            } else {
                                moviesToday++;
                            }
                        }
                    }

                    txtMoviesWatched.setText(String.valueOf(moviesToday));
                    txtMangaRead.setText(String.valueOf(mangaToday));

                    // Step 2: Cập nhật streak
                    firestore.collection("users").document(user.getUid())
                            .get().addOnSuccessListener(userDoc -> {
                                if(userDoc.exists()){
                                    int streak = userDoc.get("streak") != null ?
                                            ((Number)userDoc.get("streak")).intValue() : 0;

                                    if(hasActivityToday[0]){
                                        // Nếu chưa tăng streak hôm nay
                                        Object lastStreakUpdateObj = userDoc.get("lastStreakUpdate");
                                        long lastStreakUpdate = lastStreakUpdateObj != null ? ((Number)lastStreakUpdateObj).longValue() : 0;

                                        // Nếu lastStreakUpdate không phải hôm nay -> tăng streak
                                        String lastUpdateDay = lastStreakUpdate > 0 ? sdfDay.format(new Date(lastStreakUpdate)) : "";
                                        if(!today.equals(lastUpdateDay)){
                                            streak += 1;
                                            firestore.collection("users").document(user.getUid())
                                                    .update("streak", streak,
                                                            "lastStreakUpdate", System.currentTimeMillis());
                                        }
                                    } else {
                                        // Không có activity hôm nay -> reset streak
                                        streak = 0;
                                        firestore.collection("users").document(user.getUid())
                                                .update("streak", 0);
                                    }

                                    txtStreak.setText(String.valueOf(streak));
                                }
                            });

                });
    }
}