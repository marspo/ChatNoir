package com.example.martinjonovski.chatnoir;

import android.content.Context;
import android.content.Intent;
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
public class RequestsFragment extends Fragment {


    private RecyclerView mRequestsList;

    private DatabaseReference mDatabseReference;


    private DatabaseReference mUsersReference;
    private FirebaseAuth mAuth;

    private String currentUserId;
    private TextView noUsersText;
    private ImageView infoIcon;
    private View mainView;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_requests, container, false);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mainView.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        noUsersText = (TextView) mainView.findViewById(R.id.empty_view);
//        infoIcon = (ImageView) mainView.findViewById(R.id.imageView2);
        mRequestsList = (RecyclerView) mainView.findViewById(R.id.requests_list);

        mRequestsList.setLayoutManager(layoutManager);
        mUsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        //  mUsersReference.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        mDatabseReference = FirebaseDatabase.getInstance().getReference().child("Notifications").child(currentUserId);
        mDatabseReference.keepSynced(true);
        mRequestsList.setHasFixedSize(true);


        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<String, RequestsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<String, RequestsViewHolder>(
                String.class,
                R.layout.users_row_layout,
                RequestsViewHolder.class,
                mDatabseReference
        ) {
            @Override
            protected void populateViewHolder(final RequestsViewHolder viewHolder, String model, int position) {
                viewHolder.setDate(model);
                final String userId = getRef(position).getKey();

                mUsersReference.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("name").getValue(String.class);
                        final String img_thumb = dataSnapshot.child("image_thumb").getValue(String.class);
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
                                Intent chatIntent = new Intent(getContext(), ProfileActivity.class);
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
        mRequestsList.setAdapter(firebaseRecyclerAdapter);
//        if (firebaseRecyclerAdapter.getItemCount() == 0) {
//            mRequestsList.setVisibility(View.INVISIBLE);
//            infoIcon.setVisibility(View.VISIBLE);
//            noUsersText.setVisibility(View.VISIBLE);
//        } else {
//            mRequestsList.setVisibility(View.VISIBLE);
//            infoIcon.setVisibility(View.INVISIBLE);
//            noUsersText.setVisibility(View.INVISIBLE);
//        }
    }


    public static class RequestsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public RequestsViewHolder(View itemView) {
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
            ImageView imageView = (ImageView) mView.findViewById(R.id.single_online_img);
            if (bool) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
