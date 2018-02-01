package com.example.martinjonovski.chatnoir;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {


    private RecyclerView mFriendsList;

    private DatabaseReference mDatabseReference;
    private DatabaseReference mUsersReference;
    private FirebaseAuth mAuth;

    private String currentUserId;

    private View mainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mainView.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mFriendsList = (RecyclerView) mainView.findViewById(R.id.friends_list);
        mFriendsList.setLayoutManager(layoutManager);
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        //  mUsersReference.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        mDatabseReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        mDatabseReference.keepSynced(true);
        mFriendsList.setHasFixedSize(true);


        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<String, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<String, FriendsViewHolder>(
                String.class,
                R.layout.users_row_layout,
                FriendsViewHolder.class,
                mDatabseReference
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, String model,
                                              int position) {
                viewHolder.setDate(model);
                final String userId = getRef(position).getKey();

                mUsersReference.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("name").getValue().toString();
                        final String img_thumb = dataSnapshot.child("image_thumb").getValue().toString();
                        String online = "false";
                        try {
                            online = dataSnapshot.child("online").getValue().toString();
                        } catch (Exception ex) {
                            online = "false";
                        }
                        viewHolder.setOnlineStatus(online.equals("true"));

                        viewHolder.setName(username);
                        viewHolder.setImgThumb(img_thumb, getContext());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", userId);
                                chatIntent.putExtra("username", username);
                                chatIntent.putExtra("thumb", img_thumb);
                                startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mFriendsList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String date) {
            // this.date = date;
            TextView status = (TextView) mView.findViewById(R.id.user_single_status);
            status.setText(date);

        }

        public void setName(String name) {
            TextView displayName = (TextView) mView.findViewById(R.id.user_single_name);
            displayName.setText(name);
        }

        public void setImgThumb(String imgThumb, Context context) {
            CircleImageView circleImage = (CircleImageView) mView.findViewById(R.id.user_single_image);
            //Picasso.with(context).load(imgThumb).placeholder(R.drawable.photo).into(circleImage);
        }

        public void setOnlineStatus(boolean bool) {
            CircleImageView imageView = (CircleImageView) mView.findViewById(R.id.single_online_img);
            Bitmap icon = BitmapFactory.decodeResource(mView.getContext().getResources(),
                    R.drawable.online_bg);

            if (bool) {

                imageView.setImageBitmap(icon);

            } else {
                Bitmap iconOff = BitmapFactory.decodeResource(mView.getContext().getResources(),
                        R.drawable.offline_icon);
                imageView.setImageBitmap(iconOff);

            }
        }
    }
}
