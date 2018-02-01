package com.example.martinjonovski.chatnoir;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private EditText email, pass;
    private Button mButtonLogin;
    private FirebaseAuth mAuth;

    private ProgressDialog loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        loginProgress = new ProgressDialog(this);

        email = (EditText) findViewById(R.id.login_username_txt);
        pass = (EditText) findViewById(R.id.login_text_pass);
        mButtonLogin = (Button) findViewById(R.id.login_login_btn);

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginProgress.setTitle("Logging In");
                loginProgress.setMessage("Please wait while we check your creentials.");
                loginProgress.setCanceledOnTouchOutside(false);
                loginProgress.show();
                String mail = email.getText().toString();
                String passw = pass.getText().toString();

                //login(mail.trim(), passw);
            }
        });
    }


}
