package com.example.martinjonovski.chatnoir;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class StartActivity extends AppCompatActivity {

    private Button mRegButton, mLoginButton;
    private EditText email, pass;
    private Button mButtonLogin;
    private FirebaseAuth mAuth;

    private ProgressDialog loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mAuth = FirebaseAuth.getInstance();
        loginProgress = new ProgressDialog(this);

        email = (EditText) findViewById(R.id.loginEditText);
        pass = (EditText) findViewById(R.id.passLoginText);
//        mLoginButton.setColoredModeColors(Color.WHITE,
//                fetchColor(R.color.colorPrim));

        mRegButton = (Button) findViewById(R.id.start_reg_btn);
        mRegButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
                finish();
            }
        });
        mLoginButton = (Button) findViewById(R.id.start_login_btn);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginProgress.setTitle("Logging In");
                loginProgress.setMessage("Please wait while we check your creentials.");
                loginProgress.setCanceledOnTouchOutside(false);
                loginProgress.show();
                String mail = email.getText().toString();
                String passw = pass.getText().toString();

                login(mail.trim(), passw);
            }
        });
    }

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {


                            loginProgress.dismiss();
                            Toast.makeText(StartActivity.this, "Error occured during sign in.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            String tokenId = FirebaseInstanceId.getInstance().getToken();
                            String currentUserId = mAuth.getCurrentUser().getUid();
                            FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId).child("tokenid").setValue(tokenId).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                            loginProgress.dismiss();
                            Intent mainIntent = new Intent(StartActivity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }

                        // ...
                    }
                });
    }
}
