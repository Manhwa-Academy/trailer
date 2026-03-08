package com.mari.magic.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mari.magic.R;
import com.mari.magic.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
public class SignupFormActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtPassword, edtConfirmPassword;
    Button btnContinue;
    TextView txtLogin;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_form);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnContinue = findViewById(R.id.btnContinue);
        txtLogin = findViewById(R.id.txtLogin);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // bật icon mắt cho password
        setupPasswordToggle(edtPassword);
        setupPasswordToggle(edtConfirmPassword);

        btnContinue.setOnClickListener(v -> {

            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();
            String confirm = edtConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (name.length() < 6 || !name.matches("[a-zA-Z ]+")) {
                Toast.makeText(this, "Tên >=6 ký tự và không có ký tự đặc biệt", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.endsWith("@gmail.com")) {
                Toast.makeText(this, "Email phải có @gmail.com", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass.length() < 6) {
                Toast.makeText(this, "Password >=6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Password không trùng nhau", Toast.LENGTH_SHORT).show();
                return;
            }

            // ===== Đăng ký Firebase =====
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();

                            // ===== Lưu Firestore =====
                            Map<String, Object> data = new HashMap<>();
                            data.put("name", name);
                            data.put("email", email);
                            data.put("uid", user.getUid());

                            db.collection("users")
                                    .document(user.getUid())
                                    .set(data);

                            Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(this, MainActivity.class));
                            finish();

                        } else {

                            Toast.makeText(this,
                                    "Lỗi: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();

                        }

                    });

        });


// chuyển sang login
        txtLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    // toggle icon mắt
    private void setupPasswordToggle(EditText editText){

        editText.setOnTouchListener((v, event) -> {

            if(event.getAction() == MotionEvent.ACTION_UP){

                int drawableEnd = 2;

                if(event.getRawX() >= (editText.getRight() -
                        editText.getCompoundDrawables()[drawableEnd].getBounds().width())){

                    if(editText.getInputType() ==
                            (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)){

                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        editText.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_eye_off,0);

                    }else{

                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editText.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_eye,0);

                    }

                    editText.setSelection(editText.getText().length());
                    return true;
                }
            }
            return false;
        });
    }
}