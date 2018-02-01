package com.example.martinjonovski.chatnoir;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSettingsActivity extends AppCompatActivity {

    private static final int GALLERY_INT = 2;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser mCuurentUser;

    private CircleImageView mCircleImageView;
    private TextView displayName, status;

    private StorageReference mStorageReference;
    private Button mUploadImg;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        mStorageReference = FirebaseStorage.getInstance().getReference();
        mCircleImageView = (CircleImageView) findViewById(R.id.account_image_view);
        displayName = (TextView) findViewById(R.id.account_display_txt);
        status = (TextView) findViewById(R.id.account_desc_txt);

        mUploadImg = (Button) findViewById(R.id.account_changeimg_btn);

        mCuurentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = mCuurentUser.getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        mDatabaseRef.keepSynced(true);

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                final String statusS = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("image_thumb").getValue().toString();

                displayName.setText(name);
                if (image != null && !image.isEmpty() && !image.equals("default"))
                    Picasso.with(AccountSettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.photo).into(mCircleImageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(AccountSettingsActivity.this).load(image).placeholder(R.drawable.photo).into(mCircleImageView);

                        }
                    });
                status.setText(statusS);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(AccountSettingsActivity.this);

                // startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_INT);
            }
        });
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


                String userId = mCuurentUser.getUid();
                File thumbFile = new File(resultUri.getPath());
                Bitmap thumbBitmap = null;
                try {
                    thumbBitmap = new Compressor(this).setMaxHeight(200).setMaxWidth(200).setQuality(75).compressToBitmap(thumbFile);
                } catch (IOException e) {
                    Toast.makeText(AccountSettingsActivity.this, "Error IO", 5);
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] dataThumb = baos.toByteArray();


                StorageReference filePath = mStorageReference.child("profile_images").child(userId + ".jpg");
                final StorageReference thumbFilePath = mStorageReference.child("profile_images").child("thumbs").child(userId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            final Uri downloadUrl = task.getResult().getDownloadUrl();
                            final String resultString = downloadUrl.toString();

                            UploadTask uploadTask = thumbFilePath.putBytes(dataThumb);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask) {
                                    String thumbDownloadUrl = thumbTask.getResult().getDownloadUrl().toString();
                                    if (thumbTask.isSuccessful()) {
                                        Map updateHashMap = new HashMap<>();
                                        mProgressDialog.dismiss();
                                        updateHashMap.put("image", resultString);
                                        updateHashMap.put("image_thumb", thumbDownloadUrl);

                                        mDatabaseRef.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mProgressDialog.dismiss();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(AccountSettingsActivity.this, "Error in uploading thumbnail", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(AccountSettingsActivity.this, "Error in uploading", Toast.LENGTH_SHORT).show();
                            Exception e = task.getException();
                            e.printStackTrace();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(AccountSettingsActivity.this, "error", 10);
            }
        } else if (requestCode == GALLERY_INT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setAspectRatio(1, 1).start(this);

        }
    }

    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
