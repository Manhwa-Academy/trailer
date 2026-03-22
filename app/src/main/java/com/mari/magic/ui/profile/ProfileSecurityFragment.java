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
import com.google.firebase.auth.UserInfo;
import com.mari.magic.R;
import com.mari.magic.ui.base.BaseFragment;

public class ProfileSecurityFragment extends BaseFragment {

    private EditText edtCurrentPassword, edtNewPassword, edtConfirmPassword;
    private Button btnChangePassword;
    private TextView txtGoogleInfo;

    private FirebaseUser user;

    public ProfileSecurityFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile_security, container, false);

        edtCurrentPassword = view.findViewById(R.id.edtCurrentPassword);
        edtNewPassword = view.findViewById(R.id.edtNewPassword);
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        txtGoogleInfo = view.findViewById(R.id.txtGoogleInfo);

        user = FirebaseAuth.getInstance().getCurrentUser();

        // Kiểm tra user login bằng phương thức email/password hiện tại
        boolean isEmailLogin = false;

        if (user != null && user.getProviderData().size() > 1) {

            String provider = user.getProviderData().get(1).getProviderId();

            Log.d("ProfileSecurity", "Login Provider: " + provider);

            if ("password".equals(provider)) {
                isEmailLogin = true;
            }
        }
        final boolean canChangePassword = isEmailLogin; // final để dùng trong lambda

        // Ẩn/hiện EditText + Button đổi mật khẩu, hiển thị thông báo cho user Google login
        if(!canChangePassword){
            edtCurrentPassword.setVisibility(View.GONE);
            edtNewPassword.setVisibility(View.GONE);
            edtConfirmPassword.setVisibility(View.GONE);
            btnChangePassword.setVisibility(View.GONE);

            txtGoogleInfo.setVisibility(View.VISIBLE); // hiển thị thông báo
        } else {
            edtCurrentPassword.setVisibility(View.VISIBLE);
            edtNewPassword.setVisibility(View.VISIBLE);
            edtConfirmPassword.setVisibility(View.VISIBLE);
            btnChangePassword.setVisibility(View.VISIBLE);

            txtGoogleInfo.setVisibility(View.GONE);
        }

        btnChangePassword.setOnClickListener(v -> changePassword(canChangePassword));
        setupPasswordToggle(edtCurrentPassword);
        setupPasswordToggle(edtNewPassword);
        setupPasswordToggle(edtConfirmPassword);
        return view;
    }

    private void changePassword(boolean canChangePassword){
        if(!canChangePassword){
            Toast.makeText(getContext(), R.string.google_user_cant_change, Toast.LENGTH_SHORT).show();
            return;
        }

        String current = edtCurrentPassword.getText().toString().trim();
        String newPass = edtNewPassword.getText().toString().trim();
        String confirm = edtConfirmPassword.getText().toString().trim();

        if(TextUtils.isEmpty(current) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirm)){
            Toast.makeText(getContext(), R.string.enter_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if(!newPass.equals(confirm)){
            Toast.makeText(getContext(), R.string.password_not_match, Toast.LENGTH_SHORT).show();
            return;
        }

        if(user != null){
            user.updatePassword(newPass)
                    .addOnSuccessListener(aVoid -> {

                        Toast.makeText(getContext(),
                                R.string.change_password_success,
                                Toast.LENGTH_SHORT).show();

                        clearPasswordFields();

                    });
        }
    }
    private void setupPasswordToggle(EditText editText){

        final boolean[] isVisible = {false};

        editText.setOnTouchListener((v, event) -> {

            if(event.getAction() == android.view.MotionEvent.ACTION_UP){

                if(event.getRawX() >= (editText.getRight()
                        - editText.getCompoundDrawables()[2].getBounds().width())){

                    if(isVisible[0]){

                        editText.setInputType(
                                android.text.InputType.TYPE_CLASS_TEXT |
                                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

                        editText.setCompoundDrawablesWithIntrinsicBounds(
                                0,0,R.drawable.ic_eye,0);

                    }else{

                        editText.setInputType(
                                android.text.InputType.TYPE_CLASS_TEXT |
                                        android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

                        editText.setCompoundDrawablesWithIntrinsicBounds(
                                0,0,R.drawable.ic_eye_off,0);
                    }

                    isVisible[0] = !isVisible[0];

                    editText.setSelection(editText.getText().length());

                    return true;
                }
            }

            return false;
        });
    }
    private void clearPasswordFields(){
        edtCurrentPassword.setText("");
        edtNewPassword.setText("");
        edtConfirmPassword.setText("");
    }
}