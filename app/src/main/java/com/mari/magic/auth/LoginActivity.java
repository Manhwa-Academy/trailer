package com.mari.magic.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mari.magic.R;
import com.mari.magic.home.MainActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
public class LoginActivity extends AppCompatActivity {

    LinearLayout btnLoginGoogle;
    Button btnLogin;
    EditText edtEmail, edtPassword;
    TextView txtSignup, txtForgotPassword;

    FirebaseAuth mAuth;
    GoogleSignInClient googleSignInClient;

    int RC_SIGN_IN = 100;

    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        btnLogin = findViewById(R.id.btnLogin);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);
        txtSignup = findViewById(R.id.txtSignup);

        mAuth = FirebaseAuth.getInstance();

        // =========================
        // Google Sign In config
        // =========================

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .requestProfile() // thêm dòng này
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // =========================
        // Google Login
        // =========================

        btnLoginGoogle.setOnClickListener(v -> {

            googleSignInClient.signOut().addOnCompleteListener(task -> {

                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            });

        });
        // =========================
        // Show / Hide password
        // =========================

        edtPassword.setOnTouchListener((v, event) -> {

            if(event.getAction() == MotionEvent.ACTION_UP){

                if(event.getRawX() >= (edtPassword.getRight()
                        - edtPassword.getCompoundDrawables()[2].getBounds().width())){

                    if(isPasswordVisible){

                        edtPassword.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_PASSWORD);

                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                                0,0,R.drawable.ic_eye,0);

                        isPasswordVisible = false;

                    }else{

                        edtPassword.setInputType(
                                InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

                        edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                                0,0,R.drawable.ic_eye_off,0);

                        isPasswordVisible = true;

                    }

                    edtPassword.setSelection(edtPassword.getText().length());
                    return true;
                }
            }

            return false;
        });

        // =========================
        // Login bằng Email
        // =========================

        btnLogin.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this,
                        "Vui lòng nhập email và password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if(task.isSuccessful()){

                            Toast.makeText(this,
                                    "Đăng nhập thành công",
                                    Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();

                        }else{

                            Toast.makeText(this,
                                    "Sai email hoặc mật khẩu",
                                    Toast.LENGTH_SHORT).show();

                        }

                    });

        });

        // =========================
        // Forgot password
        // =========================

        txtForgotPassword.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();

            if(email.isEmpty()){
                Toast.makeText(this,
                        "Nhập email để reset password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {

                        if(task.isSuccessful()){

                            Toast.makeText(this,
                                    "Email reset password đã được gửi",
                                    Toast.LENGTH_LONG).show();

                        }else{

                            Toast.makeText(this,
                                    "Email không tồn tại",
                                    Toast.LENGTH_SHORT).show();

                        }

                    });

        });

        // =========================
        // chuyển sang đăng ký
        // =========================

        txtSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, AuthActivity.class));
        });
    }

    // =========================
    // Google Login Result
    // =========================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);

            try {

                GoogleSignInAccount account =
                        task.getResult(ApiException.class);

                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {

                Toast.makeText(this,
                        "Google Login Failed",
                        Toast.LENGTH_SHORT).show();

            }
        }
    }

    // =========================
    // Firebase Google Auth
    // =========================

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential =
                GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if(task.isSuccessful()){

                        FirebaseUser user = mAuth.getCurrentUser();

                        // Lấy thông tin từ Google
                        String name = account.getDisplayName();
                        String email = account.getEmail();
                        String avatar = "";

                        if(account.getPhotoUrl() != null){
                            avatar = account.getPhotoUrl().toString();
                        }

                        // Lưu user vào Firestore
                        saveUserToFirestore(user.getUid(), name, email, avatar);

                        Toast.makeText(this,
                                "Google Login thành công",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this, MainActivity.class));
                        finish();

                    }else{

                        Toast.makeText(this,
                                "Firebase Login Failed",
                                Toast.LENGTH_SHORT).show();
                    }

                });
    }
    private void saveUserToFirestore(String uid, String name, String email, String avatar){

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("avatar", avatar);

        db.collection("users")
                .document(uid)
                .set(user);
    }
}