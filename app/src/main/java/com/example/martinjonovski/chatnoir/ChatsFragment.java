package com.example.martinjonovski.chatnoir;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mFriendsList;

    private DatabaseReference mDatabseReference;


    private DatabaseReference mPublicChatsReference;
    private DatabaseReference mUsersReference;


    private FirebaseAuth mAuth;

    private String currentUserId;
    private List<String> userIds;
    private Map<String, Users> usersList;
    //    private TextView noUsersText;
//    private ImageView infoIcon;
    private View mainView;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_chats, container, false);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mainView.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        usersList = new HashMap<>();
        mFriendsList = (RecyclerView) mainView.findViewById(R.id.chats_list);
//        noUsersText = (TextView) mainView.findViewById(R.id.empty_view_chats);
//        infoIcon = (ImageView) mainView.findViewById(R.id.info_img_chats);
        mFriendsList.setLayoutManager(layoutManager);

        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        //  mUsersReference.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userIds = new ArrayList<>();
        mPublicChatsReference = FirebaseDatabase.getInstance().getReference().child("publicchats").child(currentUserId);
        mDatabseReference = FirebaseDatabase.getInstance().getReference().child("publicchats").child(currentUserId);

        mDatabseReference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> list = dataSnapshot.getChildren();
                // Filter User
                for (DataSnapshot dataSnapshot1 : list) {
                    userIds.add(dataSnapshot1.getKey());
                }

                for (String s : userIds) {

                    mDatabseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(s);
                    mDatabseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final Users c = dataSnapshot.getValue(Users.class);
                            c.setUid(dataSnapshot.getKey());
                            if (!c.getUid().equals(currentUserId)) {
                                mPublicChatsReference = mPublicChatsReference.child(c.getUid());
                                Query messageQuery = mPublicChatsReference.orderByChild("time").limitToLast(1);
                                messageQuery.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        Messages messages = dataSnapshot.getValue(Messages.class);
                                        if (messages != null) {
                                            c.setStatus(messages.getMessage());
                                            if (messages.getFrom().equals(currentUserId)) {
                                                c.setMyMessage(true);
                                            }
                                            if (usersList.get(c.getUid()) != null) {
                                                usersList.get(c.getUid()).setStatus(messages.getMessage());
                                            } else {
                                                usersList.put(c.getUid(), c);
                                            }
                                        }
                                        UsersAdapter mAdapter = new UsersAdapter(usersList, getContext(), false);
                                        mFriendsList.setHasFixedSize(true);
                                        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

                                        mFriendsList.setAdapter(mAdapter);
                                        mDatabseReference.keepSynced(true);
                                        mFriendsList.setHasFixedSize(true);
                                    }

                                    @Override
                                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                UsersAdapter mAdapter = new UsersAdapter(usersList, getContext(), false);
                mFriendsList.setHasFixedSize(true);
                mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

                mFriendsList.setAdapter(mAdapter);
                mDatabseReference.keepSynced(true);
                mFriendsList.setHasFixedSize(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //  mFriendsList.setAdapter(firebaseRecyclerAdapter);
//        if (firebaseRecyclerAdapter.getItemCount() == 0) {
//            mFriendsList.setVisibility(View.INVISIBLE);
//            infoIcon.setVisibility(View.VISIBLE);
//            noUsersText.setVisibility(View.VISIBLE);
//        } else {
//            mFriendsList.setVisibility(View.VISIBLE);
//            infoIcon.setVisibility(View.INVISIBLE);
//            noUsersText.setVisibility(View.INVISIBLE);
//        }
    }

}
