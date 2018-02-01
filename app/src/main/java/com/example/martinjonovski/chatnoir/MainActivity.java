package com.example.martinjonovski.chatnoir;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Toolbar mToolbar;
    private DatabaseReference mUserReference;
    private String userId;

    private ViewPager mviewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar) findViewById(R.id.main_page_appbar);

        setSupportActionBar(mToolbar);
        mviewPager = (ViewPager) findViewById(R.id.tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);


        mviewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mviewPager);
        mTabLayout.getTabAt(0).setIcon(getResources().getDrawable(R.drawable.notifications));
        mTabLayout.getTabAt(1).setIcon(getResources().getDrawable(R.drawable.chatting));
        mTabLayout.getTabAt(2).setIcon(getResources().getDrawable(R.drawable.friends));

        if (mAuth.getCurrentUser() != null) {
            mUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            userId = mAuth.getCurrentUser().getUid();
        } else {
            sendToStart();

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            sendToStart();
        } else {
            mUserReference.child("online").setValue("true");
            mUserReference.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        String name = dataSnapshot.getValue(String.class);
                        if (name != null) {
                            name = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
                            mToolbar.setTitle(name);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);


        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null)
            mUserReference.child("online").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_log_out) {
            mAuth.signOut();
            sendToStart();
        }
        if (item.getItemId() == R.id.main_account_btn) {
            Intent settingIntent = new Intent(MainActivity.this, AccountSettingsActivity.class);
            startActivity(settingIntent);
        }

        if (item.getItemId() == R.id.main_all_btn) {
            Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
            usersIntent.putExtra("user_id", userId);
            startActivity(usersIntent);
        }

        return true;
    }
}

