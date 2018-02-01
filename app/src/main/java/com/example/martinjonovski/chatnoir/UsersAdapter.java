package com.example.martinjonovski.chatnoir;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Martin Jonovski on 10/25/2017.
 */

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    private List<Users> messagesList;
    private boolean b;
    private Context context;

    public UsersAdapter(List<Users> userList, Context context, boolean b) {
        this.messagesList = userList;
        this.context = context;
        this.b = b;
    }

    public UsersAdapter(Map<String, Users> userList, Context context, boolean b) {
        this.messagesList = new ArrayList<>();
        for (Users u : userList.values()) {
            this.messagesList.add(u);
        }
        this.context = context;
        this.b = b;
    }

    @Override
    public UsersAdapter.UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_row_layout, parent, false);
        return new UsersViewHolder(v, context, b);
    }

    @Override
    public void onBindViewHolder(UsersViewHolder holder, int position) {
        Users c = messagesList.get(position);

        holder.circleImageView.setVisibility(View.VISIBLE);
        Picasso.with(context).load(c.getImage_thumb()).placeholder(R.drawable.photo).into(holder.circleImageView);
        holder.userName.setText(c.getName());
        holder.setCircleImageViewString(c.getImage_thumb());
        holder.setUserIdStr(c.getUid());
        holder.userId.setText(c.getStatus());
        if (c.isMyMessage()) {
            holder.userId.setTextColor(Color.LTGRAY);
        } else {
            holder.userId.setTextColor(Color.GRAY);
        }


    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView userId, userName;
        public CircleImageView circleImageView;
        public ImageView onlineIcon;
        private String circleImageViewString;
        private String userIdStr;
        private Context context;
        private boolean b;

        public UsersViewHolder(View itemView, Context context, boolean b) {
            super(itemView);
            this.b = b;
            this.context = context;
            userName = (TextView) itemView.findViewById(R.id.user_single_name);
            userId = (TextView) itemView.findViewById(R.id.user_single_status);
            circleImageView = (CircleImageView) itemView.findViewById(R.id.user_single_image);
            onlineIcon = (ImageView) itemView.findViewById(R.id.single_online_img);
            itemView.setOnClickListener(this);

        }

        public void setCircleImageViewString(String s) {
            this.circleImageViewString = s;
        }

        public void setUserIdStr(String str) {
            this.userIdStr = str;
        }

        @Override
        public void onClick(View view) {
            Intent profileIntent = null;
            if (b)
                profileIntent = new Intent(context, ProfileActivity.class);
            else
                profileIntent = new Intent(context, PublicChatActivity.class);
            profileIntent.putExtra("user_id", userIdStr);
            profileIntent.putExtra("thumb", circleImageViewString);
            profileIntent.putExtra("username", userName.getText().toString());
            profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(profileIntent);

        }


    }
}
