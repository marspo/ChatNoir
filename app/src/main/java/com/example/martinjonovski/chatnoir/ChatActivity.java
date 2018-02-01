package com.example.martinjonovski.chatnoir;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity {

    private static final int NUMBER_OF_USERS = 10;
    private int mCurrentPage = 0;
    private Toolbar mToolbar;
    private DatabaseReference databaseReference;
    private String userId;
    private String userChat, thumbImage;
    private CircleImageView image;
    private TextView displayName, lastSeen;
    private FirebaseAuth mAuth;
    private StorageReference mStorageReference;

    private String mCurrentUserId;

    private ProgressDialog mProgressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton sendImage;
    private ImageButton sendChat;
    private EditText messageText;
    private RecyclerView recyclerView;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter mAdapter;
    private DatabaseReference messagesReference;

    private String mLastKey = "";
    private String mPreviousKey = "";
    private TripleDES tripleDES;

    private int itemPosition = 0;
    private String msgType = "text";

    private String symetricKey;

    private ChatNoirDbHandler chatNoirDbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        tripleDES = new TripleDES();
        this.userId = getIntent().getStringExtra("user_id");
        userChat = getIntent().getStringExtra("username");
        thumbImage = getIntent().getStringExtra("thumb");

        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mToolbar = (Toolbar) findViewById(R.id.chat_toolbar);

        setSupportActionBar(mToolbar);
        chatNoirDbHandler = new ChatNoirDbHandler(this, null, null, 1);
        Contact userContact = chatNoirDbHandler.getContact(mCurrentUserId + userId);
        if (userContact != null) {
            this.symetricKey = userContact.getKey();
            tripleDES.setKey(symetricKey);
        } else {
            symetricKey = null;
        }
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_row, null);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(userChat);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarView);

        image = (CircleImageView) findViewById(R.id.chat_row_thumb_img);
        displayName = (TextView) findViewById(R.id.custom_chat_name);
        lastSeen = (TextView) findViewById(R.id.custom_chat_last);

        sendImage = (ImageButton) findViewById(R.id.chat_add_extra);
        sendChat = (ImageButton) findViewById(R.id.chat_send_btn);
        messageText = (EditText) findViewById(R.id.chat_text);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layoutr);
        recyclerView = (RecyclerView) findViewById(R.id.chat_list_recycler);


        mAuth = FirebaseAuth.getInstance();
        messagesReference = FirebaseDatabase.getInstance().getReference();
        displayName.setText(userChat);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        mStorageReference = FirebaseStorage.getInstance().getReference();

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        if (thumbImage != null && !thumbImage.isEmpty()) {
            Picasso.with(ChatActivity.this).load(thumbImage).placeholder(R.drawable.photo).into(image);
        }

        mAdapter = new MessageAdapter(messagesList, mCurrentUserId, thumbImage, getApplicationContext(), userChat);

        recyclerView.setAdapter(mAdapter);
        loadMessages();


        databaseReference.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    String online = "false";
                    try {
                        online = dataSnapshot.child("online").getValue().toString();
                    } catch (Exception e) {
                        online = "false";
                    }
                    String image_thumb = dataSnapshot.child("image_thumb").getValue().toString();


                    if (online.equals("true")) {
                        lastSeen.setText("Online now");
                    } else {
                        TimeAgo timeAgo = new TimeAgo();
                        long lastTime = 0;
                        try {
                            lastTime = Long.parseLong(online);
                        } catch (Exception e) {
                            lastTime = 0;
                        }
                        String lastSeenT = timeAgo.getTimeAgo(lastTime);
                        lastSeen.setText(lastSeenT);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReference.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userId)) {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + userId, chatAddMap);
                    chatUserMap.put("Chat/" + userId + "/" + mCurrentUserId, chatAddMap);

                    databaseReference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(ChatActivity.this, "Error in chat", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        sendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                messageText.setText("");
            }
        });

        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImageMessage();
                messageText.setText("");
                msgType = "text";
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPreviousKey = mLastKey;
                itemPosition = 0;
                if (messagesList.size() >= NUMBER_OF_USERS)
                    loadMoreMessages();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


    }

    private void sendImageMessage() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        // startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_INT);
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(ChatActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wait while the upload is finished.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                final String currentUser = "messages" + "/" + mCurrentUserId + "/" + userId;
                final String otherUser = "messages" + "/" + userId + "/" + mCurrentUserId;

                DatabaseReference userMessagePush = databaseReference.child("messages").child(mCurrentUserId).child(userId).push();

                final String pushId = userMessagePush.getKey();

                StorageReference filePath = mStorageReference.child("msg_images").child(pushId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            final String resultString = task.getResult().getDownloadUrl().toString();
                            messageText.setText(resultString);
                            String encryptedMessage = "";
                            if (symetricKey != null) {
                                try {

                                    encryptedMessage = new String(Base64.encode(tripleDES.encrypt(resultString), Base64.DEFAULT));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                encryptedMessage = resultString;
                            }
                            Map messageMap = new HashMap();
                            messageMap.put("message", encryptedMessage);
                            messageMap.put("seen", false);
                            messageMap.put("type", "image");
                            messageMap.put("time", ServerValue.TIMESTAMP);
                            messageMap.put("from", mCurrentUserId);

                            Map messageUserMap = new HashMap();
                            messageUserMap.put(currentUser + "/" + pushId, messageMap);
                            messageUserMap.put(otherUser + "/" + pushId, messageMap);

                            databaseReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError != null) {
                                        Log.d("CHAT", databaseError.getMessage().toString());
                                    }
                                }
                            });
                            recyclerView.scrollToPosition(messagesList.size() - 1);
                            msgType = "text";
                            mProgressDialog.dismiss();

                        } else {
                            Toast.makeText(ChatActivity.this, "Error in uploading", Toast.LENGTH_SHORT).show();
                            Exception e = task.getException();
                            e.printStackTrace();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(ChatActivity.this, "error", 10);
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setAspectRatio(1, 1).start(this);

        }
    }

    public void sendMessage() {

        final String message = messageText.getText().toString();
        messageText.setText("");
        final String type = msgType;
        if (!TextUtils.isEmpty(message)) {
            Map messageMap = new HashMap();


            String currentUser = "messages" + "/" + mCurrentUserId + "/" + userId;
            String otherUser = "messages" + "/" + userId + "/" + mCurrentUserId;

            DatabaseReference userMessagePush = databaseReference.child("messages").child(mCurrentUserId).child(userId).push();

            String pushId = userMessagePush.getKey();
            String encryptedMessage = "";
            if (symetricKey != null) {
                try {

                    encryptedMessage = new String(Base64.encode(tripleDES.encrypt(message), Base64.DEFAULT));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                encryptedMessage = message;
            }
            messageMap.put("message", encryptedMessage);
            messageMap.put("seen", false);
            messageMap.put("type", type);
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUser + "/" + pushId, messageMap);
            messageUserMap.put(otherUser + "/" + pushId, messageMap);

            databaseReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("CHAT", databaseError.getMessage().toString());
                    }
                }
            });
            recyclerView.scrollToPosition(messagesList.size() - 1);
        }
    }

    public void loadMoreMessages() {
        DatabaseReference mReference = messagesReference.child("messages").child(mCurrentUserId).child(userId);

        Query messageQuery = mReference.orderByKey().endAt(mLastKey).limitToLast(NUMBER_OF_USERS);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getKey().equals(mPreviousKey)) {
                    Messages message = dataSnapshot.getValue(Messages.class);
                    if (symetricKey != null) {
                        try {
                            byte[] msgTmp = Base64.decode(message.getMessage(), Base64.DEFAULT);
                            String finalMsg = tripleDES.decrypt(msgTmp);
                            message.setMessage(finalMsg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    messagesList.add(itemPosition++, message);

                    if (itemPosition == 1) {
                        mLastKey = dataSnapshot.getKey();
                    }
                    mAdapter.notifyDataSetChanged();

                    swipeRefreshLayout.setRefreshing(false);
                }
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

    public void loadMessages() {
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
        mReference = mReference.child("messages").child(mCurrentUserId).child(userId);

        Query messageWuery = mReference.orderByKey().limitToLast(NUMBER_OF_USERS * (mCurrentPage + 1));

        messageWuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                itemPosition++;
                if (itemPosition == 1) {
                    mLastKey = dataSnapshot.getKey();
                }
                if (symetricKey != null) {
                    try {
                        byte[] trk = Base64.decode(message.getMessage().getBytes(), Base64.DEFAULT);
                        message.setMessage(tripleDES.decrypt(trk));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                //     recyclerView.scrollToPosition(messagesList.size() - 1);

                swipeRefreshLayout.setRefreshing(false);
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
