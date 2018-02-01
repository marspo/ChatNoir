package com.example.martinjonovski.chatnoir;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.PrivateKey;
import java.security.Security;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class ProfileActivity extends AppCompatActivity {

    static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public enum FriendshipStatus {
        not_friends,
        req_sent,
        req_received,
        friends
    }

    private String userId;
    private TextView mDisplayName, mStatus, mTotalFriend;
    private ImageView mImageView;
    private Button sendRequestButton, mDeclineButton;
    private DatabaseReference usersDatabase, mNotificationsDatabase;
    private ProgressDialog mProgressDialog;
    private int mCurrentState;
    private ChatNoirDbHandler chatNoirDbHandler;
    private FirebaseUser firebaseUser;
    private DatabaseReference friendDatabase;
    private TripleDES tripleDES;
    private String publicKey;
    private String publicSignatureKey;
    private DatabaseReference mDatabaseReference;
    private RSAUtil rsaUtil;

    private String sharedKey;
    private String username, img_thumb;
    private String signature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        chatNoirDbHandler = new ChatNoirDbHandler(this, null, null, 1);
        try {
            rsaUtil = new RSAUtil();
        } catch (Exception e) {
            rsaUtil = null;
            e.printStackTrace();
        }
        mDeclineButton = (Button) findViewById(R.id.profile_decline_btn);
        mCurrentState = FriendshipStatus.not_friends.ordinal();
        userId = getIntent().getStringExtra("user_id");
        username = getIntent().getStringExtra("username");
        img_thumb = getIntent().getStringExtra("thumb");

        mDisplayName = (TextView) findViewById(R.id.profile_display_name);
        mStatus = (TextView) findViewById(R.id.profile_status_txt);
        //  mTotalFriend = (TextView) findViewById(R.id.profile_total_friends_txt);
        mImageView = (ImageView) findViewById(R.id.profile_image_view);
        sendRequestButton = (Button) findViewById(R.id.profile_friend_request_btn);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User");
        mProgressDialog.setMessage("Please wait while we load user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        usersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationsDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //  mDeclineButton.setVisibility(View.INVISIBLE);
        //mDeclineButton.setEnabled(false);

        usersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("name").getValue(String.class);
                String status = dataSnapshot.child("status").getValue(String.class);
                String image = dataSnapshot.child("image").getValue(String.class);
                publicKey = dataSnapshot.child("public").getValue(String.class);
                publicSignatureKey = dataSnapshot.child("publicsign").getValue(String.class);

                mDisplayName.setText(displayName);
                mStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.maxresdefault).into(mImageView);

                //-------------Friends list/request---------------
                mDatabaseReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(userId)) {
                            String req_type = dataSnapshot.child(userId).child("req_type").getValue().toString();
                            if (req_type.equals("received")) {
                                mCurrentState = FriendshipStatus.req_received.ordinal();
                                sendRequestButton.setText("ACCEPT FRIEND REQUEST");
                                mDeclineButton.setVisibility(View.VISIBLE);
                                mDeclineButton.setEnabled(true);
                            } else if (req_type.equals("sent")) {
                                mCurrentState = FriendshipStatus.req_sent.ordinal();
                                sendRequestButton.setText("CANCEL FRIEND REQUEST");
                                sendRequestButton.setBackgroundColor(Color.GRAY);
                                mDeclineButton.setVisibility(View.VISIBLE);
                                mDeclineButton.setEnabled(true);
                            }
                        } else {
                            friendDatabase.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userId)) {
                                        mCurrentState = FriendshipStatus.friends.ordinal();
                                        sendRequestButton.setText("Unfriend this person");
                                        mDeclineButton.setText("Open conversation");
//                                        mDeclineButton.setVisibility(View.INVISIBLE);
//                                        mDeclineButton.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequestButton.setEnabled(false);
                // ---------------------------------NOTFRIENDS----------------------------------------
                if (mCurrentState == FriendshipStatus.not_friends.ordinal()) {
                    tripleDES = new TripleDES();
                    HashMap<String, String> reqData = new HashMap<String, String>();
                    reqData.put("date", ServerValue.TIMESTAMP.toString());
                    reqData.put("req_type", "sent");


                    String encryptedSymetric = "";
                    String signature = "";
                    try {
                        // Convert the public key bytes into a PublicKey object

                        encryptedSymetric = rsaUtil.encryptString(publicKey, tripleDES.getKey());
                        //create new local contact with the user uid generated from firebase and add the shared key to it
                        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        //store the shared contact's key in the DB
                        Contact contact = new Contact(uId + userId, tripleDES.getKey());
                        chatNoirDbHandler.deleteContact(contact.getName());
                        chatNoirDbHandler.addContact(contact);

                        //get the private key file for signing
                        Contact myDigitalSignature = chatNoirDbHandler.getContact(uId + "PRIVATESIGNATURE");
                        PrivateKey myPrivateSignatureKey = rsaUtil.loadPrivateKey(myDigitalSignature.getKey());
                        signature = rsaUtil.signString(uId, encryptedSymetric.getBytes(), myPrivateSignatureKey);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    final String finalKeyString = encryptedSymetric;
                    reqData.put("signature", signature);
                    reqData.put("key", finalKeyString);
                    mDatabaseReference.child(firebaseUser.getUid()).child(userId).setValue(reqData).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                HashMap<String, String> reqDataR = new HashMap<String, String>();
                                reqDataR.put("req_type", "received");
                                reqDataR.put("key", finalKeyString);
                                mDatabaseReference.child(userId).child(firebaseUser.getUid()).setValue(reqDataR).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mNotificationsDatabase.child(userId).child(firebaseUser.getUid()).setValue("Friend request").addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                sendRequestButton.setText("CANCEL FRIEND REQUEST");
                                                sendRequestButton.setBackgroundColor(Color.GRAY);
                                                sendRequestButton.setEnabled(true);
                                                mCurrentState = 1;
//                                                mDeclineButton.setVisibility(View.INVISIBLE);
//                                                mDeclineButton.setEnabled(false);
                                            }
                                        });


                                    }
                                });
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                //-------------------------------------REQUEST SENT-----------------------------------

                else if (mCurrentState == FriendshipStatus.req_sent.ordinal()) {
                    try {
                        removeRequest(userId, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mCurrentState = FriendshipStatus.not_friends.ordinal();
                    sendRequestButton.setText(R.string.send_friend_request);
                    mDeclineButton.setVisibility(View.VISIBLE);
                    mDeclineButton.setEnabled(true);
                }
                //-------------------------------REQUEST RECEIVED---------------------------------------

                else if (mCurrentState == FriendshipStatus.req_received.ordinal()) {
                    final String currentDate = new Date().toString();

                    friendDatabase.child(firebaseUser.getUid()).child(userId).setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull final Task<Void> task) {
                            if (task.isSuccessful()) {
                                friendDatabase.child(userId).child(firebaseUser.getUid()).setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        try {

                                            removeRequest(userId, true);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        mCurrentState = FriendshipStatus.friends.ordinal();
                                        //TODO
                                        try {
                                            removeRequest(userId, true);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        sendRequestButton.setText("Unfriend this person");
                                        mDeclineButton.setVisibility(View.INVISIBLE);
                                        mDeclineButton.setEnabled(false);
                                    }
                                });

                            }
                        }
                    });


                } else if (mCurrentState == FriendshipStatus.friends.ordinal()) {
                    friendDatabase.child(firebaseUser.getUid()).child(userId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            friendDatabase.child(userId).child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    mCurrentState = FriendshipStatus.not_friends.ordinal();
                                    sendRequestButton.setText(R.string.send_friend_request);
                                    mDeclineButton.setVisibility(View.INVISIBLE);
                                    mDeclineButton.setEnabled(false);
                                }
                            });
                        }
                    });
                }
            }
        });

        mDeclineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentState != FriendshipStatus.friends.ordinal()) {
                    Intent chatIntent = new Intent(view.getContext(), PublicChatActivity.class);
                    chatIntent.putExtra("user_id", userId);
                    chatIntent.putExtra("username", username);
                    chatIntent.putExtra("thumb", img_thumb);
                    startActivity(chatIntent);
                } else if (mCurrentState == FriendshipStatus.friends.ordinal()) {
                    Intent chatIntent = new Intent(view.getContext(), ChatActivity.class);
                    chatIntent.putExtra("user_id", userId);
                    chatIntent.putExtra("username", username);
                    chatIntent.putExtra("thumb", img_thumb);
                    startActivity(chatIntent);

                }
            }
        });
    }

    public void removeRequest(final String userId, boolean save) throws Exception {
        if (save) {
            final String result = "";
            mDatabaseReference.child(userId).child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    sharedKey = dataSnapshot.child("key").getValue(String.class);
                    signature = dataSnapshot.child("signature").getValue(String.class);

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    byte[] encodedKey = Base64.decode(sharedKey.getBytes(), Base64.DEFAULT);
//                    if (myContact != null) {
//
//                        String myKey = myContact.getKey();
//
//                        byte[] encodedPrivateKey = Base64.decode(myKey.getBytes(), Base64.DEFAULT);
//
//                        PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(encodedPrivateKey);

//                            KeyFactory kf = KeyFactory.getInstance("RSA");
//                            PrivateKey privKey = kf.generatePrivate(specPriv);
                    String result1 = new String(rsaUtil.decryptString(uid, sharedKey));
                    if (rsaUtil.verifySign(sharedKey, signature, publicSignatureKey)) {
                        Contact contact = new Contact(uid + userId, result1);
                        chatNoirDbHandler.addContact(contact);
                    }

                    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Friend_req");
                    mDatabaseReference.child(userId).child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mDatabaseReference.child(firebaseUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
                                            mDatabaseReference.child(userId).child(firebaseUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        mDatabaseReference.child(firebaseUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    mCurrentState = 0;

                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });


                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            //log.error("FATAL ERROR CONTACT NOT FOUND,KEY COULDNT BE EXTRACTED!");


        }


    }


    /**
     * generates a random string with length 10
     *
     * @return
     */
    private String getSaltString() {
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { // length of the random string.
            int index = (int) (rnd.nextFloat() * ALPHABET.length());
            salt.append(ALPHABET.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }


}
