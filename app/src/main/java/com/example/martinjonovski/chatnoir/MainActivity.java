package com.example.martinjonovski.chatnoir;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
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
import com.google.firebase.database.ServerValue;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Toolbar mToolbar;
    private DatabaseReference mUserReference;
    private String userId;
    private CircleImageView mDrawerImage;
    private ViewPager mviewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;
    private TextView nameText;
    private StorageReference mStorageReference;
    private Button mUploadImg;
    private int currentColorId;
    private MenuItem mSearchItem;

    private static final int GALLERY_INT = 2;
    private DatabaseReference mDatabaseRef;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.main_page_appbar);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.open, R.string.close);
        actionBarDrawerToggle.syncState();
        mDrawer.addDrawerListener(actionBarDrawerToggle);
        setSupportActionBar(mToolbar);

        mviewPager = (ViewPager) findViewById(R.id.tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        mviewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mviewPager);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        mDrawerImage = header.findViewById(R.id.imageView23);
        nameText = header.findViewById(R.id.fok_en_name);
        currentColorId = R.attr.colorPrimary;

        mviewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        currentColorId = getResources().getColor(R.color.colorPrim);
                        break;
                    case 1:
                        currentColorId = getResources().getColor(R.color.colorPrimarydva);
                        break;
                    case 2:
                        currentColorId = getResources().getColor(R.color.mpraska);
                        break;
                    default:
                        break;
                }
                setColors(currentColorId);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mTabLayout.getTabAt(0).setIcon(getResources().getDrawable(R.drawable.friends_selector));
        mTabLayout.getTabAt(1).setIcon(getResources().getDrawable(R.drawable.notifications_selector));
        mTabLayout.getTabAt(2).setIcon(getResources().getDrawable(R.drawable.chat_selector));

        setColors(getResources().getColor(R.color.colorPrim));

        if (mAuth.getCurrentUser() != null) {
            mUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            userId = mAuth.getCurrentUser().getUid();
            mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
            mDatabaseRef.keepSynced(true);
            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    final String image = dataSnapshot.child("image").getValue().toString();
                    final String statusS = dataSnapshot.child("status").getValue().toString();
                    String thumb_image = dataSnapshot.child("image_thumb").getValue().toString();

                    nameText.setText(name);
                    if (image != null && !image.isEmpty() && !image.equals("default"))
                        Picasso.with(MainActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.photo).into(mDrawerImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.photo).into(mDrawerImage);

                            }
                        });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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
//            mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if (dataSnapshot != null) {
//                        String name = dataSnapshot.child("name").getValue(String.class);
//                        String image = dataSnapshot.child("image").getValue(String.class);
//                        if (name != null) {
//                            name = name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
//                            nameText.setText(name);
//
//                        }
//                        if (image != null) {
//                            Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.photo).into(mDrawerImage);
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
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
        mSearchItem = menu.findItem(R.id.m_search);

        MenuItemCompat.setOnActionExpandListener(mSearchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Called when SearchView is collapsing
                if (mSearchItem.isActionViewExpanded()) {
                    animateSearchToolbar(1, false, false);
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Called when SearchView is expanding
                animateSearchToolbar(1, true, true);
                return true;
            }
        });

        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Long time = System.currentTimeMillis();
            mUserReference.child("online").setValue(time.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        super.onOptionsItemSelected(item);
        return true;
    }

    public void setColors(int colors) {
        mToolbar.setBackgroundColor(colors);
        mTabLayout.setBackgroundColor(colors);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera_img) {
            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(MainActivity.this);
        } else if (id == R.id.nav_info) {

        } else if (id == R.id.nav_log_out) {
            mAuth.signOut();
            sendToStart();

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_change_name) {

        } else if (id == R.id.nav_people) {
            Intent settingIntent = new Intent(MainActivity.this, UsersActivity.class);
            settingIntent.putExtra("user_id", userId);
            startActivity(settingIntent);
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private Drawable myDrawable;
        private Context context;

        public SectionsPagerAdapter(FragmentManager fm, Context ctx) {
            super(fm);
            this.context = ctx;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 1:
                    RequestsFragment requestsFragment = new RequestsFragment();
                    return requestsFragment;
                case 0:
                    FriendsFragment friendsFragment = new FriendsFragment();
                    return friendsFragment;
                case 2:
                    ChatsFragment chatsFragment = new ChatsFragment();
                    return chatsFragment;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        //
//
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 1:
                    return "REQUESTS";
                case 0:
                    return "FRIENDS";
                case 2:
                    return "CHATS";
                default:
                    return null;

            }
        }

    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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


                String userId = this.userId;
                File thumbFile = new File(resultUri.getPath());
                Bitmap thumbBitmap = null;
                try {
                    thumbBitmap = new Compressor(this).setMaxHeight(200).setMaxWidth(200).setQuality(75).compressToBitmap(thumbFile);
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Error IO", 5);
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
                                        Toast.makeText(MainActivity.this, "Error in uploading thumbnail", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(MainActivity.this, "Error in uploading", Toast.LENGTH_SHORT).show();
                            Exception e = task.getException();
                            e.printStackTrace();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(MainActivity.this, "error", 10);
            }
        } else if (requestCode == GALLERY_INT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setAspectRatio(1, 1).start(this);

        }
    }

    public void animateSearchToolbar(int numberOfMenuIcon, boolean containsOverflow, boolean show) {

        mToolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white));
        mDrawer.setStatusBarBackgroundColor(ContextCompat.getColor(this, R.color.quantum_grey_600));

        if (show) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = mToolbar.getWidth() -
                        (containsOverflow ? getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(mToolbar,
                        isRtl(getResources()) ? mToolbar.getWidth() - width : width, mToolbar.getHeight() / 2, 0.0f, (float) width);
                createCircularReveal.setDuration(250);
                createCircularReveal.start();
            } else {
                TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, (float) (-mToolbar.getHeight()), 0.0f);
                translateAnimation.setDuration(220);
                mToolbar.clearAnimation();
                mToolbar.startAnimation(translateAnimation);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = mToolbar.getWidth() -
                        (containsOverflow ? getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(mToolbar,
                        isRtl(getResources()) ? mToolbar.getWidth() - width : width, mToolbar.getHeight() / 2, (float) width, 0.0f);
                createCircularReveal.setDuration(250);
                createCircularReveal.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ColorDrawable buttonColor = (ColorDrawable) mTabLayout.getBackground();
                        mToolbar.setBackgroundColor(buttonColor.getColor());
                        mDrawer.setStatusBarBackgroundColor(buttonColor.getColor());
                        super.onAnimationEnd(animation);
                    }
                });
                createCircularReveal.start();
            } else {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                Animation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-mToolbar.getHeight()));
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(alphaAnimation);
                animationSet.addAnimation(translateAnimation);
                animationSet.setDuration(220);
                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ColorDrawable buttonColor = (ColorDrawable) mTabLayout.getBackground();
                        mToolbar.setBackgroundColor(getThemeColor(MainActivity.this, buttonColor.getColor()));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mToolbar.startAnimation(animationSet);
            }
        }
    }

    private boolean isRtl(Resources resources) {
        return resources.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private static int getThemeColor(Context context, int id) {
        Resources.Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[]{id});
        int result = a.getColor(0, 0);
        a.recycle();
        return result;
    }
}

