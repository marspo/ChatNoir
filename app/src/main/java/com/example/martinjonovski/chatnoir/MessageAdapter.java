package com.example.martinjonovski.chatnoir;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Martin Jonovski on 10/25/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> messagesList;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String mUserImage;
    private String mUserString;
    private Context context;
    private boolean showImage, showed = false;

    public MessageAdapter(List<Messages> messagesList, String user, String image, Context context, String username) {
        this.messagesList = messagesList;
        this.currentUserId = user;
        this.context = context;
        this.mUserImage = image;
        this.mUserString = username;
    }

    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MessageAdapter.MessageViewHolder holder, int position) {

        Messages c = messagesList.get(position);
        String messageType = c.getType();

        final String from_user = c.getFrom();
        if (from_user != null && currentUserId != null) {
            if (from_user.equals(currentUserId)) {
                holder.setCurrentUser(true);
                showImage = false;
                showed = false;

//                holder.messageMe.setBackgroundResource(R.drawable.message_text_me);
//                holder.messageMe.setGravity(Gravity.RIGHT);
//                holder.messageMe.setTextColor(Color.BLACK);
//                holder.messageMe.setText(c.getMessage());
//                holder.message.setVisibility(View.INVISIBLE);
//                holder.messageMe.setVisibility(View.VISIBLE);

            } else {
                if (!showImage && !showed) {
                    showImage = true;
                    showed = false;
                }
                holder.setCurrentUser(false);
//                holder.message.setBackgroundResource(R.drawable.message_text_background);
//                holder.message.setGravity(Gravity.LEFT);
//                holder.message.setTextColor(Color.WHITE);
//
//                holder.messageMe.setVisibility(View.INVISIBLE);
//                holder.message.setVisibility(View.VISIBLE);

            }
            holder.setText(c.getMessage());
            holder.circleImageView.setVisibility(View.INVISIBLE);


            if (showImage && !showed) {
                holder.circleImageView.setVisibility(View.VISIBLE);
                Picasso.with(context).load(mUserImage).placeholder(R.drawable.photo).into(holder.circleImageView);
                holder.circleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(context, ProfileActivity.class);
                        profileIntent.putExtra("user_id", from_user);
                        profileIntent.putExtra("img_thumb", mUserImage);
                        profileIntent.putExtra("username", mUserString);
                        profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(profileIntent);

                    }
                });
                showed = true;
            }
            if (messageType == null || messageType.equals("text")) {
//                holder.message.setVisibility(View.VISIBLE);
//                holder.message.setText(c.getMessage());
//                holder.imageView.setVisibility(View.INVISIBLE);
            } else {
                holder.setImageVisibility();
                // Picasso.with(context).load(c.getMessage()).placeholder(R.drawable.photo).into(holder.imageView);
                Picasso.with(context).load(c.getMessage()).into(holder.imageView);
            }
        }

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView message, messageMe;
        public CircleImageView circleImageView;
        public ImageView imageView;
        private boolean currentUser = false;

        public MessageViewHolder(View itemView) {
            super(itemView);
            message = (TextView) itemView.findViewById(R.id.message_single_layout);
            messageMe = (TextView) itemView.findViewById(R.id.message_single_layout_me);
            circleImageView = (CircleImageView) itemView.findViewById(R.id.message_profile_layout);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
        }

        public void setText(String text) {
            if (currentUser) {
                this.messageMe.setText(text);
            } else {
                this.message.setText(text);
            }
        }

        public void setCurrentUser(boolean bool) {
            this.currentUser = bool;
            if (bool) {
                this.messageMe.setVisibility(View.VISIBLE);
                this.message.setVisibility(View.INVISIBLE);

            } else {
                this.messageMe.setVisibility(View.INVISIBLE);
                this.message.setVisibility(View.VISIBLE);
            }
        }

        public void setImageVisibility() {
            message.setVisibility(View.INVISIBLE);
            messageMe.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }
    }
}
