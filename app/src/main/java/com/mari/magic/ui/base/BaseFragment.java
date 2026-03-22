package com.mari.magic.ui.base;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mari.magic.R;
import com.mari.magic.utils.AppSettings;

public class BaseFragment extends Fragment {

    // Áp dụng background cho tất cả fragment kế thừa
    protected void applyBackground() {
        View root = getView();
        if (root == null) return;

        String bg = AppSettings.getBackground(requireContext());

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
            // ✅ không còn default → nếu bg không khớp anh1~4, fragment giữ nguyên nền hiện tại
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        applyBackground(); // tự động áp dụng background khi fragment được tạo
    }
}