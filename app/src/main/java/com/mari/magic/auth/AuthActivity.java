package com.mari.magic.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mari.magic.R;
import com.mari.magic.home.MainActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.*;

public class AuthActivity extends AppCompatActivity {

    LinearLayout btnGoogleSignup, btnEmailSignup;
    TextView txtLogin;

    GoogleSignInClient googleSignInClient;
    FirebaseAuth mAuth;

    int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        btnGoogleSignup = findViewById(R.id.btnGoogleSignup);
        btnEmailSignup = findViewById(R.id.btnEmailSignup);
        txtLogin = findViewById(R.id.txtLogin);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Google Sign In config
        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Google signup
        btnGoogleSignup.setOnClickListener(v -> {
            signInWithGoogle();
        });

        // Email signup
        btnEmailSignup.setOnClickListener(v -> {
            Intent intent = new Intent(AuthActivity.this, SignupFormActivity.class);
            startActivity(intent);
        });

        // chuyển sang login
        txtLogin.setOnClickListener(v -> {
            Intent intent = new Intent(AuthActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    // =========================
    // Google Sign In
    // =========================

    private void signInWithGoogle(){

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);

            try {

                GoogleSignInAccount account =
                        task.getResult(ApiException.class);

                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {

                Toast.makeText(this,
                        "Google Login Failed",
                        Toast.LENGTH_SHORT).show();

            }
        }
    }

    // =========================
    // Firebase Auth
    // =========================

    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        Toast.makeText(this,
                                "Login thành công: " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this, MainActivity.class));
                        finish();

                    } else {

                        Toast.makeText(this,
                                "Firebase Login Failed",
                                Toast.LENGTH_SHORT).show();

                    }

                });
    }
}