package com.example.martinjonovski.chatnoir;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.HashMap;

import javax.security.auth.x500.X500Principal;

public class RegisterActivity extends AppCompatActivity {

    private EditText mDisplayName, mPassword, mEmail;
    private Button mCreateButton;

    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    private FirebaseDatabase mDatabase;
    private DatabaseReference databaseReference;
    private ChatNoirDbHandler chatNoirDbHandler;
    private Context context;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(this);
        context = this;
        mAuth = FirebaseAuth.getInstance();
        mDisplayName = (EditText) findViewById(R.id.reg_display_name);
        mPassword = (EditText) findViewById(R.id.reg_text_pass);
        mEmail = (EditText) findViewById(R.id.reg_text_email);

        mDatabase = FirebaseDatabase.getInstance();
        chatNoirDbHandler = new ChatNoirDbHandler(this, null, null, 1);
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCreateButton = (Button) findViewById(R.id.reg_create_btn);

        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayName = mDisplayName.getText().toString();
                String pass = mPassword.getText().toString();
                String email = mEmail.getText().toString();
                if (!TextUtils.isEmpty(displayName) && !TextUtils.isEmpty(email) && email.contains("@") && pass.length() > 4) {
                    progressDialog.setTitle("Registering User");
                    progressDialog.setMessage("Please wait while we create your account");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    registerUser(displayName, email, pass);
                }
            }
        });
    }

    public void registerUser(final String displayName, String email, String password) {
        email = email.toLowerCase();
        email.trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            String error = "";

                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                error = "Weak password";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                error = "Invalid email";
                            } catch (FirebaseAuthUserCollisionException e) {
                                error = "User already exists";
                            } catch (Exception e) {
                                error = "Unknown error";
                            }
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, error,
                                    Toast.LENGTH_SHORT).show();


                        } else {
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = null;
                            if (firebaseUser != null) {
                                uid = firebaseUser.getUid();
                                databaseReference = mDatabase.getReference().child("Users").child(uid);
                                HashMap<String, String> userMap = new HashMap<String, String>();
                                userMap.put("name", displayName);
                                userMap.put("status", "not much to say bro");
                                userMap.put("image", "default");
                                userMap.put("image_thumb", "default");
                                //    db.addContact(new Contact("Ravi", "9100000000"));
                                String tokenId = FirebaseInstanceId.getInstance().getToken();
                                String outFile = "";
                                String publicSignatureKey = "";
                                String privateFile = "";
                                userMap.put("tokenid", tokenId);


                                final String finalPrivateFile = privateFile;
                                final String finalUid = uid;
                                try {
                                    String alias = finalUid;
                                    try {
                                        KeyStore keyStore = null;
                                        try {
                                            keyStore = KeyStore.getInstance("AndroidKeyStore");
                                            keyStore.load(null);

                                        } catch (KeyStoreException e) {
                                            e.printStackTrace();
                                        } catch (CertificateException e) {
                                            e.printStackTrace();
                                        } catch (NoSuchAlgorithmException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        // Create new key if needed
                                        if (!keyStore.containsAlias(alias)) {
                                            Calendar start = Calendar.getInstance();
                                            Calendar end = Calendar.getInstance();
                                            end.add(Calendar.YEAR, 1);
                                            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                                                    .setAlias(alias)
                                                    .setSubject(new X500Principal("CN=Sample Name, O=Android Authority"))
                                                    .setSerialNumber(BigInteger.ONE)
                                                    .setStartDate(start.getTime())
                                                    .setEndDate(end.getTime())
                                                    .build();
                                            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                                            generator.initialize(spec);

                                            KeyPair keyPair = generator.generateKeyPair();
                                            PublicKey publicKey = keyPair.getPublic();
                                            outFile = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);

                                            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                                            kpg.initialize(1024);

                                            //signature related key
                                            KeyPair keyPair2 = kpg.genKeyPair();
                                            PrivateKey privateKey = keyPair2.getPrivate();
                                            PublicKey signatureKey = keyPair2.getPublic();
                                            String privateSignatureKey = Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);
                                            Contact contact = new Contact(finalUid + "PRIVATESIGNATURE", privateSignatureKey);
                                            chatNoirDbHandler.addContact(contact);
//                                            PublicKey publicKey = keyPair.getPublic();
                                            publicSignatureKey = Base64.encodeToString(signatureKey.getEncoded(), Base64.DEFAULT);
//                                            outFile = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                userMap.put("public", outFile);
                                userMap.put("publicsign", publicSignatureKey);
                                databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            progressDialog.dismiss();

                                            Intent mainIntenet = new Intent(RegisterActivity.this, MainActivity.class);
                                            startActivity(mainIntenet);
                                            finish();
                                        } else {
                                            dismis();
                                        }
                                    }
                                });


                            } else {
                                dismis();
                            }
                        }
                    }
                });
    }

    private void dismis() {
        progressDialog.dismiss();
        Toast.makeText(RegisterActivity.this, "CannotNULLLLLLand try again",
                Toast.LENGTH_SHORT).show();
    }
}
