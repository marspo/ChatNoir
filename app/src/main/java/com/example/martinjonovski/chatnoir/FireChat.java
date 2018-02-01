package com.example.martinjonovski.chatnoir;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by Martin Jonovski on 10/24/2017.
 */

public class FireChat extends Application {

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Picasso.Builder picassoBuilder = new Picasso.Builder(this);
        picassoBuilder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = picassoBuilder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        mDatabaseReference.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                        // mDatabaseReference.child("online").setValue(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
